package ru.trader.store.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Item;
import ru.trader.core.OFFER_TYPE;
import ru.trader.core.Offer;
import ru.trader.core.Vendor;

public class SimpleOffer extends Offer {
    private final static Logger LOG = LoggerFactory.getLogger(SimpleOffer.class);

    private Vendor vendor;
    private final Item item;
    private final OFFER_TYPE type;
    private volatile double price;

    public SimpleOffer(OFFER_TYPE type, Item item, double price) {
        this.item = item;
        this.type = type;
        setPrice(price);
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
    public double getPrice() {
        return price;
    }

    @Override
    protected void setPrice(double price) {
        this.price = price;
    }

    @Override
    public Vendor getVendor() {
        return vendor;
    }

    @Override
    protected void setVendor(Vendor vendor) {
        LOG.trace("Set vendor {} to item {}", vendor, this);
        this.vendor = vendor;
    }

}
