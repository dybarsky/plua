package dmax.words.ui.about;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import dmax.words.R;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 26.01.15 at 14:06
 */
public class AboutFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.f_about, container, false);


        return root;
    }
}
