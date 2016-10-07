package ru.trader.store.imp.entities;

import org.jetbrains.annotations.Nullable;
import ru.trader.core.FACTION;
import ru.trader.core.GOVERNMENT;
import ru.trader.core.POWER;
import ru.trader.core.POWER_STATE;

import java.util.Collection;

public interface StarSystemData {
    @Nullable
    Long getId();
    String getName();

    double getX();
    double getY();
    double getZ();

    @Nullable
    FACTION getFaction();
    @Nullable
    GOVERNMENT getGovernment();
    @Nullable
    POWER getPower();
    @Nullable
    POWER_STATE getPowerState();

    @Nullable
    Collection<StationData> getStations();
}
