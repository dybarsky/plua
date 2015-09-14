package dmax.plua.ui.cards;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.Snackbar.Callback;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import dmax.plua.DataSource;
import dmax.plua.R;
import dmax.plua.domain.Language;
import dmax.plua.domain.Link;
import dmax.plua.domain.Word;
import dmax.plua.importer.Importer;
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
public class CardsFragment extends Fragment implements View.OnClickListener, Importer.Callback {

    private View emptyView;
    private ViewPager pager;
    private CardsPagerAdapter adapter;
    private LanguageSwitcher switcher;
    private RemoveTransaction removeTransaction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.f_cards, container, false);

        emptyView = root.findViewById(R.id.empty_view);
        pager = (ViewPager) root.findViewById(R.id.pager);

        FloatingActionButton add = (FloatingActionButton) root.findViewById(R.id.add);
        add.setOnClickListener(this);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        MainActivity activity = getCastedActivity();

        Toolbar toolbar = (Toolbar) activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        setHasOptionsMenu(true);

        DataSource dataSource = activity.getDataSource();
        CardInteractionListener listener = new CardInteractionListener(new CardStateSwitcher(), new CardPriorityManager(pager, dataSource));
        this.switcher = new LanguageSwitcher(this, dataSource.getSelectedLanguage());
        this.adapter = new CardsPagerAdapter(activity, dataSource, listener);

        showCards();

        new Importer(activity, dataSource).execute(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        ActionBar actionBar = getCastedActivity().getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle(null);
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

        Resources resources = getResources();

        // change margin in dependency from orientation
        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) pager.getLayoutParams();
        lp.setMargins(0, resources.getDimensionPixelSize(R.dimen.pager_margin_top),
                      0, resources.getDimensionPixelSize(R.dimen.pager_margin_bottom));
        pager.setLayoutParams(lp);

        // change margin and background for empty view
        ImageView emptyImage = (ImageView) emptyView.findViewById(R.id.empty_image);
        emptyImage.setImageDrawable(resources.getDrawable(R.drawable.placeholder));
        TextView emptyText = (TextView) emptyView.findViewById(R.id.empty_text);
        RelativeLayout.LayoutParams lp2 = (RelativeLayout.LayoutParams) emptyText.getLayoutParams();
        lp2.setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.empty_text_bottom_margin));
        emptyText.setLayoutParams(lp2);
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

    @Override
    public void onDatabaseUpdated() {
        reload();
        getCastedActivity().invalidateOptionsMenu();
    }

    /**
     * Clear datasource cache, load links and show cards pager
     */
    private void reload() {
        // hide undo bar for previous deletion operation (if exists)
        if (removeTransaction != null) removeTransaction.snackbar.dismiss();

        getDataSource().reset();
        showCards();
    }

    //~

    /**
     * Hide empty view and show cards pager
     */
    private void showCards() {
        // if datasource is not empty - show pager with cards
        if (adapter.getCount() == 0) return;

        emptyView.setVisibility(View.GONE);
        pager.setVisibility(View.VISIBLE);
        pager.setAdapter(adapter);
    }

    MainActivity getCastedActivity() {
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
        if (removeTransaction != null && removeTransaction.inProgress) return;

        int id = pager.getCurrentItem();
        CardView cardView = (CardView) pager.findViewById(id).findViewById(R.id.card);
        CardViewHolder holder = (CardViewHolder) cardView.getTag();

        // play collapsing animation and do actual removing in PageRemover instance
        Animator transition = Util.prepareCardCollapseTransition(cardView);
        transition.addListener(new PageRemover(id, holder));
        transition.start();
    }

    private void openDetailedFragment(Bundle params) {
        LinkDetailFragment fragment = new LinkDetailFragment();
        fragment.setArguments(params);
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void showAboutFragment() {
        AboutFragment fragment = new AboutFragment();
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_up, 0, 0, R.anim.slide_down)
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    //~

    /**
     * Class for encapsulation removing words pair logic
     */
    private class PageRemover extends AnimatorListenerAdapter implements ViewPager.OnPageChangeListener  {

        // pager item number to be removed
        private int pageToRemove;

        // pager item number to be showed when collapsing animation finished
        private int pageToShowBeforeRemoving;

        // pager item number to be showed when removing finished and adapter updated
        private int pageToShowAfterRemoving;

        private View.OnClickListener undoClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUndo();
            }
        };

        private Callback dismissListener = new Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                CardsFragment.this.removeTransaction = null;
            }
        };


        private PageRemover(int pageToRemove, CardViewHolder holder) {
            // hide undo bar for previous deletion operation (if exists)
            if (CardsFragment.this.removeTransaction != null) {
                CardsFragment.this.removeTransaction.snackbar.setCallback(null);
                CardsFragment.this.removeTransaction.snackbar.dismiss();
            }
            RemoveTransaction removeTransaction = new RemoveTransaction();
            removeTransaction.link = holder.link;
            removeTransaction.originalWord = holder.originalWord;
            removeTransaction.translationWord = holder.translationWord;
            CardsFragment.this.removeTransaction = removeTransaction;

            this.pageToRemove = pageToRemove;
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
            pager.addOnPageChangeListener(this);
        }

        private void hidePager() {
            pager.setVisibility(View.INVISIBLE);
            emptyView.setVisibility(View.VISIBLE);
        }

        /**
         * Remove current item data from datasource AND update adapter
         */
        private void removeItem() {
            RemoveTransaction transaction = CardsFragment.this.removeTransaction;
            getDataSource().removeWords(transaction.link, transaction.originalWord, transaction.translationWord);
            adapter.notifyDataSetChanged();
        }

        private void showUndo() {
            removeTransaction.snackbar = Snackbar.make(pager, R.string.deleted, Snackbar.LENGTH_SHORT);
            removeTransaction.snackbar
                    .setAction(R.string.undo, undoClickListener)
                    .setCallback(dismissListener)
                    .show();
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
                CardsFragment.this.removeTransaction.inProgress = false;
            }
        }

        /**
         * Next page appears callback. See {@link #showNextPage()}
         */
        @Override
        public void onPageScrollStateChanged(int state) {
            // if next page appears - do actual removing and update pager with new adapter
            if (state == ViewPager.SCROLL_STATE_IDLE && pager.getCurrentItem() == pageToShowBeforeRemoving) {
                pager.removeOnPageChangeListener(this);
                removeItem();
                showUndo();
                pager.setCurrentItem(pageToShowAfterRemoving, false);
                // removing procedure finished
                CardsFragment.this.removeTransaction.inProgress = false;
            }
        }

        /**
         * Reacts on undo bar button click
         */
        public void onUndo() {
            RemoveTransaction transaction = CardsFragment.this.removeTransaction;
            // restore item data into datasource
            getDataSource().addWords(transaction.originalWord, transaction.translationWord);
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

    private class RemoveTransaction {
        Link link;
        Word originalWord;
        Word translationWord;

        Snackbar snackbar;
        boolean inProgress;
    }
}
