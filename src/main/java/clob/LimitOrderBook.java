package clob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static clob.DisplayUtil.displayBook;
import static clob.DisplayUtil.displayTrades;

public class LimitOrderBook implements ILimitOrderBook {
    private final List<IOrder> offerByPriceTime;
    private final List<IOrder> bidsByPriceTime;

    public LimitOrderBook() {
        bidsByPriceTime = new ArrayList<>();
        offerByPriceTime = new ArrayList<>();
    }

    @Override
    public List<Trade> newOrder(IOrder newOrder) {
        return processOrder(newOrder);
    }

    private List<Trade> processOrder(IOrder newOrder) {
        List<Trade> trades;
        if (newOrder.isBuy()) {
            trades = matchOrders(offerByPriceTime, newOrder, order -> newOrder.getPrice() >= order.getPrice());
        } else {
            trades = matchOrders(bidsByPriceTime, newOrder, order -> newOrder.getPrice() <= order.getPrice());
        }

        displayTrades(trades);
        displayBook(this);
        return trades;
    }

    private void updateOrderBook(List<IOrder> book, List<IOrder> matched, IOrder newOrder) {
        if (newOrder.getRemainingQty() > 0) {
            addToBook(newOrder);
        }

        for (int i = 0; i < matched.size(); i++) {
            IOrder order = matched.get(i);
            if (order.isFilled()) {
                book.remove(0);
            }
        }
    }

    protected List<Trade> matchOrders(List<IOrder> book, IOrder newOrder, Predicate<IOrder> predicate) {
        List<IOrder> matched = new ArrayList<>();
        List<Trade> trades = new ArrayList<>();
        List<IOrder> subBook = new ArrayList<>();
        IOrder prevOrder = null;

        for (IOrder order : book) {
            if (predicate.test(order) && newOrder.getRemainingQty() > 0) {
                // order is: 1. matched 2. iceberg 3. has remaining qty
                if (order.isIcebergOrder() && order.getRemainingQty() > 0) {
                    subBook.add(order);
                }
                if (!subBook.isEmpty() && priceWorse(order, prevOrder)) {
                    trades.addAll(matchOrders(book, newOrder, predicate));
                    continue;
                }

                matched.add(order);
                trades.add(executeTrade(newOrder, order));

                prevOrder = order;
            }
        }
        if (newOrder.isIcebergOrder()) {
            ((IcebergOrder) newOrder).refreshPeak(trades);
        }
        updateOrderBook(book, matched, newOrder);
        return trades;
    }

    private boolean priceWorse(IOrder order, IOrder prevOrder) {
        if(prevOrder == null) return false;

        if(order.isBuy()) {
            return order.getPrice() < prevOrder.getPrice();
        } else {
            return order.getPrice() > prevOrder.getPrice();
        }
    }

    private void addToBook(IOrder order) {
        if (order.isBuy()) {
            addToBids(order);
        } else {
            addToOffers(order);
        }
    }

    private int addToBids(IOrder order) {
        int index = insertBidPriceIndex(bidsByPriceTime, order.getPrice());
        bidsByPriceTime.add(index, order);
        return index;
    }

    private int addToOffers(IOrder order) {
        int index = insertOfferPriceIndex(offerByPriceTime, order.getPrice());
        offerByPriceTime.add(index, order);
        return index;
    }

    private Trade executeTrade(IOrder order1, IOrder order2) {
        if (order1.isBuy() && order2.isSell()) {
            return doExecuteTrade(order1, order2);
        } else if (order2.isBuy() && order1.isSell()) {
            return doExecuteTrade(order2, order1);
        }

        return null;
    }

    private Trade doExecuteTrade(IOrder buyOrder, IOrder sellOrder) {
//        int tradedQty = Math.min(buyOrder.getRemainingQty(), sellOrder.getRemainingQty());
        int tradedQty = Math.min(buyOrder.getRevealedQty(), sellOrder.getRevealedQty());
        Trade trade = new Trade(buyOrder.getUid(), sellOrder.getUid(), buyOrder.getPrice(), tradedQty);
        buyOrder.executedTrade(trade);
        sellOrder.executedTrade(trade);
        return trade;
    }

    public List<IOrder> getBidsByPriceTime() {
        return bidsByPriceTime;
    }

    public List<IOrder> getOffersByPriceTime() {
        return offerByPriceTime;
    }

    /**
     * Find the index at which to place the new offer. Offers sorted from lowest to highest.
     * 3 4 4   6      offers
     * 4        new entry
     *
     * @return If the same price already exists return the index of the last occurrence (time preference)
     */
    public static int insertOfferPriceIndex(List<IOrder> book, int price) {
        for (int idx = 0; idx < book.size(); idx++) {
            IOrder order = book.get(idx);
            if (price < order.getPrice()) {
                return idx;
            }
        }
        return book.size();
    }

    /**
     * Find the index at which to place the new offer. Offers sorted from lowest to highest.
     * 11 9 9   7   bids
     * 9     new entry
     *
     * @return If the same price already exists return the index of the last occurrence (time preference)
     */
    public static int insertBidPriceIndex(List<IOrder> book, int price) {
        for (int idx = 0; idx < book.size(); idx++) {
            IOrder order = book.get(idx);
            if (price > order.getPrice()) {
                return idx;
            }
        }
        return book.size();
    }

    public int getBidDepth() {
        return bidsByPriceTime.size();
    }

    public int getOfferDepth() {
        return offerByPriceTime.size();
    }

    public void reset() {
        bidsByPriceTime.clear();
        offerByPriceTime.clear();
    }

    public IOrder getBestBid() {
        if (!bidsByPriceTime.isEmpty())
            return bidsByPriceTime.get(0);

        return null;
    }

    public IOrder getBestOffer() {
        if (!offerByPriceTime.isEmpty())
            return offerByPriceTime.get(0);

        return null;
    }

    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        ILimitOrderBook book = new LimitOrderBook();

        while (true) {
            try {
                String input = br.readLine();
                String[] fields = input.split(",");
                IOrder order = IOrder.parse(fields);
                book.newOrder(order);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
