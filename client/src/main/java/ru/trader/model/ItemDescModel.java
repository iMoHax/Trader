package ru.trader.model;

import javafx.beans.property.*;
import ru.trader.core.OFFER_TYPE;

import java.util.List;


public interface ItemDescModel {
    ItemModel getItem();

    ReadOnlyStringProperty nameProperty();

    ReadOnlyDoubleProperty avgBuyProperty();

    ReadOnlyObjectProperty<OfferModel> minBuyProperty();

    ReadOnlyObjectProperty<OfferModel> maxBuyProperty();

    ReadOnlyObjectProperty<OfferModel> bestBuyProperty();

    ReadOnlyDoubleProperty avgSellProperty();

    ReadOnlyObjectProperty<OfferModel> minSellProperty();

    ReadOnlyObjectProperty<OfferModel> maxSellProperty();

    ReadOnlyObjectProperty<OfferModel> bestSellProperty();


    boolean hasItem(OfferModel offer);

    List<OfferModel> getSeller();

    List<OfferModel> getBuyer();

    void refresh();

    void refresh(OFFER_TYPE type);
}
