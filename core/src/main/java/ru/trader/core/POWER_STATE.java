package ru.trader.core;

public enum POWER_STATE {
    CONTROL, EXPLOITED, EXPANSION, NONE, CONTESTED, HEADQUARTERS, BLOCKED, TURMOIL;

    public boolean isControl(){
        return this == CONTROL || this == HEADQUARTERS || this == TURMOIL;
    }

    public boolean isExploited(){
        return this == EXPLOITED;
    }

    public boolean isExpansion(){
        return this == EXPANSION;
    }

    public boolean isContested(){
        return this == CONTESTED;
    }
}
