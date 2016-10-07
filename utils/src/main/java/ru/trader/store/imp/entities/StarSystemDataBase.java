package ru.trader.store.imp.entities;

import org.jetbrains.annotations.Nullable;
import ru.trader.core.FACTION;
import ru.trader.core.GOVERNMENT;
import ru.trader.core.POWER;
import ru.trader.core.POWER_STATE;

import java.util.Collection;

public abstract class StarSystemDataBase implements StarSystemData {
    @Override
    public Long getId() {
        return null;
    }

    @Override
    public double getX() {
        return Double.NaN;
    }

    @Override
    public double getY() {
        return Double.NaN;
    }

    @Override
    public double getZ() {
        return Double.NaN;
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
    public POWER getPower() {
        return null;
    }

    @Nullable
    @Override
    public POWER_STATE getPowerState() {
        return null;
    }

    @Nullable
    @Override
    public Collection<StationData> getStations() {
        return null;
    }
}
