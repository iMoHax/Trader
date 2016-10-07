package ru.trader.store.imp.entities;

import org.jetbrains.annotations.Nullable;

public interface ItemData {

    @Nullable
    Long getId();
    String getName();
    @Nullable
    String getGroup();
    long getBuyOfferPrice();
    long getSellOfferPrice();
    long getSupply();
    long getDemand();

}
