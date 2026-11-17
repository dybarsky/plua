package dmax.plua.importer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dmax.plua.ui.dialog.SpotsDialog;
import dmax.plua.DataSource;
import dmax.plua.R;
import dmax.plua.domain.Language;
import dmax.plua.domain.Word;

/**
 * Reads words data line by line from 'data' asset file and stores into database in the background thread
 * (all inserts in one database transaction). Stores data file version into shared preferences and checks is this
 * file already imported.
 * <br/>
 * <b>Asset file format:</b><br/>
 * <ul>
 * <li>All data items are separated with '|' character.<br/>
 * <li>First line must contain version description header in format 'version|int'<br/>
 * <li>Second line must contain languages description header in format 'language1|language2'<br/>
 * <li>Words data stored in same format as headers. Position of items corresponds to position of languages<br/>
 * </ul>
 * For example:<br/>
 * version|1<br/>
 * UKRAINIAN|POLISH<br/>
 * Брехати|kłamać<br/>
 * Брехун|kłamca<br/>
 * Parsing is done by {@link DataFileParser}. A malformed header aborts the import quietly (logged).
 * <br/>
 * <b>Cancelling:</b>
 * <ul>
 * <li>User cancels the dialog: import stops, the transaction is rolled back (nothing partial is stored, version is not
 * saved), dialog is dismissed and {@link Callback#onDatabaseUpdated()} is still called.
 * <li>{@link #detach()} (lifecycle teardown): import stops, dialog is dismissed and the callback is NOT called.
 * </ul>
 * <br/>
 * <b>When it runs:</b> {@link #execute(Callback)} first decides synchronously on the main thread whether an import is
 * needed: only the first line of the asset is read and its version is compared with the one saved in shared
 * preferences. If the asset is already imported (or its header is unreadable/invalid) nothing happens at all: no
 * dialog, no thread and no callback. So {@link Callback#onDatabaseUpdated()} is called only when an import ran to
 * completion or was cancelled by the user (the links cache is stale then), never when there was nothing to do.
 * <br/>
 * Must be created and executed on the main thread.
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 22.12.14 at 12:57
 */
public class Importer implements DialogInterface.OnCancelListener {

    private static final String TAG = "Importer";

    private static final String FILE_NAME = "data";
    private static final String VERSION = "version";
    private static final String PREFS_NAME = "import";

    private final DataSource dataSource;
    /** Application context: used from the background thread, must not leak an Activity. */
    private final Context appContext;
    /** Activity context: used only on the main thread to show the dialog, released in {@link #detach()}. */
    private Context uiContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private ExecutorService executor;
    private boolean executed;
    private AlertDialog dialog;

    /** User cancelled the dialog or {@link #detach()} was called: import loop must stop. */
    private volatile boolean cancelled;
    /** {@link #detach()} was called: callback must not be called, nothing UI related must be touched. */
    private volatile boolean detached;

    public Importer(Context context, DataSource dataSource) {
        this.uiContext = context;
        this.appContext = context.getApplicationContext() != null ? context.getApplicationContext() : context;
        this.dataSource = dataSource;
    }

