package ru.trader.analysis.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.AnalysisCallBack;
import ru.trader.core.Profile;
import ru.trader.core.Ship;

import java.util.Collection;
import java.util.stream.Collectors;

public class ConnectibleGraph<T extends Connectable<T>> extends AbstractGraph<T> {
    private final static Logger LOG = LoggerFactory.getLogger(ConnectibleGraph.class);

    protected final Profile profile;

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

    @Override
    protected Collection<T> filtered(Collection<T> set) {
        final double maxDistance = getShip().getMaxJumpRange() * profile.getJumps();
        return set.parallelStream().filter(v -> root.getEntry().getDistance(v) <= maxDistance).collect(Collectors.toList());
    }

    protected class ConnectibleGraphBuilder extends GraphBuilder {

        protected ConnectibleGraphBuilder(Vertex<T> vertex, Collection<T> set, int deep, double limit) {
            super(vertex, set, deep, limit);
        }

        @Override
        protected BuildHelper<T> createHelper(T entry) {
            double distance = vertex.getEntry().getDistance(entry);
            if (distance > getShip().getMaxJumpRange()){
                LOG.trace("Vertex {} is far away, {}", entry, distance);
                return new BuildHelper<>(entry,-1);
            }
            double maxFuel = getShip().getMaxFuel(distance);
            double minFuel = getShip().getMinFuel(distance);
            double fuel = getProfile().withRefill() ? vertex.getEntry().canRefill() ? getShip().getRoundMaxFuel(distance) : limit : getShip().getTank();
            double fuelCost = getShip().getFuelCost(fuel, distance);
            double nextLimit = getProfile().withRefill() ? fuel - fuelCost : fuel;
            return new CBuildHelper<>(entry, nextLimit, minFuel, maxFuel, distance);
        }

        @Override
        protected BuildEdge createEdge(BuildHelper<T> helper, Vertex<T> target) {
            CBuildHelper h = (CBuildHelper) helper;
            BuildEdge res = new BuildEdge(vertex, target);
            res.setFuel(h.minFuel, h.maxFuel);
            res.setDistance(h.distance);
            return res;
        }
    }

    public class BuildEdge extends Edge<T> {
        private double distance;
        private double minFuel;
        private double maxFuel;

        public BuildEdge(Vertex<T> source, Vertex<T> target) {
            super(source, target);
        }

        public double getMinFuel() {
            return minFuel;
        }

        public double getMaxFuel() {
            return maxFuel;
        }

        public double getRoundMaxFuel() {
            return getShip().getRoundFuel(maxFuel);
        }

        public void setFuel(double minFuel, double maxFuel) {
            this.minFuel = minFuel;
            this.maxFuel = maxFuel;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }

        public double getDistance() {
            return distance;
        }

        public double getFuelCost(double fuel){
            return getProfile().withRefill() ? getShip().getFuelCost(fuel, distance) : 0;
        }

        public double getRefill(){
            return getShip().getRoundMaxFuel(distance);
        }

        public Ship getShip(){
            return ConnectibleGraph.this.getShip();
        }

        @Override
        protected double computeWeight() {
            return distance;
        }

        @Override
        public String toString() {
            return source.getEntry().toString() + " - "+ weight
                    +"("+minFuel + " - " + maxFuel + ")"
                    +" -> " + target.getEntry().toString();
        }

    }

    public class CBuildHelper<T> extends BuildHelper<T> {
        private final double minFuel;
        private final double maxFuel;
        private final double distance;

        private CBuildHelper(T entry, double nextLimit, double minFuel, double maxFuel, double distance) {
            super(entry, nextLimit);
            this.minFuel = minFuel;
            this.maxFuel = maxFuel;
            this.distance = distance;
        }


    }
}
