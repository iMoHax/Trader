package ru.trader.core;

public enum POWER_STATE {
    CONTROL, EXPLOITED, EXPANSION, NONE, CONTESTED, HEADQUARTERS;

    boolean isControl(){
        return this == CONTROL || this == HEADQUARTERS;
    }

    boolean isExploited(){
        return this == EXPLOITED;
    }
}
