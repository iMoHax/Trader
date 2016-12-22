package ru.trader.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.analysis.graph.Connectable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public abstract class AbstractPlace implements Place {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractPlace.class);
    private AbstractMarket market;

    private Collection<Place> controlling;

    protected abstract Vendor createVendor(String name);
    protected abstract void updateName(String name);
    protected abstract void updatePopulation(long population);
    protected abstract void updateFaction(FACTION faction);
    protected abstract void updateGovernment(GOVERNMENT government);
    protected abstract void updatePower(POWER power, POWER_STATE state);
    protected abstract void updateUpkeep(long upkeep);
    protected abstract void updateIncome(long income);
    protected abstract void updatePosition(double x, double y, double z);
    protected abstract void addVendor(Vendor vendor);
    protected abstract void removeVendor(Vendor vendor);

    protected final void setMarket(AbstractMarket market){
        assert this.market == null;
        this.market = market;
    }

    protected final AbstractMarket getMarket(){
        return market;
    }

    @Override
    public Collection<Place> getControllingSystems() {
        return controlling != null ? controlling : Collections.emptyList();
    }

    @Override
    public final void setName(String name){
        if (market != null){
            LOG.debug("Change name of place {} to {}", this, name);
            market.updateName(this, name);
            market.setChange(true);
        } else {
            updateName(name);
        }
    }

    @Override
    public final void setPosition(double x, double y, double z) {
        if (market != null){
            LOG.debug("Change position of place {} to {},{},{}", this, x, y, z);
            market.updatePosition(this, x, y, z);
            market.setChange(true);
        } else {
            updatePosition(x, y, z);
        }
    }

    @Override
    public final void setPopulation(long population) {
        if (market != null){
            LOG.debug("Change population of place {} to {}", this, population);
            market.updatePopulation(this, population);
            market.setChange(true);
        } else {
            updatePopulation(population);
        }
    }

    @Override
    public final void setFaction(FACTION faction){
        if (market != null){
            LOG.debug("Change faction of place {} to {}", this, faction);
            market.updateFaction(this, faction);
            market.setChange(true);
        } else {
            updateFaction(faction);
        }
    }

    @Override
    public final void setGovernment(GOVERNMENT government){
        if (market != null){
            LOG.debug("Change government of place {} to {}", this, government);
            market.updateGovernment(this, government);
            market.setChange(true);
        } else {
            updateGovernment(government);
        }
    }

    @Override
    public final void setPower(POWER power, POWER_STATE state){
        POWER_STATE old = getPowerState();
        if (market != null){
            LOG.debug("Change power of place {} to {} of {}", this, state, power);
            updatePower(power, state);
            if (!market.isBatch()) {
                updateControlling(old, state);
            }
            market.setChange(true);
        } else {
            updatePower(power, state);
            updateControlling(old, state);
        }
    }

    @Override
    public final void setUpkeep(long upkeep) {
        if (market != null){
            LOG.debug("Change upkeep of place {} to {}", this, upkeep);
            market.updateUpkeep(this, upkeep);
            market.setChange(true);
        } else {
            updateUpkeep(upkeep);
        }
    }

    @Override
    public final void setIncome(long income) {
        if (market != null){
            LOG.debug("Change income of place {} to {}", this, income);
            market.updateIncome(this, income);
            market.setChange(true);
        } else {
            updateIncome(income);
        }
    }

    @Override
    public final void add(Vendor vendor) {
        if (market != null){
            LOG.debug("Add vendor {} to place {}", vendor, this);
            addVendor(vendor);
            market.setChange(true);
            market.onAdd(vendor);
        } else {
            addVendor(vendor);
        }
    }

    @Override
    public Vendor addVendor(String name) {
        Vendor vendor = createVendor(name);
        add(vendor);
        return vendor;
    }

    @Override
    public final void remove(Vendor vendor) {
        if (market != null){
            LOG.debug("Remove vendor {} from place {}", vendor, this);
            removeVendor(vendor);
            market.setChange(true);
            market.onRemove(vendor);
        } else {
            removeVendor(vendor);
        }
    }

    protected void updateControlling(POWER_STATE oldState, POWER_STATE newState){
        LOG.debug("Update controlling systems, place {}, old = {}, new = {} ", this, oldState, newState);
        if (market == null) return;
        if (oldState == null) oldState = POWER_STATE.NONE;
        if (newState == null) newState = POWER_STATE.NONE;
        if (oldState.isExpansion() && !newState.isExpansion()){
            market.getInControllingRadius(this).forEach(p -> {
                if (p.getPowerState() == POWER_STATE.BLOCKED) p.setPower(getPower(), POWER_STATE.NONE);
            });
        }
        if (!oldState.isControl() && newState.isControl()){
            market.getInControllingRadius(this).forEach(p -> {
                if (p instanceof AbstractPlace) ((AbstractPlace)p).addControlling(this);
            });
        }
        if (oldState.isControl() && !newState.isControl()){
            market.getInControllingRadius(this).forEach(p -> {
                if (p instanceof AbstractPlace) ((AbstractPlace)p).removeControlling(this);
            });
        }
        if (!oldState.isExpansion() && newState.isExpansion()){
            market.getInControllingRadius(this).forEach(p -> {
                if (p.getPowerState() == POWER_STATE.NONE) p.setPower(getPower(), POWER_STATE.BLOCKED);
            });
        }
    }

    protected void addControlling(Place controllingSystem){
        if (controlling == null){
            controlling = new ArrayList<>();
        }
        controlling.add(controllingSystem);
        if (controlling.size() == 1){
            this.setPower(controllingSystem.getPower(), POWER_STATE.EXPLOITED);
        } else {
            boolean singlePower = true;
            POWER power = null;
            for (Place place : controlling) {
                if (power == null) power = place.getPower();
                 else if (!power.equals(place.getPower())){
                    singlePower = false;
                    break;
                }
            }
            if (singlePower){
                this.setPower(getPower(), POWER_STATE.EXPLOITED);
            } else {
                this.setPower(getPower(), POWER_STATE.CONTESTED);
            }
        }
    }

    protected void removeControlling(Place controllingSystem){
        if (controlling != null) {
            controlling.remove(controllingSystem);
            if (controlling.isEmpty()) this.setPower(POWER.NONE, POWER_STATE.NONE);
            else if (controlling.size() == 1){
                Place place = controlling.iterator().next();
                this.setPower(place.getPower(), POWER_STATE.EXPLOITED);
            } else {
                boolean singlePower = true;
                POWER power = null;
                for (Place place : controlling) {
                    if (power == null) power = place.getPower();
                    else if (!power.equals(place.getPower())){
                        singlePower = false;
                        break;
                    }
                }
                if (singlePower){
                    this.setPower(getPower(), POWER_STATE.EXPLOITED);
                } else {
                    this.setPower(getPower(), POWER_STATE.CONTESTED);
                }
            }
        }
    }

    protected void clearControlling() {
        if (controlling != null){
            controlling.clear();
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(@NotNull Connectable<Place> o) {
        Objects.requireNonNull(o, "Not compare with null");
        Place other = (Place) o;
        if (this == other) return 0;
        String name = getName();
        String otherName = other.getName();
        return name != null ? otherName != null ? name.compareTo(otherName) : -1 : 0;
    }

}
