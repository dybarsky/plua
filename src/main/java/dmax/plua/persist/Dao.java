package dmax.plua.persist;

import android.database.sqlite.SQLiteDatabase;

import java.io.Closeable;
import java.util.Iterator;

import dmax.plua.domain.Persistable;
import dmax.plua.persist.dao.CloseableIterator;

/**
 * Base class for encapsulate database operation with persistable entities.
 *
 * <br/><br/>
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

    /**
     * Set entity to do operation with. Must contain valid id for operations <b>delete</b> and <b>update</b>.
     */
    public Dao<T> setPersistable(T persistable) {
        this.persistable = persistable;
        return this;
    }

    /**
     * Set id for <b>retrieve</b> operation.
     */
    public Dao<T> setRetrieveId(long id) {
        this.id = id;
        return this;
    }

    /**
     * Reset persistable data set by methods {@link #setPersistable(dmax.plua.domain.Persistable)}
     * and {@link #setRetrieveId(long)}
     */
    public void reset() {
        id = -1;
        persistable = null;
    }

    /**
     * Get it set by by methods {@link #setPersistable(dmax.plua.domain.Persistable)}
     * or {@link #setRetrieveId(long)}
     * @throws IllegalArgumentException if no correct id set.
     */
    protected long getId() {
        long id = -1;
        if (this.persistable != null) id = this.persistable.getId();
        if (this.id != -1) id = this.id;

        if (id == -1) throw new IllegalArgumentException("No id set");
        return id;
    }

    /**
     * Inserts into database new record based on data set by method {@link #setPersistable(dmax.plua.domain.Persistable)}
     * @return inserted entity with correct id.
     */
    public abstract T insert(SQLiteDatabase db);

    /**
     * Load from database entity using id set by method {@link #setPersistable(dmax.plua.domain.Persistable)}
     * or {@link #setRetrieveId(long)}
     * @return loaded entity with correct id or null.
     */
    public abstract T retrieve(SQLiteDatabase db);

    /**
     * Removed from database record with id set by method {@link #setPersistable(dmax.plua.domain.Persistable)}
     * or {@link #setRetrieveId(long)}
     * @return true if removed, false otherwise
     */
    public abstract boolean delete(SQLiteDatabase db);

    /**
     * Load all data from database table. Returns iterator based on {@link android.database.Cursor}.
     */
    public abstract CloseableIterator<T> retrieveIterator(SQLiteDatabase db);

    /**
     * Updates in database record based on data set by method {@link #setPersistable(dmax.plua.domain.Persistable)}
     * @return updated entity with correct id if updated or null if nothing updated.
     */
    public abstract T update(SQLiteDatabase db);
}