package dmax.words.persist.dao;

import dmax.words.domain.Link;
import dmax.words.domain.Persistable;
import dmax.words.domain.Word;
import dmax.words.persist.Dao;

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
