package ru.trader.core;

public class Profile {

    private double balance;
    private int jumps;
    private Ship ship;
    private boolean refill;

    public Profile(Ship ship) {
        this.ship = ship;
        refill = true;
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
}
