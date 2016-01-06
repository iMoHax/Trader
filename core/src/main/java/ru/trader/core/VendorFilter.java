package ru.trader.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;

public class VendorFilter {
    private final static Logger LOG = LoggerFactory.getLogger(VendorFilter.class);

    private boolean disable;
    private boolean skipIllegal;
    private boolean dontBuy;
    private boolean dontSell;
    private final Collection<Item> buyExcludes;
    private final Collection<Item> sellExcludes;

    public VendorFilter() {
        buyExcludes = new ArrayList<>();
        sellExcludes = new ArrayList<>();
        skipIllegal = false;
    }

    public boolean isDisable() {
        return disable;
    }

    public void setDisable(boolean disable) {
        this.disable = disable;
    }

    public boolean isSkipIllegal() {
        return skipIllegal;
    }

    public void setSkipIllegal(boolean skipIllegal) {
        this.skipIllegal = skipIllegal;
    }

    public boolean isDontBuy() {
        return dontBuy;
    }

    public void dontBuy(boolean dontBuy){
        this.dontBuy = dontBuy;
    }

    public boolean isDontSell() {
        return dontSell;
    }

    public void dontSell(boolean dontSell) {
        this.dontSell = dontSell;
    }

    public void addBuyExclude(Item item){
        buyExcludes.add(item);
    }

    public void removeBuyExclude(Item item){
        buyExcludes.remove(item);
    }

    public void clearBuyExcludes(){
        buyExcludes.clear();
    }

    public Collection<Item> getBuyExcludes(){
        return buyExcludes;
    }

    public void addSellExclude(Item item){
        sellExcludes.add(item);
    }

    public void removeSellExclude(Item item){
        sellExcludes.remove(item);
    }

    public void clearSellExcludes(){
        sellExcludes.clear();
    }

    public Collection<Item> getSellExcludes(){
        return sellExcludes;
    }

    public boolean isFiltered(Offer offer){
        if (disable) return false;
        if (skipIllegal && offer.isIllegal()) return true;
        switch (offer.getType()) {
            case SELL: return dontSell || sellExcludes.contains(offer.getItem());
            case BUY: return dontBuy || buyExcludes.contains(offer.getItem());
        }
        return false;
    }

    public boolean isFiltered(Vendor vendor, Item item, OFFER_TYPE offerType){
        if (disable) return false;
        if (skipIllegal && (item.isIllegal(vendor))) return true;
        switch (offerType) {
            case SELL: return dontSell || sellExcludes.contains(item);
            case BUY: return dontBuy || buyExcludes.contains(item);
        }
        return false;
    }

}
