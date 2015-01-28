package dmax.words.domain;

import java.io.Serializable;
import java.util.EnumMap;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 10.12.14 at 11:15
 */
public class Link implements Persistable, Serializable {

    private long id = -1;
    private int priority;
    private long updated;
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

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link that = (Link) o;

        if (this.id != -1 && that.id != -1 && this.id != that.id) return false;

        if (priority != that.priority) return false;
        if (updated != that.updated) return false;
        if (!map.equals(that.map)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + priority;
        result = 31 * result + (int) (updated ^ (updated >>> 32));
        result = 31 * result + map.hashCode();
        return result;
    }
}
