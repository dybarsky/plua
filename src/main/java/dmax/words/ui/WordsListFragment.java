package dmax.words.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import dmax.words.R;
import dmax.words.domain.Language;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 18.12.14 at 12:25
 */
public class WordsListFragment extends Fragment {

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

        int pageMargin = getResources().getDimensionPixelSize(R.dimen.pager_page_margin);
        pager.setPageMargin(pageMargin);
        pager.setAdapter(adapter);

        return root;
    }
}
