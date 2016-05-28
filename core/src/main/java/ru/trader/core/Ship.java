package ru.trader.core;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ship {
    private final static int REFILL_FUEL_STEP = 10;

    private long cargo;
    private Engine engine;
    private double tank;
    private double mass;

    public Ship() {
        //Default sidewinder
        this.mass = 44.9;
        this.cargo = 4;
        this.tank = 2;
        this.engine = new Engine(2, 'E');
    }

    protected Ship(Ship ship){
        this.mass = ship.mass;
        this.cargo = ship.cargo;
        this.tank = ship.tank;
        this.engine = ship.getEngine();

    }

    public static Ship clone(Ship ship){
        return ship != null ? new Ship(ship) : null;
    }

    public long getCargo() {
        return cargo;
    }

    public void setCargo(long cargo) {
        this.cargo = cargo;
        clearCache();
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(int clazz, char rating) {
        setEngine(new Engine(clazz, rating));
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
        clearCache();
    }

    public double getTank() {
        return tank;
    }

    public void setTank(double tank) {
        this.tank = tank;
        clearCache();
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
        clearCache();
    }

    public double getLadenMass(){
        return mass+tank+cargo;
    }

    public double getLadenMass(double fuel){
        return mass+fuel+cargo;
    }

    public double getFuelCost(double fuel, double distance){
        return engine.getFuelCost(distance, getLadenMass(fuel));
    }

    public double getMaxFuelCost(double distance){
        return engine.getMaxFuel(distance, getLadenMass(engine.getMaxFuel()));
    }

    public double getRoundMaxFuel(double distance){
        return getRoundMaxFuel(distance, REFILL_FUEL_STEP);
    }

    public double getRoundFuel(double fuel){
        return getRoundFuel(fuel, REFILL_FUEL_STEP);
    }

    public double getRoundFuel(double fuel, int step){
        fuel = Math.floor(fuel*step/tank) * tank / step;
        return fuel < 0 ? 0 : fuel;

    }

    private double getRoundMaxFuel(double distance, int step){
        double fuel = getMaxFuel(distance);
        if (fuel == 0 || fuel == tank) return fuel;
        double minFuel = getMinFuel(distance);
        fuel = Math.floor(fuel*step/tank) * tank / step;
        return fuel < minFuel ? 0 : fuel;
    }

    public double getMaxJumpRange(){
        if (Double.isNaN(maxJumpRange)){
            maxJumpRange = getJumpRange(Math.min(engine.getMaxFuel(), tank));
        }
        return maxJumpRange;
    }

    //Jump range with full fuel tank
    public double getJumpRange(){
        if (Double.isNaN(ladenJumpRange)){
            ladenJumpRange = getJumpRange(tank);
        }
        return ladenJumpRange;
    }

    //Laden jump range
    public double getJumpRange(double fuel){
        return engine.getJumpRange(fuel, getLadenMass(fuel));
    }

    public double getEmptyMaxJumpRange(){
        return getEmptyJumpRange(Math.min(engine.getMaxFuel(), tank));
    }

    //Unladen jump range
    public double getEmptyJumpRange(double fuel){
        return engine.getJumpRange(fuel, mass + fuel);
    }

    public double getFullTankJumpRange(){
        double fuel = tank;
        double range = 0;
        while (fuel > 0){
            double distance = engine.getJumpRange(fuel, getLadenMass(fuel));
            range += distance;
            fuel -= engine.getFuelCost(distance, getLadenMass(fuel));
        }
        return range;
    }


    @Override
    public String toString() {
        return "Ship{" +
                "cargo=" + cargo +
                ", engine=" + engine +
                ", tank=" + tank +
                ", mass=" + mass +
                ", jumpRange=" + getJumpRange() +
                ", maxDist=" + getMaxJumpRange() +
                ", fullTankDist=" + getFullTankJumpRange() +
                '}';
    }

    private final static float FUEL_TABLE_STEP = 0.01f;
    private FuelHelper[] fuelTable;
    private double maxJumpRange = Double.NaN;
    private double ladenJumpRange = Double.NaN;
    private void fillFuelTable(){
        double fuel = getEngine().getMaxFuel();
        FuelHelper[] fuelTable = new FuelHelper[(int) (fuel/FUEL_TABLE_STEP)];
        maxJumpRange = Double.NaN; ladenJumpRange = Double.NaN;
        for (int i = fuelTable.length - 1; i >= 0; i--) {
            double distance = getJumpRange(fuel);
            fuelTable[i] = new FuelHelper(distance, fuel);
            fuel = fuel - FUEL_TABLE_STEP;
        }
        this.fuelTable = fuelTable;
    }

    private void clearCache(){
        fuelTable = null;
        maxJumpRange = Double.NaN;
        ladenJumpRange = Double.NaN;
    }

    public double getMaxFuel(double distance){
        if (distance > getMaxJumpRange()) return 0;
        if (distance <= getJumpRange()) return tank;
        return engine.getMaxFuel(distance, getLadenMass(0));
    }

    public double getMinFuel(double distance){
        if (fuelTable == null) fillFuelTable();
        if (distance > getMaxJumpRange()) return 0;
        for (int i = 0; i < fuelTable.length; i++) {
            FuelHelper h = fuelTable[i];
            if (distance <= h.distance) {
                return i == 0 ? 0 : h.fuel <= tank ? h.fuel : 0;
            }
        }
        return 0;
    }

    private class FuelHelper {
        private final double distance;
        private final double fuel;

        private FuelHelper(double distance, double fuel) {
            this.distance = distance;
            this.fuel = fuel;
        }
    }

    private final static Pattern ENGINE_REGEXP = Pattern.compile("(\\d)(\\w)");
    public static Ship readFrom(Properties values){
        Ship ship = new Ship();
        ship.setMass(Double.valueOf(values.getProperty("ship.mass", "44.9")));
        ship.setCargo(Integer.valueOf(values.getProperty("ship.cargo","4")));
        ship.setTank(Double.valueOf(values.getProperty("ship.tank", "2")));
        String e = values.getProperty("ship.engine","2E");
        Matcher matcher = ENGINE_REGEXP.matcher(e);
        if (matcher.find()){
            double optMass = Double.valueOf(values.getProperty("ship.engine.optmass", "-1"));
            if (optMass != -1) {
                ship.setEngine(new ModEngine(Integer.valueOf(matcher.group(1)), matcher.group(2).charAt(0), optMass));
            } else {
                ship.setEngine(Integer.valueOf(matcher.group(1)), matcher.group(2).charAt(0));
            }
        }
        return ship;
    }

    public void writeTo(Properties values){
        values.setProperty("ship.mass", String.valueOf(mass));
        values.setProperty("ship.cargo", String.valueOf(cargo));
        values.setProperty("ship.tank", String.valueOf(tank));
        values.setProperty("ship.engine", String.valueOf(engine.getClazz()) + engine.getRating());
        double optMass = -1;
        if (engine instanceof ModEngine){
            optMass = engine.getOptMass();
        }
        values.setProperty("ship.engine.optmass", String.valueOf(optMass));
    }
}
