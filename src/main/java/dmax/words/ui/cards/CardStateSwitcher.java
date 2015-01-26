package dmax.words.ui.cards;

import android.animation.Animator;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.TextView;

import dmax.words.ui.Util;
import dmax.words.ui.cards.CardsPagerAdapter.CardViewHolder;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 05.01.15 at 11:44
 */
class CardStateSwitcher {

    public void switchCardState(CardViewHolder holder, MotionEvent event) {
        View viewGroup;
        TextView textView;
        String text;
        if (holder.isTranslationState) {
            viewGroup = holder.originalViewGroup;
            textView = holder.originalTextView;
            text = holder.originalWord.getData();
        } else {
            viewGroup = holder.translationViewGroup;
            textView = holder.translationTextView;
            text = holder.translationWord.getData();
        }

        textView.setText(text);
        viewGroup.bringToFront();
        Util.prepareCircularRevealTransition(viewGroup, event).start();

        holder.isTranslationState = !holder.isTranslationState;
    }
}
