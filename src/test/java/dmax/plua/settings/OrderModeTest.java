package dmax.plua.settings;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OrderModeTest {

    @Test
    public void test_shouldParsePersistedNames() {
        assertEquals(OrderMode.RANDOM, OrderMode.fromName("RANDOM"));
        assertEquals(OrderMode.SPACED, OrderMode.fromName("SPACED"));
    }

    @Test
    public void test_shouldReturnDefault_IfNameMissingOrUnknown() {
        assertEquals(OrderMode.DEFAULT, OrderMode.fromName(null));
        assertEquals(OrderMode.DEFAULT, OrderMode.fromName(""));
        assertEquals(OrderMode.DEFAULT, OrderMode.fromName("spaced"));
        assertEquals(OrderMode.DEFAULT, OrderMode.fromName("LEITNER"));
    }

    @Test
    public void test_defaultShouldBeRandom() {
        assertEquals(OrderMode.RANDOM, OrderMode.DEFAULT);
    }
}
