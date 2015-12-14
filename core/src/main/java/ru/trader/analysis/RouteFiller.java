package ru.trader.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Offer;
import ru.trader.core.Order;
import ru.trader.core.Vendor;

import java.util.*;

public class RouteFiller {
    private final static Logger LOG = LoggerFactory.getLogger(Route.class);
    private final static OrderStack TRANSIT = null;

    private final double balance;
    private final long cargo;
    private final Scorer scorer;
    private Route route;
    private List<OEntry> oList;

    public RouteFiller(Scorer scorer) {
        this.scorer = scorer;
        this.balance = scorer.getProfile().getBalance();
        this.cargo = scorer.getProfile().getShip().getCargo();
    }

    public void fill(Route route){
        this.route = route;
        route.setBalance(balance);
        route.setCargo(cargo);
        fillOrders();
        updateEntries();
        route.updateStats();
    }

    private void fillOrders(){
        fillOrdersList();
        sort();
    }

    private void fillOrdersList(){
        List<RouteEntry> entries = route.getEntries();
        oList = new ArrayList<>(entries.size());
        for (int i = 0; i < entries.size(); i++) {
            RouteEntry rEntry = entries.get(i);
            OEntry entry = new OEntry(rEntry.getVendor(), rEntry.isRefill(), rEntry.getFuel());
            oList.add(entry);
            Vendor seller = entries.get(i).getVendor();
            LOG.trace("Fill orders for {}", seller);
            final int nextIndex = i+1;
            Collection<Vendor> vendors = route.getVendors(nextIndex);
            for (Offer sell : seller.getAllSellOffers()) {
                for (Vendor buyer : vendors) {
                    Offer buy = buyer.getBuy(sell.getItem());
                    if (buy != null) {
                        Order order = new Order(sell, buy, 1);
                        if (order.getProfit() <= 0) {
                            LOG.trace("{} - is no profit, skip", order);
                        } else {
                            entry.add(order);
                        }
                    }
                }
            }
        }
    }

    private void sort() {
        LOG.trace("Start forward sort");
        for (int i = 0; i < oList.size(); i++) {
            OEntry entry = oList.get(i);
            entry.setBalance(getBalance(entry.vendor, i - 1));
            entry.fillToMax(i+1);
        }
        LOG.trace("Start backward sort");
        for (int i = oList.size() - 1; i >= 0; i--) {
            OEntry entry = oList.get(i);
            final int nextIndex = i+1;
            BackwardComparator comparator = new BackwardComparator(nextIndex);
            entry.sort(comparator);
            OrderStack best = entry.getBest();
            entry.setFullScore(comparator.getScore(best));
            entry.setLands(getLands(best, nextIndex) + (best != TRANSIT || entry.refill ? 1 : 0));
        }
    }

    private int getIndex(Vendor vendor, int startIndex){
        for (int i = startIndex; i < oList.size(); i++) {
            if (oList.get(i).vendor.equals(vendor)) return i;
        }
        return -1;
    }

    private double getBalance(Vendor vendor, int startIndex){
        double res = 0;
        double score = 0;
        double fuel = 0;
        int refills = 0;
        int jumps = 0;
        for (int i = startIndex; i >= 0; i--) {
            OEntry entry = oList.get(i);
            fuel += entry.fuel;
            if (entry.fuel > 0) jumps++;
            OrderStack best = entry.getStack(vendor);
            if (best == null) continue;
            double profit = best.getProfit();
            if (res < profit){
                double newScore = scorer.getScore(entry.vendor, profit, jumps, refills, fuel);
                if (newScore > score) {
                    score = newScore;
                    res = profit;
                    LOG.trace("Orders {} is best to {}, score {}, profit {}", best, vendor, score, res);
                }
            }
            if (entry.refill){
                refills++;
            }
        }
        LOG.trace("Max score on {} = {}, profit {}", vendor, score, res);
        return balance + res;
    }



