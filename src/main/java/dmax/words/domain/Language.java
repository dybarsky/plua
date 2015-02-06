package dmax.words.domain;

import java.io.Serializable;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 10.12.14 at 13:46
 */
public enum Language implements Serializable {
    POLISH,
    UKRAINIAN;

    private String codeName;

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }
}
