package ru.trader.analysis.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.AnalysisCallBack;
import ru.trader.core.Profile;
import ru.trader.graph.Connectable;

import java.util.Collection;
import java.util.function.Predicate;

public class ConnectibleGraph<T extends Connectable<T>> extends AbstractGraph<T> {
    private final static Logger LOG = LoggerFactory.getLogger(ConnectibleGraph.class);

    protected final Profile profile;

    public ConnectibleGraph(Profile profile) {
        super();
        this.profile = profile;
    }

    public ConnectibleGraph(Profile profile, AnalysisCallBack callback) {
        super(callback);
        this.profile = profile;
    }

    public Profile getProfile() {
        return profile;
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

    protected class ConnectibleGraphBuilder extends GraphBuilder {
        private final DistanceFilter distanceFilter;
        protected boolean refill;
        protected double fuelCost;

        protected ConnectibleGraphBuilder(Vertex<T> vertex, Collection<T> set, int deep, double limit) {
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
            fuelCost = profile.getShip().getFuelCost(limit, distance);
            double nextLimit = profile.withRefill() ? limit - fuelCost : profile.getShip().getTank();
            if (nextLimit < 0) {
                LOG.trace("Refill");
                refill = true;
                fuelCost = profile.getShip().getFuelCost(distance);
                nextLimit = profile.getShip().getTank() - fuelCost;
            } else {
                refill = false;
            }
            return nextLimit;
        }

        @Override
        protected ConnectibleEdge<T> createEdge(Vertex<T> target) {
            return new ConnectibleEdge<>(vertex, target, refill, fuelCost);
        }
    }

}
