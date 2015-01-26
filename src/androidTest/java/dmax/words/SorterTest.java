package dmax.words;

import android.test.AndroidTestCase;

import java.util.ArrayList;
import java.util.Collections;

import dmax.words.domain.Link;

/**
 * Created by Maxim Dybarsky | maxim.dybarskyy@gmail.com
 * on 23.01.15 at 18:00
 */
public class SorterTest extends AndroidTestCase {

    DataSource.PrioritySorter instance;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        instance = new DataSource.PrioritySorter();
    }

    public void test_shouldReturnPositive_IfPriorityRightMoreLeft() {
        Link left = new Link();
        Link right = new Link();

        left.setPriority(4);
        right.setPriority(6);

        int result = instance.compare(left, right);

        assertTrue(result > 0);
    }

    public void test_shouldReturnNegative_IfPriorityRightLessLeft() {
        Link left = new Link();
        Link right = new Link();

        left.setPriority(7);
        right.setPriority(6);

        int result = instance.compare(left, right);

        assertTrue(result < 0);
    }

    public void test_shouldReturnNegative_IfPrioritySameAndUpdatedRightMoreLeft() {
        Link left = new Link();
        Link right = new Link();

        left.setPriority(2);
        left.setUpdated(4);
        right.setPriority(2);
        right.setUpdated(5);

        int result = instance.compare(left, right);

        assertTrue(result < 0);
    }

    public void test_shouldReturnPositive_IfPrioritySameAndUpdatedRightLessLeft() {
        Link left = new Link();
        Link right = new Link();

        left.setPriority(2);
        left.setUpdated(4);
        right.setPriority(2);
        right.setUpdated(1);

        int result = instance.compare(left, right);

        assertTrue(result > 0);
    }

    public void test_shouldReturnZero_IfPrioritySameAndUpdatedSame() {
        Link left = new Link();
        Link right = new Link();

        left.setPriority(2);
        left.setUpdated(2);
        right.setPriority(2);
        right.setUpdated(2);

        int result = instance.compare(left, right);

        assertTrue(result == 0);
    }

    public void test_shouldSortCollectionPriorityDescendingAndUpdatedAscending() {
        Link[] links = new Link[5];
        for (int i = 0; i < 5; i++) {
            links[i] = new Link();
            links[i].setId(i);
        }

        links[0].setPriority(1);
        links[0].setUpdated(2);

        links[1].setPriority(2);
        links[1].setUpdated(2);

        links[2].setPriority(4);
        links[2].setUpdated(1);

        links[3].setPriority(3);
        links[3].setUpdated(2);

        links[4].setPriority(3);
        links[4].setUpdated(1);

        ArrayList<Link> list = new ArrayList<Link>();
        Collections.addAll(list, links);
        Collections.sort(list, instance);

        assertEquals(list.get(0), links[2]);
        assertEquals(list.get(1), links[4]);
        assertEquals(list.get(2), links[3]);
        assertEquals(list.get(3), links[1]);
        assertEquals(list.get(4), links[0]);
    }
}
