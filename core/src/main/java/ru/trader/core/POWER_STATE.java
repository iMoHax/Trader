package ru.trader.core;

public enum POWER_STATE {
    CONTROL, EXPLOITED, EXPANSION, NONE, CONTESTED, HEADQUARTERS, BLOCKED, TURMOIL;

    boolean isControl(){
        return this == CONTROL || this == HEADQUARTERS || this == TURMOIL;
    }

    boolean isExploited(){
        return this == EXPLOITED || this == BLOCKED;
    }
}
