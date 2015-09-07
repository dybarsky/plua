package dmax.plua.ui.cards;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import dmax.plua.BuildConfig;
import dmax.plua.ui.Util;
import dmax.plua.ui.cards.CardsPagerAdapter.CardViewHolder;

/**
 * Switches card to original/translation states.
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 05.01.15 at 11:44
 */
class CardStateSwitcher extends AnimatorListenerAdapter {

    private View groupToHide;
    private boolean animate;

    public void switchCardState(CardViewHolder holder, MotionEvent event) {
        // avoid switching while previous is still playing (animation)
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

        Animator anim = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                ? Util.prepareCircularRevealTransition(groupToShow, event)
                : Util.prepareAlphaTransition(groupToShow);
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
