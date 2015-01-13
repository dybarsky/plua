package dmax.words.ui.animation;

import android.view.animation.Interpolator;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 12.01.15 at 15:47
 */
public class DecelerateKeepAccelerateInterpolator implements Interpolator {

    private double pow = 1.0/2.0;

    @Override
    public float getInterpolation(float input) {
        return input < 0.5
                ? (float) Math.pow(input * 2, pow) * 0.5f
                : (float) Math.pow((1 - input) * 2, pow) * -0.5f + 1;
    }
}
