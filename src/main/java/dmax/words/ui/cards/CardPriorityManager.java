package dmax.words.ui.cards;

import android.support.v4.view.ViewPager;
import android.view.View;

import dmax.words.DataSource;

/**
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

    public void onRisePriorityEvent() {
        // todo save new priority of link into database
        showNextCard();
    }

    private void showNextCard() {
        int currentPage = pager.getCurrentItem();
        int pageToShow = currentPage != pager.getAdapter().getCount() - 1
                ? currentPage + 1
                : currentPage - 1;
        pager.setCurrentItem(pageToShow, true);
    }
}
