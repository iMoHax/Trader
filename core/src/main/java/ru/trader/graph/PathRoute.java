package ru.trader.graph;

import ru.trader.core.Offer;
import ru.trader.core.Order;
import ru.trader.core.Vendor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class PathRoute extends Path<Vendor> {
    private final ArrayList<Order> orders = new ArrayList<>();

    public PathRoute(Vertex<Vendor> source) {
        super(source);
    }

    protected PathRoute(Path<Vendor> head, Vertex<Vendor> vertex, boolean refill) {
        super(head, vertex, refill);
    }

    @Override
    public Path<Vendor> connectTo(Edge<Vendor> edge, boolean refill) {
        return new PathRoute(this.getCopy(), edge.getTarget(), refill);
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

    public Collection<Order> getOrders() {
        return orders;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Order order : orders) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(order.getBuy().getItem());
            sb.append(" (").append(order.getBuy().getVendor()).append(") ");
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

    public Path<Vendor> getCopy(){
        Path<Vendor> res;
        LinkedList<Path<Vendor>> v = new LinkedList<>();
        Path<Vendor> p = this;
        while (!p.isRoot()){
            v.add(p);
            p = p.getHead();
        }
        res = p;
        Iterator<Path<Vendor>> it = v.descendingIterator();
        while (it.hasNext()){
            p = it.next();
            res = new PathRoute(res, p.getTarget(), p.isRefill());
        }
        return res;
    }
}
