package dmax.words.ui;

import android.app.Activity;
import android.os.Bundle;

import dmax.words.R;
import dmax.words.persist.DataBaseHelper;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.a_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // create database
        new DataBaseHelper(this).getReadableDatabase();
    }
}