package ru.trader.core;

public class Profile {
    public static enum PATH_PRIORITY {FAST, ECO}

    private double balance;
    private int jumps;
    private int lands;
    private Ship ship;
    private boolean refill;
    private int routesCount;
    //Scorer multipliers
    private int scoreOrdersCount;
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
        scoreOrdersCount = 5;
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

    public int getScoreOrdersCount() {
        return scoreOrdersCount;
    }

    public void setScoreOrdersCount(int scoreOrdersCount) {
        this.scoreOrdersCount = scoreOrdersCount;
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
}
