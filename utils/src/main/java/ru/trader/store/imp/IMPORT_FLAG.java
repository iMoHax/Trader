package ru.trader.store.imp;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum IMPORT_FLAG {
    STARSYSTEMS, STATIONS, ITEMS,
    ADD_STARSYSTEMS,
    REMOVE_STATIONS, ADD_STATIONS,
    REMOVE_COMMODITY, ADD_COMMODITY,
    REMOVE_MODULE, ADD_MODULE,
    REMOVE_SHIP, ADD_SHIP;

    public static final Set<IMPORT_FLAG> UPDATE_ONLY = Collections.unmodifiableSet(EnumSet.of(STARSYSTEMS, STATIONS, ITEMS));
    public static final Set<IMPORT_FLAG> ADD_AND_UPDATE = Collections.unmodifiableSet(EnumSet.of(STARSYSTEMS, STATIONS, ITEMS, ADD_STARSYSTEMS, ADD_STATIONS, ADD_COMMODITY, ADD_SHIP, ADD_MODULE));
    public static final Set<IMPORT_FLAG> ADD_AND_REMOVE = Collections.unmodifiableSet(EnumSet.of(ADD_STATIONS, ADD_COMMODITY, ADD_SHIP, ADD_MODULE, REMOVE_STATIONS, REMOVE_COMMODITY, REMOVE_MODULE, REMOVE_SHIP));
    public static final Set<IMPORT_FLAG> UPDATE_MARKET = Collections.unmodifiableSet(EnumSet.of(ITEMS, ADD_STARSYSTEMS, ADD_STATIONS, ADD_COMMODITY, ADD_SHIP, ADD_MODULE));


}
