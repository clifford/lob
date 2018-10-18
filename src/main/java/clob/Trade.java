package clob;

import java.util.Objects;

public class Trade {

    private final int buyOrderId;
    private final int sellOrderId;
    private final short price;
    private final int tradedQty;

    public Trade(int buyOrderId, int sellOrderId, short price, int tradedQty) {

        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.tradedQty = tradedQty;
    }

    public int getBuyOrderId() {
        return buyOrderId;
    }

    public int getSellOrderId() {
        return sellOrderId;
    }

    public short getPrice() {
        return price;
    }

    public int getTradedQty() {
        return tradedQty;
    }

    @Override
    public String toString() {
        return String.format("%s,%s,%s,%s", buyOrderId, sellOrderId, price, tradedQty);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Trade trade = (Trade) o;
        return buyOrderId == trade.buyOrderId &&
                sellOrderId == trade.sellOrderId &&
                price == trade.price &&
                tradedQty == trade.tradedQty;
    }

    @Override
    public int hashCode() {

        return Objects.hash(buyOrderId, sellOrderId, price, tradedQty);
    }
}
