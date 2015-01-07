package dmax.words.domain;

import java.io.Serializable;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 10.12.14 at 11:15
 */
public class Word implements Persistable, Serializable {

    private long id = -1;
    private String data;
    private Language language;

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Word that = (Word) o;

        if (this.id != -1 && that.id != -1 && this.id != that.id) return false;
        if (language != that.language) return false;
        if (!data.equals(that.data)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + data.hashCode();
        result = 31 * result + language.hashCode();
        return result;
    }
}