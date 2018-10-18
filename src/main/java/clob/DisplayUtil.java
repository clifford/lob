package clob;

import java.util.Arrays;
import java.util.List;

public class DisplayUtil {

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
    public static void displayBook(LimitOrderBook book) {
        StringBuilder stringBuilder = new StringBuilder();
        displayBook(stringBuilder, book);
        System.out.println(stringBuilder.toString());
    }
    public static void displayBook(StringBuilder stringBuilder, LimitOrderBook book) {
        printHeaderOn(stringBuilder);
        printBookrOn(stringBuilder, book);
        stringBuilder.append(dashedLine(67));
    }

    public static void printBookrOn(StringBuilder stringBuilder, LimitOrderBook book) {
        List<IOrder> bids = book.getBidsByPriceTime();
        List<IOrder> offers = book.getOffersByPriceTime();
        int depth = Math.max(bids.size(), offers.size());

        for (int i = 0; i < depth; i++) {
            IOrder bid = i < bids.size() ? bids.get(i) : null;
            IOrder offer = i < offers.size() ? offers.get(i) : null;

            printOrderRowOn(stringBuilder, bid, offer);
        }
    }

    /*
    Id columns width (excluding formatting marks) = 10
    Volume columns width (excluding formatting marks) = 13
    Price columns width (excluding formatting marks) = 7
    Total width including 7 formatting marks = 67
     */
    public static void printHeaderOn(StringBuilder stringBuilder) {
        stringBuilder.append(dashedLine(67)).append("\n");
        printRowOn(stringBuilder,
                new ColumnSpec("BUY", 33),
                new ColumnSpec("SELL", 33));
        printRowOn(stringBuilder,
                new ColumnSpec("Id", 11),
                new ColumnSpec("Volume", 14),
                new ColumnSpec("Price", 8),
                new ColumnSpec("Price", 8),
                new ColumnSpec("Volume", 14),
                new ColumnSpec("Id", 11));

        stringBuilder.append(columnDashedLine(67)).append("\n");
    }

    public static void printRowOn(StringBuilder stringBuilder, ColumnSpec... rowSpec) {
        stringBuilder.append('|');
        for (ColumnSpec columnSpec : rowSpec) {
            printColumnOn(stringBuilder, columnSpec.columnName, columnSpec.columnWidth);
        }
        stringBuilder.append("\n");
    }

    /*
      print a blank, followed by column name, followed by blanks which pad the width and end with a '|'
      " BUY       |"
     */
    public static void printColumnOn(StringBuilder stringBuilder, String columnName, int width) {
        int remainder = width - columnName.length() - 2;
        char[] blanks = new char[remainder];
        Arrays.fill(blanks, ' ');
        stringBuilder.append(" ").append(columnName).append(blanks).append('|');
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
    public static void printOrderRowOn(StringBuilder stringBuilder, IOrder bid, IOrder offer) {
        stringBuilder.append('|');
        printOrderOn(stringBuilder, bid, true);
        printOrderOn(stringBuilder, offer, false);
    }

    public static void printOrderOn(StringBuilder stringBuilder, IOrder order, boolean bid) {
        if (order != null) {
            if (bid) {
                stringBuilder.append(String.format("%10d|%,13d|%,7d|", order.getUid(), order.getRevealedQty(), order.getPrice()));
            } else {
                stringBuilder.append(String.format("%,7d|%,13d|%10d|\n", order.getPrice(), order.getRevealedQty(), order.getUid()));
            }
        } else {
            if (bid) {
                stringBuilder.append(String.format("%10s|%13s|%7s|", "", "", ""));
            } else {
                stringBuilder.append(String.format("%7s|%13s|%10s|\n", "", "", ""));
            }
        }
    }

    public static char[] dashedLine(int length) {
        char[] dashedLine = new char[length];
        Arrays.fill(dashedLine, '-');
        insertCharAt(dashedLine, '+', 0, length - 1);
        return dashedLine;
    }

    public static char[] columnDashedLine(int length) {
        char[] dashedLine = new char[length];
        Arrays.fill(dashedLine, '-');
        insertCharAt(dashedLine, '+', 0, 11, 25, 33, 41, 55, length - 1);
        return dashedLine;
    }

    public static void insertCharAt(char[] chars, char c, int... indices) {
        for (int index : indices) {
            if (index < chars.length) {
                chars[index] = c;
            }
        }
    }

    public static void displayTrades(List<Trade> trades) {
        for (Trade trade : trades) {
            System.out.println(trade.toString());
        }
    }

    public static class ColumnSpec {
        public final String columnName;
        public final int columnWidth;

        public ColumnSpec(String columnName, int columnWidth) {
            this.columnName = columnName;
            this.columnWidth = columnWidth;
        }
    }

}
