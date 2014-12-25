package dmax.words.domain;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 10.12.14 at 13:46
 */
public enum Language {
    POLISH("Polish"),
    UKRAINIAN("Ukrainian");

    private String codeName;

    Language(String codeName) {
        this.codeName = codeName;
    }

    public String getCodeName() {
        return codeName;
    }
}
