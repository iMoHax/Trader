package ru.trader.core;

import java.util.Properties;

public class Profile {
    public static enum PATH_PRIORITY {FAST, ECO}

    private double balance;
    private int jumps;
    private int lands;
    private Ship ship;
    private boolean refill;
    private int routesCount;
    //Scorer multipliers
    private double distanceTime;
    private double jumpTime;
    private double landingTime;
    private double takeoffTime;
    private double rechargeTime;
    private double fuelPrice;
    private PATH_PRIORITY pathPriority;

    public Profile(Ship ship) {
        this.ship = ship;
        refill = true;
        jumps = 6;
        lands = 4;
        routesCount = 30;
        distanceTime = 0.3;
        fuelPrice = 100;
        landingTime = 80;
        takeoffTime = 40;
        jumpTime = 32;
        rechargeTime = 12;
        pathPriority = PATH_PRIORITY.FAST;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
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

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
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

    public static Profile readFrom(Properties values){
        Ship ship = Ship.readFrom(values);
        Profile profile = new Profile(ship);
        profile.setBalance(Double.valueOf(values.getProperty("profile.balance","1000")));
        profile.setJumps(Integer.valueOf(values.getProperty("profile.jumps", "6")));
        profile.setLands(Integer.valueOf(values.getProperty("profile.lands", "4")));
        profile.setPathPriority(PATH_PRIORITY.valueOf(values.getProperty("profile.search.priority", "FAST")));
        profile.setRoutesCount(Integer.valueOf(values.getProperty("profile.search.routes", "100")));
        profile.setFuelPrice(Double.valueOf(values.getProperty("profile.search.fuel.price", "100")));
        profile.setDistanceTime(Double.valueOf(values.getProperty("profile.search.times.distance", "0.3")));
        profile.setLandingTime(Double.valueOf(values.getProperty("profile.search.times.landing", "80")));
        profile.setTakeoffTime(Double.valueOf(values.getProperty("profile.search.times.takeoff", "40")));
        profile.setJumpTime(Double.valueOf(values.getProperty("profile.search.times.jump", "32")));
        profile.setRechargeTime(Double.valueOf(values.getProperty("profile.search.times.recharge", "12")));
        return profile;
    }

    public void writeTo(Properties values){
        values.setProperty("profile.balance", String.valueOf(balance));
        values.setProperty("profile.jumps", String.valueOf(jumps));
        values.setProperty("profile.lands", String.valueOf(lands));
        values.setProperty("profile.search.priority", String.valueOf(pathPriority));
        values.setProperty("profile.search.routes", String.valueOf(routesCount));
        values.setProperty("profile.search.fuel.price", String.valueOf(fuelPrice));
        values.setProperty("profile.search.times.distance", String.valueOf(distanceTime));
        values.setProperty("profile.search.times.landing", String.valueOf(landingTime));
        values.setProperty("profile.search.times.takeoff", String.valueOf(takeoffTime));
        values.setProperty("profile.search.times.jump", String.valueOf(jumpTime));
        values.setProperty("profile.search.times.recharge", String.valueOf(rechargeTime));
        ship.writeTo(values);
    }

}
