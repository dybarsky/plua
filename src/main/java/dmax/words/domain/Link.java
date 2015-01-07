package dmax.words.domain;

import java.io.Serializable;
import java.util.EnumMap;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 10.12.14 at 11:15
 */
public class Link implements Persistable, Serializable {

    private long id = -1;
    private EnumMap<Language, Long> map = new EnumMap<Language, Long>(Language.class);

    public long getWordId(Language language) {
        return map.get(language);
    }

    public void setWordId(Language language, long id) {
        map.put(language, id);
    }

    public void setWord(Word word) {
        setWordId(word.getLanguage(), word.getId());
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link that = (Link) o;

        if (this.id != -1 && that.id != -1 && this.id != that.id) return false;

        return map.equals(that.map);
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + map.hashCode();
        return result;
    }
}