    private double getScore(OrderStack order, int startIndex){
        double fuel = 0;
        int refills = 0;
        int jumps = 0;
        Vendor buyer = order.getBuyer();
        for (int i = startIndex; i < oList.size(); i++) {
            OEntry entry = oList.get(i);
            fuel += entry.fuel;
            if (entry.fuel > 0) jumps++;
            if (entry.vendor.equals(buyer)){
                return scorer.getScore(order.getBuyer(), order.getProfit(), jumps, refills + entry.lands, fuel);
            }
            if (entry.refill){
                refills++;
            }
        }
        return 0;
    }


    private double getFullScore(OrderStack order, int startIndex){
        if (startIndex >= oList.size()) return 0;
        if (order == TRANSIT) return oList.get(startIndex).score;
        int index = getIndex(order.getBuyer(), startIndex);
        if (index > 0) {
            return oList.get(index).score + getScore(order, startIndex);
        }
        return 0;
    }

    private int getLands(OrderStack order, int startIndex) {
        if (startIndex >= oList.size()) return 1;
        if (order == TRANSIT) return oList.get(startIndex).lands;
        int index = getIndex(order.getBuyer(), startIndex);
        if (index > 0) {
            return oList.get(index).lands;
        }
        throw new IllegalStateException(String.format("Not found buyer for order %s", order));
    }

    private void updateEntries(){
        List<RouteEntry> entries = route.getEntries();
        OrderStack best = TRANSIT;
        for (int i = 0; i < entries.size(); i++) {
            RouteEntry entry = entries.get(i);
            entry.clearOrders();
            entry.setLand(false);
            OEntry o = oList.get(i);
            if (best == TRANSIT || o.vendor.equals(best.getBuyer())){
                if (best != TRANSIT)
                    entry.setLand(true);
                best = o.getBest();
                if (best != TRANSIT)
                    entry.addAll(best.bestOrders);
            }
            RouteEntry prev = i != 0 ? entries.get(i-1) : null;
            if (prev != null){
                prev.setProfit(scorer.getProfit(prev.getProfitByOrders(), prev.getFuel()));
                prev.setTime(scorer.getTime(entry, prev));
                prev.setFullTime(prev.getTime());
            } else {
                entry.setProfit(0);
                entry.setTime(0);
            }
        }
    }

    private class ForwardComparator implements Comparator<OrderStack> {
        private final HashMap<OrderStack, Double> cache = new HashMap<>(10);
        private final int index;

        private ForwardComparator(int index) {
            this.index = index;
        }

        @Override
        public int compare(OrderStack o1, OrderStack o2) {
            if (o1 != TRANSIT && o2 !=  TRANSIT){
                double score1 = getScore(o1);
                double score2 = getScore(o2);
                return Double.compare(score2, score1);
            }
            return o1 == TRANSIT ? o2 ==  TRANSIT ? 0 : Double.compare(o2.getProfit(), 0) : Double.compare(0, o1.getProfit());
        }

        private double getScore(OrderStack stack){
            Double score = cache.get(stack);
            if (score == null){
                score = RouteFiller.this.getScore(stack, index);
                cache.put(stack, score);
            }
            return score;
        }
    }


    private class BackwardComparator implements Comparator<OrderStack> {
        private final HashMap<OrderStack, Double> cache = new HashMap<>(10);
        private final int index;

        private BackwardComparator(int index) {
            this.index = index;
        }

        @Override
        public int compare(OrderStack o1, OrderStack o2) {
            double score1 = getScore(o1);
            double score2 = getScore(o2);
            return Double.compare(score2, score1);
        }

        public double getScore(OrderStack stack){
            Double score = cache.get(stack);
            if (score == null){
                score = RouteFiller.this.getFullScore(stack, index);
                cache.put(stack, score);
            }
            return score;
        }
    }

    private class OEntry {
        private final List<OrderStack> orders;

        private final Vendor vendor;
        private final boolean refill;
        private final double fuel;
        private double balance;
        private double score;
        private int lands;


        private OEntry(Vendor vendor, boolean refill, double fuel){
            this.vendor = vendor;
            orders = new ArrayList<>();
            orders.add(TRANSIT);
            this.refill = refill;
            this.fuel = fuel;
        }

        public void add(Order order){
            LOG.trace("Add order {}", order);
            OrderStack stack = getStack(order.getBuyer());
            if (stack == null){
                stack = new OrderStack();
                orders.add(stack);
            }
            stack.add(order);
        }

