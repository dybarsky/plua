package dmax.plua.importer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import dmax.dialog.SpotsDialog;
import dmax.plua.DataSource;
import dmax.plua.R;
import dmax.plua.domain.Language;
import dmax.plua.domain.Word;

/**
 * Reads words data line by line from 'data' asset file and stores into database in the background thread.
 * Stores data file version into shared preferences and checks is this file already imported.
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
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 22.12.14 at 12:57
 */
public class Importer extends AsyncTask<Importer.Callback, Void, Importer.Callback> implements DialogInterface.OnCancelListener {

    private static final String FILE_NAME = "data";
    private static final String VERSION = "version";
    private static final String PREFS_NAME = "import";
    private static final String SEPARATOR = "\\|";

    private DataSource dataSource;

    private Context context;
    private BufferedReader reader;
    private AlertDialog dialog;

    public Importer(Context context, DataSource dataSource) {
        this.context = context;
        this.dataSource = dataSource;
    }

    @Override
    protected void onPreExecute() {
        showDialog();
    }

    @Override
    protected Callback doInBackground(Callback... callback) {
        importData();
        return callback[0];
    }

    @Override
    protected void onPostExecute(Callback callback) {
        finished(callback);
    }

    @Override
    protected void onCancelled(Callback callback) {
        finished(callback);
    }

    private void finished(Callback callback) {
        dialog.dismiss();
        callback.onDatabaseUpdated();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        cancel(true);
    }

    //~

    private void importData() {
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(FILE_NAME)));
            // check if can read asset file version
            int assetVersion = getAssetVersion();
            if (assetVersion == -1) return;

            // check if asset file with this version is already imported
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_APPEND);
            if (assetVersion == prefs.getInt(VERSION, -1)) return;

            // check if can read asset file languages header
            Language[] languages = getLanguages();
            if (languages == null) return;

            String line;
            while(!isCancelled() && (line = reader.readLine()) != null) {
                String[] data = line.split(SEPARATOR);
                if (data.length == 2) {
                    Word w1 = new Word();
                    w1.setLanguage(languages[0]);
                    w1.setData(data[0].toLowerCase());

                    Word w2 = new Word();
                    w2.setLanguage(languages[1]);
                    w2.setData(data[1].toLowerCase());

                    dataSource.addWords(w1, w2);
                }
            }

            // save asset file version
            prefs.edit().putInt(VERSION, assetVersion).apply();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private int getAssetVersion() throws IOException {
        String header = reader.readLine();
        if (header == null) {
            return -1;
        }
        String[] headerData = header.split(SEPARATOR);
        return headerData.length == 2 && headerData[0].equals(VERSION)
                ? Integer.parseInt(headerData[1])
                : -1;
    }

    private Language[] getLanguages() throws IOException {
        String languages = reader.readLine();
        if (languages == null) {
            return null;
        }
        String[] data = languages.split(SEPARATOR);
        return data.length == 2
                ? new Language[] { Language.valueOf(data[0]), Language.valueOf(data[1]) }
                : null;
    }

    private void showDialog() {
        dialog = new SpotsDialog(context, R.style.Dialog);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(this);
        dialog.show();
    }

    //~

    public interface Callback {
        void onDatabaseUpdated();
    }
}