package dmax.words.ui.cards;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
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

    private static final int DURATION = 250;

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

        AnimatorSet set = new AnimatorSet();
        set.setDuration(DURATION);

        if (expanded) {
            ObjectAnimator rotate = ObjectAnimator.ofFloat(actionBarIcon, "rotation", 180, 360);
            ObjectAnimator move = ObjectAnimator.ofFloat(languagesList, "yRatio", 0, -1);
            move.setInterpolator(new AccelerateInterpolator(1f));
            set.playTogether(rotate, move);
            set.addListener(this);
        } else {
            cardsFragment.getActivity().getActionBar().setElevation(0);
            languagesList.setElevation(elevation);
            ObjectAnimator rotate = ObjectAnimator.ofFloat(actionBarIcon, "rotation", 0, 180);
            ObjectAnimator move = ObjectAnimator.ofFloat(languagesList, "yRatio", -1, 0);
            move.setInterpolator(new DecelerateInterpolator(1f));
            set.playTogether(rotate, move);
        }
        set.start();

        expanded = !expanded;
    }

    @Override
    public void onAnimationEnd(Animator animation) {
        languagesList.setElevation(0);
        cardsFragment.getActivity().getActionBar().setElevation(elevation);
    }
}