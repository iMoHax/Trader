package ru.trader.analysis.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.AnalysisCallBack;
import ru.trader.core.Profile;
import ru.trader.core.Ship;
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
    
    protected Ship getShip(){
        return profile.getShip();
    }

    @Override
    protected GraphBuilder createGraphBuilder(Vertex<T> vertex, Collection<T> set, int deep, double limit) {
        return new ConnectibleGraphBuilder(vertex, set, deep, limit);
    }

    public void build(T start, Collection<T> set){
        super.build(start, set, profile.getJumps(), getShip().getTank());
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
            return distance <= getShip().getJumpRange(limit) || (profile.withRefill() && distance <= getShip().getJumpRange() && source.canRefill());
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
            fuelCost = getShip().getFuelCost(limit, distance);
            double nextLimit = profile.withRefill() ? limit - fuelCost : getShip().getTank();
            if (nextLimit < 0) {
                LOG.trace("Refill");
                refill = true;
                fuelCost = getShip().getFuelCost(distance);
                nextLimit = getShip().getTank() - fuelCost;
            } else {
                refill = false;
            }
            return nextLimit;
        }

        @Override
        protected ConnectibleEdge<T> createEdge(Vertex<T> target) {
            ConnectibleEdge<T> res = new ConnectibleEdge<>(vertex, target);
            res.setRefill(refill);
            res.setFuelCost(fuelCost);
            return res;
        }
    }

}
