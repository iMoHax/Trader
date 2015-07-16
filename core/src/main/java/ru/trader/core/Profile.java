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
    private double distanceMult;
    private double jumpMult;
    private double landMult;
    private double fuelPrice;
    private PATH_PRIORITY pathPriority;

    public Profile(Ship ship) {
        this.ship = ship;
        refill = true;
        jumps = 6;
        lands = 4;
        routesCount = 30;
        distanceMult = 0.8;
        landMult = 0.95;
        fuelPrice = 100;
        jumpMult = 0.5;
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

    public double getDistanceMult() {
        return distanceMult;
    }

    public void setDistanceMult(double distanceMult) {
        this.distanceMult = distanceMult;
    }

    public double getJumpMult() {
        return jumpMult;
    }

    public void setJumpMult(double jumpMult) {
        this.jumpMult = jumpMult;
    }

    public double getLandMult() {
        return landMult;
    }

    public void setLandMult(double landMult) {
        this.landMult = landMult;
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
        profile.setLands(Integer.valueOf(values.getProperty("profile.lands","4")));
        profile.setPathPriority(PATH_PRIORITY.valueOf(values.getProperty("profile.search.priority","FAST")));
        profile.setRoutesCount(Integer.valueOf(values.getProperty("profile.search.routes","100")));
        profile.setFuelPrice(Double.valueOf(values.getProperty("profile.search.fuel.price","100")));
        profile.setDistanceMult(Double.valueOf(values.getProperty("profile.search.mult.distance","0.8")));
        profile.setLandMult(Double.valueOf(values.getProperty("profile.search.mult.land","0.95")));
        profile.setJumpMult(Double.valueOf(values.getProperty("profile.search.mult.jump","0.5")));
        return profile;
    }

    public void writeTo(Properties values){
        values.setProperty("profile.balance", String.valueOf(balance));
        values.setProperty("profile.jumps", String.valueOf(jumps));
        values.setProperty("profile.lands", String.valueOf(lands));
        values.setProperty("profile.search.priority", String.valueOf(pathPriority));
        values.setProperty("profile.search.routes", String.valueOf(routesCount));
        values.setProperty("profile.search.fuel.price", String.valueOf(fuelPrice));
        values.setProperty("profile.search.mult.distance", String.valueOf(distanceMult));
        values.setProperty("profile.search.mult.land", String.valueOf(landMult));
        values.setProperty("profile.search.mult.jump", String.valueOf(jumpMult));
        ship.writeTo(values);
    }

}
