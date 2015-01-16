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
    private final boolean byAvg;
    private double profit = 0;
    private double balance = 0;
    private double distance = 0;
    private int landsCount = 0;
    private PathRoute tail;
    public final static Order TRANSIT = null;

    public PathRoute(Vertex<Vendor> source) {
        this(source, false);
    }

    public static PathRoute buildAvg(Vertex<Vendor> source){
        return new PathRoute(source, true);
    }

    private PathRoute(Vertex<Vendor> source, boolean byAvg) {
        super(source);
        this.byAvg = byAvg;
    }


    private PathRoute(PathRoute head, Vertex<Vendor> vertex, boolean refill) {
        super(head, vertex, refill);
        assert head.tail == null;
        head.tail = this;
        byAvg = head.byAvg;
        //transit
        orders.add(TRANSIT);
    }

    @Override
    public Path<Vendor> connectTo(Vertex<Vendor> vertex, boolean refill) {
        LOG.trace("Connect path {} to {}", this, vertex);
        return new PathRoute(this.getCopy(), vertex, refill);
    }

    public PathRoute add(PathRoute path, boolean noSort) {
        LOG.trace("Add path {} to {}", path, this);
        PathRoute res = this;
        path = path.getRoot();
        if (!path.getTarget().equals(getTarget())){
            LOG.trace("Is not connected path, add edge from {} to {}", path.getTarget(), getTarget());
            res = new PathRoute(res, path.getTarget(), true);
            res.updateDistance();
        }
        while (path.hasNext()){
            path = path.getNext();
            res = new PathRoute(res, path.getTarget(), path.isRefill());
            if (noSort){
                copyField(path, res);
            }
        }
        if (noSort){
            update();
        } else {
            res.finish();
        }
        return res;
    }

    private void copyField(PathRoute source, PathRoute dest){
        dest.orders.clear();
        dest.orders.addAll(source.getOrders());
        dest.distance = source.distance;
        dest.profit = source.profit;
        dest.balance = source.balance;
        dest.landsCount = source.landsCount;
    }

    public PathRoute getCopy(){
        return getCopy(false);
    }

    public PathRoute getCopy(boolean withOrders){
        PathRoute path = getRoot();
        PathRoute res = new PathRoute(path.getTarget(), path.byAvg);
        if (withOrders) {
            copyField(path, res);
        }
        while (path.hasNext()){
            path = path.getNext();
            res = new PathRoute(res, path.getTarget(), path.isRefill());
            if (withOrders) {
                copyField(path, res);
            }
        }
        return res;
    }

    private void addOrder(Order order){
        LOG.trace("Add order {} to path {}", order, this);
        orders.add(order);
    }

    public void refresh(){
        getEnd().finish();
    }

    @Override
    protected void finish() {
        if (!isRoot()){
            fillOrders();
            getPrevious().finish();
        }
        updateDistance();
    }

    private void update(){
        PathRoute p = this;
        p.updateBalance();
        while (p.hasNext()){
            p = p.getNext();
            p.updateBalance();
        }
        p = this;
        p.updateProfit();
        p.updateLandsCount();
        while (!p.isRoot()){
            p = p.getPrevious();
            p.updateProfit();
            p.updateLandsCount();
        }
        p.updateDistance();
    }

    private void fillOrders(){
        orders.clear();
        orders.add(TRANSIT);
        LOG.trace("Fill orders of path {}", this);
        Vendor seller = getPrevious().get();
        for (Offer sell : seller.getAllSellOffers()) {
            PathRoute p = this;
            while (p != null) {
                Vendor buyer = p.get();
                Offer buy = buyer.getBuy(sell.getItem());
                if (buy != null){
                    Order order = new Order(sell, buy, 1);
                    if (order.getProfit() <= 0) {
                        LOG.trace("{} - is no profit, skip", order);
                    } else {
                        addOrder(order);
                    }
                }
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

    public void sort(double balance, long cargo){
        // start on root only
        if (isRoot()){
            this.balance = balance;
            if (hasNext()){
                getNext().forwardSort(cargo);
            }
        } else {
            getPrevious().sort(balance, cargo);
        }
    }

    private void forwardSort(long cargo){
        updateBalance();
        boolean needSort = false;
        for (Order order : orders) {
            if (order == TRANSIT) continue;
            if (order.getCount() < cargo){
                needSort = true;
                order.setMax(balance, cargo);
            }
        }
        if (needSort){
            LOG.trace("Simple sort");
            orders.sort(this::simpleCompareOrders);
            LOG.trace("New order of orders {}", orders);
        }
        if (hasNext()){
            getNext().forwardSort(cargo);
        } else {
            LOG.trace("Start back sort");
            Order best = orders.get(0);
            profit = best == TRANSIT ? 0 : best.getProfit();
            LOG.trace("Max profit from {} = {}", getPrevious().get(), profit);
            updateLandsCount();
            getPrevious().backwardSort();
        }
    }

    private void backwardSort(){
        orders.sort(byAvg ? this::compareByAvgProfit : this::compareOrders);
        LOG.trace("New order of orders {}", orders);
        updateProfit();
        updateLandsCount();
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

    public double getAvgProfit(){
        return isRoot()? profit/landsCount : getPrevious().getAvgProfit();
    }

    public double getBalance() {
        return balance;
    }

    public double getProfit(Order order){
        return getProfit(order, true);
    }

    private double getProfit(Order order, boolean first){
        if (order == TRANSIT) return getTransitProfit();
        if (isPathFrom(order.getBuyer())) {
            return first ? order.getProfit() : order.getProfit() + profit;
        }
        return hasNext() ? getNext().getProfit(order, false) : order.getProfit();
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

    private int compareByAvgProfit(Order o1, Order o2){
        if (o1 != TRANSIT && o2 !=  TRANSIT){
            if (!hasNext() || o1.isBuyer(o2.getBuyer()))
                return o2.compareTo(o1);
        }
        double profit1 = getProfit(o1)/computeLandsCount(o1);
        double profit2 = getProfit(o2)/computeLandsCount(o2);
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
        if (orders.isEmpty()) return null;
        return orders.get(0);
    }

    private double computeDistance(){
        if (isRoot()){
            double res = 0;
            PathRoute p = this;
            while (p.hasNext()){
                p = p.getNext();
                res += p.computeDistance();
            }
            return res;
        }
        else return getPrevious().get().getDistance(get());
    }

    private void updateDistance(){
        this.distance = computeDistance();
    }

    public double getDistance(){
        return distance;
    }

    private int computeLandsCount(Order order){
        int res = 0;
        PathRoute p = isRoot()? getNext() : this;
        while (p.hasNext()){
            p = p.getNext();
            // lands for sell
            if (order != null && p.isPathFrom(order.getBuyer())){
                LOG.trace("{} is lands for sell by order {}", p, order);
                return res + p.getLandsCount() + 1;
            } else {
                if (order == null){
                    order = p.getBest();
                    if (order != null){
                        LOG.trace("{} is lands for buy by order {}", p, order);
                        return res + p.getLandsCount() + 1;
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

    private void updateLandsCount(){
        Order best = isRoot() ? getNext().getBest() : getBest();
        landsCount = computeLandsCount(best);
        LOG.trace("Lands count from {} = {}", isRoot() ? get() : getPrevious().get(), landsCount);
    }

    public int getLandsCount() {
        return landsCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Order order = getBest();
        if (order != TRANSIT){
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
            sb.append(" ").append(balance).append(" ");
            if (isRefill()) sb.append("(R)");
            if (o.length()>0) sb.append(" (").append(o).append(") ");
            sb.append(" -> ").append(get());
        }
        return sb.toString();
    }

    public void setOrder(Order order) {
        orders.set(0, order);
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

    public boolean isRoute(PathRoute path){
        return this == path || (isRoot() ? path.isRoot() : !path.isRoot() && getPrevious().isRoute(path.getPrevious()))
                && this.getTarget().equals(path.getTarget())
                && this.profit == path.profit
                && this.balance == path.balance
                && (this.getBest() == null && path.getBest() == null || this.getBest().equals(path.getBest()));

    }
}