        public OrderStack getStack(Vendor vendor){
            OrderStack stack = null;
            for (OrderStack s : orders) {
                if (s == TRANSIT) continue;
                if (s.is(vendor)){
                    stack = s;
                    break;
                }
            }
            return stack;
        }

        public void setBalance(double balance){
            this.balance = balance;
        }

        public void fillToMax(int nextIndex){
            orders.forEach(o -> {
                if (o != TRANSIT)
                    o.fillBest(balance);
            });
            sort(new ForwardComparator(nextIndex));
        }

        public double getProfit(){
            OrderStack best = orders.get(0);
            return best.getProfit();
        }

        public void setFullScore(double score) {
            LOG.trace("New full score {}", score);
            this.score = score;
        }

        public void setLands(int lands) {
            LOG.trace("New lands count {}", lands);
            this.lands = lands;
        }

        public OrderStack getBest(){
            return orders.get(0);
        }

        public void sort(Comparator<OrderStack> comparator) {
            LOG.trace("Sort");
            orders.sort(comparator);
            LOG.trace("New order of orders {}", orders);
        }
    }

    private class OrderStack {
        private final List<Order> orders;
        private final List<Order> bestOrders;

        private OrderStack() {
            orders = new ArrayList<>();
            bestOrders = new ArrayList<>();
        }

        public void add(Order order){
            orders.add(order);
        }

        public Vendor getBuyer(){
            return orders.isEmpty() ? null : orders.get(0).getBuyer();
        }

        public boolean is(Vendor buyer){
            return orders.isEmpty() || orders.get(0).isBuyer(buyer);
        }

        public void fillBest(double balance){
            bestOrders.clear();
            bestOrders.addAll(MarketUtils.getStack(orders, balance, cargo));
        }

        public double getProfit(){
            return bestOrders.stream().mapToDouble(Order::getProfit).sum();
        }


        @Override
        public String toString() {
            return "{" + bestOrders + "}";
        }
    }

    public static double[] getLostProfits(Route route, int fromIndex, Vendor target, long count) {
        final double[] profits = getLostProfits(route, count);
        int size = route.isLoop() ? route.getJumps()-1: route.getJumps();
        double[] res = new double[size];
        for (int i = 0; i < size; i++) {
            int index = i + fromIndex;
            if (index >= size) index -= size;
            double profit = profits[index];
            for (int j = 0; j < size; j++) {
                index = i - j + fromIndex;
                if (index >= size) index -= size;
                if (index < 0) index += size;
                RouteEntry entry = route.get(index);
                if (!entry.isTransit() && entry.is(target)) {
                    break;
                }
                res[index] += profit;
            }
        }
        return res;
    }


    public static double[] getLostProfits(Route route, long count){
        List<RouteEntry> entries = route.getEntries();
        int size = entries.size();
        double[] res = new double[size];
        for (int i = 0; i < size; i++) {
            RouteEntry entry = entries.get(i);
            if (entry.isTransit()) continue;
            List<Order> orders = new ArrayList<>(entry.getFixedOrders());
            orders.sort((o1, o2) -> Double.compare(o1.getProfitByTonne(), o2.getProfitByTonne()));
            long empty = route.getCargo() - entry.getCargo();
            long need = count - empty;
            double profit = 0;
            for (Order order : orders) {
                if (need > 0) {
                    long reserved = Math.min(order.getCount(), need);
                    profit += reserved * order.getProfitByTonne();
                    need -= reserved;
                } else {
                    break;
                }
            }
            res[i] += profit;
        }
        return res;
    }

    private static double getLostProfit(int fromIndex, int toIndex, double[] lostProfits) {
        double res = 0;
        for (int i = 0; i < lostProfits.length; i++) {
            int index = i + fromIndex;
            if (index >= lostProfits.length) index -= lostProfits.length;
            if (index == toIndex && (i > 0 || fromIndex != toIndex)) {
                break;
            }
            res += lostProfits[index];
        }
        return res;
    }

