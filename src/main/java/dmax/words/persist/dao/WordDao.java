package dmax.words.persist.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Iterator;

import dmax.words.domain.Language;
import dmax.words.domain.Word;
import dmax.words.persist.Dao;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 15.12.14 at 11:14
 */
public class WordDao extends Dao<Word> {

    public WordDao() {
        super(Word.class);
    }

    @Override
    public Word save(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_WORD_DATA, persistable.getData());

        long id = db.insert(getTable(), null, values);
        persistable.setId(id);
        return persistable;
    }

    @Override
    public Word retrieve(SQLiteDatabase db) {
        String[] args = new String[] {
                getTable(),
                String.valueOf(getId())
        };

        Cursor result = db.rawQuery(SQL_SELECT_BY_ID, args);
        if (result.getCount() == 0) return null;

        return createWord(result, persistable.getLanguage());
    }

    @Override
    public boolean delete(SQLiteDatabase db) {
        int rows = db.delete(getTable(), COLUMN_ID + "=" + getId(), null);
        return rows > 0;
    }

    @Override
    public Iterator<Word> retrieveIterator(SQLiteDatabase db) {
        Cursor result = db.rawQuery(SQL_SELECT_ALL, new String[] {getTable()});
        return new WordIterator(result, persistable.getLanguage());
    }

    //~

    private String getTable() {
        String table;
        Language language = persistable.getLanguage();
        table = Language.POLISH.equals(language) ? TABLE_POLISH : null;
        table = Language.UKRAINIAN.equals(language) ? TABlE_UKRAINIAN : null;

        if (table == null) throw new IllegalArgumentException("Wrong language");
        return table;
    }

    private static Word createWord(Cursor cursor, Language language) {
        Word word = new Word();
        word.setId(cursor.getLong(COLUMN_ID_INDEX));
        word.setData(cursor.getString(COLUMN_WORD_DATA_INDEX));
        word.setLanguage(language);
        return word;
    }

    //~

    private static class WordIterator implements Iterator<Word> {

        private Cursor cursor;
        private Language language;

        private WordIterator(Cursor cursor, Language language) {
            this.cursor = cursor;
            this.language = language;
        }

        @Override
        public boolean hasNext() {
            return !cursor.isLast();
        }

        @Override
        public Word next() {
            cursor.moveToNext();
            return createWord(cursor, language);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
