package dmax.words.ui.cards;

import android.view.MotionEvent;
import android.view.View;

import dmax.words.R;
import dmax.words.ui.cards.CardsPagerAdapter.CardViewHolder;

/**
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
        if (holder.isTranslationState) {
            priorityManager.onChangePriority(holder, v.getId() == R.id.forgot);
        }
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
