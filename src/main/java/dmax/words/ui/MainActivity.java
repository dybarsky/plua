package dmax.words.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import dmax.words.R;
import dmax.words.importer.Importer;
import dmax.words.persist.DataBaseManager;

public class MainActivity extends FragmentActivity {

    private DataBaseManager database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_main);

        database = new DataBaseManager(this);
        database.open();
    }

    @Override
    protected void onStart() {
        super.onStart();

        WordsListFragment fragment = new WordsListFragment();
        getFragmentManager().beginTransaction()
                .add(R.id.container, fragment)
                .commit();

        new Importer(this, database).execute(fragment);
    }

    @Override
    protected void onResume() {
        super.onResume();

        database.open();
    }

    @Override
    protected void onPause() {
        super.onPause();

        database.close();
    }

    public DataBaseManager getDataBaseManager() {
        return database;
    }
}