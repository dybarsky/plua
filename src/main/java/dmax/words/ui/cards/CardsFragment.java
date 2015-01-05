package dmax.words.ui.cards;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import dmax.words.DataSource;
import dmax.words.R;
import dmax.words.domain.Language;
import dmax.words.ui.detail.LinkDetailFragment;
import dmax.words.ui.MainActivity;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 18.12.14 at 12:25
 */
public class CardsFragment extends Fragment implements View.OnClickListener {

    private ViewPager pager;
    private CardsPagerAdapter adapter;
    private LanguageSwitcher switcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity activity = getCastedActivity();
        DataSource dataSource = activity.getDataSource();
        this.switcher = new LanguageSwitcher(this, dataSource.getSelectedLanguage());
        this.adapter = new CardsPagerAdapter(activity, dataSource);
    }

    private MainActivity getCastedActivity() {
        return (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.f_wordslist, container, false);

        pager = (ViewPager) root.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        ImageButton add = (ImageButton) root.findViewById(R.id.add);
        add.setOnClickListener(this);

        switcher.init(root);

        return root;
    }

    public void updateLanguage(Language language) {
        getCastedActivity().getDataSource().setSelectedLanguage(language);
        adapter.onLanguageChanged();
    }

    public void reloadList() {
        pager.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(switcher.createActionBar());
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
                .replace(R.id.container, new LinkDetailFragment())
                .addToBackStack(null)
                .commit();
    }
}
