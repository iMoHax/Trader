package ru.trader.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Offer implements Comparable<Offer>{
    private final static Logger LOG = LoggerFactory.getLogger(Offer.class);

    private Vendor vendor;
    private final Item item;
    private final OFFER_TYPE type;
    private double price;

    Offer(){
        item = null;
        type = null;
    }

    public Offer(OFFER_TYPE type, Item item, double price) {
        this.item = item;
        this.type = type;
        setPrice(price);
    }

    public Item getItem() {
        return item;
    }

    public OFFER_TYPE getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    void setPrice(double price) {
        this.price = price;
    }

    public Vendor getVendor() {
        return vendor;
    }

    void setVendor(Vendor vendor) {
        LOG.trace("Set vendor {} to item {}", vendor, this);
        this.vendor = vendor;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("").append(vendor);
        sb.append(", ").append(item);
        sb.append(", ").append(type);
        if (LOG.isTraceEnabled()){
            sb.append(", ").append(price);
        } else {
            sb.append(", ").append(getPrice());
        }
        sb.append('}');
        return sb.toString();
    }

    public boolean hasType(OFFER_TYPE offerType) {
        return this.type.equals(offerType);
    }

    public boolean hasItem(Item item) {
        return this.item.equals(item);
    }

    public boolean equalsPrice(Offer offer){
        return equalsType(offer) && price==offer.price;
    }

    public boolean equalsType(Offer offer){
        return offer!=null &&
               type.equals(offer.type) &&
               item.equals(offer.item);
    }

    @Override
    public int compareTo(@NotNull Offer other) {
        Objects.requireNonNull(other, "Not compare with null");
        if (this == other) return 0;
        int cmp = type.compareTo(other.type);
        if (cmp!=0) return cmp;
        cmp = Double.compare(price, other.price);
        if (cmp!=0) return cmp;
        cmp = vendor.compareTo(other.vendor);
        if (cmp!=0) return cmp;
        return item.compareTo(other.item);
    }

    public String toVString(){
        return String.format("%s (%.0f)", getVendor().getName(), price);
    }

    public String toIString(){
        return String.format("%s (%.0f)", item.getName(), price);
    }

}
