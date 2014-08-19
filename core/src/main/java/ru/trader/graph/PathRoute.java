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
    private final int index;
    private double profit = 0;
    private double balance = 0;
    private PathRoute tail;
    private int ordersCount = 0;
    public final static Order TRANSIT = null;

    public PathRoute(Vertex<Vendor> source, boolean expand) {
        super(source);
        this.expand = expand;
        index = 0;
    }

    public PathRoute(Vertex<Vendor> source) {
        super(source);
        expand = false;
        index = 0;
    }

    private PathRoute(PathRoute head, Vertex<Vendor> vertex, boolean refill) {
        super(head, vertex, refill);
        assert head.tail == null;
        head.tail = this;
        expand = head.expand;
        //transit
        orders.add(ordersCount++, TRANSIT);
        index = head.index+1;
    }

    @Override
    public Path<Vendor> connectTo(Vertex<Vendor> vertex, boolean refill) {
        LOG.trace("Connect path {} to {}", this, vertex);
        return new PathRoute(this.getCopy(), vertex, refill);
    }

    public PathRoute getCopy(){
        PathRoute path = getRoot();
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
        if (expand) expand(order);
    }

    private void expand(Order order){
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

    public void sort(double balance, long limit){
        // start on root only
        if (isRoot()){
            this.balance = balance;
            if (hasNext())
                getNext().forwardSort(limit);
        } else {
            getPrevious().sort(balance, limit);
        }
    }

    private void forwardSort(long limit){
        updateBalance();
        boolean needSort = false;
        for (Order order : orders) {
            if (order == TRANSIT) continue;
            if (order.getCount() < limit){
                needSort = true;
                order.setMax(balance, limit);
            }
        }
        if (needSort){
            LOG.trace("Simple sort");
            orders.sort(this::simpleCompareOrders);
            LOG.trace("New order of orders {}", orders);
        }
        if (hasNext()){
            getNext().forwardSort(limit);
        } else {
            LOG.trace("Start back sort");
            Order best = orders.get(0);
            profit = best == TRANSIT ? 0 : best.getProfit();
            LOG.trace("Max profit from {} = {}",getPrevious().get(), profit);
            getPrevious().backwardSort();
        }
    }

    private void backwardSort(){
        orders.sort(this::compareOrders);
        LOG.trace("New order of orders {}", orders);
        updateProfit();
        if (!isRoot())
            getPrevious().backwardSort();
    }

    private void updateBalance() {
        PathRoute p = getPrevious();
        balance = p.balance;
        if (!p.isRoot()) {
            Vendor buyer = p.get();
            while (!p.isRoot()){
                for (Order order : p.orders) {
                    if (order == TRANSIT) continue;
                    if (order.isBuyer(buyer) && balance < p.balance + order.getProfit()){
                        balance = p.balance + order.getProfit();
                        LOG.trace("Order {} is best to {}, new balance {}", order, buyer, balance);
                    }

                }
                p = p.getPrevious();
            }
        }
        LOG.trace("Max balance on {} = {}", getPrevious().get(), balance);
    }


    private void updateProfit() {
        Order best = orders.isEmpty()? TRANSIT : orders.get(0);
        if (best == TRANSIT) profit = getTransitProfit();
        else profit = getProfit(best);
        LOG.trace("Max profit from {} = {}", isRoot() ? get() : getPrevious().get(), profit);
    }

    private double getTransitProfit(){
        return hasNext() ? getNext().getProfit() : 0;
    }

    public double getProfit(){
        return profit;
    }

    public double getBalance() {
        return balance;
    }

    public double getProfit(Order order){
        if (order == TRANSIT) return getTransitProfit();
        if (isPathFrom(order.getBuyer())) return order.getProfit() + profit;
        return hasNext() ? getNext().getProfit(order) : order.getProfit();
    }

    private int simpleCompareOrders(Order o1, Order o2){
        if (o1 != TRANSIT && o2 !=  TRANSIT){
            return o2.compareTo(o1);
        }
        return o1 == TRANSIT ? o2 ==  TRANSIT ? 0 : Double.compare(o2.getProfit(), 0) : Double.compare(0, o1.getProfit());
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

    public PathRoute getPath(int index){
        if (this.index == index) return this;
        if (this.index > index){
            return isRoot() ? null : getPrevious().getPath(index);
        } else {
            return hasNext() ? getNext().getPath(index) : null;
        }
    }

    @Override
    public PathRoute getRoot() {
        return (PathRoute) super.getRoot();
    }

    public Order getBest(){
        return orders.get(0);
    }

    public double getMaxProfit(){
        Order o = orders.get(0);
        return  o != TRANSIT ? o.getProfit() : 0;
    }

    public double getDistance(){
        return isRoot() ? 0 : getPrevious().get().getDistance(get());
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
