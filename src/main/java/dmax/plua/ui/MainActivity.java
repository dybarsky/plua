package dmax.plua.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import javax.inject.Inject;

import dmax.plua.R;
import dmax.plua.di.DaggerDependenciesGraph;
import dmax.plua.di.DependenciesGraph;
import dmax.plua.di.DependenciesProvider;
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

    @Inject
    DataBaseManager database;

    private DependenciesGraph graph;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_main);

        initLanguageCodeNames();

        graph = DaggerDependenciesGraph
                .builder()
                .dependenciesProvider(new DependenciesProvider(getApplicationContext()))
                .build();
        graph.inject(this);

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

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public DependenciesGraph getGraph() {
        return graph;
    }

    /**
     * Set localized names of languages to enums items
     */
    private void initLanguageCodeNames() {
        Language.UKRAINIAN.setCodeName(getString(R.string.ukrainian));
        Language.GERMAN.setCodeName(getString(R.string.german));
    }
}
