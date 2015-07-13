package ru.trader.analysis.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.AnalysisCallBack;
import ru.trader.core.Profile;
import ru.trader.core.Ship;
import ru.trader.graph.Connectable;

import java.util.Collection;

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

    protected class ConnectibleGraphBuilder extends GraphBuilder {
        protected double minFuel;
        protected double maxFuel;
        protected double distance;

        protected ConnectibleGraphBuilder(Vertex<T> vertex, Collection<T> set, int deep, double limit) {
            super(vertex, set, deep, limit);
        }

        @Override
        protected double onConnect(T entry) {
            distance = vertex.getEntry().getDistance(entry);
            if (distance > getShip().getMaxJumpRange()){
                LOG.trace("Vertex {} is far away, {}", entry, distance);
                return -1;
            }
            maxFuel = getShip().getMaxFuel(distance);
            minFuel = getShip().getMinFuel(distance);
            double fuel = getProfile().withRefill() ? vertex.getEntry().canRefill() ? getShip().getRoundMaxFuel(distance) : limit : getShip().getTank();
            double fuelCost = getShip().getFuelCost(fuel, distance);
            return fuel - fuelCost;
        }

        @Override
        protected BuildEdge createEdge(Vertex<T> target) {
            BuildEdge res = new BuildEdge(vertex, target);
            res.setFuel(minFuel, maxFuel);
            res.setDistance(distance);
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
}
