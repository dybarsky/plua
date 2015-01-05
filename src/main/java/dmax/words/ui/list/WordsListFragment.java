package dmax.words.ui.list;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import dmax.words.DataSource;
import dmax.words.R;
import dmax.words.domain.Language;
import dmax.words.ui.add.AddWordFragment;
import dmax.words.ui.AnimationLayout;
import dmax.words.ui.MainActivity;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 18.12.14 at 12:25
 */
public class WordsListFragment extends Fragment implements View.OnClickListener {

    private ViewPager pager;
    private WordsListPagerAdapter adapter;
    private LanguageSwitcher switcher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity activity = (MainActivity) getActivity();
        DataSource dataSource = activity.getDataSource();
        this.switcher = new LanguageSwitcher(dataSource.getSelectedLanguage());
        this.adapter = new WordsListPagerAdapter(activity, activity.getDataSource());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.f_wordslist, container, false);

        pager = (ViewPager) root.findViewById(R.id.pager);
        pager.setAdapter(adapter);

        ImageButton add = (ImageButton) root.findViewById(R.id.add);
        add.setOnClickListener(this);

        switcher.init(root);

        return root;
    }

    public void updateList() {
        adapter.setSelectedLanguage(switcher.getSelectedLanguage());
        pager.setAdapter(adapter);
    }

    public void reloadList() {
        adapter.reset();
        pager.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(null);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(switcher.createActionBar());
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.word_list, menu);
    }

    @Override
    public void onClick(View v) {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.animator.slide_up, 0, 0, R.animator.slide_down)
                .replace(R.id.container, new AddWordFragment())
                .addToBackStack(null)
                .commit();
    }

    //~

    private class LanguageSwitcher extends AnimatorListenerAdapter implements View.OnClickListener {

        private static final int DURATION = 250;

        private boolean expanded = false;
        private float elevation = -1;

        private Language selectedLanguage;

        private ImageView actionBarIcon;
        private TextView actionBarText;
        private AnimationLayout languagesList;

        private LanguageSwitcher(Language selectedLanguage) {
            this.selectedLanguage = selectedLanguage;
        }

        private void init(View rootView) {
            languagesList = (AnimationLayout) rootView.findViewById(R.id.languages);
            languagesList.setYRatio(-1);
            rootView.findViewById(R.id.ukrainian).setOnClickListener(this);
            rootView.findViewById(R.id.polish).setOnClickListener(this);
        }

        private View createActionBar() {
            View root = View.inflate(getActivity(), R.layout.v_action_language, null);

            actionBarIcon = (ImageView) root.findViewById(R.id.ab_icon);
            actionBarText = (TextView) root.findViewById(R.id.ab_lang);

            actionBarText.setText(selectedLanguage.getCodeName());

            root.setOnClickListener(this);

            return root;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.actionbar: onActionBarClicked(); break;
                case R.id.ukrainian: onLanguageItemClicked(Language.UKRAINIAN); break;
                case R.id.polish:    onLanguageItemClicked(Language.POLISH); break;
            }
        }

        private void onLanguageItemClicked(Language language) {
            if (!selectedLanguage.equals(language)) {
                selectedLanguage = language;
                actionBarText.setText(selectedLanguage.getCodeName());
                updateList();
            }
            onActionBarClicked();
        }

        private void onActionBarClicked() {
            if (elevation == -1) {
                this.elevation = getActivity().getActionBar().getElevation();
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
                getActivity().getActionBar().setElevation(0);
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
            getActivity().getActionBar().setElevation(elevation);
        }

        public Language getSelectedLanguage() {
            return selectedLanguage;
        }
    }
}
