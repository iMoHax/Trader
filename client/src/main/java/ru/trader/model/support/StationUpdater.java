package ru.trader.model.support;

import javafx.beans.property.*;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.controllers.MainController;
import ru.trader.core.*;
import ru.trader.model.*;

import java.util.Optional;


public class StationUpdater {
    private final static Logger LOG = LoggerFactory.getLogger(StationUpdater.class);
    private final static SERVICE_TYPE[] SERVICE_TYPES = SERVICE_TYPE.values();
    private final ObservableList<FakeOffer> offers;
    private final StringProperty name;
    private final ObjectProperty<STATION_TYPE> type;
    private final ObjectProperty<FACTION> faction;
    private final ObjectProperty<GOVERNMENT> government;
    private final ObjectProperty<ECONOMIC_TYPE> economic;
    private final ObjectProperty<ECONOMIC_TYPE> subEconomic;
    private final DoubleProperty distance;
    private final BooleanProperty[] services;
    private final MarketModel market;

    private SystemModel system;
    private StationModel station;
    private boolean updateOnly;

    public StationUpdater(MarketModel market) {
        this.market = market;
        this.offers = BindingsHelper.observableList(MainController.getMarket().itemsProperty(), FakeOffer::new);
        this.name = new SimpleStringProperty();
        this.type = new SimpleObjectProperty<>();
        this.distance = new SimpleDoubleProperty(0);
        this.faction = new SimpleObjectProperty<>(FACTION.NONE);
        this.government = new SimpleObjectProperty<>(GOVERNMENT.NONE);
        this.economic = new SimpleObjectProperty<>(ECONOMIC_TYPE.NONE);
        this.subEconomic = new SimpleObjectProperty<>(ECONOMIC_TYPE.NONE);
        this.services = new BooleanProperty[SERVICE_TYPES.length];
        for (int i = 0; i < services.length; i++) {
            services[i] = new SimpleBooleanProperty();
        }
        this.updateOnly = false;
    }

    public void edit(StationModel station){
        init(station.getSystem(), station);
    }

    public void create(SystemModel system){
        init(system, null);
    }

    private void init(SystemModel system, StationModel station){
        LOG.debug("Init update of {}", station);
        this.station = station;
        this.system = system;
        for (BooleanProperty service : services) {
            service.set(false);
        }
        if (station != null){
            name.setValue(station.getName());
            type.setValue(station.getType());
            faction.setValue(station.getFaction());
            government.setValue(station.getGovernment());
            distance.setValue(station.getDistance());
            economic.setValue(station.getEconomic());
            subEconomic.setValue(station.getSubEconomic());
            for (SERVICE_TYPE service : station.getServices()) {
                serviceProperty(service).set(true);
            }
            station.getAllSells().forEach(this::fillOffer);
            station.getAllBuys().forEach(this::fillOffer);
        } else {
            name.setValue("");
            type.setValue(null);
            faction.setValue(FACTION.NONE);
            government.setValue(GOVERNMENT.NONE);
            distance.setValue(0);
            economic.setValue(ECONOMIC_TYPE.NONE);
            subEconomic.setValue(ECONOMIC_TYPE.NONE);
        }
    }

