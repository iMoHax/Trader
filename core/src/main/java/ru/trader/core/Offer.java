package ru.trader.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public abstract class Offer implements Comparable<Offer> {
    private final static Logger LOG = LoggerFactory.getLogger(Offer.class);

    public abstract Item getItem();

    public abstract OFFER_TYPE getType();

    public abstract double getPrice();
    protected abstract void setPrice(double price);

    public abstract Vendor getVendor();
    protected abstract void setVendor(Vendor vendor);

    public boolean hasType(OFFER_TYPE offerType) {
        return getType().equals(offerType);
    }

    public boolean hasItem(Item item) {
        return getItem().equals(item);
    }

    public boolean equalsPrice(Offer offer){
        return equalsType(offer) && getPrice() == offer.getPrice();
    }

    public boolean equalsType(Offer offer){
        return offer != null &&
               getType().equals(offer.getType()) &&
               getItem().equals(offer.getItem());
    }

    @Override
    public int compareTo(@NotNull Offer other) {
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

    public String toVString(){
        return String.format("%s (%.0f)", getVendor().getName(), getPrice());
    }

    public String toIString(){
        return String.format("%s (%.0f)", getItem().getName(), getPrice());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("").append(getVendor());
        sb.append(", ").append(getItem());
        sb.append(", ").append(getType());
        if (LOG.isTraceEnabled()){
            sb.append(", ").append(getPrice());
        } else {
            sb.append(", ").append(getPrice());
        }
        sb.append('}');
        return sb.toString();
    }
}
