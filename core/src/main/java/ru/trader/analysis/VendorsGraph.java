package ru.trader.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.*;
import ru.trader.core.Order;
import ru.trader.core.TransitVendor;
import ru.trader.core.Vendor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
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
            return new VendorsEdge(vertex, target, refill, fuelCost);
        }
    }

    public class VendorsEdge extends ConnectibleEdge<Vendor> {
        private List<Order> orders;

        protected VendorsEdge(Vertex<Vendor> source, Vertex<Vendor> target, boolean refill, double fuel) {
            super(source, target, refill, fuel);
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
            int lands = !(target.getEntry() instanceof TransitVendor) ? 1 : 0;
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
        protected VendorsTraversalEntry start(Vertex<Vendor> vertex) {
            double balance = getProfile().getBalance();
            return new VendorsTraversalEntry(super.start(vertex), balance);
        }

        @Override
        protected VendorsTraversalEntry travers(final CostTraversalEntry entry, final Edge<Vendor> edge, final Vendor target) {
            VendorsEdge vEdge = (VendorsEdge) edge;
            CCostTraversalEntry ce = super.travers(entry, edge, target);
            return new VendorsTraversalEntry((VendorsTraversalEntry) entry, edge, ce.getFuel(), ((VendorsTraversalEntry)entry).balance + vEdge.getProfit());
        }

        protected class VendorsTraversalEntry extends CCostTraversalEntry {
            private final double balance;

            protected VendorsTraversalEntry(CCostTraversalEntry entry, double balance) {
                super(entry.getTarget(), entry.getFuel());
                this.balance = balance;
            }

            protected VendorsTraversalEntry(VendorsTraversalEntry head, Edge<Vendor> edge, double fuel, double balance) {
                super(head, edge, fuel);
                this.balance = balance;
            }

            @Override
            protected boolean check(Edge<Vendor> e) {
                boolean good = super.check(e);
                // remove transit cicles
                if (good && e.getSource().getEntry() instanceof TransitVendor && !(e.getTarget().getEntry() instanceof TransitVendor)){
                    Optional<Vendor> seller = getSeller();
                    good = seller.isPresent() && !e.getTarget().isEntry(seller.get());
                }
                return good;
            }

            @Override
            protected VendorsEdge wrap(Edge<Vendor> edge) {
                ConnectibleEdge<Vendor> cEdge = super.wrap(edge);
                Vendor buyer = edge.getTarget().getEntry();
                List<Order> orders = new ArrayList<>();
                orders.addAll(((VendorsEdge) edge).getOrders());
                if (edge.getSource().getEntry() instanceof TransitVendor && !(buyer instanceof TransitVendor)){
                    LOG.trace("{} is transit, search seller", edge.getSource().getEntry());
                    Optional<Vendor> seller = getSeller();
                    if (seller.isPresent()){
                        orders = MarketUtils.getOrders(seller.get(), buyer);
                    }
                }
                orders = MarketUtils.getStack(orders, balance, getShip().getCargo());
                VendorsEdge res = new VendorsEdge(edge.getSource(), edge.getTarget(), cEdge.isRefill(), cEdge.getFuel());
                res.setOrders(orders);
                return res;
            }

            private Optional<Vendor> getSeller(){
                Vendor res = null;
                Edge<Vendor> e = getEdge();
                Vendor seller = e.getSource().getEntry();
                if (!(seller instanceof TransitVendor)){
                    res = seller;
                } else {
                    for (int i = head.size() - 1; i >= 0; i--) {
                        e = head.getEdge();
                        seller = e.getSource().getEntry();
                        if (!(seller instanceof TransitVendor)){
                            res = seller;
                            break;
                        }
                    }
                }
                return Optional.ofNullable(res);
            }
        }


    }
}
