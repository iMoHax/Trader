package ru.trader.model.support;

import javafx.beans.property.*;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.controllers.MainController;
import ru.trader.core.OFFER_TYPE;
import ru.trader.model.ItemModel;
import ru.trader.model.MarketModel;
import ru.trader.model.OfferModel;
import ru.trader.model.VendorModel;


public class VendorUpdater {
    private final static Logger LOG = LoggerFactory.getLogger(VendorUpdater.class);
    private final ObservableList<FakeOffer> offers;
    private final StringProperty name;
    private final DoubleProperty x;
    private final DoubleProperty y;
    private final DoubleProperty z;
    private final MarketModel market;
    private VendorModel vendor;
    private boolean updateOnly;

    public VendorUpdater(MarketModel market) {
        this.market = market;
        this.offers = BindingsHelper.observableList(MainController.getMarket().itemsProperty(), (item) -> new FakeOffer(item.getItem()));
        this.name = new SimpleStringProperty();
        this.x = new SimpleDoubleProperty(0);
        this.y = new SimpleDoubleProperty(0);
        this.z = new SimpleDoubleProperty(0);
        this.updateOnly = false;
    }

    public void init(VendorModel vendor){
        LOG.debug("Init update of {}", vendor);
        this.vendor = vendor;
        if (vendor != null){
            name.setValue(vendor.getName());
            x.setValue(vendor.getX());
            y.setValue(vendor.getY());
            z.setValue(vendor.getZ());
            vendor.getSells().forEach(this::fillOffer);
            vendor.getBuys().forEach(this::fillOffer);
        } else {
            name.setValue("");
            x.setValue(0);
            y.setValue(0);
            z.setValue(0);
        }
    }

    private void fillOffer(OfferModel offer) {
        for (FakeOffer o : offers) {
            if (offer.hasItem(o.item)) {
                switch (offer.getType()) {
                    case SELL:
                        o.setSell(offer);
                        break;
                    case BUY:
                        o.setBuy(offer);
                        break;
                }
                return;
            }
        }
    }

    public ObservableList<FakeOffer> getOffers() {
        return offers;
    }

    public VendorModel getVendor() {
        return vendor;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public double getX() {
        return x.get();
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public double getY() {
        return y.get();
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public double getZ() {
        return z.get();
    }

    public DoubleProperty zProperty() {
        return z;
    }

    public void add(int index, ItemModel item){
        offers.add(index, new FakeOffer(item));
    }

    public void commit(){
        LOG.debug("Save changes of {}", vendor);
        if (isNew()) {
            market.setAlert(false);
            vendor = market.newVendor(name.get());
            vendor.setPosition(x.get(), y.get(), z.get());
            offers.forEach(FakeOffer::commit);
            market.setAlert(true);
            market.add(vendor);
        } else {
            vendor.setName(name.get());
            vendor.setPosition(x.get(), y.get(), z.get());
            offers.forEach(FakeOffer::commit);
        }
    }

    public void reset(){
        offers.forEach(FakeOffer::reset);
        vendor = null;
    }

    public boolean isNew() {
        return vendor == null;
    }

    public void setUpdateOnly(boolean updateOnly) {
        this.updateOnly = updateOnly;
    }

    public boolean isUpdateOnly() {
        return updateOnly;
    }

    public class FakeOffer {
        private final ItemModel item;
        private DoubleProperty sprice;
        private DoubleProperty bprice;
        private OfferModel sell;
        private OfferModel buy;

        public FakeOffer(ItemModel item){
            this.item = item;
            this.sprice = new SimpleDoubleProperty(0);
            this.bprice = new SimpleDoubleProperty(0);
        }

        public ReadOnlyStringProperty nameProperty(){
            return item.nameProperty();
        }

        public double getSprice() {
            return sprice.get();
        }

        public void setSprice(double sprice) {
            this.sprice.set(sprice);
        }

        public double getBprice() {
            return bprice.get();
        }

        public void setBprice(double bprice) {
            this.bprice.set(bprice);
        }

        public DoubleProperty bpriceProperty() {
            return bprice;
        }

        public DoubleProperty spriceProperty() {
            return sprice;
        }

        public boolean isChangeSell() {
            return sell!=null && getSprice() != sell.getPrice();
        }

        public boolean isChangeBuy() {
            return buy!=null && getBprice() != buy.getPrice();
        }


        public boolean isNewSell() {
            return sell == null && getSprice() != 0;
        }

        public boolean isNewBuy() {
            return buy == null && getBprice() != 0;
        }

        public boolean isRemoveSell() {
            return sell != null && getSprice() == 0;
        }

        public boolean isRemoveBuy() {
            return buy != null && getBprice() == 0;
        }

        public boolean isBlank(){
            return sell == null && getSprice() == 0 && buy == null && getBprice() == 0;
        }

        public boolean hasItem(ItemModel item){
            return this.item.equals(item);
        }

        public ItemModel getItem() {
            return item;
        }

        public double getOldSprice() {
            return sell != null ? sell.getPrice() : 0;
        }

        public double getOldBprice() {
            return buy != null ? buy.getPrice() : 0;
        }

        public void setSell(OfferModel sell) {
            this.sell = sell;
            sprice.set(sell.getPrice());
        }

        public void setBuy(OfferModel buy) {
            this.buy = buy;
            bprice.set(buy.getPrice());
        }

        public void reset(){
            if (sell != null){
                sell = null;
                sprice.setValue(Double.NaN);
            }
            if (buy != null){
                buy = null;
                bprice.setValue(Double.NaN);
            }
            sprice.setValue(0);
            bprice.setValue(0);
        }

        private void commit(){
            LOG.trace("Commit changes of offers {}", this);
            if (isBlank()){
                LOG.trace("Is blank offer, skip");
                return;
            }

            if (isNewBuy()){
                LOG.trace("Is new buy offer");
                if (updateOnly) {
                    LOG.trace("Is update only, skip");
                } else {
                    vendor.add(market.newOffer(OFFER_TYPE.BUY, item, getBprice()));
                }
            } else if (isRemoveBuy()) {
                LOG.trace("Is remove buy offer");
                if (updateOnly) {
                    LOG.trace("Is update only, skip");
                } else {
                    vendor.remove(buy);
                }
            } else if (isChangeBuy()){
                LOG.trace("Is change buy price to {}", getBprice());
                buy.setPrice(getBprice());
            } else {
                LOG.trace("No change buy offer");
            }

            if (isNewSell()){
                LOG.trace("Is new sell offer");
                if (updateOnly) {
                    LOG.trace("Is update only, skip");
                } else {
                    vendor.add(market.newOffer(OFFER_TYPE.SELL, item, getSprice()));
                }
            } else if (isRemoveSell()) {
                LOG.trace("Is remove sell offer");
                if (updateOnly) {
                    LOG.trace("Is update only, skip");
                } else {
                    vendor.remove(sell);
                }
            } else if (isChangeSell()){
                LOG.trace("Is change sell price to {}", getSprice());
                sell.setPrice(getSprice());
            } else {
                LOG.trace("No change sell offer");
            }
        }


        @Override
        public String toString() {
            return "FakeOffer{" +
                    "item=" + item +
                    ", sprice=" + sprice.get() +
                    ", bprice=" + bprice.get() +
                    ", sell=" + sell +
                    ", buy=" + buy +
                    '}';
        }
    }

}
