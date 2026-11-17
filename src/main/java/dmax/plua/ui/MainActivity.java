package dmax.plua.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
        // Edge-to-edge is enforced on targetSdk 35+; opt in explicitly (also on older releases) and keep the
        // indigo toolbar behind a transparent status bar with light (white) icons. The navigation bar uses
        // the default auto style since the content background is light.
        EdgeToEdge.enable(this, SystemBarStyle.dark(Color.TRANSPARENT));
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_main);
        applyWindowInsets();

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

    public DependenciesGraph getGraph() {
        return graph;
    }

    /**
     * Distributes window insets: the app bar takes the status bar / cutout (its indigo background extends
     * under the status bar), the fragment container takes the navigation bar, side bars and the keyboard.
     * Insets are consumed here so the fragments (which live inside the container) never apply them twice.
     */
    private void applyWindowInsets() {
        final View root = findViewById(R.id.root);
        final View appBar = findViewById(R.id.app_bar);
        final View container = findViewById(R.id.container);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            Insets ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

            appBar.setPadding(bars.left, bars.top, bars.right, 0);
            container.setPadding(bars.left, 0, bars.right, Math.max(bars.bottom, ime.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /**
     * Set localized names of languages to enums items
     */
    private void initLanguageCodeNames() {
        Language.UKRAINIAN.setCodeName(getString(R.string.ukrainian));
        Language.POLISH.setCodeName(getString(R.string.polish));
    }
}
