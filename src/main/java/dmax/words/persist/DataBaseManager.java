package dmax.words.persist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.Iterator;

import dmax.words.domain.Persistable;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 12.12.14 at 17:46
 */
public class DataBaseManager {

    private SQLiteDatabase database;
    private DataBaseHelper helper;

    public DataBaseManager(Context context) {
        helper = new DataBaseHelper(context);
    }

    public void open() {
        database = helper.getWritableDatabase();
    }

    public void close() {
        helper.close();
    }


    public <T extends Persistable> T insert(Dao<T> dao) {
        if (database == null || !database.isOpen()) throw new IllegalStateException("DBManager not initialized");
        T t = dao.insert(database);
        dao.reset();
        return t;
    }

    public <T extends Persistable> boolean delete(Dao<T> dao) {
        if (database == null || !database.isOpen()) throw new IllegalStateException("DBManager not initialized");
        boolean res = dao.delete(database);
        dao.reset();
        return res;
    }

    public <T extends Persistable> T retrieve(Dao<T> dao) {
        if (database == null || !database.isOpen()) throw new IllegalStateException("DBManager not initialized");
        T t = dao.retrieve(database);
        dao.reset();
        return t;
    }

    public <T extends Persistable> Iterator<T> retrieveIterator(Dao<T> dao) {
        if (database == null || !database.isOpen()) throw new IllegalStateException("DBManager not initialized");
        Iterator<T> it = dao.retrieveIterator(database);
        dao.reset();
        return it;
    }

    public <T extends Persistable> T update(Dao<T> dao) {
        if (database == null || !database.isOpen()) throw new IllegalStateException("DBManager not initialized");
        T t = dao.update(database);
        dao.reset();
        return t;
    }
}
