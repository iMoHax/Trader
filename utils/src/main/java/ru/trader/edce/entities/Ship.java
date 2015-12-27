package ru.trader.edce.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Ship {
    private String name;
    private FuelTanks fuel;
    private Cargo cargo;
    private Map<String, List<Slot>> modules = new LinkedHashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FuelTanks getFuel() {
        return fuel;
    }

    public void setFuel(FuelTanks fuel) {
        this.fuel = fuel;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public Map<String, List<Slot>> getModules() {
        return modules;
    }

    public void setModules(Map<String, List<Slot>> modules) {
        this.modules = modules;
    }

    @JsonIgnore
    public double getFuelLevel(){
        return fuel != null ? fuel.getMain().getLevel() : 0;
    }

    @JsonIgnore
    public double getFuelCapacity(){
        return fuel != null ? fuel.getMain().getCapacity() : 0;
    }

    @JsonIgnore
    public int getCargoCapacity(){
        return cargo != null ? cargo.getCapacity() : 0;
    }

    @JsonIgnore
    public int getCargoLimit(){
        return cargo != null ? cargo.getCapacity() - cargo.getQty() : 0;
    }

    @JsonIgnore
    public Module getFSD(){
        if (modules == null) return null;
        Slot fsd = modules.get("FrameShiftDrive").get(0);
        return fsd != null ? fsd.getModule() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ship ship = (Ship) o;
        return Objects.equals(name, ship.name) &&
                Objects.equals(fuel, ship.fuel) &&
                Objects.equals(cargo, ship.cargo) &&
                Objects.equals(modules, ship.modules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
