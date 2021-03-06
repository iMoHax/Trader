package ru.trader.core;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;

public interface Item extends Comparable<Item> {
    String getName();
    void setName(String name);

    default boolean isIllegal(Vendor vendor){
        FACTION faction = vendor.getFaction();
        GOVERNMENT government = vendor.getGovernment();
        return isIllegal(vendor.getPlace(), faction, government);
    }

    default boolean isIllegal(Place place, FACTION faction, GOVERNMENT government){
        if (place != null){
            POWER power = place.getPower();
            if (power != null){
                if (power.isLegal(faction, this, place.getPowerState())) return false;
                if (power.isIllegal(faction, this, place.getPowerState())) return true;
            }
        }
        if (faction != null && getLegalFactions().contains(faction)) return false;
        if (government != null && getLegalGovernments().contains(government)) return false;
        return faction != null && getIllegalFactions().contains(faction) ||
               government != null && getIllegalGovernments().contains(government);
    }

    Collection<FACTION> getIllegalFactions();
    void setIllegal(FACTION faction, boolean illegal);
    Collection<GOVERNMENT> getIllegalGovernments();
    void setIllegal(GOVERNMENT government, boolean illegal);

    Collection<FACTION> getLegalFactions();
    void setLegal(FACTION faction, boolean legal);
    Collection<GOVERNMENT> getLegalGovernments();
    void setLegal(GOVERNMENT government, boolean legal);

    Group getGroup();

    @Override
    default int compareTo(@NotNull Item other){
        Objects.requireNonNull(other, "Not compare with null");
        if (this == other) return 0;
        String name = getName();
        String otherName = other.getName();
        return name != null ? otherName != null ? name.compareTo(otherName) : -1 : 0;
    }

}
