package dmax.words.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import dmax.words.R;
import dmax.words.ui.animation.AnimationLayout;
import dmax.words.ui.animation.DecelerateKeepAccelerateInterpolator;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 09.01.15 at 16:51
 */
public class UpdateDialog extends AlertDialog {

    private static final int COUNT = 5;
    private static final int DELAY = 200;
    private static final int DURATION = 1500;

    private AnimationLayout[] spots;
    private Animator[] animators;

    private AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            animate();
        }
    };

    public UpdateDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.v_loading_dilalog);
        setCanceledOnTouchOutside(false);

        ((TextView) findViewById(R.id.title)).setText(R.string.importing);

        initProgress();
    }

    @Override
    protected void onStart() {
        super.onStart();

        animate();
    }

    //~

    private void animate() {
        if (animators == null) animators = createAnimations();
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators);
        set.addListener(listener);
        set.start();
    }

    private void initProgress() {
        FrameLayout progress = (FrameLayout) findViewById(R.id.progress);

        spots = new AnimationLayout[COUNT];
        int size = getContext().getResources().getDimensionPixelSize(R.dimen.spot_size);
        int progressWidth = getContext().getResources().getDimensionPixelSize(R.dimen.progress_width);
        for (int i = 0; i < COUNT; i++) {
            AnimationLayout v = new AnimationLayout(getContext());
            v.setBackgroundResource(R.drawable.spot);
            v.setXRatio(-1f);
            v.setTarget(progressWidth);
            progress.addView(v, size, size);
            spots[i] = v;
        }
    }

    private Animator[] createAnimations() {
        Animator[] animators = new Animator[COUNT];
        for (int i = 0; i < COUNT; i++) {
            Animator move = ObjectAnimator.ofFloat(spots[i], "xFactor", 0, 1);
            move.setDuration(DURATION);
            move.setInterpolator(new DecelerateKeepAccelerateInterpolator());
            move.setStartDelay(DELAY * i);
            animators[i] = move;
        }
        return animators;
    }
}
