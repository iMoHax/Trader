package ru.trader.analysis;

import ru.trader.core.Order;

import java.util.Collection;

public class RouteReserve {
    private final Order order;
    private final int fromIndex;
    private final int toIndex;
    private final long count;

    public RouteReserve(int fromIndex, int toIndex, long count) {
        order = null;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        this.count = count;
    }

    public RouteReserve(Order order, int fromIndex, int toIndex) {
        this.order = order;
        this.fromIndex = fromIndex;
        this.toIndex = toIndex;
        count = order.getCount();
    }

    public Order getOrder() {
        return order;
    }

    public int getFromIndex() {
        return fromIndex;
    }

    public int getToIndex() {
        return toIndex;
    }

    public long getCount() {
        return count;
    }

    public static int getCompleteIndex(Collection<RouteReserve> reserves, int fromIndex){
        int completeIndex = -1;
        for (RouteReserve reserve : reserves) {
            if (completeIndex == -1 || completeIndex + fromIndex < reserve.getToIndex() + fromIndex){
                completeIndex = reserve.getToIndex();
            }
        }
        return completeIndex;
    }
}
