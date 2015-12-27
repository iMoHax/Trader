package ru.trader.edce.entities;


import java.util.Objects;

public class FuelTanks {
    private Fuel main;
    private Fuel reserve;

    public Fuel getMain() {
        return main;
    }

    public void setMain(Fuel main) {
        this.main = main;
    }

    public Fuel getReserve() {
        return reserve;
    }

    public void setReserve(Fuel reserve) {
        this.reserve = reserve;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FuelTanks fuelTanks = (FuelTanks) o;
        return Objects.equals(main, fuelTanks.main) &&
                Objects.equals(reserve, fuelTanks.reserve);
    }

    @Override
    public int hashCode() {
        return Objects.hash(main, reserve);
    }
}
