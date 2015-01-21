package ru.trader.core;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface Offer extends Comparable<Offer> {

    Item getItem();
    OFFER_TYPE getType();
    Vendor getVendor();

    double getPrice();
    void setPrice(double price);

    long getCount();
    void setCount(long count);

    default boolean hasType(OFFER_TYPE offerType) {
        return getType().equals(offerType);
    }

    default boolean hasItem(Item item) {
        return getItem().equals(item);
    }

    @Override
    default int compareTo(@NotNull Offer other) {
        Objects.requireNonNull(other, "Not compare with null");
        if (this == other) return 0;
        int cmp = getType().compareTo(other.getType());
        if (cmp!=0) return cmp;
        cmp = Double.compare(getPrice(), other.getPrice());
        if (cmp!=0) return cmp;
        cmp = getVendor().compareTo(other.getVendor());
        if (cmp!=0) return cmp;
        return getItem().compareTo(other.getItem());
    }

    default String toPString(){
        return String.format("%.0f (%s - %s (%.0f Ls))", getPrice(), getVendor().getPlace().getName(), getVendor().getName(), getVendor().getDistance());
    }

    default String toIString(){
        return String.format("%s (%.0f)", getItem().getName(), getPrice());
    }

}
