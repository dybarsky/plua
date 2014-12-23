package dmax.words.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import dmax.words.R;
import dmax.words.domain.Language;
import dmax.words.importer.Importer;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 18.12.14 at 12:25
 */
public class WordsListFragment extends Fragment implements View.OnClickListener {

    private ViewPager pager;
    private WordsListPagerAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        adapter = new WordsListPagerAdapter(activity, activity.getDataBaseManager(), Language.UKRAINIAN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.f_wordslist, container, false);

        pager = (ViewPager) root.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        ImageButton add = (ImageButton) root.findViewById(R.id.add);
        add.setOnClickListener(this);

        return root;
    }

    public void updateList() {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(R.string.app_label);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.word_list, menu);
    }

    @Override
    public void onClick(View v) {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_up, 0, 0, R.animator.slide_down)
                .replace(R.id.container, new AddWordFragment())
                .addToBackStack(null)
                .commit();
    }
}
