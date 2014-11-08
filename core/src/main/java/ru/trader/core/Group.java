package ru.trader.core;

public interface Group {
    public String getName();
    public GROUP_TYPE getType();

    public default boolean isMarket(){
        return GROUP_TYPE.MARKET.equals(getType());
    }

    public default boolean isShip(){
        return GROUP_TYPE.SHIP.equals(getType());
    }

    public default boolean isOutfit(){
        return GROUP_TYPE.OUTFIT.equals(getType());
    }

}
