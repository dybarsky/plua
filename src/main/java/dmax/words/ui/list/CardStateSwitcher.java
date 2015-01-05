package dmax.words.ui.list;

import android.animation.Animator;
import android.support.v7.widget.CardView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.TextView;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
* on 05.01.15 at 11:44
*/
class CardStateSwitcher implements View.OnTouchListener {

    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            CardView card = (CardView) v;
            final WordsListPagerAdapter.CardViewHolder holder = (WordsListPagerAdapter.CardViewHolder) v.getTag();

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
            prepareTransition(viewGroup, event).start();

            holder.isTranslationState = !holder.isTranslationState;

            return true;
        }
        return false;
    }

    private Animator prepareTransition(View v, MotionEvent event) {
        int cx = (int) event.getX();
        int cy = (int) event.getY();

        float maxX = Math.max(cx, v.getWidth() - cx);
        float maxY = Math.max(cy, v.getHeight() - cy);
        float radius = (float) Math.sqrt(Math.pow(maxX, 2) + Math.pow(maxY, 2));

        return ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);
    }
}