    /**
     * If the asset file is not imported yet: show progress dialog and start importing in the background. Result is
     * delivered to the callback on the main thread (unless {@link #detach()} is called before).
     * If there is nothing to import - does nothing (no dialog, no thread, callback is not called).
     */
    public void execute(final Callback callback) {
        if (executed) throw new IllegalStateException("Importer can be executed only once");
        executed = true;
        if (!isImportNeeded()) return;
        showDialog();
        executor = Executors.newSingleThreadExecutor();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    importData();
                } catch (RuntimeException e) {
                    // never crash the process from the import thread (e.g. database closed during teardown)
                    if (detached) {
                        Log.d(TAG, "Import stopped after detach: " + e);
                    } else {
                        Log.w(TAG, "Import failed", e);
                    }
                }
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        finished(callback);
                    }
                });
            }
        });
        // already submitted task will be completed, thread is released after that
        executor.shutdown();
    }

    /**
     * Stop importing (the transaction is rolled back), dismiss the dialog and never call the callback.
     * Call it when the screen which started the import is destroyed.
     */
    public void detach() {
        detached = true;
        cancelled = true;
        mainHandler.removeCallbacksAndMessages(null);
        dismissDialog();
        uiContext = null;
    }

    /** Main thread. Can be called after {@link #detach()}: then does nothing. */
    private void finished(Callback callback) {
        if (detached) return;
        dismissDialog();
        // After a cancel (rollback) the DataSource links cache (filled by addWords) is stale, but callback
        // reloads the data: CardsFragment.reload() calls DataSource.reset().
        callback.onDatabaseUpdated();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        cancelled = true;
    }

    //~

    /**
     * Cheap check on the main thread: reads only the first line of the asset file and compares its version with the
     * saved one. Any read problem or invalid header means "no import needed" (logged).
     */
    private boolean isImportNeeded() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(appContext.getAssets().open(FILE_NAME), StandardCharsets.UTF_8));
            int assetVersion = DataFileParser.parseVersion(reader.readLine());
            if (assetVersion == DataFileParser.NO_VERSION) {
                Log.w(TAG, "Asset file has no valid 'version|int' header, nothing to import");
                return false;
            }
            SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return assetVersion != prefs.getInt(VERSION, -1);
        } catch (IOException e) {
            Log.w(TAG, "Can't read asset file, nothing to import", e);
            return false;
        } catch (RuntimeException e) {
            Log.w(TAG, "Can't check asset file version, nothing to import", e);
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, "Can't close asset file", e);
                }
            }
        }
    }

    /** Background thread. */
    private void importData() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(appContext.getAssets().open(FILE_NAME), StandardCharsets.UTF_8));
            // check if can read asset file version
            int assetVersion = DataFileParser.parseVersion(reader.readLine());
            if (assetVersion == DataFileParser.NO_VERSION) {
                Log.w(TAG, "Asset file has no valid 'version|int' header, nothing imported");
                return;
            }

            // check if asset file with this version is already imported
            SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            if (assetVersion == prefs.getInt(VERSION, -1)) return;

            // check if can read asset file languages header
            final Language[] languages = DataFileParser.parseLanguages(reader.readLine());
            if (languages == null) {
                Log.w(TAG, "Asset file has no valid languages header, nothing imported");
                return;
            }

            final BufferedReader in = reader;
            try {
                dataSource.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            importWords(in, languages);
                        } catch (IOException e) {
                            // abort: transaction will be rolled back
                            throw new ImportAbortedException(e);
                        }
                    }
                });
            } catch (ImportAbortedException e) {
                if (e.getCause() != null) Log.w(TAG, "Import failed, rolled back", e.getCause());
                return;
            }

            // save asset file version only after completed import
            prefs.edit().putInt(VERSION, assetVersion).apply();
        } catch (IOException e) {
            Log.w(TAG, "Can't read asset file", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.w(TAG, "Can't close asset file", e);
                }
            }
        }
    }

    /** Runs inside the transaction. Throws {@link ImportAbortedException} if cancelled. */
    private void importWords(BufferedReader reader, Language[] languages) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (cancelled) throw new ImportAbortedException(null);

            String[] data = DataFileParser.parsePair(line);
            if (data == null) continue;

            Word w1 = new Word();
            w1.setLanguage(languages[0]);
            w1.setData(data[0].toLowerCase(Locale.ROOT));

            Word w2 = new Word();
            w2.setLanguage(languages[1]);
            w2.setData(data[1].toLowerCase(Locale.ROOT));

            dataSource.addWords(w1, w2);
        }
        if (cancelled) throw new ImportAbortedException(null);
    }

    private void showDialog() {
        dialog = new SpotsDialog.Builder()
            .setContext(uiContext)
            .setTheme(R.style.Dialog)
            .setCancelable(true)
            .setCancelListener(this)
            .build();
        dialog.show();
    }

    private void dismissDialog() {
        if (dialog != null) {
            try {
                dialog.dismiss();
            } catch (IllegalArgumentException e) {
                // window is already gone
                Log.w(TAG, "Dialog already detached from window", e);
            }
            dialog = null;
        }
    }

    //~

    /** Thrown inside the transaction to roll it back (cancel or read error). */
    private static class ImportAbortedException extends RuntimeException {
        ImportAbortedException(Throwable cause) {
            super(cause);
        }
    }

    public interface Callback {
        /**
         * Main thread. Called only after an import ran to completion or was cancelled by the user (the data source
         * cache is stale then). Never called when there was nothing to import or after {@link #detach()}.
         */
        void onDatabaseUpdated();
    }
}
