package ru.trader.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.*;
import ru.trader.core.Order;
import ru.trader.core.TransitVendor;
import ru.trader.core.Vendor;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;


public class VendorsGraph extends ConnectibleGraph<Vendor> {
    private final static Logger LOG = LoggerFactory.getLogger(VendorsGraph.class);

    private final Scorer scorer;

    public VendorsGraph(Scorer scorer) {
        super(scorer.getProfile());
        this.scorer = scorer;
    }

    public VendorsGraph(Scorer scorer, AnalysisCallBack callback) {
        super(scorer.getProfile(), callback);
        this.scorer = scorer;
    }

    public VendorsCrawler crawler(Consumer<List<Edge<Vendor>>> onFoundFunc){
        return new VendorsCrawler(onFoundFunc);
    }

    @Override
    protected GraphBuilder createGraphBuilder(Vertex<Vendor> vertex, Collection<Vendor> set, int deep, double limit) {
        return new VendorsGraphBuilder(vertex, set, deep, limit);
    }

    protected class VendorsGraphBuilder extends ConnectibleGraphBuilder {
        protected VendorsGraphBuilder(Vertex<Vendor> vertex, Collection<Vendor> set, int deep, double limit) {
            super(vertex, set, deep, limit);
        }

        @Override
        protected double onConnect(Vendor buyer) {
            double nextlimit = super.onConnect(buyer);
            Vendor seller = vertex.getEntry();
            if (nextlimit > 0){
                if (buyer instanceof TransitVendor && seller.getPlace().equals(buyer.getPlace())) nextlimit = -1;
                if (seller instanceof TransitVendor && seller.getPlace().equals(buyer.getPlace())) nextlimit = -1;
            }
            return nextlimit;
        }

        @Override
        protected ConnectibleEdge<Vendor> createEdge(Vertex<Vendor> target) {
            return new VendorsEdge(vertex, target, refill, fuelCost, false);
        }
    }

    public class VendorsEdge extends ConnectibleEdge<Vendor> {
        private List<Order> orders;
        private boolean isTarget;

        protected VendorsEdge(Vertex<Vendor> source, Vertex<Vendor> target, boolean refill, double fuel, boolean isTarget) {
            super(source, target, refill, fuel);
            this.isTarget = isTarget;
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
                orders = MarketUtils.getOrders(seller, buyer);
            }
            return orders;
        }

        @Override
        protected double computeWeight() {
            int jumps = source.getEntry().getPlace().equals(target.getEntry().getPlace())? 0 : 1;
            int lands = refill || !orders.isEmpty() || isTarget ? 1 : 0;
            boolean transit = lands == 0 && source.getEntry() instanceof TransitVendor || target.getEntry() instanceof TransitVendor;
            double profit = getProfit();
            double score = transit ? scorer.getTransitScore(fuel) :
                           scorer.getScore(target.getEntry(), profit, jumps, lands, fuel);
            score = scorer.getMaxScore() - score;
            if (score < 0)
                score = 0;
            return score;
        }
    }

    public class VendorsCrawler extends CCrawler<Vendor> {
        protected VendorsCrawler(Consumer<List<Edge<Vendor>>> onFoundFunc) {
            super(VendorsGraph.this, onFoundFunc);
        }

        @Override
        protected CostTraversalEntry start(Vertex<Vendor> vertex) {
            double balance = scorer.getProfile().getBalance();
            return new VendorsTraversalEntry((CCostTraversalEntry) super.start(vertex), balance);
        }

        @Override
        protected CostTraversalEntry travers(final CostTraversalEntry entry, final List<Edge<Vendor>> head, final Edge<Vendor> edge, final Vendor target) {
            VendorsTraversalEntry ve = (VendorsTraversalEntry)entry;
            double balance = ve.balance;
            Vendor buyer = edge.getTarget().getEntry();
            List<Order> orders = ((VendorsEdge) edge).getOrders();
            if (edge.getSource().getEntry() instanceof TransitVendor &&
                !(buyer instanceof TransitVendor)){
                LOG.trace("{} is transit, search seller", edge.getSource().getEntry());
                for (int i = head.size() - 1; i >= 0; i--) {
                    Vendor seller = head.get(i).getSource().getEntry();
                    if (!(seller instanceof TransitVendor)){
                        orders = MarketUtils.getOrders(seller, buyer);
                        break;
                    }
                }
            }
            orders = MarketUtils.getStack(orders, balance, scorer.getProfile().getShip().getCargo());

            CCostTraversalEntry ce = (CCostTraversalEntry) super.travers(entry, head, edge, target);
            ConnectibleEdge<Vendor> cedge = (ConnectibleEdge<Vendor>) ce.getEdge();
            VendorsEdge addingEdge = new VendorsEdge(cedge.getSource(), cedge.getTarget(), cedge.isRefill(), cedge.getFuel(), target.equals(buyer));
            addingEdge.setOrders(orders);
            return new VendorsTraversalEntry(head, addingEdge, entry.getWeight(), ce.getFuel(), balance+addingEdge.getProfit());
        }

        protected class VendorsTraversalEntry extends CCostTraversalEntry {
            private final double balance;

            protected VendorsTraversalEntry(CCostTraversalEntry entry, double balance) {
                super(entry.getHead(), entry.getVertex(), entry.getFuel());
                this.balance = balance;
            }

            protected VendorsTraversalEntry(List<Edge<Vendor>> head, Edge<Vendor> edge, double cost, double fuel, double balance) {
                super(head, edge, cost, fuel);
                this.balance = balance;
            }

        }


    }
}
