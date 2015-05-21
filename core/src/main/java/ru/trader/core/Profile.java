package ru.trader.core;

public class Profile {

    private double balance;
    private int jumps;
    private Ship ship;
    private boolean refill;
    private int routesCount;
    //Scorer multipliers
    private int scoreOrdersCount;
    private double distanceMult;
    private double jumpMult;
    private double landMult;
    private double fuelPrice;

    public Profile(Ship ship) {
        this.ship = ship;
        refill = true;
        scoreOrdersCount = 5;
        distanceMult = 1;
        landMult = 1;
        fuelPrice = 1;
        jumpMult = 0.01;
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
}
