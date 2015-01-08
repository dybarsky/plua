package dmax.words.ui.cards;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
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

import dmax.words.DataSource;
import dmax.words.R;
import dmax.words.domain.Language;
import dmax.words.ui.Util;
import dmax.words.ui.detail.LinkDetailFragment;
import dmax.words.ui.MainActivity;
import dmax.words.ui.cards.CardsPagerAdapter.CardViewHolder;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 18.12.14 at 12:25
 */
public class CardsFragment extends Fragment implements View.OnClickListener {

    private static final int DURATION = 250;

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

    @Override
    public void onClick(View v) {
        openDetailedFragment(null);
    }

    //~

    public void updateLanguage(Language language) {
        getDataSource().setSelectedLanguage(language);
        adapter.onLanguageChanged();
    }

    public void reloadList() {
        pager.setAdapter(adapter);
    }


    private MainActivity getCastedActivity() {
        return (MainActivity) getActivity();
    }

    private DataSource getDataSource() {
        return getCastedActivity().getDataSource();
    }

    private void editCurrentCard() {
        int id = pager.getCurrentItem();
        CardView cardView = (CardView) pager.findViewById(id).findViewById(R.id.card);
        CardViewHolder holder = (CardViewHolder) cardView.getTag();

        Bundle params = new Bundle();
        params.putSerializable(LinkDetailFragment.KEY_ORIGINAL, holder.originalWord);
        params.putSerializable(LinkDetailFragment.KEY_TRANSLATION, holder.translationWord);
        params.putSerializable(LinkDetailFragment.KEY_LINK, holder.link);

        openDetailedFragment(params);
    }

    private void removeCurrentCard() {
        final int id = pager.getCurrentItem();
        CardView cardView = (CardView) pager.findViewById(id).findViewById(R.id.card);
        CardViewHolder holder = (CardViewHolder) cardView.getTag();

        Animator transition = prepareCollapseTransition(cardView);
        transition.addListener(new PageRemover(id, holder));
        transition.start();
    }

    private Animator prepareCollapseTransition(View v) {
        Animator vertical = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0);
        Animator horizontal = ObjectAnimator.ofFloat(v, "scaleX", 1f, 0);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(vertical, horizontal);
        set.setDuration(DURATION);

        return set;
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

    //~

    private class PageRemover extends AnimatorListenerAdapter implements ViewPager.OnPageChangeListener {

        private int pageToRemove;
        private int pageToShowBeforeRemoving;
        private int pageToShowAfterRemoving;
        private CardViewHolder holder;

        private PageRemover(int pageToRemove, CardViewHolder holder) {
            this.pageToRemove = pageToRemove;
            this.holder = holder;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (adapter.getCount() > 1) {
                showNextPage();
            } else {
                hidePager();
                removeItem();
            }
        }

        private void showNextPage() {
            boolean pageOnRightExist = pageToRemove != adapter.getCount() - 1;
            pageToShowBeforeRemoving = pageOnRightExist
                    ? pageToRemove + 1
                    : pageToRemove - 1;
            pageToShowAfterRemoving = pageOnRightExist
                    ? pageToShowBeforeRemoving - 1
                    : pageToShowBeforeRemoving;

            pager.setCurrentItem(pageToShowBeforeRemoving, true);
            pager.setOnPageChangeListener(this);
        }

        private void hidePager() {
            pager.setVisibility(View.INVISIBLE);
        }

        private void removeItem() {
            getDataSource().removeWords(holder.link, holder.originalWord, holder.translationWord);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE && pager.getCurrentItem() == pageToShowBeforeRemoving) {
                pager.setOnPageChangeListener(null);
                removeItem();
                pager.setCurrentItem(pageToShowAfterRemoving, false);
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {/* */}
        @Override
        public void onPageSelected(int position) {/* */}
    }
}
