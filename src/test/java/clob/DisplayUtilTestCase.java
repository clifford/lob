package clob;

import org.junit.Test;

import static clob.DisplayUtil.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DisplayUtilTestCase {
    @Test
    public void testRow() {
        StringBuilder sb = new StringBuilder();
        printRowOn(sb,
                new DisplayUtil.ColumnSpec("BUY", 33),
                new DisplayUtil.ColumnSpec("SELL", 33));
        assertEquals(67, sb.toString().replace("\n", "").length());
        assertEquals("| BUY                            | SELL                           |", sb.toString().replace("\n", ""));
    }

    @Test
    public void testColumnNameRow() {
        StringBuilder sb = new StringBuilder();
        printRowOn(sb,
                new DisplayUtil.ColumnSpec("Id", 11),
                new DisplayUtil.ColumnSpec("Volume", 14),
                new DisplayUtil.ColumnSpec("Price", 8),
                new DisplayUtil.ColumnSpec("Price", 8),
                new DisplayUtil.ColumnSpec("Volume", 14),
                new DisplayUtil.ColumnSpec("Id", 11));
        assertEquals(67, sb.toString().replace("\n", "").length());
        assertEquals("| Id       | Volume      | Price | Price | Volume      | Id       |", sb.toString().replace("\n", ""));
    }

    @Test
    public void testDashedLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(dashedLine(67));
;
        assertEquals(67, sb.toString().length());
        assertEquals("+-----------------------------------------------------------------+", sb.toString());
    }

    @Test
    public void testColumnDashedLine() {
        StringBuilder sb = new StringBuilder();
        sb.append(columnDashedLine(67));
;
        assertEquals(67, sb.toString().length());
        assertEquals("+----------+-------------+-------+-------+-------------+----------+", sb.toString());
    }

    @Test
    public void testPrintHeader() {
        String expected = "+-----------------------------------------------------------------+\n" +
                "| BUY                            | SELL                           |\n" +
                "| Id       | Volume      | Price | Price | Volume      | Id       |\n" +
                "+----------+-------------+-------+-------+-------------+----------+\n";
        StringBuilder sb = new StringBuilder();
        printHeaderOn(sb);
        assertEquals(expected, sb.toString());
    }

    @Test
    public void testPrintOrderOn() {
        StringBuilder sb = new StringBuilder();
        printOrderOn(sb, new LimitOrder('B', 1138, (short) 31502, 7500), true);

        assertEquals(33, sb.toString().length());
        assertEquals("      1138|        7,500| 31,502|", sb.toString());
    }

    @Test
    public void testPrintNoOrderOn() {
        StringBuilder sb = new StringBuilder();
        printOrderOn(sb, null, true);

        assertEquals(33, sb.toString().length());
        assertEquals("          |             |       |", sb.toString());
    }

    @Test
    public void testPrintOrderRowOn() {
        StringBuilder sb = new StringBuilder();
        printOrderRowOn(sb,
                new LimitOrder('B', 1138, (short) 31502, 7500),
                new LimitOrder('S', 6808, (short) 32505, 7777));

        String result = sb.toString().replace("\n", "");
        assertEquals(67, result.length());
        assertEquals("|      1138|        7,500| 31,502| 32,505|        7,777|      6808|", result);
    }

    /*
        +-----------------------------------------------------------------+
        | BUY                            | SELL                           |
        | Id       | Volume      | Price | Price | Volume      | Id       |
        +----------+-------------+-------+-------+-------------+----------+
        |1234567890|1,234,567,890| 32,503| 32,504|1,234,567,890|1234567891|
        |      1138|        7,500| 31,502| 32,505|        7,777|      6808|
        |          |             |       | 32,507|        3,000|     42100|
        +-----------------------------------------------------------------+
*/
    @Test
    public void testPrintBookOn() {
        LimitOrderBook lob = new LimitOrderBook();
        lob.newOrder(new LimitOrder('B', 1234567890, (short) 32_503, 1_234_567_890));
        lob.newOrder(new LimitOrder('B', 1138, (short) 31_502, 7_500));
        lob.newOrder(new LimitOrder('S', 1234567891, (short) 32_504, 1_234_567_890));
        lob.newOrder(new LimitOrder('S', 6808, (short) 32_505, 7_777));
        lob.newOrder(new LimitOrder('S', 42100, (short) 32_507, 3_000));
        StringBuilder sb = new StringBuilder();
        displayBook(sb, lob);
        assertEquals("+-----------------------------------------------------------------+\n" +
                "| BUY                            | SELL                           |\n" +
                "| Id       | Volume      | Price | Price | Volume      | Id       |\n" +
                "+----------+-------------+-------+-------+-------------+----------+\n" +
                "|1234567890|1,234,567,890| 32,503| 32,504|1,234,567,890|1234567891|\n" +
                "|      1138|        7,500| 31,502| 32,505|        7,777|      6808|\n" +
                "|          |             |       | 32,507|        3,000|     42100|\n" +
                "+-----------------------------------------------------------------+", sb.toString());
    }
}
