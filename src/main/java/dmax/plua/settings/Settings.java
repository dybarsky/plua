package dmax.plua.settings;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * User settings persisted in shared preferences (file {@code settings}, not part of the backup rules).
 */
public class Settings {

    static final String PREFS_NAME = "settings";
    static final String KEY_ORDER_MODE = "order_mode";

    private final SharedPreferences prefs;

    public Settings(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public OrderMode getOrderMode() {
        return OrderMode.fromName(prefs.getString(KEY_ORDER_MODE, null));
    }

    public void setOrderMode(OrderMode mode) {
        prefs.edit().putString(KEY_ORDER_MODE, mode.name()).apply();
    }
}
