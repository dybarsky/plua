package dmax.plua.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import dmax.plua.DataSource;
import dmax.plua.R;
import dmax.plua.domain.Language;
import dmax.plua.persist.DataBaseManager;
import dmax.plua.ui.cards.CardsFragment;

/**
 * Main UI class. Hosts fragments.
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 12.12.14 at 14:20
 */
public class MainActivity extends AppCompatActivity  {

    private static String TAG = "list";

    private static Language DEFAULT = Language.POLISH;

    private DataBaseManager database;
    private DataSource dataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_main);

        initLanguageCodeNames();

        database = new DataBaseManager(this);
        dataSource = new DataSource(database, DEFAULT);

        database.open();

        // do not add fragment when activity restored (not created freshly)
        // because old fragment will be restored as well.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CardsFragment(), TAG)
                    .commit();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    /**
     * Set localized names of languages to enums items
     */
    private void initLanguageCodeNames() {
        Language.UKRAINIAN.setCodeName(getString(R.string.ukrainian));
        Language.POLISH.setCodeName(getString(R.string.polish));
    }
}