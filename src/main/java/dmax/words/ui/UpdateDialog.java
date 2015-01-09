package dmax.words.ui;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 09.01.15 at 16:51
 */
public class UpdateDialog extends AlertDialog {

    public UpdateDialog(Context context) {
        super(context);

        setCanceledOnTouchOutside(false);
        setMessage("Loading");
    }
}
