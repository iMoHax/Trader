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
        return getControlling(Collections.singleton(starSystem));
    }

    public Collection<IntersectData> getControlling(Collection<Place> starSystems){
        Stream<Place> candidates = market.get().stream().filter(Place::isPopulated);
        return getControlling(starSystems, candidates, Market.CONTROLLING_RADIUS).collect(Collectors.toList());
    }

    public Collection<IntersectData> getIntersects(Collection<Place> starSystems){
        return getIntersects(market.get(), starSystems, Market.CONTROLLING_RADIUS);
    }

    public Collection<IntersectData> getIntersects(Place starSystem, Collection<Place> starSystems){
        Stream<Place> candidates = market.get().stream().filter(Place::isPopulated);
        return getIntersects(starSystem, candidates, starSystems, Market.CONTROLLING_RADIUS).collect(Collectors.toList());
    }

    public Collection<IntersectData> getMaxProfit(Place headquarter, Collection<Place> starSystems){
        Stream<Place> candidates = market.get().stream().filter(Place::isPopulated);
        return getMaxProfit(candidates, headquarter, starSystems, Market.CONTROLLING_RADIUS, 200).collect(Collectors.toList());
    }

    public Collection<IntersectData> getMaxIntersect(Collection<Place> starSystems){
        Stream<Place> candidates = market.get().stream().filter(Place::isPopulated);
        return getMaxIntersect(candidates, starSystems, Market.CONTROLLING_RADIUS).collect(Collectors.toList());
    }

    public Collection<IntersectData> getNear(Collection<Place> starSystems){
        Stream<Place> candidates = market.get().stream().filter(Place::isPopulated);
        return getNear(candidates, starSystems, Market.CONTROLLING_RADIUS, Market.CONTROLLING_RADIUS * 2).collect(Collectors.toList());
    }

    public Collection<IntersectData> getNearExpansions(Collection<Place> starSystems){
        return getNearExpansions(market.get(), starSystems, Market.CONTROLLING_RADIUS * 2);
    }

    public static Collection<IntersectData> getControlling(Collection<Place> checkedSystems, Collection<Place> starSystems, double radius){
        return getControlling(checkedSystems, starSystems.stream(), radius).collect(Collectors.toList());
    }

    public static Stream<IntersectData> getControlling(Collection<Place> checkedSystems, Stream<Place> starSystems, double radius){
        IntersectsMapper controllingMapper = new IntersectsMapper(checkedSystems, radius, true, true);
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

    public static Stream<IntersectData> getMaxIntersect(Stream<Place> starSystems, Collection<Place> centers, double radius){
        IntersectsMapper distanceMapper = new IntersectsMapper(centers, radius*2, false, true);
        Predicate<Place> isControlling = intersectsAnyPredicate(centers, radius);
        Collection<Place> candidates = new ArrayList<>();
        Collection<Place> checked = new ArrayList<>();
        starSystems.filter(new FarDropper(centers, radius*2))
                .forEach(p -> {
                    if (isControlling != null && isControlling.test(p)) checked.add(p);
                    else {
                        if (p.getPower() == POWER.NONE) {
                            candidates.add(p);
                        }
                    }
                });
        candidates.removeAll(centers);
        IntersectsMapper checkedMapper = new IntersectsMapper(checked, radius, false, true);
        return candidates.stream()
                .map(checkedMapper)
                .filter(IntersectData::isIntersect)
                .sorted((d1, d2) -> {
                    long cc1 = d1.getControllingIncome();
                    long cc2 = d2.getControllingIncome();
                    int cmp = Long.compare(cc2, cc1);
                    if (cmp == 0) cmp = Integer.compare(d2.getCount(), d1.getCount());
                    return cmp;
                })
                .map(IntersectData::getStarSystem)
                .map(distanceMapper)
                .filter(IntersectData::isIntersect);
    }

    public static Stream<IntersectData> getMaxProfit(Stream<Place> starSystems, Place headquarter, Collection<Place> centers, double radius, double maxDistance){
        Collection<Place> candidates = new ArrayList<>();
        starSystems.filter(p ->p.getPowerState() == POWER_STATE.NONE && p.getDistance(headquarter) <= maxDistance)
                .forEach(candidates::add);
        IntersectsMapper candidatesMapper = new IntersectsMapper(candidates, radius, false, true);
        IntersectsMapper centersMapper = new IntersectsMapper(centers, radius*2, false, true);

        return candidates.stream()
                .map(candidatesMapper)
                .filter(IntersectData::isIntersect)
                .filter(d -> d.getIncome() - d.getStarSystem().computeUpkeep(headquarter) > 0)
                .sorted((d1, d2) -> {
                    double upkeep1 = d1.getStarSystem().computeUpkeep(headquarter);
                    double upkeep2 = d2.getStarSystem().computeUpkeep(headquarter);
                    double profit1 = d1.getIncome() - upkeep1;
                    double profit2 = d2.getIncome() - upkeep2;
                    int cmp = Double.compare(profit2, profit1);
                    if (cmp == 0) cmp = Integer.compare(d1.getCount(), d2.getCount());
                    return cmp;
                })
                .map(IntersectData::getStarSystem)
                .map(centersMapper)
                .filter(d -> d.getMinDistance() > radius || Double.isNaN(d.getMinDistance()))
                ;
    }

    public static Collection<IntersectData> getNearExpansions(Collection<Place> starSystems, Collection<Place> centers, double maxDistance){
        return getNearExpansions(starSystems.stream(), centers, maxDistance).collect(Collectors.toList());
    }

    public static Stream<IntersectData> getNearExpansions(Stream<Place> starSystems, Collection<Place> centers,  double maxDistance){
        IntersectsMapper mapper = new IntersectsMapper(centers, maxDistance, false, true);
        return starSystems.filter(new FarDropper(centers, maxDistance))
                .filter(p -> p.getPowerState() != null && p.getPowerState().isExpansion())
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
        private final long income;

        public IntersectData(Place starSystem) {
            this.starSystem = starSystem;
            this.contollings = new ControllingData[0];
            this.income = computeIncome();
        }


        public IntersectData(Place starSystem, Place control, double distance) {
            this.starSystem = starSystem;
            this.contollings = new ControllingData[]{new ControllingData(control,distance)};
            this.income = computeIncome();
        }

        public IntersectData(Place starSystem, Collection<ControllingData> controlling) {
            this.starSystem = starSystem;
            this.contollings = controlling.toArray(new ControllingData[controlling.size()]);
            Arrays.sort(contollings);
            this.income = computeIncome();
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

        public long getIncome() {
            return income;
        }

        public long getControllingIncome(){
            long income = 0;
            for (ControllingData contolling : contollings) {
                Place place = contolling.center;
                if (!place.getPowerState().isContested()){
                    income += place.computeCC();
                }
            }
            return income;
        }

        private long computeIncome(){
            return starSystem.computeCC() + getControllingIncome();
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

    public static class ControllingRadiusStat {
        private final Collection<Place> starSystems;
        private final Place headquarter;
        private long income;
        private double upkeep;
        private long contest;
        private long exploited;
        private long enemyExploited;
        private long blocked;
        private long enemyBlocked;

        Map<POWER, StarSystemsStat> contestStat;
        Map<Place, StarSystemsStat> contestStatByStarSystems;


        public ControllingRadiusStat(Collection<Place> starSystems, Place headquarter, Collection<IntersectData> exploitedSystems) {
            this.starSystems = starSystems;
            this.headquarter = headquarter;
            contestStat = new HashMap<>();
            contestStatByStarSystems = new HashMap<>();
            fillStat(exploitedSystems);
        }

        private void fillStat(Collection<IntersectData> exploitedSystems) {
            income = 0; contest = 0; exploited = 0; enemyExploited = 0; blocked = 0; enemyBlocked = 0; upkeep = 0;
            if (headquarter != null && starSystems != null){
                for (Place starSystem : starSystems) {
                    upkeep += starSystem.computeUpkeep(headquarter);
                }
            }
            contestStat.clear();
            contestStatByStarSystems.clear();
            for (IntersectData exploitedSystem : exploitedSystems) {
                long cc = exploitedSystem.getStarSystem().computeCC();
                income += cc;
                POWER power = exploitedSystem.getStarSystem().getPower();
                POWER_STATE state = exploitedSystem.getStarSystem().getPowerState();
                if (state == null) continue;
                if (state.isExploited() || state.isControl()){
                    if (headquarter != null && headquarter.getPower() == power){
                        exploited += cc;
                    } else {
                        enemyExploited += cc;
                    }
                } else
                if (state.isExpansion() || state.isBlocked()){
                    if (headquarter != null && headquarter.getPower() == power){
                        blocked += cc;
                    } else {
                        enemyBlocked += cc;
                    }
                } else
                if (state.isContested()){
                    contest += cc;
                }

                Set<POWER> powers = EnumSet.noneOf(POWER.class);
                for (Place system : exploitedSystem.getStarSystem().getControllingSystems()) {
                    powers.add(system.getPower());
                    StarSystemsStat stat = contestStatByStarSystems.get(system);
                    if (stat == null){
                        stat = new StarSystemsStat();
                        contestStatByStarSystems.put(system, stat);
                    }
                    stat.put(exploitedSystem.getStarSystem());
                }
                if (state.isControl()){
                    Place system = exploitedSystem.getStarSystem();
                    powers.add(system.getPower());
                    StarSystemsStat stat = contestStatByStarSystems.get(system);
                    if (stat == null){
                        stat = new StarSystemsStat();
                        contestStatByStarSystems.put(system, stat);
                    }
                    stat.put(exploitedSystem.getStarSystem());
                }
                for (POWER p : powers) {
                    StarSystemsStat stat = contestStat.get(p);
                    if (stat == null){
                        stat = new StarSystemsStat();
                        contestStat.put(p, stat);
                    }
                    stat.put(exploitedSystem.getStarSystem());
                }
            }
        }

        public Collection<Place> getStarSystems() {
            return starSystems;
        }

        public Place getHeadquarter() {
            return headquarter;
        }

        public long getIncome() {
            return income;
        }

        public double getUpkeep() {
            return upkeep;
        }

        public long getContest() {
            return contest;
        }

        public long getExploited() {
            return exploited;
        }

        public long getEnemyExploited() {
            return enemyExploited;
        }

        public long getBlocked() {
            return blocked;
        }

        public long getEnemyBlocked() {
            return enemyBlocked;
        }

        public double getCurrentRadiusProfit(){
            return income - upkeep - contest - enemyExploited - exploited;
        }

        public double getFutureRadiusProfit(){
            return income - upkeep - getFutureContest() - getFutureExploited();
        }

        public long getFutureContest(){
            return contest + enemyExploited + enemyBlocked;
        }

        public long getFutureExploited(){
            return exploited + blocked;
        }

        public Map<POWER, StarSystemsStat> getContestStat() {
            return contestStat;
        }

        public Map<Place, StarSystemsStat> getContestStatByStarSystems() {
            return contestStatByStarSystems;
        }
    }

    public static class StarSystemsStat {
        private long income;
        private long contest;
        private long intersect;
        private long blocked;
        private long exploited;

        public void put(Place starSystem){
            long cc = starSystem.computeCC();
            income += cc;
            if (starSystem.getPowerState() != null){
                if (starSystem.getPowerState().isExploited() || starSystem.getPowerState().isControl()){
                    exploited += cc;
                    if (starSystem.getControllingSystems().size() > 1){
                        intersect += cc;
                    }
                } else
                if (starSystem.getPowerState().isContested()){
                    contest += cc;
                } else
                if (starSystem.getPowerState().isBlocked() || starSystem.getPowerState().isExpansion()){
                    blocked += cc;
                }
            }
        }

        public long getIncome() {
            return income;
        }

        public long getExploited() {
            return exploited;
        }

        public long getContest() {
            return contest;
        }

        public long getIntersect() {
            return intersect;
        }

        public long getBlocked() {
            return blocked;
        }
    }

}

