package clob;

public interface IOrder {
    void executedTrade(Trade trade);

    boolean isFilled();

    boolean isBuy();

    boolean isSell();

    default boolean isIcebergOrder() {
        return false;
    }

    default boolean isLimitOrder() {
        return false;
    }

    int getUid();

    short getPrice();

    int getRemainingQty();

    int getRevealedQty();

    static IOrder parse(String[] fields) {
        char buySellIndicator = fields[0].charAt(0);
        int uid = Integer.valueOf(fields[1]);
        short price = Short.valueOf(fields[2]);
        int qty = Integer.valueOf(fields[3]);
        int peak;

        return fields.length == 5 ? new IcebergOrder(buySellIndicator, uid, price, qty, Integer.valueOf(fields[4])) :
                new LimitOrder(buySellIndicator, uid, price, qty);
    }
}
