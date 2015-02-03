package dmax.words.ui;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

/**
 * Class for creating animations and other UI utils methods.
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 07.01.15 at 13:01
 */
public class Util {

    private static final int DURATION = 250;

    public static View createDarkThemedView(Context context, int resId) {
        ContextThemeWrapper themed = new ContextThemeWrapper(context, android.R.style.ThemeOverlay_Material_Dark);
        return View.inflate(themed, resId, null);
    }

    public static Animator prepareCardCollapseTransition(View card) {
        Animator vertical = ObjectAnimator.ofFloat(card, "scaleY", 1f, 0);
        Animator horizontal = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(vertical, horizontal);
        set.setDuration(DURATION);

        return set;
    }

    public static Animator prepareCircularRevealTransition(View v, MotionEvent event) {
        int cx = (int) event.getX();
        int cy = (int) event.getY();

        // radius is the longest distance from event point to view corners
        float maxX = Math.max(cx, v.getWidth() - cx);
        float maxY = Math.max(cy, v.getHeight() - cy);
        float radius = (float) Math.sqrt(Math.pow(maxX, 2) + Math.pow(maxY, 2));

        return ViewAnimationUtils.createCircularReveal(v, cx, cy, 0, radius);
    }

    public static Animator prepareCollapseTransition(ImageView actionBarIcon, View languagesList) {
        ObjectAnimator rotate = ObjectAnimator.ofFloat(actionBarIcon, "rotation", 180, 360);
        ObjectAnimator move = ObjectAnimator.ofFloat(languagesList, "yRatio", 0, -1);
        move.setInterpolator(new AccelerateInterpolator(1f));
        AnimatorSet set = new AnimatorSet();
        set.setDuration(DURATION);
        set.playTogether(rotate, move);
        return set;
    }

    public static Animator prepareExpandTransition(ImageView actionBarIcon, View languagesList) {
        ObjectAnimator rotate = ObjectAnimator.ofFloat(actionBarIcon, "rotation", 0, 180);
        ObjectAnimator move = ObjectAnimator.ofFloat(languagesList, "yRatio", -1, 0);
        move.setInterpolator(new DecelerateInterpolator(1f));
        AnimatorSet set = new AnimatorSet();
        set.setDuration(DURATION);
        set.playTogether(rotate, move);
        return set;
    }

    public static Animator prepareSwitchTransition(View switcher, View view1, View view2) {
        ObjectAnimator rotate = ObjectAnimator.ofFloat(switcher, "rotation", 0, 180);
        ObjectAnimator moveOriginal = ObjectAnimator.ofFloat(view1, "y", view1.getY(), view2.getY());
        ObjectAnimator moveTranslation = ObjectAnimator.ofFloat(view2, "y", view2.getY(), view1.getY());
        AnimatorSet set = new AnimatorSet();
        set.playTogether(rotate, moveOriginal, moveTranslation);
        set.setDuration(DURATION);
        return set;
    }
}
