package ru.trader.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.Connectable;

import java.util.Objects;

public abstract class AbstractVendor implements Vendor {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractVendor.class);

    protected abstract Offer createOffer(OFFER_TYPE type, Item item, double price, long count);
    protected abstract void updateName(String name);
    protected abstract void updateFaction(FACTION faction);
    protected abstract void updateGovernment(GOVERNMENT government);
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
    public final void setFaction(FACTION faction){
        AbstractMarket market = getMarket();
        if (market != null){
            LOG.debug("Change faction of vendor {} to {}", this, faction);
            market.updateFaction(this, faction);
            market.setChange(true);
        } else {
            updateFaction(faction);
        }
    }

    @Override
    public final void setGovernment(GOVERNMENT government){
        AbstractMarket market = getMarket();
        if (market != null){
            LOG.debug("Change government of vendor {} to {}", this, government);
            market.updateGovernment(this, government);
            market.setChange(true);
        } else {
            updateGovernment(government);
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

    @Override
    public int compareTo(@NotNull Connectable<Vendor> o) {
        Objects.requireNonNull(o, "Not compare with null");
        Vendor other = (Vendor) o;
        if (this == other) return 0;
        int cmp = Double.compare(getDistance(), other.getDistance());
        if (cmp!=0) return cmp;
        String name = getName();
        String otherName = other.getName();
        return name != null ? otherName != null ? name.compareTo(otherName) : -1 : 0;
    }
}
