package ru.trader.core;

import java.util.Collection;
import java.util.Collections;

public abstract class AbstractItemStat implements ItemStat {

    protected void updatePrice(AbstractOffer offer, double price){
        offer.updatePrice(price);
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
                return AbstractItemStat.this.getItem();
            }

            @Override
            public OFFER_TYPE getType() {
                return AbstractItemStat.this.getType();
            }

            @Override
            public Vendor getVendor() {
                return NONE_VENDOR;
            }

            @Override
            public double getPrice() {
                return Double.NaN;
            }

            @Override
            public void setPrice(double price) {
                throw new UnsupportedOperationException("Is fake offer, change unsupported");
            }

            @Override
            public long getCount() {
                return 0;
            }

            @Override
            public void setCount(long count) {
                throw new UnsupportedOperationException("Is fake offer, change unsupported");
            }
        };
    }

    private static Place FAKE_PLACE = new Place() {
        @Override
        public String getName() {
            return "None";
        }

        @Override
        public void setName(String name) {
            throw new UnsupportedOperationException("Is fake place, change unsupported");
        }

        @Override
        public double getX() {
            return 0;
        }

        @Override
        public double getY() {
            return 0;
        }

        @Override
        public double getZ() {
            return 0;
        }

        @Override
        public void setPosition(double x, double y, double z) {
            throw new UnsupportedOperationException("Is fake place, change unsupported");
        }

        @Override
        public Collection<Vendor> get() {
            return Collections.singleton(NONE_VENDOR);
        }

        @Override
        public Vendor addVendor(String name) {
            throw new UnsupportedOperationException("Is fake place, change unsupported");
        }

        @Override
        public void add(Vendor vendor) {
            throw new UnsupportedOperationException("Is fake place, change unsupported");
        }

        @Override
        public void remove(Vendor vendor) {
            throw new UnsupportedOperationException("Is fake place, change unsupported");
        }
    };

    private static Vendor NONE_VENDOR = new Vendor() {
        @Override
        public String getName() {
            return "None";
        }

        @Override
        public void setName(String name) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public Place getPlace() {
            return FAKE_PLACE;
        }

        @Override
        public double getDistance() {
            return 0;
        }

        @Override
        public void setDistance(double distance) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public void add(SERVICE_TYPE service) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public void remove(SERVICE_TYPE service) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public boolean has(SERVICE_TYPE service) {
            return false;
        }

        @Override
        public Offer addOffer(OFFER_TYPE type, Item item, double price, long count) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public void add(Offer offer) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public void remove(Offer offer) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public Collection<Offer> get(OFFER_TYPE type) {
            return null;
        }

        @Override
        public Offer get(OFFER_TYPE type, Item item) {
            return null;
        }

        @Override
        public boolean has(OFFER_TYPE type, Item item) {
            return false;
        }
    };
}
