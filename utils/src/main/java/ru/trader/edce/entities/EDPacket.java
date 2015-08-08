package ru.trader.edce.entities;

import java.util.Objects;

public class EDPacket {
    private Commander commander;
    private System lastSystem;
    private Starport lastStarport;
    private Ship ship;

    public Commander getCommander() {
        return commander;
    }

    public void setCommander(Commander commander) {
        this.commander = commander;
    }

    public System getLastSystem() {
        return lastSystem;
    }

    public void setLastSystem(System lastSystem) {
        this.lastSystem = lastSystem;
    }

    public Starport getLastStarport() {
        return lastStarport;
    }

    public void setLastStarport(Starport lastStarport) {
        this.lastStarport = lastStarport;
    }

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        this.ship = ship;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EDPacket edPacket = (EDPacket) o;
        return Objects.equals(commander, edPacket.commander) &&
                Objects.equals(lastSystem, edPacket.lastSystem) &&
                Objects.equals(lastStarport, edPacket.lastStarport) &&
                Objects.equals(ship, edPacket.ship);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commander, lastSystem, lastStarport, ship);
    }
}
