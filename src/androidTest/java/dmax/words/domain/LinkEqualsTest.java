package dmax.words.domain;

import android.test.AndroidTestCase;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 15.12.14 at 15:55
 */
public class LinkEqualsTest extends AndroidTestCase {

    public void test_shouldBeNotEqualIfIdsDifferent() {
        Link l1 = new Link();
        l1.setId(1);
        Link l2 = new Link();
        l2.setId(2);

        assertFalse(l1.equals(l2));
    }

    public void test_shouldBeNotEqualIfIdsSameAndDataDifferent() {
        Link l1 = new Link();
        l1.setId(1);
        l1.setWordId(Language.POLISH, 1);
        l1.setWordId(Language.UKRAINIAN, 2);
        Link l2 = new Link();
        l2.setId(1);
        l2.setWordId(Language.POLISH, 3);
        l2.setWordId(Language.UKRAINIAN, 4);

        assertFalse(l1.equals(l2));
    }

    public void test_shouldBeNotEqualIfIdsNotSetAndDataDifferent() {
        Link l1 = new Link();
        l1.setWordId(Language.POLISH, 1);
        l1.setWordId(Language.UKRAINIAN, 2);
        Link l2 = new Link();
        l2.setWordId(Language.POLISH, 3);
        l2.setWordId(Language.UKRAINIAN, 4);

        assertFalse(l1.equals(l2));
    }

    public void test_shouldBeNotEqualIfOneIdNotSetAndDataDifferent() {
        Link l1 = new Link();
        l1.setId(1);
        l1.setWordId(Language.POLISH, 1);
        l1.setWordId(Language.UKRAINIAN, 2);
        Link l2 = new Link();
        l2.setWordId(Language.POLISH, 3);
        l2.setWordId(Language.UKRAINIAN, 4);

        assertFalse(l1.equals(l2));
    }

    public void test_shouldBeEqualIfOneIdNotSetAndDataSame() {
        Link l1 = new Link();
        l1.setId(9);
        l1.setWordId(Language.POLISH, 1);
        l1.setWordId(Language.UKRAINIAN, 2);
        Link l2 = new Link();
        l2.setWordId(Language.POLISH, 1);
        l2.setWordId(Language.UKRAINIAN, 2);

        assertTrue(l1.equals(l2));
    }

    public void test_shouldBeEqualIfIdsNotSetAndDataSame() {
        Link l1 = new Link();
        l1.setWordId(Language.POLISH, 1);
        l1.setWordId(Language.UKRAINIAN, 2);
        Link l2 = new Link();
        l2.setWordId(Language.POLISH, 1);
        l2.setWordId(Language.UKRAINIAN, 2);

        assertTrue(l1.equals(l2));
    }

    public void test_shouldBeEqualIfIdsSameAndDataSame() {
        Link l1 = new Link();
        l1.setId(1);
        l1.setWordId(Language.POLISH, 1);
        l1.setWordId(Language.UKRAINIAN, 2);
        Link l2 = new Link();
        l2.setId(1);
        l2.setWordId(Language.POLISH, 1);
        l2.setWordId(Language.UKRAINIAN, 2);

        assertTrue(l1.equals(l2));
    }
}
