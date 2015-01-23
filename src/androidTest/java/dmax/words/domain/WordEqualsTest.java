package dmax.words.domain;

import android.test.AndroidTestCase;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 15.12.14 at 15:55
 */
public class WordEqualsTest extends AndroidTestCase {

    public void test_shouldBeNotEqual_IfIdsDifferent() {
        Word w1 = new Word();
        w1.setId(1);
        Word w2 = new Word();
        w2.setId(2);

        assertFalse(w1.equals(w2));
    }

    public void test_shouldBeNotEqual_IfIdsSameAndDataDifferent() {
        Word w1 = new Word();
        w1.setId(1);
        w1.setData("укр");
        w1.setLanguage(Language.UKRAINIAN);
        Word w2 = new Word();
        w2.setId(1);
        w2.setLanguage(Language.POLISH);

        assertFalse(w1.equals(w2));
    }

    public void test_shouldBeNotEqual_IfIdsNotSetAndDataDifferent() {
        Word w1 = new Word();
        w1.setData("укр");
        w1.setLanguage(Language.UKRAINIAN);
        Word w2 = new Word();
        w2.setLanguage(Language.POLISH);

        assertFalse(w1.equals(w2));
    }

    public void test_shouldBeNotEqual_IfOneIdNotSetAndDataDifferent() {
        Word w1 = new Word();
        w1.setId(1);
        w1.setData("укр");
        w1.setLanguage(Language.UKRAINIAN);
        Word w2 = new Word();
        w2.setLanguage(Language.POLISH);

        assertFalse(w1.equals(w2));
    }

    public void test_shouldBeEqual_IfOneIdNotSetAndDataSame() {
        Word w1 = new Word();
        w1.setId(1);
        w1.setData("укр");
        w1.setLanguage(Language.UKRAINIAN);
        Word w2 = new Word();
        w2.setLanguage(Language.UKRAINIAN);
        w2.setData("укр");

        assertTrue(w1.equals(w2));
    }

    public void test_shouldBeEqual_IfIdsNotSetAndDataSame() {
        Word w1 = new Word();
        w1.setData("укр");
        w1.setLanguage(Language.UKRAINIAN);
        Word w2 = new Word();
        w2.setLanguage(Language.UKRAINIAN);
        w2.setData("укр");

        assertTrue(w1.equals(w2));
    }

    public void test_shouldBeEqual_IfIdsSameAndDataSame() {
        Word w1 = new Word();
        w1.setId(1);
        w1.setData("укр");
        w1.setLanguage(Language.UKRAINIAN);
        Word w2 = new Word();
        w2.setId(1);
        w2.setLanguage(Language.UKRAINIAN);
        w2.setData("укр");

        assertTrue(w1.equals(w2));
    }
}
