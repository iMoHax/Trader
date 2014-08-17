package ru.trader.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Offer;
import ru.trader.core.Order;
import ru.trader.core.Vendor;

import java.util.*;

public class PathRoute extends Path<Vendor> {
    private final static Logger LOG = LoggerFactory.getLogger(PathRoute.class);

    private final ArrayList<Order> orders = new ArrayList<>();
    private final boolean expand;
    private double profit = 0;
    private PathRoute tail;
    private int ordersCount = 0;
    public final static Order TRANSIT = null;

    public PathRoute(Vertex<Vendor> source, boolean expand) {
        super(source);
        this.expand = expand;
    }

    public PathRoute(Vertex<Vendor> source) {
        super(source);
        expand = false;
    }

    private PathRoute(PathRoute head, Vertex<Vendor> vertex, boolean refill) {
        super(head, vertex, refill);
        assert head.tail == null;
        head.tail = this;
        expand = head.expand;
        //transit
        orders.add(ordersCount++, TRANSIT);
    }

    @Override
    public Path<Vendor> connectTo(Vertex<Vendor> vertex, boolean refill) {
        LOG.trace("Connect path {} to {}", this, vertex);
        return new PathRoute(this.getCopy(), vertex, refill);
    }

    public PathRoute getCopy(){
        PathRoute path = (PathRoute) getRoot();
        PathRoute res = new PathRoute(path.getTarget());
        while (path.hasNext()){
            path = path.getNext();
            res = new PathRoute(res, path.getTarget(), path.isRefill());
        }
        return res;
    }

    private void addOrder(Order order){
        LOG.trace("Add order {} to path {}", order, this);
        orders.add(ordersCount++, order);
        if (!expand) return;
        LOG.trace("Expand orders");
        if (hasNext()){
            PathRoute next = getNext();
            LOG.trace("Add {} clone of order", next.ordersCount - 1);
            for (int i = 1; i < next.ordersCount; i++) {
                orders.add(ordersCount++, new Order(order.getSell(), order.getBuy()));
                addTransitsToHead();
            }
            cloneTailOrders(next.ordersCount);
        }
        addTransitsToHead();
    }

    private void addTransitsToHead(){
        PathRoute p = getPrevious();
        while (!p.isRoot()) {
            LOG.trace("Add transit order to path {}", p);
            p.orders.add(p.ordersCount++, TRANSIT);
            p = p.getPrevious();
        }
    }

    private void cloneTailOrders(int count){
        if (hasNext()) {
            PathRoute p = getNext();
            LOG.trace("Duplicate {} orders of path {}", count, p);
            for (int i = 0; i < count; i++) {
                Order o = p.orders.get(i);
                if (o == TRANSIT) p.orders.add(TRANSIT);
                else p.orders.add(new Order(o.getSell(), o.getBuy()));
            }
            p.cloneTailOrders(count);
        }
    }

    @Override
    protected void finish() {
        if (!isRoot()){
            fillOrders();
            getPrevious().finish();
        }
    }

    private void fillOrders(){
        LOG.trace("Fill orders of path {}", this);
        Vendor seller = getPrevious().get();
        for (Offer sell : seller.getAllSellOffers()) {
            PathRoute p = this;
            while (p != null) {
                Offer buy = p.get().getBuy(sell.getItem());
                if (buy != null) addOrder(new Order(sell, buy));
                p = p.getNext();
            }
        }
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    public boolean isEmpty(){
        return ordersCount <= 1;
    }

    @Override
    protected PathRoute getPrevious() {
        return (PathRoute) super.getPrevious();
    }

    public PathRoute getNext() {
        return tail;
    }

    public boolean hasNext(){
        return tail != null;
    }

    public void resort(double balance, long limit){
        if (isRoot()) return;
        for (Order order : orders) {
            if (order == TRANSIT) continue;
            order.setMax(balance, limit);
        }
        orders.sort(this::compareOrders);

        updateProfit();
        getPrevious().resort(balance, limit);
    }

    private double getTransitProfit(){
        return hasNext() ? getNext().getProfit() : 0;
    }

    public double getProfit(){
        return profit;
    }

    public double getProfit(Order order){
        if (order == TRANSIT) return getTransitProfit();
        if (isPathFrom(order.getBuyer())) return order.getProfit() + profit;
        return hasNext() ? getNext().getProfit(order) : order.getProfit();
    }

    private void updateProfit() {
        Order best = orders.get(0);
        if (best == TRANSIT) profit = getTransitProfit();
            else profit = getProfit(best);

    }

    private int compareOrders(Order o1, Order o2){
        if (o1 != TRANSIT && o2 !=  TRANSIT){
            if (!hasNext() || o1.isBuyer(o2.getBuyer()))
                return o2.compareTo(o1);
        }
        double profit1 = getProfit(o1);
        double profit2 = getProfit(o2);
        return Double.compare(profit2, profit1);
    }



    @Override
         public String toString() {
        StringBuilder sb = new StringBuilder();
        int step = !hasNext() || tail.ordersCount == 0 ? 1 : tail.ordersCount;
        for (int i = 0; i < ordersCount; i += step) {
            Order order = orders.get(i);
            if (order == TRANSIT) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(order.getBuy().getItem());
            sb.append(" (").append(order.getBuyer()).append(") ");
        }
        String o = sb.toString();
        sb = new StringBuilder();
        if (isRoot()){
            sb.append(get());
            if (o.length()>0) sb.append(" (").append(o).append(") ");
        } else {
            sb.append(getPrevious().toString());
            if (isRefill()) sb.append("(R)");
            if (o.length()>0) sb.append(" (").append(o).append(") ");
            sb.append(" -> ").append(get());
        }
        return sb.toString();
    }

}
