package dmax.words.ui;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 07.01.15 at 13:01
 */
public class Util {

    public static View createDarkThemedView(Context context, int resId) {
        ContextThemeWrapper themed = new ContextThemeWrapper(context, android.R.style.ThemeOverlay_Material_Dark);
        return View.inflate(themed, resId, null);
    }
}
