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
        return faction != null && getIllegalFactions().contains(vendor.getFaction())
               || government != null && getIllegalGovernments().contains(government);
    }

    Collection<FACTION> getIllegalFactions();
    void setIllegal(FACTION faction, boolean illegal);
    Collection<GOVERNMENT> getIllegalGovernments();
    void setIllegal(GOVERNMENT government, boolean illegal);

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
