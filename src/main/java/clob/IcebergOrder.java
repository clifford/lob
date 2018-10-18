package clob;

import java.util.ArrayList;
import java.util.List;

public class IcebergOrder implements IOrder {
    private final char buySellIndicator;
    private final int uid;
    private final short price;
    private final int qty;
    private final int peakSize;
    private int revealedPeakQty;
    private int remainingQty;
    private final List<Trade> trades = new ArrayList<>();

    public IcebergOrder(char buySellIndicator, int uid, short price, int qty, int peakSize) {

        this.buySellIndicator = buySellIndicator;
        this.uid = uid;
        this.price = price;
        this.qty = qty;
        this.peakSize = peakSize;
        this.remainingQty = qty;
        this.revealedPeakQty = peakSize;
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
        return peakSize == 0;
    }

    public boolean isIcebergOrder() {
        return peakSize > 0;
    }

    public boolean isFilled() {
        return remainingQty == 0;
    }

    public int getRemainingQty() {
        return remainingQty;
    }

    @Override
    public int getRevealedQty() {
        return Math.min(revealedPeakQty, remainingQty);
    }

    public void executedTrade(Trade trade) {
        trades.add(trade);
        if (trade.getTradedQty() > remainingQty) {
            System.out.println(String.format("traded quantity [%s] exceeds remaining quantity [%s] of order!", trade.getTradedQty(), remainingQty));
        }
        remainingQty -= trade.getTradedQty();
        revealedPeakQty -= trade.getTradedQty();

        if(trade.getTradedQty() >= peakSize) {
            int peekSizeOverhang = trade.getTradedQty() - peakSize;
            revealedPeakQty = Math.max(Math.min(peakSize, remainingQty) - peekSizeOverhang, 0);
        }
    }

    public int getPeakSize() {
        return peakSize;
    }

    public int getRevealedPeakQty() {
        return revealedPeakQty;
    }

    public List<Trade> getTrades() {
        return trades;
    }

    public void refreshPeak(List<Trade> trades) {
        int totalTraded = trades.stream().mapToInt(Trade::getTradedQty).sum();
        if(totalTraded >= peakSize) {
            revealedPeakQty = Math.min(peakSize, remainingQty);
        }
    }

    @Override
    public String toString() {
        return "LimitOrder{" +
                "buySellIndicator=" + buySellIndicator +
                ", uid=" + uid +
                ", price=" + price +
                ", qty=" + qty +
                ", peakSize=" + peakSize +
                ", remainingQty=" + remainingQty +
                ", trades=" + trades +
                '}';
    }
}
