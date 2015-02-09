package dmax.plua.persist.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Iterator;

import dmax.plua.domain.Language;
import dmax.plua.domain.Word;
import dmax.plua.persist.Dao;

/**
 * Dao class for encapsulate database operations with {@link dmax.plua.domain.Word} instances.
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 15.12.14 at 11:14
 */
class WordDao extends Dao<Word> {

    public WordDao() {
        super(Word.class);
    }

    @Override
    public Word insert(SQLiteDatabase db) {
        if (persistable == null) throw new IllegalArgumentException("No data set");

        ContentValues values = new ContentValues();
        values.put(COLUMN_WORD_DATA, persistable.getData());

        long id = db.insert(getTable(), null, values);
        persistable.setId(id);
        return persistable;
    }

    @Override
    public Word update(SQLiteDatabase db) {
        if (persistable == null) throw new IllegalArgumentException("No data set");

        ContentValues values = new ContentValues();
        values.put(COLUMN_WORD_DATA, persistable.getData());

        int count = db.update(getTable(), values, COLUMN_ID + "=?", new String[]{ String.valueOf(getId()) });
        return count > 0 ? persistable : null;
    }

    @Override
    public Word retrieve(SQLiteDatabase db) {
        Language language = getLanguage();
        String sql;
        switch (language) {
            case POLISH: sql = SQL_SELECT_BY_ID_POLISH; break;
            case UKRAINIAN: sql = SQL_SELECT_BY_ID_UKRAINIAN; break;
            default: throw new IllegalArgumentException("Language not supported");
        }
        Cursor result = db.rawQuery(sql, new String[]{ String.valueOf(getId()) });
        if (result.getCount() == 0) return null;
        result.moveToFirst();

        return createWord(result, language);
    }

    @Override
    public boolean delete(SQLiteDatabase db) {
        int rows = db.delete(getTable(), COLUMN_ID + "=" + getId(), null);
        return rows > 0;
    }

    @Override
    public Iterator<Word> retrieveIterator(SQLiteDatabase db) {
        Language language = getLanguage();
        String sql;
        switch (language) {
            case POLISH: sql = SQL_SELECT_ALL_POLISH; break;
            case UKRAINIAN: sql = SQL_SELECT_ALL_UKRAINIAN; break;
            default: throw new IllegalArgumentException("Language not supported");
        }
        Cursor result = db.rawQuery(sql, null);
        return new WordIterator(result, persistable.getLanguage());
    }

    //~

    private String getTable() {
        switch (getLanguage()) {
            case POLISH: return TABLE_POLISH;
            case UKRAINIAN: return TABlE_UKRAINIAN;
            default: throw new IllegalArgumentException("Language not supported");
        }
    }

    private Language getLanguage() {
        Language language = null;
        if (persistable != null) {
            language = persistable.getLanguage();
        }
        if (language == null) throw new IllegalArgumentException("No persistable with correct language set");
        return language;
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
            return !cursor.isLast() && cursor.getCount() != 0;
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
