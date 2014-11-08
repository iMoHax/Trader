package ru.trader.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractOffer implements Offer {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractOffer.class);

    protected abstract void updatePrice(double price);
    protected abstract void updateCount(long count);

    protected AbstractMarket getMarket(){
        Vendor vendor = getVendor();
        if (vendor != null && vendor instanceof AbstractVendor){
            return ((AbstractVendor) vendor).getMarket();
        }
        return null;
    }

    @Override
    public final void setPrice(double price) {
        AbstractMarket market = getMarket();
        if (market != null){
            LOG.debug("Change price of offer {} to {}", this, price);
            market.updatePrice(this, price);
            market.setChange(true);
        } else {
            updatePrice(price);
        }
    }

    @Override
    public final void setCount(long count) {
        AbstractMarket market = getMarket();
        if (market != null){
            LOG.debug("Change count of offer {} to {}", this, count);
            updateCount(count);
            market.setChange(true);
        } else {
            updateCount(count);
        }
    }


}
