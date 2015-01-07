package dmax.words.ui.cards;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import dmax.words.DataSource;
import dmax.words.R;
import dmax.words.domain.Language;
import dmax.words.ui.Util;
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
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.f_wordslist, container, false);

        pager = (ViewPager) root.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        ImageButton add = (ImageButton) root.findViewById(R.id.add);
        add.setOnClickListener(this);

        View panel = Util.createDarkThemedView(getActivity(), R.layout.v_languages_panel);
        root.addView(panel, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        switcher.init(panel);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                removeCurrentCard();
                return true;
            case R.id.edit:
                editCurrentCard();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editCurrentCard() {
        int pagesCount = pager.getOffscreenPageLimit() * 2 + 1;
        int index = pager.getCurrentItem() % pagesCount;

        CardView cardView = (CardView) pager.getChildAt(index).findViewById(R.id.card);
        CardsPagerAdapter.CardViewHolder holder = (CardsPagerAdapter.CardViewHolder) cardView.getTag();

        Bundle params = new Bundle();
        params.putSerializable(LinkDetailFragment.KEY_ORIGINAL, holder.originalWord);
        params.putSerializable(LinkDetailFragment.KEY_TRANSLATION, holder.translationWord);
        params.putSerializable(LinkDetailFragment.KEY_LINK, holder.link);

        openDetailedFragment(params);
    }

    private void removeCurrentCard() {
        //TODO implement
    }

    @Override
    public void onClick(View v) {
        openDetailedFragment(null);
    }

    private void openDetailedFragment(Bundle params) {
        LinkDetailFragment fragment = new LinkDetailFragment();
        fragment.setArguments(params);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_up, 0, 0, R.animator.slide_down)
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
