package dmax.words.persist;

import android.database.sqlite.SQLiteDatabase;

import java.util.Iterator;

import dmax.words.domain.Persistable;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 11.12.14 at 15:48
 */
public abstract class Dao<T extends Persistable> implements Constants {

    protected long id = -1;
    protected T persistable;
    protected Class<T> clas;

    protected Dao(Class<T> clas) {
        this.clas = clas;
    }

    public Dao<T> setPersistable(T persistable) {
        this.persistable = persistable;
        return this;
    }

    public Dao<T> setRetrieveId(long id) {
        this.id = id;
        return this;
    }

    public void reset() {
        id = -1;
        persistable = null;
    }

    protected long getId() {
        long id = -1;
        if (this.persistable != null) id = this.persistable.getId();
        if (this.id != -1) id = this.id;

        if (id == -1) throw new IllegalArgumentException("No id set");
        return id;
    }

    public abstract T save(SQLiteDatabase db);

    public abstract T retrieve(SQLiteDatabase db);

    public abstract boolean delete(SQLiteDatabase db);

    public abstract Iterator<T> retrieveIterator(SQLiteDatabase db);

    public abstract T update(SQLiteDatabase db);
}