package dmax.plua.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import dmax.plua.R;
import dmax.plua.settings.OrderMode;
import dmax.plua.settings.Settings;
import dmax.plua.ui.MainActivity;

/**
 * Settings screen: a list of settings, one item for now - cards order mode (random / spaced repetition).
 * A change is saved immediately; {@link dmax.plua.DataSource} picks it up next time links are requested.
 */
public class SettingsFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    @Inject
    Settings settings;

    private SwitchCompat orderSwitch;
    private TextView orderDescription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MainActivity) requireActivity()).getGraph().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.f_settings, container, false);

        orderSwitch = (SwitchCompat) root.findViewById(R.id.order_switch);
        orderDescription = (TextView) root.findViewById(R.id.order_description);

        // set the state before attaching the listener so initial value is not saved again
        orderSwitch.setChecked(settings.getOrderMode() == OrderMode.SPACED);
        showOrderDescription(orderSwitch.isChecked());
        orderSwitch.setOnCheckedChangeListener(this);
        root.findViewById(R.id.setting_order).setOnClickListener(this);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        ActionBar actionBar = ((MainActivity) requireActivity()).getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setTitle(R.string.settings);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            requireActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Click on the whole row toggles the switch. */
    @Override
    public void onClick(View v) {
        orderSwitch.toggle();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        settings.setOrderMode(isChecked ? OrderMode.SPACED : OrderMode.RANDOM);
        showOrderDescription(isChecked);
    }

    private void showOrderDescription(boolean spaced) {
        orderDescription.setText(spaced ? R.string.settings_order_spaced : R.string.settings_order_random);
    }
}
