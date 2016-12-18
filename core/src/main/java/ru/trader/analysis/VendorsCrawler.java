package ru.trader.analysis;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.*;
import ru.trader.core.Order;
import ru.trader.core.SERVICE_TYPE;
import ru.trader.core.STATION_TYPE;
import ru.trader.core.Vendor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VendorsCrawler extends Crawler<Vendor> {
    private final static Logger LOG = LoggerFactory.getLogger(VendorsCrawler.class);

    private double startFuel;
    private double startBalance;
    private final VendorsCrawlerSpecification specification;

    public VendorsCrawler(VendorsGraph graph, VendorsCrawlerSpecification specification, AnalysisCallBack callback) {
        super(graph, specification, callback);
        this.specification = specification;
        startFuel = graph.getProfile().getShip().getTank();
        startBalance = graph.getProfile().getBalance();
    }

    private Scorer getScorer(){
        return ((VendorsGraph)graph).getScorer();
    }

    public void setStartFuel(double startFuel) {
        this.startFuel = startFuel;
    }

    public void setStartBalance(double startBalance) {
        this.startBalance = startBalance;
    }

    @Override
    protected VendorsTraversalEntry start(Vertex<Vendor> vertex) {
        return new VendorsTraversalEntry(super.start(vertex), startFuel, startBalance);
    }

    @Override
    protected VendorsTraversalEntry travers(final CostTraversalEntry entry, final Edge<Vendor> edge) {
        VendorsTraversalEntry vEntry = (VendorsTraversalEntry)entry;
        VendorsEdge vEdge = (VendorsEdge) edge;
        return new VendorsTraversalEntry(vEntry, vEdge);
    }

    protected class VendorsTraversalEntry extends CostTraversalEntry {
        private final double fuel;
        private final double balance;
        private Long time;

        protected VendorsTraversalEntry(CostTraversalEntry entry, double fuel, double balance) {
            super(entry.getTarget());
            this.fuel = fuel;
            this.balance = balance;
        }

        protected VendorsTraversalEntry(VendorsTraversalEntry head, VendorsEdge edge) {
            super(head, edge);
            this.balance = head.balance + edge.getProfit();
            this.fuel = edge.getRemain();
        }

        @Override
        public List<Edge<Vendor>> collect(Collection<Edge<Vendor>> src) {
            return src.stream().filter(this::check).map(this::wrap).filter(e -> e != null).collect(Collectors.toList());
        }

        protected boolean check(Edge<Vendor> e){
            VendorsGraph.VendorsBuildEdge edge = (VendorsGraph.VendorsBuildEdge) e;
            return fuel <= edge.getMaxFuel() && (fuel >= edge.getMinFuel() || edge.getSource().getEntry().canRefill())
                   && (edge.getProfit() > 0 || VendorsCrawler.this.isContent(edge, this)
                       // adding all edges if this start entry and don't have market
                       || (isStart() && !MarketUtils.hasMarket(vertex.getEntry()))
                       );
        }

        protected VendorsEdge wrap(Edge<Vendor> e) {
            VendorsGraph.VendorsBuildEdge edge = (VendorsGraph.VendorsBuildEdge) e;
            Path<Vendor> path = edge.getPath(fuel);
            if (path == null) return null;
            VendorsEdge res;
            try {
                res = new VendorsEdge(edge.getSource(), edge.getTarget(), new TransitPath(path, fuel));
            } catch (IllegalStateException ex){
                LOG.error("Wrong path, entry {}, fuel = {}", this, fuel);
                return null;
            }
            List<Order> orders = Collections.emptyList();
            if (edge.getSource().getEntry().has(SERVICE_TYPE.MARKET) || !edge.getTarget().getEntry().has(SERVICE_TYPE.MARKET)){
                orders = edge.getOrders();
            } else {
                Vendor seller = findMarket();
                if (seller != null) {
                    orders = getScorer().getOrders(seller, edge.getTarget().getEntry());
                }
            }
            res.setOrders(MarketUtils.getStack(orders, balance, getScorer().getProfile().getShip().getCargo()));
            return res;
        }

        @Override
        public double getWeight() {
            if (weight == null){
                weight = specification.computeWeight(this);
            }
            return weight;
        }

        @Override
        public long getTime() {
            if (time == null){
                time = super.getTime();
            }
            return time;
        }

        private Vendor findMarket(){
            Optional<Traversal<Vendor>> head = getHead();
            while (head.isPresent()) {
                Traversal<Vendor> curr = head.get();
                Vendor vendor = curr.getTarget().getEntry();
                if (vendor.has(SERVICE_TYPE.MARKET)) return vendor;
                head = curr.getHead();
            }
            return null;
        }
    }

    public class VendorsEdge extends ConnectibleEdge<Vendor> {
        private TransitPath  path;
        private List<Order> orders;
        private Double profitByTonne;
        private Long time;

        protected VendorsEdge(Vertex<Vendor> source, Vertex<Vendor> target, TransitPath path) {
            super(source, target);
            if (path == null) throw new IllegalArgumentException("Path must be no-null");
            this.path = path;
        }

        protected void setOrders(List<Order> orders){
            this.orders = orders;
        }

        public double getProfit(){
            return getOrders().stream().mapToDouble(Order::getProfit).sum();
        }

        public List<Order> getOrders(){
            if (orders == null){
                Vendor seller = source.getEntry();
                Vendor buyer = target.getEntry();
                orders = getScorer().getOrders(seller, buyer);
            }
            return orders;
        }

        public double getRemain() {
            return path.getRemain();
        }

        @Override
        public boolean isRefill() {
            return path.isRefill();
        }

        @Override
        public double getFuelCost() {
            if (path != null){
                return path.getFuelCost();
            }
            return super.getFuelCost();
        }

        public TransitPath getPath() {
            return path;
        }

        public double getProfitByTonne() {
            if (profitByTonne == null){
                profitByTonne = computeProfit();
            }
            return profitByTonne;
        }

        @Override
        public long getTime() {
            if (time == null){
                time = computeTime();
            }
            return time;
        }

        @Override
        public int compareTo(@NotNull Edge other) {
            double w = getWeight();
            double ow = other.getWeight();
            if (ow >= 0 && w >= 0) return super.compareTo(other);
            if (w < 0 && ow < 0) return Double.compare(Math.abs(w), Math.abs(ow));
            return w < 0 ? 1 : -1;
        }

        protected double computeProfit(){
            return getScorer().getProfitByTonne(getProfit(), getFuelCost());
        }

        protected long computeTime(){
            int jumps = source.getEntry().getPlace().equals(target.getEntry().getPlace())? 0 : 1;
            int lands = 1;
            STATION_TYPE t = target.getEntry().getType();
            int planetLands = t != null && t.isPlanetary() ? 1 : 0;
            if (path != null){
                jumps = path.size();
                lands += path.getRefillCount();
                //not lands if refuel on this station
                if (path.isRefill()) lands--;
            } else {
                lands += isRefill() ? 1 :0;
            }
            return getScorer().getTime(target.getEntry().getDistance(), jumps, lands, planetLands);
        }

        @Override
        protected double computeWeight() {
            return specification.computeWeight(this);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof VendorsEdge)) return false;
            if (!super.equals(o)) return false;
            VendorsEdge edge = (VendorsEdge) o;
            return !(path != null ? !path.equals(edge.path) : edge.path != null);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (path != null ? path.hashCode() : 0);
            return result;
        }

    }
}
