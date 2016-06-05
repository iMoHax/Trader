package ru.trader.core;

import java.util.ArrayList;
import java.util.List;

public class Engine {
    private int rating;
    private int clazz;

    private final static char MAX_ENGINE_RATING = 'E';
    private final static int MAX_ENGINE_CLASS = 7;
    //FSD multiplier by FSD Rating A,B,C ... etc * 0.001
    //http://elite-dangerous.wikia.com/wiki/Frame_Shift_Drive
    private final static double[] FSD_MULT = {0.012,0.010,0.008,0.010,0.011};
    //FSD power multiplier by FSD Class 1,2,3 ... etc
    private final static double[] FSD_POWER_MULT = {0,0,2.00,2.15,2.30,2.45,2.60,2.75,2.90};
    //FSD Optimal Mass [class][rating]
    private final static double[][] FSD_OPT_MASS = {
            {},
            {},
            {90.0, 	    75.0, 	    60.0, 	     54.0, 	     48.0},
            {150.0,     125.0,      100.0, 	     90.0, 	     80.0},
            {525.0,     438.0,      350.0, 	     315.0,      280.0},
            {1050.0, 	875.0,      700.0, 	     630.0,      560.0},
            {1800.0,	1500.0, 	1200.0, 	 1080.0, 	 960.0},
            {2700.0,    2250.0,     1800.0,      1620.0,     1440.0}
    };
    //FSD Max fuel per jump [class][rating]
    private final static double[][] FSD_MAX_FUEL= {
            {},
            {},
            {0.90, 	 0.80, 	 0.60, 	 0.60, 	 0.60},
            {1.80, 	 1.50, 	 1.20, 	 1.20, 	 1.20},
            {3.00, 	 2.50, 	 2.00, 	 2.00, 	 2.00},
            {5.00, 	 4.10, 	 3.30, 	 3.30, 	 3.30},
            {8.00, 	 6.60, 	 5.30, 	 5.30, 	 5.30},
            {12.80,  10.60,  8.50,   8.50,   8.50}
    };

    public Engine(int clazz, char rating) {
        this.rating =  rating - 'A';
        this.clazz = clazz;
    }

    public char getRating() {
        return (char)(rating + 'A');
    }

    public int getClazz() {
        return clazz;
    }

    public double getOptMass() {
        return FSD_OPT_MASS[clazz][rating];
    }

    public double getMaxFuel() {
        return FSD_MAX_FUEL[clazz][rating];
    }

    public double getMultiplier(){
        return FSD_MULT[rating];
    }

    public double getPowMultiplier(){
        return FSD_POWER_MULT[clazz];
    }

    //https://forums.frontier.co.uk/showthread.php?p=643461#post643461
    //Fuel Cost = Coefficient * (Distance * (Mass / Optimised Mass))^Power
    public double getFuelCost(double distance, double mass){
        return getMultiplier() * Math.pow(distance * (mass / getOptMass()), getPowMultiplier());
    }

    //return max fuel for jump to distance
    public double getMaxFuel(double distance, double emptyTankMass){
        double f = Math.pow(getMaxFuel()/getMultiplier(), 1/getPowMultiplier())*getOptMass()/distance - emptyTankMass;
        return f < getMaxFuel() ? 0 : f;
    }

    public double getJumpRange(double fuel, double  mass){
        return Math.pow(Math.min(fuel, getMaxFuel())/getMultiplier(), 1/getPowMultiplier())*getOptMass()/mass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Engine)) return false;
        Engine engine = (Engine) o;
        return clazz == engine.clazz && rating == engine.rating;
    }

    @Override
    public int hashCode() {
        int result = rating;
        result = 31 * result + clazz;
        return result;
    }

    @Override
    public String toString() {
        return ""+clazz+getRating()+
                " {optMass="+getOptMass()+
                ", fuelPJ="+getMaxFuel()+"}";
    }

    public static List<Engine> getEngines(){
        List<Engine> engines = new ArrayList<>();
        for (int c = 2; c <= MAX_ENGINE_CLASS; c++) {
            for (char r = MAX_ENGINE_RATING; r >= 'A'; r--) {
                engines.add(new Engine(c,r));
            }
        }
        return engines;
    }

}
