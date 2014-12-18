package dmax.words.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.CardView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;

import dmax.words.R;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 18.12.14 at 12:38
 */
public class WordsListPagerAdapter extends PagerAdapter implements View.OnTouchListener {

    private Object[] data = new Object[] {
            new Object(),
            new Object(),
            new Object(),
            new Object(),
            new Object()
    };
    private Context context;

    public WordsListPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.getTag().equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object key = data[position];

        CardView card = (CardView) View.inflate(context, R.layout.v_wordslist_item, null);
        card.setTag(key);
        card.setOnTouchListener(this);
        container.addView(card);

        return key;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = container.findViewWithTag(object);
        container.removeView(view);
    }

    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {

            View translation = v.findViewById(R.id.translation);
            final View original = v.findViewById(R.id.original);
            int cx = (original.getLeft() + original.getRight()) / 2;
            int cy = (original.getTop() + original.getBottom()) / 2;

            translation.setVisibility(View.VISIBLE);
            Animator anim = ViewAnimationUtils.createCircularReveal(translation, cx, cy, 0, translation.getHeight());
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    original.setVisibility(View.INVISIBLE);
                    ((CardView) v).setCardBackgroundColor(context.getResources().getColor(R.color.yellow));
                }
            });
            anim.start();
        }
        return false;
    }
}
