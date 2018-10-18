package clob;

import java.util.List;

public interface ILimitOrderBook {
    List<Trade> newOrder(IOrder newOrder);

}
