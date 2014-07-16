package ru.trader.model;

import javafx.beans.property.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.Vendor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VendorModel {
    private final static Logger LOG = LoggerFactory.getLogger(VendorModel.class);
    private final Vendor vendor;
    private final MarketModel market;
    private StringProperty name;

    VendorModel(Vendor vendor, MarketModel market) {
        this.vendor = vendor;
        this.market = market;
    }

    public String getName() {return name != null ? name.get() : vendor.getName();}

    public void setName(String value) {
        if (getName().equals(value)) return;
        LOG.info("Change name vendor {} to {}", vendor, value);
        market.updateName(this, value);
        if (name != null) name.set(value);
    }

    public ReadOnlyStringProperty nameProperty() {
        if (name == null) {
            name = new SimpleStringProperty(vendor.getName());
        }
        return name;
    }

    public List<OfferModel> getSells() {
        return vendor.getAllSellOffers().stream().map(market::asModel).collect(Collectors.toList());
    }

    public List<OfferModel> getBuys() {
        return vendor.getAllBuyOffers().stream().map(market::asModel).collect(Collectors.toList());
    }

    public <T> List<T> getSells(Function<OfferModel, T> mapper) {
        return vendor.getAllSellOffers().stream().map(market::asModel).map(mapper).collect(Collectors.toList());
    }

    public <T> List<T> getBuys(Function<OfferModel, T> mapper) {
        return vendor.getAllBuyOffers().stream().map(market::asModel).map(mapper).collect(Collectors.toList());
    }


    Vendor getVendor() {
        return vendor;
    }

    public void add(OfferModel offer){
        LOG.info("Add offer {} to vendor {}", offer, vendor);
        market.add(this, offer);
    }

    public void remove(OfferModel offer) {
        LOG.info("Remove offer {} from vendor {}", offer, vendor);
        market.remove(this, offer);
    }

    public boolean hasSell(ItemModel item) {
        return vendor.hasSell(item.getItem());
    }

    public boolean hasBuy(ItemModel item) {
        return vendor.hasBuy(item.getItem());
    }

    @Override
    public String toString() {
        if (LOG.isTraceEnabled()){
            final StringBuilder sb = new StringBuilder("VendorModel{");
            sb.append("nameProp=").append(name);
            sb.append(", vendor=").append(vendor.toString());
            sb.append('}');
            return sb.toString();
        }
        return vendor.toString();
    }

    public Optional<OfferModel> getSell(ItemModel item){
        return Optional.ofNullable(market.asModel(vendor.getSell(item.getItem())));
    }

    public Optional<OfferModel> getBuy(ItemModel item){
        return Optional.ofNullable(market.asModel(vendor.getBuy(item.getItem())));
    }

}
