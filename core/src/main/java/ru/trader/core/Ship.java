package ru.trader.core;

public class Ship {

    private double balance;
    private long cargo;
    private double engine;
    private int jumps;

    public Ship(double balance, long cargo, double engine, int jumps) {
        this.balance = balance;
        this.cargo = cargo;
        this.engine = engine;
        this.jumps = jumps;
    }

    public static Ship copyOf(Ship other){
        return new Ship(other.balance, other.cargo, other.engine, other.jumps);
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public long getCargo() {
        return cargo;
    }

    public void setCargo(long cargo) {
        this.cargo = cargo;
    }

    public double getEngine() {
        return engine;
    }

    public void setEngine(double engine) {
        this.engine = engine;
    }

    public int getJumps() {
        return jumps;
    }

    public void setJumps(int jumps) {
        this.jumps = jumps;
    }
}
