package ru.trader.graph;

import ru.trader.core.Offer;
import ru.trader.core.Order;
import ru.trader.core.Vendor;

import java.util.*;

public class PathRoute extends Path<Vendor> {
    private final ArrayList<Order> orders = new ArrayList<>();
    private double profit = 0;
    private int transitIndex = 0;
    private PathRoute tail;


    public PathRoute(Vertex<Vendor> source) {
        super(source);
    }

    private PathRoute(PathRoute head, Vertex<Vendor> vertex, boolean refill) {
        super(head, vertex, refill);
        assert head.tail == null;
        head.tail = this;
    }

    @Override
    public Path<Vendor> connectTo(Vertex<Vendor> vertex, boolean refill) {
        return new PathRoute(this.getCopy(), vertex, refill);
    }

    @Override
    protected void finish(Vertex<Vendor> target) {
        if (!isRoot()) {
            if (!contains(target.getEntry())){
                updateOrders(target.getEntry());
                getHead().finish(target);
                if (getHead().getTarget() != target) getHead().finish();
            }
        }
    }

    private boolean contains(Vendor buyer){
        for (Order order : orders) {
            if (order.isBuyer(buyer)) return true;
        }
        return false;
    }

    private void updateOrders(Vendor buyer){
        Vendor seller = getHead().getTarget().getEntry();
        for (Offer sell : seller.getAllSellOffers()) {
            Offer buy = buyer.getBuy(sell.getItem());
            if (buy != null) orders.add(new Order(sell, buy));
        }
    }

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Order order : orders) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(order.getBuy().getItem());
            sb.append(" (").append(order.getBuyer()).append(") ");
        }
        String o = sb.toString();
        sb = new StringBuilder();
        if (isRoot()){
            sb.append(getTarget().getEntry());
            if (o.length()>0) sb.append(" (").append(o).append(") ");
        } else {
            sb.append(getHead().toString());
            if (isRefill()) sb.append("(R)");
            if (o.length()>0) sb.append(" (").append(o).append(") ");
            sb.append(" -> ").append(getTarget().getEntry());
        }
        return sb.toString();
    }

    public boolean isEmpty(){
        return orders.isEmpty();
    }

    public PathRoute getCopy(){
        PathRoute path = (PathRoute) getRoot();
        PathRoute res = new PathRoute(path.getTarget());
        while (path.tail != null){
            res = new PathRoute(res, path.tail.getTarget(), path.tail.isRefill());
            path = path.tail;
        }
        return res;
    }

    public double getProfit(){
        return profit;
    }

    public double getProfit(Order order){
        if (isPathFrom(order.getBuyer())) return order.getProfit() + profit;
        return tail != null ? tail.getProfit(order) : order.getProfit();
    }

    @Override
    protected PathRoute getHead() {
        return (PathRoute) super.getHead();
    }

    public void resort(double balance, long limit){
        if (isRoot()) return;
        for (Order order : orders) {
            order.setMax(balance, limit);
        }
        orders.sort(this::compareOrders);

        updateProfit();
        updateTransitIndex();
        getHead().resort(balance, limit);
    }

    private void updateTransitIndex() {
        transitIndex = orders.size();
        if (isEmpty()) return;
        double transitProfit = tail != null ? tail.getProfit() : 0;
        ListIterator<Order> itr = orders.listIterator(orders.size());
        while (itr.hasPrevious()){
            Order o = itr.previous();
            if (getProfit(o) > transitProfit){
                return;
            }
            transitIndex--;
        }
    }

    private void updateProfit() {
        if (isEmpty()){
            profit = tail != null ? tail.getProfit() : 0;
        } else {
            Order best = orders.get(0);
            profit = getProfit(best);
        }
    }

    private int compareOrders(Order o1, Order o2){
        if (tail == null || o1.isBuyer(o2.getBuyer())){
            //reverse
            return o2.compareTo(o1);
        }
        double profit1 = getProfit(o1);
        double profit2 = getProfit(o2);
        return Double.compare(profit2, profit1);
    }


    public PathRoute getTail() {
        return tail;
    }

    public int getTransitIndex() {
        return transitIndex;
    }
}
