package dmax.plua.ui.cards;

import android.view.MotionEvent;
import android.view.View;

import dmax.plua.ui.cards.CardsPagerAdapter.CardViewHolder;

/**
 * Reacts on user interaction with card (touch on card flips it)
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 21.01.15 at 15:33
 */
public class CardInteractionListener implements View.OnTouchListener {

    private CardStateSwitcher stateSwitcher;

    public CardInteractionListener(CardStateSwitcher stateSwitcher) {
        this.stateSwitcher = stateSwitcher;
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
