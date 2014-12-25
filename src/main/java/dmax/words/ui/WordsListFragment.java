package dmax.words.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import dmax.words.R;
import dmax.words.domain.Language;

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
        this.adapter = new WordsListPagerAdapter(activity, activity.getDataBaseManager(), Language.UKRAINIAN);
        this.switcher = new LanguageSwitcher(activity);
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
        adapter.notifyDataSetChanged();
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

    private static class LanguageSwitcher extends AnimatorListenerAdapter implements View.OnClickListener {

        private static final int DURATION = 300;

        private boolean expanded = false;
        private float elevation = -1;

        private Language selectedLanguage = Language.POLISH;

        private Activity activity;

        private ImageView actionBarIcon;
        private TextView actionBarText;
        private AnimationLayout languagesList;
        private View ukrainianItem;
        private View polishItem;

        private LanguageSwitcher(Activity activity) {
            this.activity = activity;
        }

        private void init(View rootView) {
            languagesList = (AnimationLayout) rootView.findViewById(R.id.languages);
            languagesList.setYRatio(-1);
        }

        private View createActionBar() {
            View root = View.inflate(activity, R.layout.v_action_language, null);

            actionBarIcon = (ImageView) root.findViewById(R.id.ab_icon);
            actionBarText = (TextView) root.findViewById(R.id.ab_lang);

            actionBarText.setText(selectedLanguage.getCodeName());

            root.setOnClickListener(this);

            return root;
        }

        @Override
        public void onClick(View v) {
            if (elevation == -1) {
                this.elevation = activity.getActionBar().getElevation();
            }

            AnimatorSet set = new AnimatorSet();
            set.setDuration(DURATION);

            if (expanded) {
                Animator rotate = ObjectAnimator.ofFloat(actionBarIcon, "rotation", 180, 360);
                Animator move = ObjectAnimator.ofFloat(languagesList, "yRatio", 0, -1);
                set.playTogether(rotate, move);
                set.addListener(this);
            } else {
                activity.getActionBar().setElevation(0);
                languagesList.setElevation(elevation);
                Animator rotate = ObjectAnimator.ofFloat(actionBarIcon, "rotation", 0, 180);
                Animator move = ObjectAnimator.ofFloat(languagesList, "yRatio", -1, 0);
                set.playTogether(rotate, move);
            }
            set.start();

            expanded = !expanded;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            languagesList.setElevation(0);
            activity.getActionBar().setElevation(elevation);
        }

        public Language getSelectedLanguage() {
            return selectedLanguage;
        }
    }
}
