package dmax.words.ui;

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
import dmax.words.persist.Dao;
import dmax.words.persist.DataBaseManager;
import dmax.words.persist.dao.DaoFactory;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 22.12.14 at 17:01
 */
public class AddWordFragment extends Fragment {

    private ImageView originalFlag;
    private ImageView translationFlag;
    private EditText originalText;
    private EditText translationText;

    private boolean updateList = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.f_addword, container, false);

        originalFlag = (ImageView) root.findViewById(R.id.origin_lang);
        translationFlag = (ImageView) root.findViewById(R.id.transl_lang);
        originalText = (EditText) root.findViewById(R.id.origin_text);
        translationText = (EditText) root.findViewById(R.id.transl_text);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.add_word_title);
        setHasOptionsMenu(true);

        originalFlag.setImageResource(R.drawable.ic_ukrainian);
        translationFlag.setImageResource(R.drawable.ic_polish);
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
    public void onDestroy() {
        super.onDestroy();

        if (updateList) {
            getCastedActivity().onDatabaseUpdated();
        }
    }

    private void save() {
        if (check(originalText) || check(translationText)) return;

        Word word1 = new Word();
        word1.setLanguage(Language.UKRAINIAN);
        word1.setData(originalText.getText().toString());

        Word word2 = new Word();
        word2.setLanguage(Language.POLISH);
        word2.setData(translationText.getText().toString());

        Link link = new Link();

        DataBaseManager dataBaseManager = getCastedActivity().getDataBaseManager();
        Dao<Word> wordDao = DaoFactory.createDao(Word.class);
        Dao<Link> linkDao = DaoFactory.createDao(Link.class);

        link.setWord(dataBaseManager.save(wordDao.setPersistable(word1)));
        link.setWord(dataBaseManager.save(wordDao.setPersistable(word2)));
        dataBaseManager.save(linkDao.setPersistable(link));

        updateList = true;

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
