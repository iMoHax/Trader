package ru.trader.core;

import java.util.Properties;

public class Profile {
    public static enum PATH_PRIORITY {FAST, ECO}

    private String name;
    private double balance;
    private Place system;
    private Vendor station;
    private Ship ship;
    private boolean docked;
    private int jumps;
    private int lands;
    private boolean refill;
    private int routesCount;
    //Scorer multipliers
    private double distanceTime;
    private double jumpTime;
    private double landingTime;
    private double orbitalTime;
    private double takeoffTime;
    private double rechargeTime;
    private double fuelPrice;
    private PATH_PRIORITY pathPriority;

    public Profile(Ship ship) {
        this.ship = ship;
        refill = true;
        jumps = 3;
        lands = 4;
        routesCount = 30;
        distanceTime = 0.3;
        fuelPrice = 100;
        landingTime = 80;
        orbitalTime = 100;
        takeoffTime = 40;
        jumpTime = 32;
        rechargeTime = 12;
        pathPriority = PATH_PRIORITY.FAST;
    }

    protected Profile(Profile profile){
        this.name = profile.name;
        this.balance = profile.balance;
        this.docked = profile.docked;
        this.jumps = profile.jumps;
        this.lands = profile.lands;
        this.refill = profile.refill;
        this.routesCount = profile.routesCount;
        this.distanceTime = profile.distanceTime;
        this.jumpTime = profile.jumpTime;
        this.landingTime = profile.landingTime;
        this.orbitalTime = profile.orbitalTime;
        this.takeoffTime = profile.takeoffTime;
        this.rechargeTime = profile.rechargeTime;
        this.fuelPrice = profile.fuelPrice;
        this.pathPriority = profile.pathPriority;
        this.system = profile.system;
        this.station = profile.station;
        this.ship = Ship.clone(profile.ship);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Place getSystem() {
        return system;
    }

    public void setSystem(Place system) {
        this.system = system;
    }

    public Vendor getStation() {
        return station;
    }

    public void setStation(Vendor station) {
        this.station = station;
    }

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    public boolean isDocked() {
        return docked;
    }

    public void setDocked(boolean docked) {
        this.docked = docked;
    }

    public int getJumps() {
        return jumps;
    }

    public void setJumps(int jumps) {
        this.jumps = jumps;
    }

    public int getLands() {
        return lands;
    }

    public void setLands(int lands) {
        this.lands = lands;
    }

    public boolean withRefill() {
        return refill;
    }

    public void setRefill(boolean refill) {
        this.refill = refill;
    }

    public int getRoutesCount() {
        return routesCount;
    }

    public void setRoutesCount(int routesCount) {
        this.routesCount = routesCount;
    }

    public double getDistanceTime() {
        return distanceTime;
    }

    public void setDistanceTime(double distanceTime) {
        this.distanceTime = distanceTime;
    }

    public double getJumpTime() {
        return jumpTime;
    }

    public void setJumpTime(double jumpTime) {
        this.jumpTime = jumpTime;
    }

    public double getLandingTime() {
        return landingTime;
    }

    public void setLandingTime(double landingTime) {
        this.landingTime = landingTime;
    }

    public double getOrbitalTime() {
        return orbitalTime;
    }

    public void setOrbitalTime(double orbitalTime) {
        this.orbitalTime = orbitalTime;
    }

    public double getTakeoffTime() {
        return takeoffTime;
    }

    public void setTakeoffTime(double takeoffTime) {
        this.takeoffTime = takeoffTime;
    }

    public double getRechargeTime() {
        return rechargeTime;
    }

    public void setRechargeTime(double rechargeTime) {
        this.rechargeTime = rechargeTime;
    }

    public double getFuelPrice() {
        return fuelPrice;
    }

    public void setFuelPrice(double fuelPrice) {
        this.fuelPrice = fuelPrice;
    }

    public PATH_PRIORITY getPathPriority() {
        return pathPriority;
    }

    public void setPathPriority(PATH_PRIORITY pathPriority) {
        this.pathPriority = pathPriority;
    }

    public static Profile readFrom(Properties values, Market market){
        Ship ship = Ship.readFrom(values);
        Profile profile = new Profile(ship);
        profile.setName(values.getProperty("profile.name","Cmdr NoName"));
        profile.setBalance(Double.valueOf(values.getProperty("profile.balance","1000")));
        String v = values.getProperty("profile.system", null);
        if (v != null){
            Place system = market.get(v);
            profile.setSystem(system);
            v = values.getProperty("profile.station", null);
            if (v != null && system != null){
                profile.setStation(system.get(v));
            } else {
                profile.setStation(null);
            }
        } else {
            profile.setSystem(null);
            profile.setStation(null);
        }
        profile.setDocked(Boolean.valueOf(values.getProperty("profile.docked","false")));
        profile.setJumps(Integer.valueOf(values.getProperty("profile.jumps", "3")));
        profile.setLands(Integer.valueOf(values.getProperty("profile.lands", "4")));
        profile.setPathPriority(PATH_PRIORITY.valueOf(values.getProperty("profile.search.priority", "FAST")));
        profile.setRoutesCount(Integer.valueOf(values.getProperty("profile.search.routes", "30")));
        profile.setFuelPrice(Double.valueOf(values.getProperty("profile.search.fuel.price", "100")));
        profile.setDistanceTime(Double.valueOf(values.getProperty("profile.search.times.distance", "0.3")));
        profile.setLandingTime(Double.valueOf(values.getProperty("profile.search.times.landing", "80")));
        profile.setOrbitalTime(Double.valueOf(values.getProperty("profile.search.times.orbital", "100")));
        profile.setTakeoffTime(Double.valueOf(values.getProperty("profile.search.times.takeoff", "40")));
        profile.setJumpTime(Double.valueOf(values.getProperty("profile.search.times.jump", "32")));
        profile.setRechargeTime(Double.valueOf(values.getProperty("profile.search.times.recharge", "12")));
        return profile;
    }

    public void writeTo(Properties values){
        values.setProperty("profile.name", String.valueOf(name));
        values.setProperty("profile.balance", String.valueOf(balance));
        values.setProperty("profile.system", String.valueOf(system != null ? system.getName():""));
        values.setProperty("profile.station", String.valueOf(station != null ? station.getName():""));
        values.setProperty("profile.docked", String.valueOf(docked));
        values.setProperty("profile.jumps", String.valueOf(jumps));
        values.setProperty("profile.lands", String.valueOf(lands));
        values.setProperty("profile.search.priority", String.valueOf(pathPriority));
        values.setProperty("profile.search.routes", String.valueOf(routesCount));
        values.setProperty("profile.search.fuel.price", String.valueOf(fuelPrice));
        values.setProperty("profile.search.times.distance", String.valueOf(distanceTime));
        values.setProperty("profile.search.times.landing", String.valueOf(landingTime));
        values.setProperty("profile.search.times.orbital", String.valueOf(orbitalTime));
        values.setProperty("profile.search.times.takeoff", String.valueOf(takeoffTime));
        values.setProperty("profile.search.times.jump", String.valueOf(jumpTime));
        values.setProperty("profile.search.times.recharge", String.valueOf(rechargeTime));
        ship.writeTo(values);
    }

    public static Profile clone(Profile profile){
        return profile != null ? new Profile(profile) : null;
    }
}
