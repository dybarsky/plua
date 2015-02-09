package dmax.plua.persist.dao;

import dmax.plua.domain.Link;
import dmax.plua.domain.Persistable;
import dmax.plua.domain.Word;
import dmax.plua.persist.Dao;

/**
 * Class for create corresponding dao instance based on entity class.
 *
 * <br/><br/>
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 15.12.14 at 13:16
 */
public class DaoFactory {

    /**
     * Creates dao instance based on corresponding entity class.
     */
    public static <T extends Persistable, D extends Dao<T>> D createDao(Class<T> clas) {
        if (clas == Word.class) return (D) new WordDao();
        if (clas == Link.class) return (D) new LinkDao();
        throw new IllegalArgumentException("Class " + clas + " not supported");
    }
}
