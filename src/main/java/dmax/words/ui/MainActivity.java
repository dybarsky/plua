package dmax.words.ui;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;

import dmax.words.DataSource;
import dmax.words.R;
import dmax.words.domain.Language;
import dmax.words.importer.Importer;
import dmax.words.persist.DataBaseManager;
import dmax.words.ui.cards.CardsFragment;

/**
 * Main UI class. Hosts fragments.
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 12.12.14 at 14:20
 */
public class MainActivity extends FragmentActivity implements Importer.Callback {

    private static String TAG = "list";

    private static Language DEFAULT = Language.POLISH;

    private DataBaseManager database;
    private DataSource dataSource;
    private Handler uiHandler;
    private Importer importer;

    // after import update cards fragment
    private Runnable updater = new Runnable() {
        public void run() {
            CardsFragment fragment = (CardsFragment) getFragmentManager().findFragmentByTag(TAG);
            if (fragment != null) {
                fragment.reload();
                invalidateOptionsMenu();
                importer = null;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.uiHandler = new Handler();

        setContentView(R.layout.a_main);

        // do not add fragment when activity restored (not created freshly)
        // because old fragment will be restored as well.
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new CardsFragment(), TAG)
                    .commit();
        }

        database = new DataBaseManager(this);
        database.open();

        dataSource = new DataSource(database, DEFAULT);

        importer = new Importer(this, database);
        importer.execute(this);
    }

    @Override
    protected void onResume() {
        database.open();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (importer != null) {
            importer.cancel(true);
        }
        database.close();
    }

    public DataSource getDataSource() {
        return dataSource;
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