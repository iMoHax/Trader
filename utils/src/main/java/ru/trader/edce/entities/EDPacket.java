package ru.trader.edce.entities;

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
}
