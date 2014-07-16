package ru.trader.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

public class ItemStat {
    private final static Logger LOG = LoggerFactory.getLogger(ItemStat.class);

    private final Item item;
    private final OFFER_TYPE type;
    private final TreeSet<Offer> offers;
    private double sum;
    private double avg;


    public ItemStat(Item item, OFFER_TYPE offerType) {
        this.offers = new TreeSet<>();
        this.item = item;
        this.type = offerType;
        this.sum = 0;
        this.avg = Double.NaN;
    }

    void put(Offer offer){
        LOG.trace("Put offer {} to item stat {}", offer, this);
        assert offer.hasType(type) && offer.hasItem(item);
        if (offers.add(offer)){
            double price = offer.getPrice();
            sum += price;
            avg = sum / offers.size();
            LOG.trace("After this = {}", this);
        }
    }

    void remove(Offer offer){
        LOG.trace("Remove offer {} from item stat {}", offer, this);
        assert offer.hasType(type) && offer.hasItem(item);
        if (offers.remove(offer)){
            if (offers.size()>0){
                double price = offer.getPrice();
                sum -= price;
                avg = sum / offers.size();
            } else {
                sum = 0; avg = Double.NaN;
            }
            LOG.trace("After this = {}", this);
        }
    }

    void update(Offer offer, double price){
        LOG.trace("Update offer {} from item stat {}", offer, this);
        assert offer.hasType(type) && offer.hasItem(item) && offers.contains(offer);
        double oldPrice = offer.getPrice();
        offers.remove(offer);
        offer.setPrice(price);
        offers.add(offer);
        sum += price - oldPrice;
        avg = sum / offers.size();
        LOG.trace("After update this = {}", this);
    }

    public OFFER_TYPE getType(){
        return type;
    }

    public Item getItem() {
        return item;
    }

    public double getAvg(){
        return avg;
    }

    public Offer getBest() {
        if (offers.isEmpty()) return getFake();
        return type.getOrder() > 0 ? offers.first() : offers.last();
    }

    public int getOffersCount(){
        return offers.size();
    }

    public Collection<Offer> getOffers() {
        return Collections.unmodifiableCollection(offers);
    }

    public Offer getMin() {
        if (offers.isEmpty()) return getFake();
        return offers.first();
    }

    public Offer getMax() {
        if (offers.isEmpty()) return getFake();
        return offers.last();
    }

    public boolean isEmpty(){
        return offers.isEmpty();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append(item);
        sb.append(", ").append(type);
        if (LOG.isTraceEnabled()){
            sb.append(", count=").append(offers.size());
            sb.append(", sum=").append(sum);
        }
        sb.append(", avg=").append(avg);
        sb.append(", best=").append(getBest());
        sb.append(", min=").append(getMin());
        sb.append(", max=").append(getMax());

        sb.append("}");
        return sb.toString();
    }


    private Offer getFake() {
        return new Offer() {

            @Override
            public Item getItem() {
                return item;
            }

            @Override
            public OFFER_TYPE getType() {
                return type;
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
            void setPrice(double price) {
                throw new UnsupportedOperationException("Is fake offer, change unsupported");
            }

            @Override
            void setVendor(Vendor vendor) {
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
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemStat)) return false;

        ItemStat itemStat = (ItemStat) o;

        return type == itemStat.type && item.equals(itemStat.item);

    }

    @Override
    public int hashCode() {
        int result = item.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
