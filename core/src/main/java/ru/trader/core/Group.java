package ru.trader.core;

public abstract class Group {
    public abstract String getName();
    public abstract GROUP_TYPE getType();

    public boolean isMarket(){
        return GROUP_TYPE.MARKET.equals(getType());
    }

    public boolean isShip(){
        return GROUP_TYPE.SHIP.equals(getType());
    }

    public boolean isOutfit(){
        return GROUP_TYPE.OUTFIT.equals(getType());
    }

}
