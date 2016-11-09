package ru.trader.analysis;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PowerPlayAnalyzator {
    private final static Logger LOG = LoggerFactory.getLogger(PowerPlayAnalyzator.class);

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

    public Collection<IntersectData> getControlling(Place starSystem){
        Stream<Place> candidates = market.get().stream().filter(p -> p.getFaction() != FACTION.NONE);
        return getControlling(starSystem, candidates, Market.CONTROLLING_RADIUS).collect(Collectors.toList());
    }

    public Collection<IntersectData> getIntersects(Collection<Place> starSystems){
        return getIntersects(market.get(), starSystems, Market.CONTROLLING_RADIUS);
    }

    public Collection<IntersectData> getIntersects(Place starSystem, Collection<Place> starSystems){
        Stream<Place> candidates = market.get().stream().filter(p -> p.getFaction() != FACTION.NONE);
        return getIntersects(starSystem, candidates, starSystems, Market.CONTROLLING_RADIUS).collect(Collectors.toList());
    }

    public Collection<IntersectData> getNear(Collection<Place> starSystems){
        Stream<Place> candidates = market.get().stream().filter(p -> p.getFaction() != FACTION.NONE);
        return getNear(candidates, starSystems, Market.CONTROLLING_RADIUS, Market.CONTROLLING_RADIUS*2).collect(Collectors.toList());
    }

    public Collection<IntersectData> getNearExpansions(Collection<Place> starSystems){
        return getNearExpansions(market.get(), starSystems, Market.CONTROLLING_RADIUS * 2);
    }

    public static Collection<IntersectData> getControlling(Place starSystem, Collection<Place> starSystems, double radius){
        return getControlling(starSystem, starSystems.stream(), radius).collect(Collectors.toList());
    }

    public static Stream<IntersectData> getControlling(Place starSystem, Stream<Place> starSystems, double radius){
        IntersectsMapper controllingMapper = new IntersectsMapper(Collections.singleton(starSystem), radius, true, true);
        return starSystems
                .map(controllingMapper)
                .filter(IntersectData::isIntersect);
    }


    public static Collection<IntersectData> getNear(Collection<Place> starSystems, Collection<Place> centers, double radius, double maxDistance){
        return getNear(starSystems.stream(), centers, radius, maxDistance).collect(Collectors.toList());
    }

    public static Stream<IntersectData> getNear(Stream<Place> starSystems, Collection<Place> centers, double radius, double maxDistance){
        IntersectsMapper distanceMapper = new IntersectsMapper(centers, maxDistance, false, true);
        Predicate<Place> isControlling = intersectsAnyPredicate(centers, radius);

        Collection<Place> candidates = new ArrayList<>();
        Collection<Place> checked = new ArrayList<>();
        starSystems.filter(new FarDropper(centers, maxDistance))
                .forEach(p -> {
                    if (isControlling != null && isControlling.test(p)) checked.add(p);
                    else{
                        if (p.getPower() == POWER.NONE) {
                            candidates.add(p);
                        }
                    }
                });
        candidates.removeAll(centers);
        Predicate<Place> isCheckedControlling = intersectsAnyPredicate(checked, radius);
        if (isCheckedControlling == null) isCheckedControlling = p -> false;

        return candidates.stream()
                .filter(isCheckedControlling.negate())
                .map(distanceMapper)
                .filter(IntersectData::isIntersect)
                .sorted(new DistanceComparator());
    }

    public static Collection<IntersectData> getNearExpansions(Collection<Place> starSystems, Collection<Place> centers, double maxDistance){
        return getNearExpansions(starSystems.stream(), centers, maxDistance).collect(Collectors.toList());
    }

    public static Stream<IntersectData> getNearExpansions(Stream<Place> starSystems, Collection<Place> centers,  double maxDistance){
        IntersectsMapper mapper = new IntersectsMapper(centers, maxDistance, false, true);
        return starSystems.filter(new FarDropper(centers, maxDistance))
                .filter(p -> p.getPowerState() == POWER_STATE.EXPANSION)
                .map(mapper)
                .sorted(new DistanceComparator());
    }


    public static Collection<IntersectData> getIntersects(Place checkedSystem, Collection<Place> starSystems, Collection<Place> centers, double radius){
        return getIntersects(checkedSystem, starSystems.stream(), centers, radius).collect(Collectors.toList());
    }

    public static Stream<IntersectData> getIntersects(Place checkedSystem, Stream<Place> starSystems, Collection<Place> centers, double radius){
        IntersectsMapper mapper = new IntersectsMapper(centers, radius, true, true);
        return starSystems
                .filter(new FarDropper(centers, radius))
                .filter(new Controlling(checkedSystem, radius))
                .map(mapper)
                .filter(IntersectData::isIntersect);
    }

    public static Collection<IntersectData> getIntersects(Collection<Place> starSystems, Collection<Place> centers, double radius){
        return getIntersects(starSystems.stream(), centers, radius).collect(Collectors.toList());
    }

    public static Stream<IntersectData> getIntersects(Stream<Place> starSystems, Collection<Place> centers, double radius){
        IntersectsMapper mapper = new IntersectsMapper(centers, radius, false, false);
        final int needCount = centers.size();
        return starSystems
                .filter(new FarDropper(centers, radius))
                .map(mapper)
                .filter(d -> d.getCount() == needCount);
    }

    @Nullable
    private static Predicate<Place> intersectsAnyPredicate(Collection<Place> places, double radius){
        Predicate<Place> intersects = null;
        for (Place place : places) {
            if (intersects == null) intersects = new Controlling(place, radius);
            else intersects = intersects.or(new Controlling(place, radius));
        }
        return intersects;
    }

    private static class Controlling implements Predicate<Place> {
        private final Place center;
        private final double radius;

        private Controlling(Place center, double radius) {
            this.center = center;
            this.radius = radius;
        }

        @Override
        public boolean test(Place place) {
            if (place == center) return false;
            double distance = center.getDistance(place);
            LOG.trace("Check {}, distance to {} = {}, radius = {}", place, center, distance, radius);
            return distance <= radius;
        }
    }


    private static class FarDropper implements Predicate<Place> {
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

    private static class DistanceComparator implements Comparator<IntersectData> {


        private double getMinDistance(IntersectData data){
            ControllingData[] contollings = data.contollings;
            double dist = 0;
            int count = Math.min(3, contollings.length);
            for (int i = 0; i < count; i++) {
                dist += contollings[i].distance;
            }
            return dist/count;
        }

        @Override
        public int compare(IntersectData o1, IntersectData o2) {
            return Double.compare(getMinDistance(o1), getMinDistance(o2));
        }
    }


    private static class IntersectsMapper implements Function<Place, IntersectData> {
        private final Collection<Place> centers;
        private final double radius;
        private final boolean findAny;
        private final boolean checkedAll;

        private IntersectsMapper(Collection<Place> centers, double radius, boolean findAny, boolean checkedAll) {
            this.centers = new ArrayList<>(centers);
            this.radius = radius;
            this.findAny = findAny;
            this.checkedAll = findAny || checkedAll;
        }

        @Override
        public IntersectData apply(Place place) {
            Collection<ControllingData> controllingDatas = null;
            for (Place center : centers) {
                if (place == center) continue;
                double distance = center.getDistance(place);
                LOG.trace("Check {}, distance to {} = {}, radius = {}", place, center, distance, radius);
                if (distance <= radius){
                    if (findAny){
                        return new IntersectData(place, center, distance);
                    }
                    if (controllingDatas == null) controllingDatas = new ArrayList<>(3);
                    controllingDatas.add(new ControllingData(center, distance));
                } else {
                    if (!checkedAll) break;
                }
            }
            if (controllingDatas == null){
                return new IntersectData(place);
            }
            return new IntersectData(place, controllingDatas);
        }
    }


    public static class IntersectData {
        private final Place starSystem;
        private final ControllingData[] contollings;

        public IntersectData(Place starSystem) {
            this.starSystem = starSystem;
            this.contollings = new ControllingData[0];
        }


        public IntersectData(Place starSystem, Place control, double distance) {
            this.starSystem = starSystem;
            this.contollings = new ControllingData[]{new ControllingData(control,distance)};
        }

        public IntersectData(Place starSystem, Collection<ControllingData> controlling) {
            this.starSystem = starSystem;
            this.contollings = controlling.toArray(new ControllingData[controlling.size()]);
            Arrays.sort(contollings);
        }

        public Place getStarSystem() {
            return starSystem;
        }

        public Collection<ControllingData> getControllingSystems(){
            return Arrays.asList(contollings);
        }

        public int getCount(){
            return contollings.length;
        }

        public double getMinDistance(){
            return contollings.length > 0 ? contollings[0].distance : Double.NaN;
        }

        public boolean isIntersect(){
            return contollings.length > 0;
        }
    }

    public static class ControllingData implements Comparable<ControllingData> {
        private final Place center;
        private final Double distance;

        public ControllingData(Place center, Double distance) {
            this.center = center;
            this.distance = distance;
        }

        public Place getCenter() {
            return center;
        }

        public Double getDistance() {
            return distance;
        }

        @Override
        public int compareTo(@NotNull ControllingData o) {
            Objects.requireNonNull(o, "Not compare with null");
            return Double.compare(distance, o.distance);
        }
    }

}

