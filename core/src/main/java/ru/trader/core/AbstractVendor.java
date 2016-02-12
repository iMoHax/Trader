package ru.trader.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.Connectable;

import java.time.LocalDateTime;
import java.util.Objects;

public abstract class AbstractVendor implements Vendor {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractVendor.class);

    protected abstract Offer createOffer(OFFER_TYPE type, Item item, double price, long count);
    protected abstract void updateName(String name);
    protected abstract void updateFaction(FACTION faction);
    protected abstract void updateGovernment(GOVERNMENT government);
    protected abstract void updateType(STATION_TYPE type);
    protected abstract void updateEconomic(ECONOMIC_TYPE economic);
    protected abstract void updateSubEconomic(ECONOMIC_TYPE economic);
    protected abstract void updateDistance(double distance);
    protected abstract void addService(SERVICE_TYPE service);
    protected abstract void removeService(SERVICE_TYPE service);
    protected abstract void addOffer(Offer offer);
    protected abstract void removeOffer(Offer offer);
    protected abstract void updateModifiedTime(LocalDateTime time);

    protected AbstractMarket getMarket(){
        Place place = getPlace();
        if (place != null && place instanceof AbstractPlace){
            return ((AbstractPlace) place).getMarket();
        }
        return null;
    }

    @Override
    public final void setName(String name) {
        LOG.trace("Change name of vendor {} to {}", this, name);
        updateName(name);
        changed();
    }

    @Override
    public final void setFaction(FACTION faction){
        LOG.trace("Change faction of vendor {} to {}", this, faction);
        updateFaction(faction);
        changed();
    }

    @Override
    public final void setGovernment(GOVERNMENT government){
        LOG.trace("Change government of vendor {} to {}", this, government);
        updateGovernment(government);
        changed();
    }

    @Override
    public final void setType(STATION_TYPE type){
        LOG.trace("Change type of vendor {} to {}", this, type);
        updateType(type);
        changed();
    }

    @Override
    public final void setEconomic(ECONOMIC_TYPE economic) {
        LOG.trace("Change economic of vendor {} to {}", this, economic);
        updateEconomic(economic);
        changed();
    }

    @Override
    public final void setSubEconomic(ECONOMIC_TYPE economic){
        LOG.trace("Change sub economic of vendor {} to {}", this, economic);
        updateSubEconomic(economic);
        changed();
    }

    @Override
    public final void setDistance(double distance) {
        LOG.trace("Change distance of vendor {} to {}", this, distance);
        updateDistance(distance);
        changed();
    }

    @Override
    public final void add(SERVICE_TYPE service) {
        LOG.trace("Add service {} to vendor {}", service, this);
        addService(service);
        changed();
    }

    @Override
    public final void remove(SERVICE_TYPE service) {
        LOG.trace("Remove offer {} from vendor {}", service, this);
        removeService(service);
        changed();
    }

    @Override
    public final void add(Offer offer){
        LOG.trace("Add offer {} to vendor {}", offer, this);
        addOffer(offer);
        changed();
        AbstractMarket market = getMarket();
        if (market != null){
            market.onAdd(offer);
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
        LOG.trace("Remove offer {} from vendor {}", offer, this);
        removeOffer(offer);
        changed();
        AbstractMarket market = getMarket();
        if (market != null){
            market.onRemove(offer);
        }
    }

    @Override
    public final void setModifiedTime(LocalDateTime time) {
        LOG.trace("Change modified time of vendor {} to {}", this, time);
        changed(time);
    }

    private void changed(){
        changed(LocalDateTime.now());
    }

    private void changed(LocalDateTime time) {
        updateModifiedTime(time);
        AbstractMarket market = getMarket();
        if (market != null){
            market.setChange(true);
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
