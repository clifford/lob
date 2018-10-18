package clob;

import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class IcebergOrderTestCase {
    @Test
    public void testPartialFll() {
        IcebergOrder o1 = new IcebergOrder('S', 1, (short) 100, 1000, 100);
        assertEquals(1000, o1.getRemainingQty());
        assertEquals(100, o1.getRevealedPeakQty());

        o1.executedTrade(new Trade(2, 1, (short)100, 50));
        assertEquals(950, o1.getRemainingQty());
        assertEquals(50, o1.getRevealedPeakQty());
    }

    @Test
    public void testFllled() {
        IcebergOrder o1 = new IcebergOrder('S', 1, (short) 100, 1000, 100);
        assertEquals(1000, o1.getRemainingQty());
        assertEquals(100, o1.getRevealedPeakQty());

        o1.executedTrade(new Trade(2, 1, (short)100, 100));
        assertEquals(900, o1.getRemainingQty());
        assertEquals(100, o1.getRevealedPeakQty());
    }

    @Test
    public void testExceedsRevealedPeak() {
        IcebergOrder o1 = new IcebergOrder('S', 1, (short) 100, 1000, 100);
        assertEquals(1000, o1.getRemainingQty());
        assertEquals(100, o1.getRevealedPeakQty());

        o1.executedTrade(new Trade(2, 1, (short)100, 150));
        assertEquals(850, o1.getRemainingQty());
        assertEquals(50, o1.getRevealedPeakQty());
    }

    @Test
    public void testAllHiddenVolume() {
        IcebergOrder o1 = new IcebergOrder('S', 1, (short) 100, 1000, 100);
        assertEquals(1000, o1.getRemainingQty());
        assertEquals(100, o1.getRevealedPeakQty());

        o1.executedTrade(new Trade(2, 1, (short)100, 1000));
        assertEquals(0, o1.getRemainingQty());
        assertEquals(0, o1.getRevealedPeakQty());
        assertTrue(o1.isFilled());
    }
}
