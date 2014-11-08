package ru.trader.store.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

public class SimpleOffer extends AbstractOffer {
    private final static Logger LOG = LoggerFactory.getLogger(SimpleOffer.class);

    private Vendor vendor;
    private final Item item;
    private final OFFER_TYPE type;
    private volatile double price;
    private volatile long count;

    public SimpleOffer(OFFER_TYPE type, Item item, double price, long count) {
        this.item = item;
        this.type = type;
        this.price = price;
        this.count = count;
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public OFFER_TYPE getType() {
        return type;
    }

    @Override
    public Vendor getVendor() {
        return vendor;
    }

    protected void setVendor(Vendor vendor) {
        assert this.vendor == null;
        LOG.trace("Set vendor {} to offer {}", vendor, this);
        this.vendor = vendor;
    }

    @Override
    public double getPrice() {
        return price;
    }

    @Override
    protected void updatePrice(double price) {
        this.price = price;
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public void updateCount(long count) {
        this.count = count;
    }

}
