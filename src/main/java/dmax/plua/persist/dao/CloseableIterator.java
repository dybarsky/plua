package dmax.plua.persist.dao;

import java.util.Iterator;

/**
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 9.09.15 at 23:14
 */
public interface CloseableIterator<T> extends Iterator<T> {

    void close();
}
