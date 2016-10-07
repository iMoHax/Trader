package ru.trader.store.imp.entities;

import org.jetbrains.annotations.Nullable;
import ru.trader.core.*;

import java.time.LocalDateTime;
import java.util.Collection;

public abstract class StationDataBase implements StationData {
    @Override
    public Long getId() {
        return null;
    }

    @Override
    public double getDistance() {
        return Double.NaN;
    }

    @Nullable
    @Override
    public STATION_TYPE getType() {
        return null;
    }

    @Nullable
    @Override
    public FACTION getFaction() {
        return null;
    }

    @Nullable
    @Override
    public GOVERNMENT getGovernment() {
        return null;
    }

    @Nullable
    @Override
    public ECONOMIC_TYPE getEconomic() {
        return null;
    }

    @Nullable
    @Override
    public ECONOMIC_TYPE getSubEconomic() {
        return null;
    }

    @Nullable
    @Override
    public Collection<SERVICE_TYPE> getServices() {
        return null;
    }

    @Nullable
    @Override
    public Collection<ItemData> getCommodities() {
        return null;
    }

    @Nullable
    @Override
    public Collection<ModuleData> getModules() {
        return null;
    }

    @Nullable
    @Override
    public Collection<ShipData> getShips() {
        return null;
    }

    @Nullable
    @Override
    public LocalDateTime getModifiedTime() {
        return null;
    }
}