    private void fillOffer(OfferModel offer) {
        for (FakeOffer o : offers) {
            if (offer.getItem().equals(o.item)) {
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

    public Optional<FakeOffer> getOffer(final ItemModel item){
        return offers.stream().filter(o -> o.hasItem(item)).findAny();
    }

    public StationModel getStation() {
        return station;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public STATION_TYPE getType() {
        return type.get();
    }

    public ObjectProperty<STATION_TYPE> typeProperty() {
        return type;
    }

    public void setType(STATION_TYPE type) {
        this.type.set(type);
    }

    public FACTION getFaction() {
        return faction.get();
    }

    public ObjectProperty<FACTION> factionProperty() {
        return faction;
    }

    public void setFaction(FACTION faction) {
        this.faction.set(faction);
    }

    public GOVERNMENT getGovernment() {
        return government.get();
    }

    public ObjectProperty<GOVERNMENT> governmentProperty() {
        return government;
    }

    public void setGovernment(GOVERNMENT government) {
        this.government.set(government);
    }

    public double getDistance() {
        return distance.get();
    }

    public DoubleProperty distanceProperty() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance.set(distance);
    }

    public ECONOMIC_TYPE getEconomic() {
        return economic.get();
    }

    public ObjectProperty<ECONOMIC_TYPE> economicProperty() {
        return economic;
    }

    public void setEconomic(ECONOMIC_TYPE economic) {
        this.economic.set(economic);
    }

    public ECONOMIC_TYPE getSubEconomic() {
        return subEconomic.get();
    }

    public ObjectProperty<ECONOMIC_TYPE> subEconomicProperty() {
        return subEconomic;
    }

    public void setSubEconomic(ECONOMIC_TYPE subEconomic) {
        this.subEconomic.set(subEconomic);
    }

    public BooleanProperty serviceProperty(SERVICE_TYPE service){
        return services[service.ordinal()];
    }

    public void add(int index, ItemModel item){
        offers.add(index, new FakeOffer(item));
    }

    public StationModel commit(){
        LOG.debug("Save changes of {}", station);
        if (isNew()) {
            Notificator notificator = market.getNotificator();
            notificator.setAlert(false);
            station = system.add(name.get());
            updateStation();
            notificator.setAlert(true);
            notificator.sendAdd(station);
        } else {
            station.setName(name.get());
            updateStation();
        }
        return station;
    }

    private void updateStation(){
        station.setType(type.get());
        station.setFaction(faction.get());
        station.setGovernment(government.get());
        station.setDistance(distance.get());
        station.setEconomic(economic.get());
        station.setSubEconomic(subEconomic.get());
        for (int i = 0; i < services.length; i++) {
            if (services[i].get()){
                station.addService(SERVICE_TYPES[i]);
            }
        }
        offers.forEach(FakeOffer::commit);
    }

    public void reset(){
        offers.forEach(FakeOffer::reset);
        station = null;
        system = null;
    }

    public boolean isNew() {
        return station == null;
    }

    public void setUpdateOnly(boolean updateOnly) {
        this.updateOnly = updateOnly;
    }

    public boolean isUpdateOnly() {
        return updateOnly;
    }

    public class FakeOffer {
        private final ItemModel item;
        private final DoubleProperty sprice;
        private final LongProperty supply;
        private final DoubleProperty bprice;
        private final LongProperty demand;
        private OfferModel sell;
        private OfferModel buy;

        public FakeOffer(ItemModel item){
            this.item = item;
            this.sprice = new SimpleDoubleProperty(0);
            this.supply = new SimpleLongProperty(0);
            this.bprice = new SimpleDoubleProperty(0);
            this.demand = new SimpleLongProperty(0);
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

        public DoubleProperty spriceProperty() {
            return sprice;
        }

        public long getSupply() {
            return supply.get();
        }

        public LongProperty supplyProperty() {
            return supply;
        }

        public void setSupply(long supply) {
            this.supply.set(supply);
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

        public long getDemand() {
            return demand.get();
        }

        public LongProperty demandProperty() {
            return demand;
        }

        public void setDemand(long demand) {
            this.demand.set(demand);
        }

        public boolean isChangeSell() {
            return sell!=null && (getSprice() != sell.getPrice() || getSupply() != sell.getCount());
        }

        public boolean isChangeBuy() {
            return buy!=null && (getBprice() != buy.getPrice() || getDemand() != buy.getCount());
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

        public double getOldSupply(){
            return sell != null ? sell.getCount() : 0;
        }

        public double getOldBprice() {
            return buy != null ? buy.getPrice() : 0;
        }

        public double getOldDemand(){
            return buy != null ? buy.getCount() : 0;
        }

        public void setSell(OfferModel sell) {
            this.sell = sell;
            sprice.set(sell.getPrice());
            supply.set(sell.getCount());
        }

        public void setBuy(OfferModel buy) {
            this.buy = buy;
            bprice.set(buy.getPrice());
            demand.set(buy.getCount());
        }

        public void reset(){
            if (sell != null){
                sell = null;
                sprice.setValue(Double.NaN);
                supply.setValue(Double.NaN);
            }
            if (buy != null){
                buy = null;
                bprice.setValue(Double.NaN);
                demand.setValue(Double.NaN);
            }
            //for fire change event
            sprice.setValue(0);
            supply.setValue(0);
            bprice.setValue(0);
            demand.setValue(0);
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
                    station.add(OFFER_TYPE.BUY, item, getBprice(), getDemand());
                }
            } else if (isRemoveBuy()) {
                LOG.trace("Is remove buy offer");
                if (updateOnly) {
                    LOG.trace("Is update only, skip");
                } else {
                    station.remove(buy);
                }
            } else if (isChangeBuy()){
                LOG.trace("Is change buy offer to {} ({})", getBprice(), getDemand());
                buy.setPrice(getBprice());
                buy.setCount(getDemand());
            } else {
                LOG.trace("No change buy offer");
            }

            if (isNewSell()){
                LOG.trace("Is new sell offer");
                if (updateOnly) {
                    LOG.trace("Is update only, skip");
                } else {
                    station.add(OFFER_TYPE.SELL, item, getSprice(), getSupply());
                }
            } else if (isRemoveSell()) {
                LOG.trace("Is remove sell offer");
                if (updateOnly) {
                    LOG.trace("Is update only, skip");
                } else {
                    station.remove(sell);
                }
            } else if (isChangeSell()){
                LOG.trace("Is change sell offer to {}({})", getSprice(), getSupply());
                sell.setPrice(getSprice());
                sell.setCount(getSupply());
            } else {
                LOG.trace("No change sell offer");
            }
        }


        @Override
        public String toString() {
            return "FakeOffer{" +
                    "item=" + item +
                    ", sprice=" + sprice.get() +
                    ", supply=" + supply.get() +
                    ", bprice=" + bprice.get() +
                    ", demand=" + demand.get() +
                    ", sell=" + sell +
                    ", buy=" + buy +
                    '}';
        }
    }

}
