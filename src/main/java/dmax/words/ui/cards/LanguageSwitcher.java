package dmax.words.ui.cards;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import dmax.words.R;
import dmax.words.domain.Language;
import dmax.words.ui.AnimationLayout;
import dmax.words.ui.Util;

/**
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

    public void init(View rootView) {
        languagesList = (AnimationLayout) rootView.findViewById(R.id.languages);
        languagesList.setYRatio(-1);
        rootView.findViewById(R.id.ukrainian).setOnClickListener(this);
        rootView.findViewById(R.id.polish).setOnClickListener(this);
    }

    public View createActionBar() {
        View root = Util.createDarkThemedView(cardsFragment.getActivity(), R.layout.v_actionbar_item_language);

        actionBarIcon = (ImageView) root.findViewById(R.id.ab_icon);
        actionBarText = (TextView) root.findViewById(R.id.ab_lang);

        actionBarText.setText(selectedLanguage.getCodeName());

        root.setOnClickListener(this);

        return root;
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

    private void onLanguageItemClicked(Language language) {
        if (!selectedLanguage.equals(language)) {
            selectedLanguage = language;
            actionBarText.setText(selectedLanguage.getCodeName());
            cardsFragment.updateLanguage(selectedLanguage);
        }
        onActionBarClicked();
    }

    public void onActionBarClicked() {
        if (elevation == -1) {
            this.elevation = cardsFragment.getActivity().getActionBar().getElevation();
        }

        Animator transition;
        if (expanded) {
            transition = Util.prepareCollapseTransition(actionBarIcon, languagesList);
            transition.addListener(this);
        } else {
            cardsFragment.getActivity().getActionBar().setElevation(0);
            languagesList.setElevation(elevation);
            transition = Util.prepareExpandTransition(actionBarIcon, languagesList);
        }
        transition.start();

        expanded = !expanded;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        languagesList.setElevation(0);
        cardsFragment.getActivity().getActionBar().setElevation(elevation);
    }
}