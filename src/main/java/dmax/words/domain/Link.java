package dmax.words.domain;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 10.12.14 at 11:15
 */
public class Link {

    private Word original;
    private Word translation;

    public Link(Word original, Word translation) {
        this.original = original;
        this.translation = translation;
    }

    public Word getOriginal() {
        return original;
    }

    public Word getTranslation() {
        return translation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Link link = (Link) o;

        if (!original.equals(link.original)) return false;
        if (!translation.equals(link.translation)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = original.hashCode();
        result = 31 * result + translation.hashCode();
        return result;
    }
}
