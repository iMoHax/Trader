package ru.trader.model.support;

import ru.trader.model.ItemDescModel;
import ru.trader.model.ItemModel;
import ru.trader.model.OfferModel;
import ru.trader.model.VendorModel;

public class ChangeMarketListener {

    public void nameChange(ItemModel item, String oldName, String newName){

    }

    public void nameChange(VendorModel vendor, String oldName, String newName) {

    }

    public void add(ItemDescModel item) {

    }

    public void priceChange(OfferModel offer, double oldPrice, double newPrice) {
    }

    public void add(OfferModel offer) {

    }

    public void add(VendorModel vendor) {
    }

    public void remove(OfferModel offer) {
    }
}
