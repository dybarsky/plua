package dmax.words.ui.animation;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 22.12.14 at 18:23
 */
public class AnimationLayout extends FrameLayout {

    public AnimationLayout(Context context) {
        super(context);
    }

    public AnimationLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AnimationLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AnimationLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public float getYRatio() {
        int height = getHeight();
        return height != 0 ? getY() / height : -1;
    }

    public void setYRatio(float yRatio) {
        int height = getHeight();
        setY(height > 0 ? yRatio * height : -9999);
    }

    public float getXRatio() {
        int width = getWidth();
        return width != 0 ? getX() / width : -1;
    }

    public void setXRatio(float xRatio) {
//        int width = getWidth();
        setX(xRatio * 500);
    }
}
