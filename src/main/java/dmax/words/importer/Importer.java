package dmax.words.importer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import dmax.dialog.SpotsDialog;
import dmax.words.R;
import dmax.words.domain.Language;
import dmax.words.domain.Link;
import dmax.words.domain.Word;
import dmax.words.persist.Dao;
import dmax.words.persist.DataBaseManager;
import dmax.words.persist.dao.DaoFactory;

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
public class Importer extends AsyncTask<Importer.Callback, Void, Importer.Callback> {

    private static final String FILE_NAME = "data";
    private static final String VERSION = "version";
    private static final String PREFS_NAME = "import";
    private static final String SEPARATOR = "\\|";

    private Dao<Word> wordDao = DaoFactory.createDao(Word.class);
    private Dao<Link> linkDao = DaoFactory.createDao(Link.class);
    private DataBaseManager db;

    private Context context;
    private BufferedReader reader;
    private AlertDialog dialog;

    public Importer(Context context, DataBaseManager db) {
        this.context = context;
        this.db = db;
    }

    @Override
    protected void onPreExecute() {
        showDialog();
    }

    @Override
    protected Callback doInBackground(Callback... callback) {
       return importData() ? callback[0] : null;
    }

    @Override
    protected void onPostExecute(Callback callback) {
        dialog.dismiss();
        if (callback != null) {
            callback.onDatabaseUpdated();
        }
    }

    //~

    public interface Callback {
        void onDatabaseUpdated();
    }

    //~

    private boolean importData() {
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open(FILE_NAME)));
            // check if can read asset file version
            int assetVersion = getVersion();
            if (assetVersion == -1) return false;

            // check if asset file with this version is already imported
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_APPEND);
            if (assetVersion == prefs.getInt(VERSION, -1)) return false;

            // check if can read asset file languages header
            Language[] languages = getLanguages();
            if (languages == null) return false;

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

                    save(w1, w2);
                }
            }

            // save asset file version
            prefs.edit().putInt(VERSION, assetVersion).apply();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
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

    private int getVersion() throws IOException {
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

    private void save(Word word1, Word word2) {
        Word w1 = db.insert(wordDao.setPersistable(word1));
        Word w2 = db.insert(wordDao.setPersistable(word2));
        Link link = new Link();
        link.setWordId(w1.getLanguage(), w1.getId());
        link.setWordId(w2.getLanguage(), w2.getId());
        db.insert(linkDao.setPersistable(link));
    }

    private void showDialog() {
        dialog = new SpotsDialog(context, R.style.Dialog);
        dialog.setCancelable(false);
        dialog.show();
    }
}