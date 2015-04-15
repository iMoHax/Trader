package ru.trader.core;

public class Ship {

    private int cargo;
    private double engine;

    public Ship(int cargo, double engine) {
        this.cargo = cargo;
        this.engine = engine;
    }

    public static Ship copyOf(Ship other){
        return new Ship(other.cargo, other.engine);
    }

    public int getCargo() {
        return cargo;
    }

    public void setCargo(int cargo) {
        this.cargo = cargo;
    }

    public double getEngine() {
        return engine;
    }

    public void setEngine(double engine) {
        this.engine = engine;
    }

}
