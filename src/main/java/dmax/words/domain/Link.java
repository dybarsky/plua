package dmax.words.domain;

import java.util.EnumMap;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 10.12.14 at 11:15
 */
public class Link implements Persistable {

    private int id = -1;
    private EnumMap<Language, Word> map = new EnumMap<Language, Word>(Language.class);

    public Word getWord(Language language) {
        return map.get(language);
    }

    public void setWord(Word word) {
        map.put(word.getLanguage(), word);
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link that = (Link) o;

        if (this.id != -1 && that.id != -1) return this.id == that.id;

        // todo implement

        return true;
    }

    @Override
    public int hashCode() {
        // todo implement
        return 0;
    }
}
