package dmax.plua.ui.cards;

import android.content.Context;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import dmax.plua.R;
import dmax.plua.domain.Language;

/**
 // switch elevation
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 05.01.15 at 12:30
 */
 class LanguageSwitcher implements AdapterView.OnItemSelectedListener {

    private Language selectedLanguage;
    private CardsFragment cardsFragment;
    private CustomAdapter spinnerAdapter;

    LanguageSwitcher(CardsFragment cardsFragment, Language selectedLanguage) {
        this.cardsFragment = cardsFragment;
        this.selectedLanguage = selectedLanguage;

        AppCompatActivity activity = cardsFragment.getCastedActivity();

        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        View spinnerContainer = LayoutInflater.from(activity).inflate(R.layout.v_spinner, null, false);
        activity.getSupportActionBar().setCustomView(spinnerContainer, lp);

        spinnerAdapter = new CustomAdapter(activity);
        Spinner spinner = (Spinner) spinnerContainer.findViewById(R.id.language_switcher);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Language language = spinnerAdapter.getItem(position);
        if (!selectedLanguage.equals(language)) {
            selectedLanguage = language;
            cardsFragment.updateLanguage(selectedLanguage);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { /*  */ }

    //~

    private static class CustomAdapter extends BaseAdapter {

        private String tagDd = "Dropdown";
        private String tagNdd = "NonDropdown";
        private Context context;

        public CustomAdapter(Context context) {
            this.context = context;
        }

        @Override
        public View getDropDownView(int position, View view, ViewGroup parent) {
            if (view == null || !view.getTag().equals(tagDd)) {
                view = LayoutInflater.from(context).inflate(R.layout.v_language_item_dropdown, parent, false);
                view.setTag(tagDd);
            }
            bindView(view, getItem(position));
            return view;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null || !view.getTag().equals(tagNdd)) {
                view = LayoutInflater.from(context).inflate(R.layout.v_language_item_actionbar, parent, false);
                view.setTag(tagNdd);
            }
            bindView(view, getItem(position));
            return view;
        }

        private void bindView(View view, Language language) {
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(language.getCodeName());

            ImageView imageView = (ImageView) view.findViewById(android.R.id.icon1);
            if (imageView != null) {
                int flagId = -1;
                switch (language) {
                    case UKRAINIAN: flagId = R.mipmap.ic_ukrainian; break;
                    case GERMAN: flagId = R.mipmap.ic_german; break;
                }
                imageView.setImageResource(flagId);
            }
        }

        @Override
        public int getCount() {
            return Language.values().length;
        }

        @Override
        public Language getItem(int position) {
            return Language.values()[position];
        }

        @Override
        public long getItemId(int position) {
            return Language.values()[position].ordinal();
        }
    }
}
