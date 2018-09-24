package dmax.plua.ui.detail;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import javax.inject.Inject;

import dmax.plua.DataSource;
import dmax.plua.R;
import dmax.plua.domain.Language;
import dmax.plua.domain.Word;
import dmax.plua.ui.MainActivity;
import dmax.plua.ui.Util;

/**
 * Fragment for show words pair for add/edit operation.
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 22.12.14 at 17:01
 */
public class LinkDetailFragment extends Fragment implements View.OnClickListener {

    public static final String KEY_ORIGINAL = "original";
    public static final String KEY_TRANSLATION = "translation";

    private ImageView originalFlag;
    private ImageView translationFlag;
    private ImageView switchFlag;
    private EditText originalText;
    private EditText translationText;

    private Language current;

    private Word originalWord;
    private Word translationWord;
    private boolean edit = false;

    @Inject
    DataSource dataSource;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.f_detail, container, false);

        this.switchFlag = (ImageView) root.findViewById(R.id.switch_lang);
        this.originalFlag = (ImageView) root.findViewById(R.id.origin_lang);
        this.translationFlag = (ImageView) root.findViewById(R.id.transl_lang);
        this.originalText = (EditText) root.findViewById(R.id.origin_text);
        this.translationText = (EditText) root.findViewById(R.id.transl_text);

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getCastedActivity().getGraph().inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        ActionBar actionBar = getCastedActivity().getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_close);
        actionBar.setTitle(R.string.add_word_title);
        setHasOptionsMenu(true);

        this.switchFlag.bringToFront();
        this.switchFlag.setOnClickListener(this);

        current = dataSource.getSelectedLanguage();

        this.originalFlag.setImageResource(current.equals(Language.UKRAINIAN)
                ? R.mipmap.ic_ukrainian
                : R.mipmap.ic_german);
        this.translationFlag.setImageResource(current.equals(Language.UKRAINIAN)
                ? R.mipmap.ic_german
                : R.mipmap.ic_ukrainian);

        Bundle args = getArguments();
        // if some args - use edit mode
        if (args != null && args.containsKey(KEY_ORIGINAL) && args.containsKey(KEY_TRANSLATION)) {
            originalWord = (Word) args.getSerializable(KEY_ORIGINAL);
            translationWord = (Word) args.getSerializable(KEY_TRANSLATION);

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
        this.current = current.equals(Language.UKRAINIAN) ? Language.GERMAN : Language.UKRAINIAN;
        Word tmp = originalWord;
        originalWord = translationWord;
        translationWord = tmp;
        Util.prepareSwitchTransition(switchFlag, originalFlag, translationFlag).start();
    }

    private void save() {
        if (check(originalText) || check(translationText)) return;

        Word word1 = new Word();
        word1.setLanguage(current);
        word1.setData(originalText.getText().toString());

        Word word2 = new Word();
        word2.setLanguage(current.equals(Language.UKRAINIAN) ? Language.GERMAN : Language.UKRAINIAN);
        word2.setData(translationText.getText().toString());

        boolean saved;
        if (edit) {
            // if not changed, use -1 as id. in this case word won't be updated in database
            word1.setId(word1.getData().equals(originalWord.getData()) ? -1 : originalWord.getId());
            word2.setId(word2.getData().equals(translationWord.getData()) ? -1 : translationWord.getId());

            saved = dataSource.updateWords(word1, word2);
        } else {
            saved = dataSource.addWords(word1, word2);
        }
        if (!saved) Util.toast(getActivity(), R.string.saving_error);

        closeSelf();
    }

    private void closeSelf() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(originalText.getWindowToken(), 0);

        getCastedActivity().getSupportFragmentManager().popBackStack();
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