    private static int getLastIndex(final int fromIndex, final Offer buyOffer, final Order[] sells, boolean isLoop){
        long need = buyOffer.getCount();
        for (int i = 0; i < sells.length; i++) {
            int index = i + fromIndex;
            if (index >= sells.length){
                if (isLoop) index -= sells.length;
                    else break;
            }
            Order sell = sells[index];
            if (sell == null) continue;
            if (sell.getCount() >= need) {
                return index;
            }
            need -= sell.getCount();
        }
        return -1;
    }

    public static RouteReserve getReserves(final Route route, final int fromIndex, final Vendor target, long count){
        int toIndex = route.find(target, fromIndex+1);
        return toIndex != -1 ? new RouteReserve(fromIndex, toIndex, count) : null;
    }

    public static Collection<RouteReserve> getReserves(final Route route, final int fromIndex, final Offer buyOffer){
        List<RouteEntry> entries = route.getEntries();
        int size = entries.size()-1;
        final double[] profits = getLostProfits(route, buyOffer.getCount());
        final Order[] orders = new Order[size];
        for (int i = 0; i < size; i++) {
            RouteEntry entry = entries.get(i);
            Offer sell = entry.getVendor().getSell(buyOffer.getItem());
            if (sell != null) {
                orders[i] = new Order(sell, buyOffer, route.getBalance(), buyOffer.getCount());
            }
        }

        class SortHelper {
            final RouteEntry entry;
            final int index;
            final int endIndex;
            final Order sell;
            final double profit;

            SortHelper(RouteEntry entry, int index) {
                this.entry = entry;
                this.index = index;
                this.sell = orders[index];
                int lastIndex = getLastIndex(index, buyOffer, orders, route.isLoop());
                if (lastIndex != -1) {
                    endIndex = route.find(buyOffer.getVendor(), lastIndex+1);
                    if (endIndex != -1) {
                        double lost = getLostProfit(index, endIndex, profits);
                        profit = sell != null ? lost - sell.getProfit() : lost;
                    } else {
                        profit = 0;
                    }
                } else {
                    endIndex = -1;
                    profit = 0;
                }
            }

            private boolean isSeller(){
                return sell != null && endIndex != -1 && (endIndex >= index || route.isLoop());
            }

            private double getProfit(){
                return profit;
            }
        }

        List<SortHelper> sortedEntries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            RouteEntry entry = entries.get(i);
            sortedEntries.add(new SortHelper(entry, i));
        }
        sortedEntries.sort((e1, e2) -> Double.compare(e1.getProfit(), e2.getProfit()));

        long need = buyOffer.getCount();
        double balance = route.getBalance();
        boolean checkIndex = !route.isLoop();
        Collection<RouteReserve> reserves = new ArrayList<>();
        for (SortHelper helper : sortedEntries) {
            if (checkIndex && fromIndex > helper.index) continue;
            RouteEntry entry = helper.entry;
            if (helper.isSeller()){
                Order order = new Order(helper.sell.getSell(), buyOffer, balance, need);
                RouteReserve reserve = new RouteReserve(order, helper.index, helper.endIndex);
                reserves.add(reserve);
                need -= order.getCount();
                if (need <= 0) break;
            }
            balance += entry.getProfit();
        }
        return reserves;
    }

    public static Collection<RouteReserve> changeReserves(final Route route, final int fromIndex, final Offer buyOffer, Collection<RouteReserve> oldReserves){
        List<RouteReserve> reserves = new ArrayList<>();
        int need = 0;
        for (RouteReserve r : oldReserves) {
            if (r.getFromIndex() > fromIndex){
                need += r.getOrder().getCount();
            }
        }
        int newEndIndex;
        if (need > 0){
            for (RouteReserve r : getReserves(route, fromIndex, buyOffer)) {
                if (need <= 0) break;
                if (r.getOrder().getCount() >= need){
                    r.getOrder().setCount(need);
                }
                reserves.add(r);
                need -= r.getOrder().getCount();
            }
            newEndIndex = reserves.get(0).getToIndex();
        } else {
            newEndIndex = route.find(buyOffer.getVendor(), fromIndex);
        }
        for (RouteReserve r : oldReserves) {
            if (r.getFromIndex() <= fromIndex){
                RouteReserve reserve = new RouteReserve(r.getOrder(), r.getFromIndex(), newEndIndex);
                reserves.add(reserve);
            }
        }
        return reserves;
    }
}