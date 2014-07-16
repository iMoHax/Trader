package ru.trader.model;

import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.OFFER_TYPE;

import java.util.List;

public class ItemDescModelImpl implements ItemDescModel {
    private final static Logger LOG = LoggerFactory.getLogger(ItemDescModelImpl.class);

    protected final ItemModel item;
    protected final ItemStatModel statSell;
    protected final ItemStatModel statBuy;

    public ItemDescModelImpl(ItemModel item, ItemStatModel statSell, ItemStatModel statBuy) {
        this.item = item;
        this.statSell = statSell;
        this.statBuy = statBuy;
    }

    @Override
    public ItemModel getItem(){
        return item;
    }

    @Override
    public ReadOnlyStringProperty nameProperty() {
        return item.nameProperty();
    }

    @Override
    public ReadOnlyDoubleProperty avgBuyProperty() {
        return statBuy.avgProperty();
    }

    @Override
    public ReadOnlyObjectProperty<OfferModel> minBuyProperty() {
        return statBuy.minProperty();
    }

    @Override
    public ReadOnlyObjectProperty<OfferModel> maxBuyProperty() {
        return statBuy.maxProperty();
    }

    @Override
    public ReadOnlyObjectProperty<OfferModel> bestBuyProperty() {
        return statBuy.bestProperty();
    }

    @Override
    public ReadOnlyDoubleProperty avgSellProperty() {
        return statSell.avgProperty();
    }

    @Override
    public ReadOnlyObjectProperty<OfferModel> minSellProperty() {
        return statSell.minProperty();
    }

    @Override
    public ReadOnlyObjectProperty<OfferModel> maxSellProperty() {
        return statSell.maxProperty();
    }

    @Override
    public ReadOnlyObjectProperty<OfferModel> bestSellProperty() {
        return statSell.bestProperty();
    }

    @Override
    public List<OfferModel> getSeller() {
        return statSell.getOffers();
    }

    @Override
    public List<OfferModel> getBuyer() {
        return statBuy.getOffers();
    }

    @Override
    public void refresh(){
        LOG.trace("Refresh stats of itemDesc {}", this);
        statBuy.refresh();
        statSell.refresh();
    }

    @Override
    public void refresh(OFFER_TYPE type){
        LOG.trace("Refresh {} stat of itemDesc {}", type, this);
        switch (type) {
            case SELL: statSell.refresh();
                break;
            case BUY: statBuy.refresh();
                break;
        }
    }

    public boolean hasItem(ItemModel item){
        return this.item.getItem().equals(item.getItem());
    }

    @Override
    public boolean hasItem(OfferModel offer){
        return this.item.getItem().equals(offer.getOffer().getItem());
    }

    @Override
    public String toString() {
        return item.toString();
    }
}

