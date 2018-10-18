package clob;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static clob.LimitOrderBook.insertBidPriceIndex;
import static clob.LimitOrderBook.insertOfferPriceIndex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LimitOrderBookTestCase {

        /*
        field idx    type     description
        0            char     'B', 'S'
        1            int      uid
        2            short    price in pence (>0)
        3            int      quantity (>0)

        B,100322,5103,7500
         */

    @Test
    public void testLimitOrder1level() {
        LimitOrderBook lob = new LimitOrderBook();
        IOrder o1 = new LimitOrder('B', 1, (short) 101, 10);
        IOrder o2 = new LimitOrder('B', 2, (short) 100, 10);
        IOrder o3 = new LimitOrder('S', 3, (short) 103, 10);

        List<Trade> trades = setupBook(lob, o1, o2, o3);
        assertTrue(trades.isEmpty());

        // Fill first buy order
        IOrder o4 = new LimitOrder('S', 4, (short) 101, 10);
        trades = lob.newOrder(o4);
        assertEquals(1, trades.size());
        assertEquals(1, lob.getBidDepth());
        assertEquals(1, lob.getOfferDepth());
        assertEquals(new Trade(o1.getUid(), o4.getUid(), (short) 101, 10), trades.get(0));
    }

    @Test
    public void testLimitOrderNlevels() {
        LimitOrderBook lob = new LimitOrderBook();
        IOrder o1 = new LimitOrder('B', 1, (short) 101, 10);
        IOrder o2 = new LimitOrder('B', 2, (short) 100, 10);
        IOrder o3 = new LimitOrder('S', 3, (short) 103, 10);

        List<Trade> trades = setupBook(lob, o1, o2, o3);
        assertTrue(trades.isEmpty());

        // Fill both buy orders
        IOrder o5 = new LimitOrder('S', 5, (short) 100, 20);
        setupBook(lob, o1, o2, o3);
        trades = lob.newOrder(o5);
        assertEquals(2, trades.size());
        assertEquals(0, lob.getBidDepth());
        assertEquals(1, lob.getOfferDepth());
        assertEquals(new Trade(o1.getUid(), o5.getUid(), (short) 101, 10), trades.get(0));
        assertEquals(new Trade(o2.getUid(), o5.getUid(), (short) 100, 10), trades.get(1));
    }

    @Test
    public void testLimitOrderExistingDepthPartiallyFilled() {
        LimitOrderBook lob = new LimitOrderBook();
        IOrder o1 = new LimitOrder('B', 1, (short) 101, 10);
        IOrder o2 = new LimitOrder('B', 2, (short) 100, 10);

        List<Trade> trades = setupBook(lob, o1, o2);
        assertTrue(trades.isEmpty());

        // Fill both buy orders
        IOrder o5 = new LimitOrder('S', 5, (short) 100, 15);
        trades = lob.newOrder(o5);
        assertEquals(2, trades.size());
        assertEquals(1, lob.getBidDepth());
        assertEquals(0, lob.getOfferDepth());
        assertEquals(new Trade(o1.getUid(), o5.getUid(), (short) 101, 10), trades.get(0));
        assertEquals(new Trade(o2.getUid(), o5.getUid(), (short) 100, o2.getRemainingQty() - o5.getRemainingQty()), trades.get(1));
        assertEquals(o2.getUid(), lob.getBestBid().getUid());
        assertEquals(5, lob.getBestBid().getRemainingQty());
    }

    @Test
    public void testLimitOrderNewOrderPartiallyFilled() {
        LimitOrderBook lob = new LimitOrderBook();
        IOrder o1 = new LimitOrder('B', 1, (short) 101, 10);
        IOrder o2 = new LimitOrder('B', 2, (short) 100, 10);

        List<Trade> trades = setupBook(lob, o1, o2);
        assertTrue(trades.isEmpty());

        // Fill both buy orders
        IOrder o5 = new LimitOrder('S', 5, (short) 100, 25);
        trades = lob.newOrder(o5);
        assertEquals(2, trades.size());
        assertEquals(0, lob.getBidDepth());
        assertEquals(1, lob.getOfferDepth());
        assertEquals(new Trade(o1.getUid(), o5.getUid(), (short) 101, 10), trades.get(0));
        assertEquals(new Trade(o2.getUid(), o5.getUid(), (short) 100, 10), trades.get(1));
        assertEquals(o5.getUid(), lob.getBestOffer().getUid());
        assertEquals(5, lob.getBestOffer().getRemainingQty());
    }

    @Test
    public void testAggresiveIcebergOrderExecution() {
        LimitOrderBook lob = new LimitOrderBook();
        IOrder o1 = new LimitOrder('B', 1, (short) 99, 50_000);
        IOrder o2 = new LimitOrder('B', 2, (short) 98, 25_500);
        IOrder o3 = new LimitOrder('S', 3, (short) 100, 10_000);
        IOrder o4 = new LimitOrder('S', 4, (short) 100, 7_500);
        IOrder o5 = new LimitOrder('S', 5, (short) 101, 20_000);

        List<Trade> trades = setupBook(lob, o1, o2, o3, o4, o5);
        assertTrue(trades.isEmpty());

        // Fill both buy orders
        IOrder o6 = new IcebergOrder('B', 6, (short) 100, 100_000, 10_000);
        trades = lob.newOrder(o6);
        assertEquals(2, trades.size());
        assertEquals(3, lob.getBidDepth());
        assertEquals(1, lob.getOfferDepth());
        assertEquals(new Trade(o6.getUid(), o3.getUid(), (short) 100, 10_000), trades.get(0));
        assertEquals(new Trade(o6.getUid(), o4.getUid(), (short) 100, 7_500), trades.get(1));
        assertEquals(o6.getUid(), lob.getBestBid().getUid());
        assertEquals(82_500, lob.getBestBid().getRemainingQty());
    }


    //    8:20:25 50,000 99 100 10,000 8:20:32
//            8:24:09 25,500 98 100 7,500 8:22:57
//            101 20,000 8:19:00
    @Test
    public void testAggressiveIcebergOrder() {
        LimitOrderBook lob = new LimitOrderBook();
        IOrder o1 = new LimitOrder('B', 1, (short) 99, 50_000);
        IOrder o2 = new LimitOrder('B', 2, (short) 98, 25_500);
        IOrder o3 = new LimitOrder('S', 3, (short) 100, 10_000);
        IOrder o4 = new LimitOrder('S', 4, (short) 100, 7_500);
        IOrder o5 = new LimitOrder('S', 5, (short) 101, 20_000);

        List<Trade> trades = setupBook(lob, o1, o2, o3, o4, o5);
        assertTrue(trades.isEmpty());

        IOrder o6 = new IcebergOrder('B', 6, (short) 100, 100_000, 10_000);
        trades = lob.newOrder(o6);

/*
8:25:00 10,000 100 101 20,000 8:19:00
8:20:25 50,000 99
8:24:09 25,500 98
 */
        assertEquals(2, trades.size());
        assertEquals(3, lob.getBidDepth());
        assertEquals(1, lob.getOfferDepth());
        assertEquals(new Trade(o6.getUid(), o3.getUid(), (short) 100, 10_000), trades.get(0));
        assertEquals(new Trade(o6.getUid(), o4.getUid(), (short) 100, 7_500), trades.get(1));
        assertEquals(o6.getUid(), lob.getBestBid().getUid());
        assertEquals(82_500, lob.getBestBid().getRemainingQty());
        assertEquals(10_000, ((IcebergOrder)lob.getBestBid()).getRevealedPeakQty());

    }

//    8:25:00 10,000 100 101 20,000 8:19:00
//    8:20:25 50,000 99
//    8:24:09 25,500 98
    @Test
    public void testPassiveIcebergOrder() {
        LimitOrderBook lob = new LimitOrderBook();
        IOrder o1 = new IcebergOrder('B', 1, (short) 100, 82_500, 10_000);
        IOrder o2 = new LimitOrder('B', 2, (short) 99, 50_000);
        IOrder o3 = new LimitOrder('B', 3, (short) 98, 25_500);
        IOrder o4 = new LimitOrder('S', 4, (short) 101, 20_000);

        List<Trade> trades = setupBook(lob, o1, o2, o3, o4);
        assertTrue(trades.isEmpty());

        IOrder o5 = new LimitOrder('S', 5, (short) 100, 10_000);
        trades = lob.newOrder(o5);

/*
8:25:32 10,000 100 101 20,000 8:19:00
8:20:25 50,000 99
8:24:09 25,500 98
 */
        assertEquals(1, trades.size());
        assertEquals(3, lob.getBidDepth());
        assertEquals(1, lob.getOfferDepth());
        assertEquals(new Trade(o1.getUid(), o5.getUid(), (short) 100, 10_000), trades.get(0));
        assertEquals(o1.getUid(), lob.getBestBid().getUid());
        assertEquals(72_500, lob.getBestBid().getRemainingQty());
        assertEquals(10_000, ((IcebergOrder)lob.getBestBid()).getRevealedPeakQty());

    }

/*
8:25:32 10,000 100 101 20,000 8:19:00
8:20:25 50,000 99
8:24:09 25,500 98
 */
    @Test
    public void testPassiveIcebergOrderExceedingPeak() {
        LimitOrderBook lob = new LimitOrderBook();
        IOrder o1 = new IcebergOrder('B', 1, (short) 100, 72_500, 10_000);
        IOrder o2 = new LimitOrder('B', 2, (short) 99, 50_000);
        IOrder o3 = new LimitOrder('B', 3, (short) 98, 25_500);
        IOrder o4 = new LimitOrder('S', 4, (short) 101, 20_000);

        List<Trade> trades = setupBook(lob, o1, o2, o3, o4);
        assertTrue(trades.isEmpty());

        // Fill both buy orders
        IOrder o5 = new LimitOrder('S', 5, (short) 99, 11_000);
        trades = lob.newOrder(o5);
        assertEquals(2, trades.size());
        assertEquals(3, lob.getBidDepth());
        assertEquals(1, lob.getOfferDepth());
        assertEquals(new Trade(o1.getUid(), o5.getUid(), (short) 100, 10_000), trades.get(0));
        assertEquals(new Trade(o1.getUid(), o5.getUid(), (short) 100, 1_000), trades.get(1));
        assertEquals(o1.getUid(), lob.getBestBid().getUid());
        assertEquals(61_500, lob.getBestBid().getRemainingQty());
        assertEquals(9_000, ((IcebergOrder)lob.getBestBid()).getRevealedPeakQty());
    }

/*
8:26:12 9,000A 100 101 20,000 8:19:00
8:28:00 20,000B 100
8:20:25 50,000 99
 */
    @Test
    public void testSecondPassiveIcebergOrder() {
        LimitOrderBook lob = new LimitOrderBook();
        IOrder o1 = new IcebergOrder('B', 1, (short) 100, 72_500, 10_000);
        IOrder o2 = new IcebergOrder('B', 2, (short) 100, 50_000, 20_000);
        IOrder o3 = new LimitOrder('B', 3, (short) 99, 50_000);
        IOrder o4 = new LimitOrder('S', 4, (short) 101, 20_000);

        List<Trade> trades = setupBook(lob, o1, o2, o3, o4);
        assertTrue(trades.isEmpty());

/*
8:30:00 4,000A 100 101 20,000 8:19:00
8:30:00 20,000B 100
8:20:25 50,000 99
 */
        // Fill both buy orders
        IOrder o5 = new LimitOrder('S', 5, (short) 99, 35_000);
        trades = lob.newOrder(o5);
        assertEquals(3, trades.size());
        assertEquals(3, lob.getBidDepth());
        assertEquals(1, lob.getOfferDepth());
        assertEquals(new Trade(o1.getUid(), o5.getUid(), (short) 100, 10_000), trades.get(0));
        assertEquals(new Trade(o2.getUid(), o5.getUid(), (short) 100, 20_000), trades.get(1));
        assertEquals(new Trade(o1.getUid(), o5.getUid(), (short) 100, 5_000), trades.get(2));
        assertEquals(o1.getUid(), lob.getBestBid().getUid());
        assertEquals(57_500, lob.getBestBid().getRemainingQty());
        assertEquals(5_000, ((IcebergOrder)lob.getBestBid()).getRevealedPeakQty());
    }


    @Test
    public void testInsertOfferPriceIndex() {
        List<IOrder> offers = Arrays.asList(
                new LimitOrder('S', 1, (short) 3, 10),
                new LimitOrder('S', 2, (short) 4, 10),
                new LimitOrder('S', 3, (short) 4, 5),
                new LimitOrder('S', 4, (short) 6, 10)
        );

        /**
         * 3 4 4   6      offers
         *       5
         */

        // offers
        assertEquals(0, insertOfferPriceIndex(offers, 2));
        assertEquals(1, insertOfferPriceIndex(offers, 3));
        assertEquals(3, insertOfferPriceIndex(offers, 4));
        assertEquals(3, insertOfferPriceIndex(offers, 5));
        assertEquals(4, insertOfferPriceIndex(offers, 8));
    }

    @Test
    public void testInsertBidPriceIndex() {
        List<IOrder> bids = Arrays.asList(
                new LimitOrder('B', 1, (short) 11, 10),
                new LimitOrder('B', 2, (short) 9, 10),
                new LimitOrder('B', 3, (short) 9, 5),
                new LimitOrder('B', 4, (short) 7, 10)
        );

        /**
         * 11 9 9   7   bids
         *        8
         *
         */

        // bids
        assertEquals(0, insertBidPriceIndex(bids, 12));
        assertEquals(1, insertBidPriceIndex(bids, 10));
        assertEquals(3, insertBidPriceIndex(bids, 9)); // always take last occurrence
        assertEquals(3, insertBidPriceIndex(bids, 8));
        assertEquals(4, insertBidPriceIndex(bids, 7));
        assertEquals(4, insertBidPriceIndex(bids, 5));
    }

    private List<Trade> setupBook(LimitOrderBook book, IOrder... orders) {
        book.reset();
        List<Trade> trades = new ArrayList<>();

        for (IOrder order : orders) {
            trades.addAll(book.newOrder(order));
        }
        long bidCount = Arrays.stream(orders).filter(order -> order.isBuy()).count();
        long offerCount = Arrays.stream(orders).filter(order -> order.isSell()).count();
        assertEquals(bidCount, book.getBidDepth());
        assertEquals(offerCount, book.getOfferDepth());
        return trades;
    }
}