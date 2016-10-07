package ru.trader.store.imp.entities;

import org.jetbrains.annotations.Nullable;
import ru.trader.core.*;

import java.time.LocalDateTime;
import java.util.Collection;

public interface StationData {
    @Nullable
    Long getId();
    String getName();

    double getDistance();

    @Nullable
    STATION_TYPE getType();
    @Nullable
    FACTION getFaction();
    @Nullable
    GOVERNMENT getGovernment();
    @Nullable
    ECONOMIC_TYPE getEconomic();
    @Nullable
    ECONOMIC_TYPE getSubEconomic();
    @Nullable
    Collection<SERVICE_TYPE> getServices();

    @Nullable
    Collection<ItemData> getCommodities();
    @Nullable
    Collection<ModuleData> getModules();
    @Nullable
    Collection<ShipData> getShips();

    @Nullable
    LocalDateTime getModifiedTime();

}
