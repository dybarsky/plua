package dmax.words.persist.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Iterator;

import dmax.words.domain.Language;
import dmax.words.domain.Link;
import dmax.words.persist.Dao;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 15.12.14 at 11:12
 */
class LinkDao extends Dao<Link> {

    public LinkDao() {
        super(Link.class);
    }

    @Override
    public Link save(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_LINK_ORIGINAL, persistable.getWordId(Language.UKRAINIAN));
        values.put(COLUMN_LINK_TRANSLATION, persistable.getWordId(Language.POLISH));

        long id = db.insert(TABlE_LINK, null, values);
        persistable.setId(id);
        return persistable;
    }

    @Override
    public Link retrieve(SQLiteDatabase db) {
        String[] args = new String[] {
                TABlE_LINK,
                String.valueOf(getId())
        };

        Cursor result = db.rawQuery(SQL_SELECT_BY_ID, args);
        if (result.getCount() == 0) return null;

        return createLink(result);
    }

    @Override
    public boolean delete(SQLiteDatabase db) {
        int rows = db.delete(TABlE_LINK, COLUMN_ID + "=" + getId(), null);
        return rows > 0;
    }

    @Override
    public Iterator<Link> retrieveIterator(SQLiteDatabase db) {
        Cursor result = db.rawQuery(SQL_SELECT_ALL, new String[] {TABlE_LINK});
        return new LinkIterator(result);
    }

    //~

    private static Link createLink(Cursor cursor) {
        Link link = new Link();
        link.setId(cursor.getLong(COLUMN_ID_INDEX));
        link.setWordId(Language.UKRAINIAN, cursor.getLong(COLUMN_LINK_ORIGINAL_INDEX));
        link.setWordId(Language.POLISH, cursor.getLong(COLUMN_LINK_TRANSLATION_INDEX));
        return link;
    }

    //~

    private static class LinkIterator implements Iterator<Link> {

        private Cursor cursor;

        private LinkIterator(Cursor cursor) {
            this.cursor = cursor;
        }

        @Override
        public boolean hasNext() {
            return !cursor.isLast();
        }

        @Override
        public Link next() {
            cursor.moveToNext();
            return createLink(cursor);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
