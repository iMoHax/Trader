package ru.trader.core;

public class Group {
    private final String name;
    private final GROUP_TYPE type;

    public Group(String name, GROUP_TYPE type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public boolean isMarket(){
        return GROUP_TYPE.MARKET.equals(type);
    }

    public boolean isShip(){
        return GROUP_TYPE.SHIP.equals(type);
    }

    public boolean isOutfit(){
        return GROUP_TYPE.OUTFIT.equals(type);
    }

    public GROUP_TYPE getType() {
        return type;
    }
}
