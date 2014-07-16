package ru.trader.model.support;

import com.sun.javafx.collections.ImmutableObservableList;
import javafx.beans.Observable;
import javafx.beans.binding.*;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.value.ObservableValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.model.ItemModel;
import ru.trader.model.OfferModel;
import ru.trader.model.VendorModel;


public class ModelBindings {
    private final static Logger LOG = LoggerFactory.getLogger(ModelBindings.class);


    public static StringBinding asString(final OfferModel offer){
        return Bindings.createStringBinding(offer::toVString, offer.priceProperty(), offer.getVendor().nameProperty());
    }

    public static StringBinding asItemString(final OfferModel offer){
        return Bindings.createStringBinding(offer::toIString, offer.priceProperty(), offer.getItem().nameProperty());
    }

    public static DoubleBinding price(final ObservableValue<OfferModel> offer){
        ObservableValue<OfferModel> offerBind = offerPrice(offer);
        return asDouble(offerBind, offerBind);
    }

    public static DoubleBinding diff(final ObservableValue<OfferModel> of1, final ObservableValue<OfferModel> of2){
        return price(of1).subtract(price(of2));
    }

    public static DoubleBinding diff(final ReadOnlyDoubleProperty price, final ObservableValue<OfferModel> offer){
        return diff(offer, price).negate();
    }

    public static DoubleBinding diff(final ObservableValue<OfferModel> offer, final ReadOnlyDoubleProperty price){
        return price(offer).subtract(price);
    }

    public static StringBinding asString(final ObservableValue<OfferModel> offer){
        ObservableValue<OfferModel> offerBind = offerPrice(offer, true);
        return asString(offerBind, offerBind);
    }

    public static StringBinding asItemString(final ObservableValue<OfferModel> offer){
        ObservableValue<OfferModel> offerBind = offerPrice(offer, true);
        return asString(offerBind, offerBind);
    }



    private static StringBinding asItemString(final ObservableValue<OfferModel> offer, final Observable... dependencies){
        return Bindings.createStringBinding(() -> {
            OfferModel o = offer.getValue();
            return o != null ? o.toIString(): "";
        }, dependencies);
    }


    private static StringBinding asString(final ObservableValue<OfferModel> offer, final Observable... dependencies){
        return Bindings.createStringBinding(() -> {
            OfferModel o = offer.getValue();
            return o != null ? o.toVString() : "";
        }, dependencies);
    }

    private static DoubleBinding asDouble(final ObservableValue<OfferModel> offer, final Observable... dependencies){
        return Bindings.createDoubleBinding(() -> {
            OfferModel o = offer.getValue();
            return o != null ? offer.getValue().getPrice() : Double.NaN;
        }, dependencies);
    }

    public static ObservableValue<OfferModel> offerPrice(final ObservableValue<OfferModel> offer){
        return offerPrice(offer, false);
    }

    public static ObservableValue<OfferModel> offerPrice(final ObservableValue<OfferModel> offer, final boolean deep){
        if ((offer == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new ObjectBinding<OfferModel>() {
            {
                super.bind(offer);
                bind(offer.getValue());
                offer.addListener((observable, oldValue, newValue) -> {
                    LOG.trace("unbind {}, bind {}", oldValue, newValue);
                    unbind(oldValue);
                    bind(newValue);
                });
            }

            @Override
            public void dispose() {
                unbind(offer.getValue());
                super.unbind(offer);
            }

            @Override
            protected OfferModel computeValue() {
                return offer.getValue();
            }

            @Override
            public javafx.collections.ObservableList<?> getDependencies() {
                if (deep){
                    OfferModel model = offer.getValue();
                    return new ImmutableObservableList<Observable>(offer, model.priceProperty(), model.getVendor().nameProperty(), model.getItem().nameProperty());
                }
                else
                    return new ImmutableObservableList<Observable>(offer, offer.getValue().priceProperty());
            }

            private void bind(OfferModel model){
                if (model == null) return;
                super.bind(model.priceProperty());
                if (deep){
                    super.bind(model.getVendor().nameProperty());
                    super.bind(model.getItem().nameProperty());
                }
            }

            private void unbind(OfferModel model){
                if (model == null) return;
                super.unbind(model.priceProperty());
                if (deep){
                    super.unbind(model.getVendor().nameProperty());
                    super.unbind(model.getItem().nameProperty());
                }
            }
        };
    }

    public static ObservableValue<ItemModel> itemName(final ObservableValue<ItemModel> item){
        if ((item == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new ObjectBinding<ItemModel>() {
            {
                super.bind(item);
                super.bind(item.getValue().nameProperty());
                item.addListener((observable, oldValue, newValue) -> {
                    super.unbind(oldValue.nameProperty());
                    super.bind(newValue.nameProperty());
                });
            }

            @Override
            public void dispose() {
                super.unbind(item.getValue().nameProperty());
                super.unbind(item);
            }

            @Override
            protected ItemModel computeValue() {
                return item.getValue();
            }

            @Override
            public javafx.collections.ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(item, item.getValue().nameProperty());
            }
        };
    }


    public static ObservableValue<VendorModel> vendorName(final ObservableValue<VendorModel> vendor){
        if ((vendor == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new ObjectBinding<VendorModel>() {
            {
                super.bind(vendor);
                super.bind(vendor.getValue().nameProperty());
                vendor.addListener((observable, oldValue, newValue) -> {
                    super.unbind(oldValue.nameProperty());
                    super.bind(newValue.nameProperty());
                });
            }

            @Override
            public void dispose() {
                super.unbind(vendor.getValue().nameProperty());
                super.unbind(vendor);
            }

            @Override
            protected VendorModel computeValue() {
                return vendor.getValue();
            }

            @Override
            public javafx.collections.ObservableList<?> getDependencies() {
                return new ImmutableObservableList<Observable>(vendor, vendor.getValue().nameProperty());
            }
        };
    }

}
