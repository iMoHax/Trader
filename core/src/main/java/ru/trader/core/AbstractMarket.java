package ru.trader.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.stream.Collectors;

public abstract class AbstractMarket implements Market {
    private final static Logger LOG = LoggerFactory.getLogger(AbstractMarket.class);

    private boolean change;
    private boolean batch;

    protected abstract Place createPlace(String name, double x, double y, double z);

    protected abstract Group createGroup(String name, GROUP_TYPE type);

    protected abstract Item createItem(String name, Group group);

    protected abstract void addGroup(Group group);

    protected abstract void removeGroup(Group group);

    protected abstract void addPlace(Place place);

    protected abstract void removePlace(Place place);

    protected abstract void addItem(Item item);

    protected abstract void removeItem(Item item);

    protected void executeBatch() {
    }

    @Override
    public void startBatch() {
        LOG.debug("Start batch");
        batch = true;
    }

    @Override
    public final void doBatch() {
        LOG.debug("End batch, updated");
        executeBatch();
        updateControllings();
        batch = false;
    }

    @Override
    public boolean isBatch() {
        return batch;
    }

    @Override
    public final void add(Place place) {
        LOG.debug("Add place {} to market {}", place, this);
        change = true;
        if (place instanceof AbstractPlace) {
            ((AbstractPlace) place).setMarket(this);
            if (!isBatch()) {
                updateControlling((AbstractPlace) place);
            }
        }
        addPlace(place);
    }

    @Override
    public Place addPlace(String name, double x, double y, double z) {
        Place place = createPlace(name, x, y, z);
        add(place);
        return place;
    }

    @Override
    public final void remove(Place place) {
        LOG.debug("Remove place {} from market {}", place, this);
        change = true;
        removePlace(place);
        if (!isBatch()) {
            removeControlling(place);
        }
    }

    @Override
    public final void add(Group group) {
        LOG.debug("Add group {} to market {}", group, this);
        change = true;
        addGroup(group);
    }

    @Override
    public Group addGroup(String name, GROUP_TYPE type) {
        Group group = createGroup(name, type);
        add(group);
        return group;
    }

    @Override
    public void remove(Group group) {
        LOG.debug("Remove group {} from market {}", group, this);
        change = true;
        removeGroup(group);
    }

    @Override
    public final void add(Item item) {
        LOG.debug("Add item {} to market {}", item, this);
        change = true;
        if (item instanceof AbstractItem) {
            ((AbstractItem) item).setMarket(this);
        }
        addItem(item);
    }

    @Override
    public Item addItem(String name, Group group) {
        Item item = createItem(name, group);
        add(item);
        return item;
    }

    @Override
    public final void remove(Item item) {
        LOG.debug("Remove item {} from market {}", item, this);
        change = true;
        removeItem(item);
    }

    @Override
    public boolean isChange() {
        return change;
    }

    @Override
    public void commit() {
        change = false;
    }

    protected final void setChange(boolean change) {
        this.change = change;
    }

    protected void onAdd(Vendor vendor) {
    }

    protected void onRemove(Vendor vendor) {
    }

    protected void onAdd(Offer offer) {
    }

    protected void onRemove(Offer offer) {
    }

    protected void updateName(AbstractPlace place, String name) {
        place.updateName(name);
    }

    protected void updatePopulation(AbstractPlace place, long population) {
        place.updatePopulation(population);
    }

    protected void updateFaction(AbstractPlace place, FACTION faction) {
        place.updateFaction(faction);
    }

    protected void updateGovernment(AbstractPlace place, GOVERNMENT government) {
        place.updateGovernment(government);
    }

    protected void updateUpkeep(AbstractPlace place, long upkeep) {
        place.updateUpkeep(upkeep);
    }

    protected void updateIncome(AbstractPlace place, long income) {
        place.updateIncome(income);
    }

    protected void updatePosition(AbstractPlace place, double x, double y, double z) {
        place.updatePosition(x, y, z);
    }

    protected void updatePrice(AbstractOffer offer, double price) {
        ItemStat itemStat = getStat(offer);
        if (itemStat instanceof AbstractItemStat) {
            ((AbstractItemStat) itemStat).updatePrice(offer, price);
        }
    }

    protected void updateName(AbstractItem item, String name) {
        item.updateName(name);
    }


    protected void updateControlling(AbstractPlace place) {
        POWER_STATE state = place.getPowerState();
        if (state != null && state.isControl()) {
            getInControllingRadius(place).forEach(p -> {
                if (p instanceof AbstractPlace) {
                    ((AbstractPlace) p).addControlling(place);
                }
            });
        } else {
            getInControllingRadius(place).forEach(p -> {
                if (p.getPowerState() != null && p.getPowerState().isControl()) {
                    place.addControlling(p);
                }
            });
        }

    }

    protected void removeControlling(Place place) {
        POWER_STATE state = place.getPowerState();
        if (state != null && state.isControl()) {
            getInControllingRadius(place).forEach(p -> {
                if (p instanceof AbstractPlace) {
                    ((AbstractPlace) p).removeControlling(place);
                }
            });
        }
    }

    protected void updateControllings() {
        LOG.debug("Update all controlling");
        Collection<Place> places = get();
        Collection<Place> controllings = places.stream()
                .filter(place -> place.getPowerState() != null && place.getPowerState().isControl())
                .collect(Collectors.toList());
        for (Place place : places) {
            if (place instanceof AbstractPlace) {
                ((AbstractPlace)place).clearControlling();
                for (Place controlling : controllings) {
                    if (place != controlling && controlling.getDistance(place) <= CONTROLLING_RADIUS) {
                        ((AbstractPlace)place).addControlling(controlling);
                    }
                }
            }
        }
    }
}

