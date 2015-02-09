package dmax.plua.ui.cards;

import android.support.v4.view.ViewPager;

import dmax.plua.DataSource;
import dmax.plua.domain.Link;

/**
 * Reacts on rise card priority button click action
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 21.01.15 at 13:19
 */
public class CardPriorityManager {

    private ViewPager pager;
    private DataSource dataSource;

    public CardPriorityManager(ViewPager pager, DataSource dataSource) {
        this.pager = pager;
        this.dataSource = dataSource;
    }

    /**
     * Updates priority of link associated with card
     */
    public void onChangePriority(CardsPagerAdapter.CardViewHolder holder, boolean increment) {
        // update link priority values in database
        Link link = holder.link;
        link.setPriority(increment
                ? link.getPriority() + 1
                : link.getPriority() - 1);
        link.setUpdated(System.currentTimeMillis());
        dataSource.updateLink(link);

        showNextCard();
    }

    private void showNextCard() {
        int currentPage = pager.getCurrentItem();
        // flip pager to left if current card is last OR to right if there are some items
        int pageToShow = currentPage != pager.getAdapter().getCount() - 1
                ? currentPage + 1
                : currentPage - 1;
        pager.setCurrentItem(pageToShow, true);
    }
}
