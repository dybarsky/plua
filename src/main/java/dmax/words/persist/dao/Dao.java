package dmax.words.persist.dao;

import android.database.sqlite.SQLiteDatabase;

import java.util.Iterator;

import dmax.words.domain.Persistable;

public abstract class Dao<T extends Persistable> {

    private T persistable;
    private Class<T> clas;

    protected Dao(T persistable, Class<T> clas) {
        this.persistable = persistable;
        this.clas = clas;
    }

    protected Dao(Class<T> clas) {
        this.clas = clas;
    }

    public T save(SQLiteDatabase db) {
        return null;
    }

    public T retrieve(SQLiteDatabase db) {
        return null;
    }

    public void delete(SQLiteDatabase db) {
    }

    public Iterator<T> retrieveIterator(SQLiteDatabase db){
        return null;
    }
}