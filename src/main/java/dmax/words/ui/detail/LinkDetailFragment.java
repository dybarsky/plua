package dmax.words.ui.detail;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import dmax.words.R;
import dmax.words.domain.Language;
import dmax.words.domain.Link;
import dmax.words.domain.Word;
import dmax.words.ui.MainActivity;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 22.12.14 at 17:01
 */
public class LinkDetailFragment extends Fragment implements View.OnClickListener {

    public static final String KEY_ORIGINAL = "original";
    public static final String KEY_TRANSLATION = "translation";
    public static final String KEY_LINK = "link";

    private static final int DURATION = 250;

    private ImageView originalFlag;
    private ImageView translationFlag;
    private ImageView switchFlag;
    private EditText originalText;
    private EditText translationText;

    private Language current;

    private Word originalWord;
    private Word translationWord;
    private Link link;
    private boolean edit = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.f_addword, container, false);

        this.switchFlag = (ImageView) root.findViewById(R.id.switch_lang);
        this.originalFlag = (ImageView) root.findViewById(R.id.origin_lang);
        this.translationFlag = (ImageView) root.findViewById(R.id.transl_lang);
        this.originalText = (EditText) root.findViewById(R.id.origin_text);
        this.translationText = (EditText) root.findViewById(R.id.transl_text);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        MainActivity activity = getCastedActivity();

        ActionBar actionBar = activity.getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE|ActionBar.DISPLAY_HOME_AS_UP);
        actionBar.setTitle(R.string.add_word_title);
        setHasOptionsMenu(true);

        this.switchFlag.bringToFront();
        this.switchFlag.setOnClickListener(this);

        current = activity.getDataSource().getSelectedLanguage();

        this.originalFlag.setImageResource(current.equals(Language.UKRAINIAN)
                ? R.drawable.ic_ukrainian
                : R.drawable.ic_polish);
        this.translationFlag.setImageResource(current.equals(Language.UKRAINIAN)
                ? R.drawable.ic_polish
                : R.drawable.ic_ukrainian);

        Bundle args = getArguments();
        if (args != null && args.containsKey(KEY_ORIGINAL) && args.containsKey(KEY_TRANSLATION)) {
            originalWord = (Word) args.getSerializable(KEY_ORIGINAL);
            translationWord = (Word) args.getSerializable(KEY_TRANSLATION);
            link = (Link) args.getSerializable(KEY_LINK);

            originalText.setText(originalWord.getData());
            translationText.setText(translationWord.getData());

            edit = true;
            actionBar.setTitle(R.string.edit_word_title);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.add_word, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                closeSelf();
                return true;
            }
            case R.id.save: {
                save();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onClick(View v) {
        this.current = current.equals(Language.UKRAINIAN) ? Language.POLISH : Language.UKRAINIAN;
        Word tmp = originalWord;
        originalWord = translationWord;
        translationWord = tmp;

        ObjectAnimator rotate = ObjectAnimator.ofFloat(switchFlag, "rotation", 0, 180);
        ObjectAnimator moveOriginal = ObjectAnimator.ofFloat(originalFlag, "y", originalFlag.getY(), translationFlag.getY());
        ObjectAnimator moveTranslation = ObjectAnimator.ofFloat(translationFlag, "y", translationFlag.getY(), originalFlag.getY());
        AnimatorSet set = new AnimatorSet();
        set.playTogether(rotate, moveOriginal, moveTranslation);
        set.setDuration(DURATION);
        set.start();
    }

    private void save() {
        if (check(originalText) || check(translationText)) return;

        Word word1 = new Word();
        word1.setLanguage(current);
        word1.setData(originalText.getText().toString());

        Word word2 = new Word();
        word2.setLanguage(current.equals(Language.UKRAINIAN) ? Language.POLISH : Language.UKRAINIAN);
        word2.setData(translationText.getText().toString());

        if (edit) {
            word1.setId(word1.getData().equals(originalWord.getData()) ? -1 : originalWord.getId());
            word2.setId(word2.getData().equals(translationWord.getData()) ? -1 : translationWord.getId());

            getCastedActivity().getDataSource().updateWords(link, word1, word2);
        } else {
            getCastedActivity().getDataSource().addWords(word1, word2);
        }

        closeSelf();
    }

    private void closeSelf() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(originalText.getWindowToken(), 0);

        getActivity().getFragmentManager().popBackStack();
    }

    private boolean check(EditText text) {
        boolean isEmpty = text.getText() == null || text.getText().length() == 0;
        text.setError(isEmpty ? getString(R.string.type_error) : null);
        return isEmpty;
    }

    private MainActivity getCastedActivity() {
        return (MainActivity) getActivity();
    }
}