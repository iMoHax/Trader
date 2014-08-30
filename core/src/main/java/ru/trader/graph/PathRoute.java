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
    private double profit = 0;
    private double balance = 0;
    private PathRoute tail;
    public final static Order TRANSIT = null;

    public PathRoute(Vertex<Vendor> source) {
        super(source);
    }

    private PathRoute(PathRoute head, Vertex<Vendor> vertex, boolean refill) {
        super(head, vertex, refill);
        assert head.tail == null;
        head.tail = this;
        //transit
        orders.add(TRANSIT);
    }

    @Override
    public Path<Vendor> connectTo(Vertex<Vendor> vertex, boolean refill) {
        LOG.trace("Connect path {} to {}", this, vertex);
        return new PathRoute(this.getCopy(), vertex, refill);
    }

    public void add(PathRoute path, boolean withOrders) {
        LOG.trace("Add path {} to {}", path, this);
        PathRoute res = this;
        path = path.getRoot();
        if (!path.getTarget().equals(getTarget())){
            res = new PathRoute(res, path.getTarget(), true);
        }
        while (path.hasNext()){
            path = path.getNext();
            res = new PathRoute(res, path.getTarget(), res == this || path.isRefill());
            if (withOrders){
                res.orders.clear();
                res.orders.addAll(path.getOrders());
            }
        }
        if (withOrders){
            update();
        } else {
            res.finish();
        }
    }

    public PathRoute getCopy(){
        return getCopy(false);
    }

    public PathRoute getCopy(boolean withOrders){
        PathRoute path = getRoot();
        PathRoute res = new PathRoute(path.getTarget());
        if (withOrders) {
            res.orders.clear();
            res.orders.addAll(path.getOrders());
        }
        while (path.hasNext()){
            path = path.getNext();
            res = new PathRoute(res, path.getTarget(), path.isRefill());
            if (withOrders) {
                res.orders.clear();
                res.orders.addAll(path.getOrders());
            }
        }
        return res;
    }

    private void addOrder(Order order){
        LOG.trace("Add order {} to path {}", order, this);
        orders.add(order);
    }

    @Override
    protected void finish() {
        if (!isRoot()){
            fillOrders();
            getPrevious().finish();
        }
    }

    private void fillOrders(){
        orders.clear();
        orders.add(TRANSIT);
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
        return orders.size() <= 1;
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

    public void update(){
        PathRoute p = this;
        p.updateBalance();
        while (p.hasNext()){
            p = p.getNext();
            p.updateBalance();
        }
        while (p != this){
            p.updateProfit();
            p = p.getPrevious();
        }
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

    @Override
    public PathRoute getRoot() {
        return (PathRoute) super.getRoot();
    }

    public PathRoute getEnd() {
        return hasNext()? getNext().getEnd() : this;
    }

    public Order getBest(){
        return orders.get(0);
    }

    public double getDistance(){
        if (isRoot()){
            double res = 0;
            PathRoute p = this;
            while (p.hasNext()){
                p = p.getNext();
                res += p.getDistance();
            }
            return res;
        }
        else return getPrevious().get().getDistance(get());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Order order : orders) {
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

    public void setOrder(Order order) {
        orders.set(0, order);
    }

    public int getLandsCount(){
        int res = 0;
        PathRoute p = this.isRoot() ? getNext() : this;
        Order o = p.getBest();
        while (p.hasNext()){
            p = p.getNext();
            // lands for sell
            if (o != null && p.isPathFrom(o.getBuyer())){
                LOG.trace("{} is lands for sell by order {}", p, o);
                o = p.getBest();
                res++;
            } else {
                if (o == null){
                    o = p.getBest();
                    if (o!= null){
                        LOG.trace("{} is lands for buy by order {}", p, o);
                        res++;
                    }
                } else {
                    if (p.isRefill()){
                        LOG.trace("{} is lands for refill", p);
                        res++;
                    }
                }
            }

        }
        LOG.trace("{} is end, landing", p);
        res++;
        return res;
    }

    public PathRoute dropTo(Vendor vendor){
        PathRoute p = getCopy(true).getEnd();
        while (!p.isRoot() && !p.get().equals(vendor)){
            p = p.getPrevious();
        }
        p.tail = null;
        return p;
    }

    public static PathRoute toPathRoute(Vendor... items){
        Vendor t = items[0];
        PathRoute path = new PathRoute(new Vertex<>(t));
        for (int i = 1; i < items.length; i++) {
            t = items[i];
            path = new PathRoute(path, new Vertex<>(t), false);
        }
        return path;
    }
}
