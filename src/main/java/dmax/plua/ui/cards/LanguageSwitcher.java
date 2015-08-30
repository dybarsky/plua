package dmax.plua.ui.cards;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import dmax.plua.R;
import dmax.plua.domain.Language;
import dmax.plua.ui.AnimationLayout;
import dmax.plua.ui.Util;

/**
 // switch elevation
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 05.01.15 at 12:30
 */
class LanguageSwitcher extends AnimatorListenerAdapter implements View.OnClickListener {

    private CardsFragment cardsFragment;
    private boolean expanded = false;
    private float elevation = -1;

    private Language selectedLanguage;

    private ImageView actionBarIcon;
    private TextView actionBarText;
    private AnimationLayout languagesList;

    LanguageSwitcher(CardsFragment cardsFragment, Language selectedLanguage) {
        this.cardsFragment = cardsFragment;
        this.selectedLanguage = selectedLanguage;
    }

    /**
     * Initialize switcher internal state and set initial settings for languages panel
     * @param rootView layout root view
     */
    public void init(View rootView) {
        languagesList = (AnimationLayout) rootView.findViewById(R.id.languages);
        // hide languages panel
        //languagesList.setYRatio(-1);
        //rootView.findViewById(R.id.ukrainian).setOnClickListener(this);
        //rootView.findViewById(R.id.polish).setOnClickListener(this);

        actionBarIcon = (ImageView) rootView.findViewById(R.id.ab_icon);
        actionBarText = (TextView) rootView.findViewById(R.id.ab_lang);

        actionBarText.setText(selectedLanguage.getCodeName());

        actionBarText.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionbar:
                onActionBarClicked();
                break;
            case R.id.ukrainian:
                onLanguageItemClicked(Language.UKRAINIAN);
                break;
            case R.id.polish:
                onLanguageItemClicked(Language.POLISH);
                break;
        }
    }

    /**
     * Reacts on languages selection
     * @param language
     */
    private void onLanguageItemClicked(Language language) {
        if (!selectedLanguage.equals(language)) {
            selectedLanguage = language;
            actionBarText.setText(selectedLanguage.getCodeName());
            cardsFragment.updateLanguage(selectedLanguage);
        }
        // hide panel
        onActionBarClicked();
    }

    /**
     * Reacts on actionbar item click
     */
    private void onActionBarClicked() {
        // init default elevation
        if (elevation == -1) {
            elevation = cardsFragment.getActivity().getActionBar().getElevation();
        }

        Animator transition;
        if (expanded) {
            transition = Util.prepareCollapseTransition(actionBarIcon, languagesList);
            transition.addListener(this);
        } else {
            // switch elevation
            cardsFragment.getActivity().getActionBar().setElevation(0);
            languagesList.setElevation(elevation);
            transition = Util.prepareExpandTransition(actionBarIcon, languagesList);
        }
        transition.start();

        expanded = !expanded;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        // switch elevation
        languagesList.setElevation(0);
        cardsFragment.getActivity().getActionBar().setElevation(elevation);
    }
}