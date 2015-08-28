package dmax.plua.ui.cards;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Fragment;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import dmax.plua.DataSource;
import dmax.plua.R;
import dmax.plua.domain.Language;
import dmax.plua.ui.Util;
import dmax.plua.ui.about.AboutFragment;
import dmax.plua.ui.detail.LinkDetailFragment;
import dmax.plua.ui.MainActivity;
import dmax.plua.ui.cards.CardsPagerAdapter.CardViewHolder;

/**
 * Fragment with cards. Main user UI component.
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 18.12.14 at 12:25
 */
public class CardsFragment extends Fragment implements View.OnClickListener {

    private View emptyView;
    private ViewPager pager;
    private CardsPagerAdapter adapter;
    private LanguageSwitcher switcher;

    private boolean removing;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.f_cards, container, false);

        emptyView = root.findViewById(R.id.empty_view);
        pager = (ViewPager) root.findViewById(R.id.pager);

        FloatingActionButton add = (FloatingActionButton) root.findViewById(R.id.add);
        add.setOnClickListener(this);

        // add languages panel
        View panel = Util.createDarkThemedView(getActivity(), R.layout.v_languages_panel);
        root.addView(panel, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = getCastedActivity();
        DataSource dataSource = activity.getDataSource();
        CardInteractionListener listener = new CardInteractionListener(new CardStateSwitcher(), new CardPriorityManager(pager, dataSource));
        this.switcher = new LanguageSwitcher(this, dataSource.getSelectedLanguage());
        this.adapter = new CardsPagerAdapter(activity, dataSource, listener);

        switcher.init(getView());

        // if datasource is not empty - show pager with cards
        if (adapter.getCount() > 0) {
            showCards();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

//        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(false);
//        actionBar.setTitle(null);
//        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
//        actionBar.setCustomView(switcher.createActionBar());
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(adapter.getCount() > 0
                ? R.menu.word_list
                : R.menu.word_list_empty
                , menu);
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
            case R.id.reload:
                reload();
                return true;
            case R.id.about:
                showAboutFragment();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Add word button clicked
     */
    @Override
    public void onClick(View v) {
        openDetailedFragment(null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // change margin in dependency from orientation
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) pager.getLayoutParams();
        Resources resources = getResources();
        lp.setMargins(0, resources.getDimensionPixelSize(R.dimen.pager_margin_top),
                      0, resources.getDimensionPixelSize(R.dimen.pager_margin_bottom));
        pager.setLayoutParams(lp);
    }

    //~

    /**
     * Reacts on language switching
     * @param language selected language
     */
    public void updateLanguage(Language language) {
        getDataSource().setSelectedLanguage(language);
        adapter.onLanguageChanged();
    }

    /**
     * Clear datasource cache, load links and show cards pager
     */
    public void reload() {
        // hide undo bar for previous deletion operation (if exists)
        //TODO

        getDataSource().reset();
        showCards();
    }

    //~

    /**
     * Hide empty view and show cards pager
     */
    private void showCards() {
        emptyView.setVisibility(View.GONE);
        pager.setVisibility(View.VISIBLE);
        pager.setAdapter(adapter);
    }

    private MainActivity getCastedActivity() {
        return (MainActivity) getActivity();
    }

    private DataSource getDataSource() {
        return getCastedActivity().getDataSource();
    }

    /**
     * Open details fragment for current pager item editing
     */
    private void editCurrentCard() {
        int id = pager.getCurrentItem();
        CardView cardView = (CardView) pager.findViewById(id).findViewById(R.id.card);
        CardViewHolder holder = (CardViewHolder) cardView.getTag();

        Bundle params = new Bundle();
        params.putSerializable(LinkDetailFragment.KEY_ORIGINAL, holder.originalWord);
        params.putSerializable(LinkDetailFragment.KEY_TRANSLATION, holder.translationWord);

        openDetailedFragment(params);
    }

    /**
     * Remove current pager item
     */
    private void removeCurrentCard() {
        // avoid removing while previous is still working (animation)
        if (removing) return;

        // hide undo bar for previous deletion operation (if exists)
        //TODO

        int id = pager.getCurrentItem();
        CardView cardView = (CardView) pager.findViewById(id).findViewById(R.id.card);
        CardViewHolder holder = (CardViewHolder) cardView.getTag();

        // play collapsing animation and do actual removing in PageRemover instance
        Animator transition = Util.prepareCardCollapseTransition(cardView);
        transition.addListener(new PageRemover(id, holder));
        transition.start();
        removing = true;
    }

    private void openDetailedFragment(Bundle params) {
        // hide undo bar for previous deletion operation (if exists)
        //TODO

        LinkDetailFragment fragment = new LinkDetailFragment();
        fragment.setArguments(params);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_up, 0, 0, R.animator.slide_down)
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showAboutFragment() {
        // hide undo bar for previous deletion operation (if exists)
        //TODO

        AboutFragment fragment = new AboutFragment();
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_up, 0, 0, R.animator.slide_down)
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    //~

    /**
     * Class for encapsulation removing words pair logic
     */
    private class PageRemover extends AnimatorListenerAdapter implements ViewPager.OnPageChangeListener {

        // pager item number to be removed
        private int pageToRemove;

        // pager item number to be showed when collapsing animation finished
        private int pageToShowBeforeRemoving;

        // pager item number to be showed when removing finished and adapter updated
        private int pageToShowAfterRemoving;

        // removing item data holder
        private CardViewHolder holder;

        private PageRemover(int pageToRemove, CardViewHolder holder) {
            this.pageToRemove = pageToRemove;
            this.holder = holder;
        }

        private void showNextPage() {
            // is there any items on the right in pager?
            boolean pageOnRightExist = pageToRemove != adapter.getCount() - 1;
            // calculate pager item to be shown before actual removing
            pageToShowBeforeRemoving = pageOnRightExist
                    ? pageToRemove + 1
                    : pageToRemove - 1;
            // calculate pager item to be shown after actual removing.
            // should be the same page, but different index cause after removing adapter will be updated
            // if there is some item at the right - item at the left from shown will be removed, index decrease to 1
            // if there is no items at the right - item at the right from shown will be removed, index the same
            pageToShowAfterRemoving = pageOnRightExist
                    ? pageToShowBeforeRemoving - 1
                    : pageToShowBeforeRemoving;

            pager.setCurrentItem(pageToShowBeforeRemoving, true);
            pager.setOnPageChangeListener(this);
        }

        private void hidePager() {
            pager.setVisibility(View.INVISIBLE);
            emptyView.setVisibility(View.VISIBLE);
        }

        /**
         * Remove current item data from datasource AND update adapter
         */
        private void removeItem() {
            getDataSource().removeWords(holder.link, holder.originalWord, holder.translationWord);
            adapter.notifyDataSetChanged();
        }

        private void showUndo() {
            // TODO
        }

        /**
         * Collapsing animation finish callback -  entry point of actual removing procedure
         */
        @Override
        public void onAnimationEnd(Animator animation) {
            if (adapter.getCount() > 1) {
                // if not last item will be removed - show next page and remove item after next page appears
                showNextPage();
            } else {
                // if last item will be removed - hide pager, remove item, etc.
                hidePager();
                removeItem();
                showUndo();
                getActivity().invalidateOptionsMenu();
                // removing procedure finished
                removing = false;
            }
        }

        /**
         * Next page appears callback. See {@link #showNextPage()}
         */
        @Override
        public void onPageScrollStateChanged(int state) {
            // if next page appears - do actual removing and update pager with new adapter
            if (state == ViewPager.SCROLL_STATE_IDLE && pager.getCurrentItem() == pageToShowBeforeRemoving) {
                pager.setOnPageChangeListener(null);
                removeItem();
                showUndo();
                pager.setCurrentItem(pageToShowAfterRemoving, false);
                removing = false;
            }
        }

        /**
         * Reacts on undo bar button click
         */
        public void onUndo(Parcelable parcelable) {
            // restore item data into datasource
            getDataSource().addWords(holder.originalWord, holder.translationWord);
            getActivity().invalidateOptionsMenu();
            // in last item restored - show pager, if not last - just update adapter
            if (adapter.getCount() > 1) {
                adapter.notifyDataSetChanged();
            } else {
                showCards();
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            /* nothing to do */
        }
        @Override
        public void onPageSelected(int position) {
            /* nothing to do */
        }
    }
}
