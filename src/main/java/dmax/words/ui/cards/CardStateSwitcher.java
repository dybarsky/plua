package dmax.words.ui.cards;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import dmax.words.ui.Util;
import dmax.words.ui.cards.CardsPagerAdapter.CardViewHolder;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 05.01.15 at 11:44
 */
class CardStateSwitcher extends AnimatorListenerAdapter {

    private View groupToHide;
    private boolean animate;

    public void switchCardState(CardViewHolder holder, MotionEvent event) {
        if (animate) return;

        View groupToShow;
        TextView textView;
        String text;

        if (holder.isTranslationState) {
            groupToShow = holder.originalViewGroup;
            groupToHide = holder.translationViewGroup;
            textView = holder.originalTextView;
            text = holder.originalWord.getData();
        } else {
            groupToShow = holder.translationViewGroup;
            groupToHide = holder.originalViewGroup;
            textView = holder.translationTextView;
            text = holder.translationWord.getData();
        }

        textView.setText(text);
        groupToShow.setVisibility(View.VISIBLE);
        groupToShow.bringToFront();

        Animator anim = Util.prepareCircularRevealTransition(groupToShow, event);
        anim.addListener(this);
        anim.start();
        animate = true;

        holder.isTranslationState = !holder.isTranslationState;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        groupToHide.setVisibility(View.INVISIBLE);
        animate = false;
    }
}
