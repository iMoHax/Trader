package ru.trader.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NavigableSet;

public abstract class ItemStat {

    protected abstract void update(Offer offer, double price);

    public abstract OFFER_TYPE getType();

    public abstract Item getItem();

    public abstract double getAvg();

    public abstract Offer getBest();

    public abstract int getOffersCount();

    public abstract NavigableSet<Offer> getOffers();

    public abstract Offer getMin();

    public abstract Offer getMax();

    public abstract boolean isEmpty();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemStat)) return false;

        ItemStat itemStat = (ItemStat) o;
        return getType() == itemStat.getType() && getItem().equals(itemStat.getItem());
    }

    @Override
    public int hashCode() {
        int result = getItem().hashCode();
        result = 31 * result + getType().hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append(getItem());
        sb.append(", ").append(getType());
        sb.append(", avg=").append(getAvg());
        sb.append(", best=").append(getBest());
        sb.append(", min=").append(getMin());
        sb.append(", max=").append(getMax());

        sb.append("}");
        return sb.toString();
    }

    protected Offer getFake() {
        return new Offer() {

            @Override
            public Item getItem() {
                return getItem();
            }

            @Override
            public OFFER_TYPE getType() {
                return getType();
            }

            @Override
            public double getPrice() {
                return Double.NaN;
            }

            @Override
            public Vendor getVendor() {
                return NONE_VENDOR;
            }

            @Override
            public boolean hasType(OFFER_TYPE offerType) {
                return false;
            }

            @Override
            public boolean hasItem(Item item) {
                return false;
            }

            @Override
            protected void setPrice(double price) {
                throw new UnsupportedOperationException("Is fake offer, change unsupported");
            }

            @Override
            protected void setVendor(Vendor vendor) {
                throw new UnsupportedOperationException("Is fake offer, change unsupported");
            }
        };
    }

    private static Vendor NONE_VENDOR = new Vendor() {

        @Override
        public String getName() {
            return "None";
        }

        @Override
        protected Collection<Offer> getOffers() {
            return new ArrayList<>();
        }

        @Override
        protected Collection<Item> getItems(OFFER_TYPE offerType) {
            return new ArrayList<>();
        }

        @Override
        protected Offer getOffer(OFFER_TYPE offerType, Item item) {
            return null;
        }

        @Override
        protected boolean hasOffer(OFFER_TYPE offerType, Item item) {
            return false;
        }

        @Override
        protected void addOffer(Offer offer) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        protected void removeOffer(Offer offer) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public void setName(String name) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public double getX() {
            return 0;
        }

        @Override
        public void setX(double x) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public double getY() {
            return 0;
        }

        @Override
        public void setY(double y) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public double getZ() {
            return 0;
        }

        @Override
        public void setZ(double z) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }
    };
}
