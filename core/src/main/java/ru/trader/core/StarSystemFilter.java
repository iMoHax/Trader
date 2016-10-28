package ru.trader.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class StarSystemFilter {
    private final static Logger LOG = LoggerFactory.getLogger(StarSystemFilter.class);

    private final Collection<DistanceFilter> distances;
    private final EnumSet<POWER> powers;
    private final EnumSet<POWER_STATE> states;
    private final EnumSet<FACTION> factions;
    private final EnumSet<GOVERNMENT> governments;
    private final Collection<Place> excludes;

    public StarSystemFilter() {
        this.powers = EnumSet.noneOf(POWER.class);
        this.states = EnumSet.noneOf(POWER_STATE.class);
        this.factions = EnumSet.noneOf(FACTION.class);
        this.governments = EnumSet.noneOf(GOVERNMENT.class);
        this.excludes = new ArrayList<>();
        this.distances = new ArrayList<>();
    }

    public Collection<DistanceFilter> getDistanceFilters() {
        return distances;
    }

    public void add(Place center, double radius){
        distances.add(new DistanceFilter(center, radius));
    }

    public void add(DistanceFilter distanceFilter){
        distances.add(distanceFilter);
    }

    public void remove(Place center){
        distances.removeIf(d -> d.center.equals(center));
    }

    public void remove(DistanceFilter distanceFilter){
        distances.remove(distanceFilter);
    }

    public void clearDistanceFilters(){
        distances.clear();
    }

    public Collection<POWER> getPowers(){
        return powers;
    }

    public void add(POWER power){
        powers.add(power);
    }

    public void remove(POWER power){
        powers.remove(power);
    }

    public void clearPower(){
        powers.clear();
    }

    public Collection<POWER_STATE> getPowerStates(){
        return states;
    }

    public void add(POWER_STATE powerState){
        states.add(powerState);
    }

    public void remove(POWER_STATE powerState){
        states.remove(powerState);
    }

    public void clearPowerStates(){
        states.clear();
    }


    public Collection<Place> getExcludes(){
        return excludes;
    }

    public void addExclude(Place starSystem){
        excludes.add(starSystem);
    }

    public void removeExclude(Place starSystem){
        excludes.remove(starSystem);
    }

    public void clearExcludes(){
        excludes.clear();
    }

    public Collection<FACTION> getFactions(){
        return factions;
    }

    public void add(FACTION faction){
        factions.add(faction);
    }

    public void remove(FACTION faction){
        factions.remove(faction);
    }

    public void clearFactions(){
        factions.clear();
    }

    public Collection<GOVERNMENT> getGovernments(){
        return governments;
    }

    public void add(GOVERNMENT government){
        governments.add(government);
    }

    public void remove(GOVERNMENT government){
        governments.remove(government);
    }

    public void clearGovernments(){
        governments.clear();
    }

    public boolean isFiltered(Place starSystem){
        for (DistanceFilter distanceFilter : distances) {
            if (distanceFilter.isFiltered(starSystem)){
                return true;
            }
        }
        if (excludes.contains(starSystem)) return true;
        POWER power = starSystem.getPower();
        if (power != null && !powers.isEmpty() && !powers.contains(power)) return true;
        POWER_STATE state = starSystem.getPowerState();
        if (state != null && !states.isEmpty() && !states.contains(state)) return true;
        FACTION faction = starSystem.getFaction();
        if (faction != null && !factions.isEmpty() && !factions.contains(faction)) return true;
        GOVERNMENT government = starSystem.getGovernment();
        if (government != null && !governments.isEmpty() && !governments.contains(government)) return true;
        return false;
    }

    @Override
    public String toString() {
        return "StarSystemFilter{" +
                "distances=" + distances +
                ", powers=" + powers +
                ", states=" + states +
                ", factions=" + factions +
                ", governments=" + governments +
                ", excludes=" + excludes +
                '}';
    }

    public class DistanceFilter {
        private final Place center;
        private final double distance;

        public DistanceFilter(Place center, double distance) {
            this.center = center;
            this.distance = distance;
        }

        public Place getCenter() {
            return center;
        }

        public double getDistance() {
            return distance;
        }

        public boolean isFiltered(Place starSystem){
            return center.getDistance(starSystem) <= distance;
        }
    }


}
