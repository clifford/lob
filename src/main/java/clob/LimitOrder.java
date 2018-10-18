package clob;

import java.util.ArrayList;
import java.util.List;

public class LimitOrder implements IOrder {
    private final char buySellIndicator;
    private final int uid;
    private final short price;
    private final int qty;
    private int hiddenQty;
    private int remainingQty;
    private final List<Trade> trades = new ArrayList<>();

    public LimitOrder(char buySellIndicator, int uid, short price, int qty) {

        this.buySellIndicator = buySellIndicator;
        this.uid = uid;
        this.price = price;
        this.qty = qty;
        this.remainingQty = qty;
    }

    public char getBuySellIndicator() {
        return buySellIndicator;
    }

    public int getUid() {
        return uid;
    }

    public short getPrice() {
        return price;
    }

    public int getQty() {
        return qty;
    }

    public boolean isBuy() {
        return buySellIndicator == 'B';
    }

    public boolean isSell() {
        return buySellIndicator == 'S';
    }

    public boolean isLimitOrder() {
        return true;
    }

    public boolean isFilled() {
        return remainingQty == 0;
    }

    public int getRemainingQty() {
        return remainingQty;
    }

    @Override
    public int getRevealedQty() {
        return getRemainingQty();
    }

    public void executedTrade(Trade trade) {
        this.trades.add(trade);
        if (trade.getTradedQty() > remainingQty) {
            System.out.println(String.format("traded quantity [%s] exceeds remaining quantity [%s] of order!", trade.getTradedQty(), remainingQty));
        }
        this.remainingQty -= trade.getTradedQty();
    }

    @Override
    public String toString() {
        return "LimitOrder{" +
                "buySellIndicator=" + buySellIndicator +
                ", uid=" + uid +
                ", price=" + price +
                ", qty=" + qty +
                ", remainingQty=" + remainingQty +
                ", trades=" + trades +
                '}';
    }
}
