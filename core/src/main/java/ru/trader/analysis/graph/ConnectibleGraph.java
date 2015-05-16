package ru.trader.analysis.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.AnalysisCallBack;
import ru.trader.core.Profile;
import ru.trader.graph.Connectable;

import java.util.Collection;
import java.util.function.Predicate;

public class ConnectibleGraph<T extends Connectable<T>> extends Graph<T> {
    private final static Logger LOG = LoggerFactory.getLogger(ConnectibleGraph.class);

    private final Profile profile;

    public ConnectibleGraph(Profile profile) {
        super();
        this.profile = profile;
    }

    public ConnectibleGraph(Profile profile, AnalysisCallBack callback) {
        super(callback);
        this.profile = profile;
    }

    @Override
    protected GraphBuilder createGraphBuilder(Vertex<T> vertex, Collection<T> set, int deep, double limit) {
        return new ConnectibleGraphBuilder(vertex, set, deep, limit);
    }

    public void build(T start, Collection<T> set){
        super.build(start, set, profile.getJumps(), profile.getShip().getTank());
    }

    private class DistanceFilter implements Predicate<Double> {
        private final double limit;
        private final T source;

        private DistanceFilter(double limit, T source) {
            this.limit = limit;
            this.source = source;
        }

        @Override
        public boolean test(Double distance) {
            return distance <= profile.getShip().getJumpRange(limit) || (profile.withRefill() && distance <= profile.getShip().getJumpRange() && source.canRefill());
        }
    }

    private class ConnectibleGraphBuilder extends GraphBuilder {
        private final DistanceFilter distanceFilter;
        protected boolean refill;

        private ConnectibleGraphBuilder(Vertex<T> vertex, Collection<T> set, int deep, double limit) {
            super(vertex, set, deep, limit);
            distanceFilter = new DistanceFilter(limit, vertex.getEntry());
        }

        @Override
        protected double onConnect(T entry) {
            double distance = vertex.getEntry().getDistance(entry);
            if (!distanceFilter.test(distance)){
                LOG.trace("Vertex {} is far away, {}", entry, distance);
                return -1;
            }
            double costFuel = profile.getShip().getFuelCost(limit, distance);
            double nextLimit = profile.withRefill() ? limit - costFuel : profile.getShip().getTank();
            if (nextLimit < 0) {
                LOG.trace("Refill");
                refill = true;
                nextLimit = profile.getShip().getTank() - profile.getShip().getFuelCost(distance);
            } else {
                refill = false;
            }
            return nextLimit;
        }

        @Override
        protected ConnectibleEdge createEdge(Vertex<T> target) {
            return new ConnectibleEdge(vertex, target, refill);
        }
    }

    protected class ConnectibleEdge extends Edge<T> {
        private final boolean refill;

        protected ConnectibleEdge(Vertex<T> source, Vertex<T> target, boolean refill) {
            super(source, target);
            this.refill = refill;
        }

        public boolean isRefill() {
            return refill;
        }

        @Override
        protected double computeWeight() {
            T s = source.getEntry();
            T t = target.getEntry();
            return s.getDistance(t);
        }

        @Override
        public String toString() {
            return source.getEntry().toString() + " - "+ weight
                   + (refill ? "R" : "")
                   +" -> " + target.getEntry().toString();
        }
    }
}
