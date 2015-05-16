package ru.trader.core;

public class Ship {
    private int cargo;
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

    public static Ship copyOf(Ship other){
        Ship copy = new Ship();
        copy.mass = other.mass;
        copy.cargo = other.cargo;
        copy.tank = other.tank;
        copy.engine = other.getEngine();
        return copy;
    }

    public int getCargo() {
        return cargo;
    }

    public void setCargo(int cargo) {
        this.cargo = cargo;
    }

    public Engine getEngine() {
        return engine;
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public double getTank() {
        return tank;
    }

    public void setTank(double tank) {
        this.tank = tank;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(double mass) {
        this.mass = mass;
    }

    public double getLadenMass(){
        return mass+tank+cargo;
    }

    public double getLadenMass(double fuel){
        return mass+fuel+cargo;
    }

    //Laden fuel cost
    public double getFuelCost(double distance){
        return engine.getFuelCost(distance, getLadenMass());
    }

    public double getFuelCost(double fuel, double distance){
        return engine.getFuelCost(distance, getLadenMass(fuel));
    }

    //Jump range with full fuel tank
    public double getJumpRange(){
        return getJumpRange(tank);
    }

    //Laden jump range
    public double getJumpRange(double fuel){
        return engine.getJumpRange(fuel, getLadenMass(fuel));
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
                ", maxDist=" + getJumpRange() +
                ", fullTankDist=" + getFullTankJumpRange() +
                '}';
    }

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
            {1,050.0, 	875.0,      700.0, 	     630.0,      560.0},
            {1,800.0,	1,500.0, 	1,200.0, 	 1,080.0, 	 960.0}
    };
    //FSD Max fuel per jump [class][rating]
    private final static double[][] FSD_MAX_FUEL= {
            {},
            {},
            {0.90, 	 0.80, 	 0.60, 	 0.60, 	 0.60},
            {1.80, 	 1.50, 	 1.20, 	 1.20, 	 1.20},
            {3.00, 	 2.50, 	 2.00, 	 2.00, 	 2.00},
            {5.00, 	 4.10, 	 3.30, 	 3.30, 	 3.30},
            {8.00, 	 6.60, 	 5.30, 	 5.30, 	 5.30}
    };

    private class Engine {
        private int rating;
        private int clazz;

        private Engine(int clazz, char rating) {
            setRating(rating);
            this.clazz = clazz;
        }

        public char getRating() {
            return (char)(rating + 'A');
        }

        public void setRating(char rating) {
            this.rating =  rating - 'A';
        }

        public int getClazz() {
            return clazz;
        }

        public void setClazz(int clazz) {
            this.clazz = clazz;
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

        public double getJumpRange(double fuel, double  mass){
            return Math.pow(Math.min(fuel, getMaxFuel())/getMultiplier(), 1/getPowMultiplier())*getOptMass()/mass;
        }

        @Override
        public String toString() {
            return ""+clazz+getRating()+
                    " {optMass="+getOptMass()+
                    ", fuelPJ="+getMaxFuel()+"}";
        }
    }
}
