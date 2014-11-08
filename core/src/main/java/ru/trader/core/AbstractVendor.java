package ru.trader.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractVendor implements Vendor {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractVendor.class);

    protected abstract Offer createOffer(OFFER_TYPE type, Item item, double price, long count);
    protected abstract void updateName(String name);
    protected abstract void updateDistance(double distance);
    protected abstract void addService(SERVICE_TYPE service);
    protected abstract void removeService(SERVICE_TYPE service);
    protected abstract void addOffer(Offer offer);
    protected abstract void removeOffer(Offer offer);

    protected AbstractMarket getMarket(){
        Place place = getPlace();
        if (place != null && place instanceof AbstractPlace){
            return ((AbstractPlace) place).getMarket();
        }
        return null;
    }

    @Override
    public final void setName(String name) {
        AbstractMarket market = getMarket();
        if (market != null){
            LOG.debug("Change name of vendor {} to {}", this, name);
            market.updateName(this, name);
            market.setChange(true);
        } else {
            updateName(name);
        }
    }

    @Override
    public final void setDistance(double distance) {
        AbstractMarket market = getMarket();
        if (market != null){
            LOG.debug("Change distance of vendor {} to {}", this, distance);
            updateDistance(distance);
            market.setChange(true);
        } else {
            updateDistance(distance);
        }
    }

    @Override
    public final void add(SERVICE_TYPE service) {
        AbstractMarket market = getMarket();
        if (market != null){
            LOG.trace("Add service {} to vendor {}", service, this);
            addService(service);
            market.setChange(true);
        } else {
            addService(service);
        }
    }

    @Override
    public final void remove(SERVICE_TYPE service) {
        AbstractMarket market = getMarket();
        if (market != null){
            LOG.trace("Remove offer {} from vendor {}", service, this);
            removeService(service);
            market.setChange(true);
        } else {
            removeService(service);
        }
    }

    public final void add(Offer offer){
        AbstractMarket market = getMarket();
        if (market != null){
            LOG.trace("Add offer {} to vendor {}", offer, this);
            addOffer(offer);
            market.setChange(true);
            market.onAdd(offer);
        } else {
            addOffer(offer);
        }
    }

    @Override
    public Offer addOffer(OFFER_TYPE type, Item item, double price, long count) {
        Offer offer = createOffer(type, item, price, count);
        add(offer);
        return offer;
    }

    public final void remove(Offer offer){
        assert this.equals(offer.getVendor());
        AbstractMarket market = getMarket();
        if (market != null){
            LOG.trace("Remove offer {} from vendor {}", offer, this);
            removeOffer(offer);
            market.setChange(true);
            market.onRemove(offer);
        } else {
            removeOffer(offer);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

}
