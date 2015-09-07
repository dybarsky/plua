package dmax.plua.persist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.Closeable;
import java.util.Iterator;

import dmax.plua.domain.Persistable;
import dmax.plua.persist.dao.CloseableIterator;

/**
 * Database operations entry point. Manages database lifecycle. Uses dao classes to do database job.
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 12.12.14 at 17:46
 */
public class DataBaseManager {

    private SQLiteDatabase database;
    private DataBaseHelper helper;

    public DataBaseManager(Context context) {
        helper = new DataBaseHelper(context);
    }

    /**
     * Obtain database access
     */
    public void open() {
        database = helper.getWritableDatabase();
    }

    /**
     * Close database access
     */
    public void close() {
        helper.close();
    }

    /**
     * Inserts into database new record with operation based on dao instance.
     * @return inserted entity with correct id.
     */
    public <T extends Persistable> T insert(Dao<T> dao) {
        if (database == null || !database.isOpen()) throw new IllegalStateException("DBManager not initialized");
        T t = dao.insert(database);
        dao.reset();
        return t;
    }

    /**
     * Removes from database record with operation based on dao instance.
     * @return true if removed, false otherwise
     */
    public <T extends Persistable> boolean delete(Dao<T> dao) {
        if (database == null || !database.isOpen()) throw new IllegalStateException("DBManager not initialized");
        boolean res = dao.delete(database);
        dao.reset();
        return res;
    }

    /**
     * Loads from database entity with operation based on dao instance.
     * @return loaded entity with correct id.
     */
    public <T extends Persistable> T retrieve(Dao<T> dao) {
        if (database == null || !database.isOpen()) throw new IllegalStateException("DBManager not initialized");
        T t = dao.retrieve(database);
        dao.reset();
        return t;
    }

    /**
     * Load all data from database table with operation based on dao instance.
     * @return iterator based on {@link android.database.Cursor}.
     */
    public <T extends Persistable> CloseableIterator<T> retrieveIterator(Dao<T> dao) {
        if (database == null || !database.isOpen()) throw new IllegalStateException("DBManager not initialized");
        CloseableIterator<T> it = dao.retrieveIterator(database);
        dao.reset();
        return it;
    }

    /**
     * Updates in database record with operation based on dao instance.
     * @return updated entity with correct id if updated or null if nothing updated.
     */
    public <T extends Persistable> T update(Dao<T> dao) {
        if (database == null || !database.isOpen()) throw new IllegalStateException("DBManager not initialized");
        T t = dao.update(database);
        dao.reset();
        return t;
    }
}
