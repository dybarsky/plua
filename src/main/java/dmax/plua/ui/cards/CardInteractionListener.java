package dmax.plua.ui.cards;

import android.view.MotionEvent;
import android.view.View;

import dmax.plua.R;
import dmax.plua.ui.cards.CardsPagerAdapter.CardViewHolder;

/**
 * Reacts on user interaction with card (click on card, click on buttons)
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 21.01.15 at 15:33
 */
public class CardInteractionListener implements View.OnTouchListener, View.OnClickListener {

    private CardStateSwitcher stateSwitcher;
    private CardPriorityManager priorityManager;

    public CardInteractionListener(CardStateSwitcher stateSwitcher, CardPriorityManager priorityManager) {
        this.stateSwitcher = stateSwitcher;
        this.priorityManager = priorityManager;
    }

    @Override
    public void onClick(View v) {
        CardViewHolder holder = (CardViewHolder) v.getTag();
        // increase priority is 'forgot' button clicked OR decrease if 'recalled' button clicked
        priorityManager.onChangePriority(holder, /*v.getId() == R.id.forgot*/ false);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            stateSwitcher.switchCardState((CardViewHolder) v.getTag(), event);
            return true;
        } else {
            return false;
        }
    }
}
