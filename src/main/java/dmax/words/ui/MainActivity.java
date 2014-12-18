package dmax.words.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import dmax.words.R;
import dmax.words.persist.DataBaseHelper;

public class MainActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_main);

        WordsListFragment fragment = new WordsListFragment();
        getFragmentManager().beginTransaction()
                            .add(R.id.container, fragment)
                            .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}