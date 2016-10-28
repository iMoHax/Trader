package ru.trader.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Market;
import ru.trader.core.Place;
import ru.trader.core.StarSystemFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PowerPlayAnalyzator {
    private final static Logger LOG = LoggerFactory.getLogger(PowerPlayAnalyzator.class);
    private final static double CONTROLLING_RADIUS = 15;

    private final Market market;
    private StarSystemFilter starSystemFilter;

    public PowerPlayAnalyzator(Market market) {
        this.market = market;
    }

    public StarSystemFilter getStarSystemFilter() {
        return starSystemFilter;
    }

    public void setStarSystemFilter(StarSystemFilter starSystemFilter) {
        this.starSystemFilter = starSystemFilter;
    }

    public Collection<Place> getNear(Collection<Place> starSystems, Collection<Place> centers, double radius, double maxDistance){
        return starSystems.stream()
                .filter(new FarDropper(centers, maxDistance))
                .filter(intersectsAnyPredicate(centers, radius).negate())
                .sorted(new DistanceComparator(centers))
                .collect(Collectors.toList());
    }




    public Collection<Place> getIntersects(Place checkedSystem, Collection<Place> starSystems, Collection<Place> centers, double radius){
        return starSystems.stream()
                .filter(new FarDropper(centers, radius))
                .filter(intersectsPredicate(checkedSystem, centers, radius))
                .collect(Collectors.toList());
    }

    public Collection<Place> getIntersects(Collection<Place> starSystems, Collection<Place> centers, double radius){
        return starSystems.stream()
                .filter(new FarDropper(centers, radius))
                .filter(intersectsPredicate(centers, radius))
                .collect(Collectors.toList());
    }

    private Predicate<Place> intersectsAnyPredicate(Collection<Place> places, double radius){
        Predicate<Place> intersects = null;
        for (Place place : places) {
            if (intersects == null) intersects = new Controlling(place, radius);
            else intersects = intersects.or(new Controlling(place, radius));
        }
        return intersects;
    }


    private Predicate<Place> intersectsPredicate(Collection<Place> places, double radius){
        Predicate<Place> intersects = null;
        for (Place place : places) {
            if (intersects == null) intersects = new Controlling(place, radius);
            else intersects = intersects.and(new Controlling(place, radius));
        }
        return intersects;
    }

    private Predicate<Place> intersectsPredicate(Place checkedPlace, Collection<Place> places, double radius){
        return new Controlling(checkedPlace, radius).and(intersectsAnyPredicate(places, radius));
    }

    private class Controlling implements Predicate<Place> {
        private final Place center;
        private final double radius;

        private Controlling(Place center, double radius) {
            this.center = center;
            this.radius = radius;
        }

        @Override
        public boolean test(Place place) {
            double distance = center.getDistance(place);
            LOG.trace("Check {}, distance to {} = {}, radius = {}", place, center, distance, radius);
            return distance <= radius;
        }
    }


    private class FarDropper implements Predicate<Place> {
        private double minX;
        private double maxX;
        private double minY;
        private double maxY;
        private double minZ;
        private double maxZ;

        private FarDropper(Collection<Place> centers, double radius) {
            minX = Double.NaN; maxX = Double.NaN;
            minY = Double.NaN; maxY = Double.NaN;
            minZ = Double.NaN; maxZ = Double.NaN;
            for (Place center : centers) {
                if (Double.isNaN(minX) || minX > center.getX()) minX = center.getX();
                if (Double.isNaN(minY) || minY > center.getY()) minY = center.getY();
                if (Double.isNaN(minZ) || minZ > center.getZ()) minZ = center.getZ();
                if (Double.isNaN(maxX) || maxX < center.getX()) maxX = center.getX();
                if (Double.isNaN(maxY) || maxY < center.getY()) maxY = center.getY();
                if (Double.isNaN(maxZ) || maxZ < center.getZ()) maxZ = center.getZ();
            }
            minX -= radius;
            minY -= radius;
            minZ -= radius;
            maxX += radius;
            maxY += radius;
            maxZ += radius;
        }

        @Override
        public boolean test(Place place) {
            boolean res = place.getX() < minX || place.getX() > maxX
                || place.getY() < minY || place.getY() > maxY
                || place.getZ() < minZ || place.getZ() > maxZ;
            LOG.trace("Test {}, dropper = {}, faraway = {}", place, this, res);
            return !res;
        }

        @Override
        public String toString() {
            return "FarDropper{" +
                    "minX=" + minX +
                    ", maxX=" + maxX +
                    ", minY=" + minY +
                    ", maxY=" + maxY +
                    ", minZ=" + minZ +
                    ", maxZ=" + maxZ +
                    '}';
        }
    }

    private class DistanceComparator implements Comparator<Place> {
        private final Collection<Place> centers;
        private final HashMap<Place, Double> distances;


        private DistanceComparator(Collection<Place> centers) {
            this.centers = centers;
            distances = new HashMap<>(100);

        }

        private double getMinDistance(Place place){
            Double distance = distances.get(place);
            if (distance != null) return distance;

            Collection<Double> ds = new LimitedQueue<>(3, Comparator.naturalOrder());
            for (Place center : centers) {
                double d = center.getDistance(place);
                ds.add(d);
            }
            double dist = 0;
            for (Double d : ds) {
                dist += d;
            }
            distances.put(place, dist);
            return dist;
        }

        @Override
        public int compare(Place o1, Place o2) {
            return Double.compare(getMinDistance(o1), getMinDistance(o2));
        }
    }

}

