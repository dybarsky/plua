package dmax.words.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import dmax.words.R;
import dmax.words.importer.Importer;
import dmax.words.persist.DataBaseManager;

public class MainActivity extends FragmentActivity implements Importer.Callback {

    private static String TAG = "list";
    private DataBaseManager database;
    private Handler uiHandler;
    private Runnable updater = new Runnable() {
        public void run() {
            WordsListFragment fragment = (WordsListFragment) getFragmentManager().findFragmentByTag(TAG);
            if (fragment != null) {
                fragment.updateList();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.uiHandler = new Handler();

        setContentView(R.layout.a_main);

        getFragmentManager().beginTransaction()
                .add(R.id.container, new WordsListFragment(), TAG)
                .commit();

        database = new DataBaseManager(this);
        database.open();

        new Importer(this, database).execute(this);
    }

    @Override
    protected void onResume() {
        database.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        database.close();
    }

    public DataBaseManager getDataBaseManager() {
        return database;
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDatabaseUpdated() {
        uiHandler.post(updater);
    }
}