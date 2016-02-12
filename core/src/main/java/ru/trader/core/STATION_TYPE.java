package ru.trader.core;

public enum STATION_TYPE {
    STARPORT(true, false),
    CORIOLIS_STARPORT(true, false),
    OCELLUS_STARPORT(true, false),
    ORBIS_STARPORT(true, false),
    OUTPOST,
    CIVILIAN_OUTPOST,
    COMMERCIAL_OUTPOST,
    INDUSTRIAL_OUTPOST,
    MILITARY_OUTPOST,
    MINING_OUTPOST,
    SCIENTIFIC_OUTPOST,
    UNSANCTIONED_OUTPOST,
    PLANETARY_PORT(true, true),
    PLANETARY_OUTPOST(true, true);

    private final boolean largeLandpad;
    private final boolean planetary;

    STATION_TYPE() {
        this(false, false);
    }

    STATION_TYPE(boolean largeLandpad, boolean planetary) {
        this.largeLandpad = largeLandpad;
        this.planetary = planetary;
    }

    public boolean hasLargeLandpad() {
        return largeLandpad;
    }

    public boolean isPlanetary() {
        return planetary;
    }
}
