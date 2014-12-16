package dmax.words.persist.dao;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;

import java.util.Iterator;

import dmax.words.domain.Language;
import dmax.words.domain.Link;
import dmax.words.domain.Word;
import dmax.words.persist.Dao;
import dmax.words.persist.DataBaseHelper;
import dmax.words.persist.DataBaseManager;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 15.12.14 at 15:56
 */
public class WordDaoTest extends AndroidTestCase {

    private DataBaseManager db;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        RenamingDelegatingContext newContext = new RenamingDelegatingContext(getContext(), "test_");
        db = new DataBaseManager(newContext);
        db.open();
    }

    public void test_shouldReturnCorrectDao() {
        Dao<Word> dao = DaoFactory.createDao(Word.class);

        assertEquals(WordDao.class, dao.getClass());
    }

    public void test_shouldSave() {
        Word w = new Word();
        w.setLanguage(Language.UKRAINIAN);
        w.setData("укр");

        Dao<Word> dao = DaoFactory.createDao(Word.class);
        Word saved = db.save(dao.setPersistable(w));

        assertFalse(saved.getId() == -1);
        assertTrue(w.equals(saved));
    }

    public void test_shouldRetrieve() {
        Word w = new Word();
        w.setLanguage(Language.UKRAINIAN);
        w.setData("укр");

        Dao<Word> dao = DaoFactory.createDao(Word.class);
        Word saved = db.save(dao.setPersistable(w));
        long id = saved.getId();

        w = new Word();
        w.setId(id);
        w.setLanguage(Language.UKRAINIAN);
        Word retrieved = db.retrieve(dao.setPersistable(w));

        assertNotNull(retrieved);
        assertTrue(retrieved.getId() == saved.getId());
        assertTrue(retrieved.getLanguage() == saved.getLanguage());
        assertTrue(retrieved.getData().equals(saved.getData()));
    }

    public void test_shouldDelete() {
        Word w = new Word();
        w.setLanguage(Language.UKRAINIAN);
        w.setData("укр");

        Dao<Word> dao = DaoFactory.createDao(Word.class);
        Word saved = db.save(dao.setPersistable(w));

        w = new Word();
        w.setId(saved.getId());
        w.setLanguage(saved.getLanguage());
        boolean deleteResult = db.delete(dao.setPersistable(w));
        Word retrieved = db.retrieve(dao.setPersistable(w));

        assertTrue(deleteResult);
        assertNull(retrieved);
    }

    public void test_shouldRetrieveIterator() {
        Word w = new Word();
        w.setLanguage(Language.UKRAINIAN);
        w.setData("укр");

        Word w2 = new Word();
        w2.setLanguage(Language.UKRAINIAN);
        w2.setData("горілка");

        Dao<Word> dao = DaoFactory.createDao(Word.class);
        assertNotNull(db.save(dao.setPersistable(w)));
        assertNotNull(db.save(dao.setPersistable(w2)));

        Word w3 = new Word();
        w3.setLanguage(Language.UKRAINIAN);
        Iterator<Word> it = db.retrieveIterator(dao.setPersistable(w3));

        assertNotNull(it);
        assertTrue(it.hasNext()); // first
        assertNotNull(it.next());
        assertTrue(it.hasNext()); // second
        assertNotNull(it.next());
        assertFalse(it.hasNext()); // no else
    }
}
