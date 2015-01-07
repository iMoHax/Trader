package ru.trader.model;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.trader.core.*;
import ru.trader.graph.PathRoute;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ModelFabric {

    private final MarketModel market;
    private final HashMap<Item, WeakReference<ItemModel>> items = new HashMap<>();
    private final HashMap<Place, WeakReference<SystemModel>> systems = new HashMap<>();
    private final HashMap<Vendor, WeakReference<StationModel>> stations = new HashMap<>();
    private final HashMap<Offer, WeakReference<OfferModel>> offers = new HashMap<>();

    public ModelFabric(MarketModel market) {
        this.market = market;
    }

    public OrderModel get(Order order) {
        return new OrderModel(get(order.getSell()), get(order.getBuy()), order.getCount());
    }

    public PathRouteModel get(PathRoute path) {
        return new PathRouteModel(path, market);
    }

    public SystemModel get(Place system){
        if (system == null) return NONE_SYSTEM;
        SystemModel res=null;
        WeakReference<SystemModel> ref = systems.get(system);
        if (ref != null){
            res = ref.get();
        }
        if (res == null){
            res = new SystemModel(system, market);
            systems.put(system, new WeakReference<>(res));
        }
        return res;
    }


    public StationModel get(Vendor station){
        if (station == null) return NONE_STATION;
        StationModel res=null;
        WeakReference<StationModel> ref = stations.get(station);
        if (ref != null){
            res = ref.get();
        }
        if (res == null){
            res = new StationModel(station, market);
            stations.put(station, new WeakReference<>(res));
        }
        return res;
    }


    public ItemModel get(Item item){
        if (item == null) return null;
        ItemModel res=null;
        WeakReference<ItemModel> ref = items.get(item);
        if (ref != null){
            res = ref.get();
        }
        if (res == null){
            res = new ItemModel(item, market);
            items.put(item, new WeakReference<>(res));
        }
        return res;
    }

    public OfferModel get(Offer offer){
        if (offer == null) return null;
        OfferModel res = null;
        WeakReference<OfferModel> ref = offers.get(offer);
        if (ref != null){
            res = ref.get();
        }
        if (res == null){
            res = new OfferModel(offer, market);
            offers.put(offer, new WeakReference<>(res));
        }
        return res;
    }

    public OfferModel get(Offer offer, ItemModel item){
        if (offer == null) return null;
        //always create new offer model
        OfferModel res = new OfferModel(offer, item, market);
        offers.put(offer, new WeakReference<>(res));
        return res;
    }

    public void clear(){
        items.clear();
        systems.clear();
        stations.clear();
        offers.clear();
    }

    public static SystemModel NONE_SYSTEM = new FAKE_SYSTEM_MODEL();
    public static StationModel NONE_STATION = new FAKE_STATION_MODEL();

    private static class FAKE_SYSTEM_MODEL extends SystemModel {
        FAKE_SYSTEM_MODEL() {
            super();
        }

        @Override
        Place getSystem() {
            throw new UnsupportedOperationException("Is fake system, change unsupported");
        }

        @Override
        public String getName() {
            throw new UnsupportedOperationException("Is fake system, change unsupported");
        }

        @Override
        public void setName(String value) {
            throw new UnsupportedOperationException("Is fake system, change unsupported");
        }

        @Override
        public ReadOnlyStringProperty nameProperty() {
            throw new UnsupportedOperationException("Is fake system, change unsupported");
        }

        @Override
        public double getX() {
            return Double.NaN;
        }

        @Override
        public double getY() {
            return Double.NaN;
        }

        @Override
        public double getZ() {
            return Double.NaN;
        }

        @Override
        public void setPosition(double x, double y, double z) {
            throw new UnsupportedOperationException("Is fake system, change unsupported");
        }

        @Override
        public double getDistance(SystemModel other) {
            throw new UnsupportedOperationException("Is fake system, change unsupported");
        }

        @Override
        public List<StationModel> getStations() {
            throw new UnsupportedOperationException("Is fake system, change unsupported");
        }

        @Override
        public ObservableList<StationModel> getStationsList() {
            return FXCollections.observableArrayList(ModelFabric.NONE_STATION);
        }

        @Override
        public List<StationModel> getStations(SERVICE_TYPE service) {
            throw new UnsupportedOperationException("Is fake system, change unsupported");
        }

        @Override
        public StationModel add(String name) {
            throw new UnsupportedOperationException("Is fake system, change unsupported");
        }

        @Override
        public String toString() {
            return "";
        }
    }

    private static class FAKE_STATION_MODEL extends StationModel {

        FAKE_STATION_MODEL() {
            super();
        }

        @Override
        Vendor getStation() {
            throw new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public String getName() {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public void setName(String value) {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public double getDistance() {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public void setDistance(double value) {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public boolean hasService(SERVICE_TYPE service) {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public Collection<SERVICE_TYPE> getServices() {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public void addService(SERVICE_TYPE service) {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public void removeService(SERVICE_TYPE service) {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public SystemModel getSystem() {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public List<OfferModel> getSells() {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public List<OfferModel> getBuys() {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public OfferModel add(OFFER_TYPE type, ItemModel item, double price, long count) {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public void remove(OfferModel offer) {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public boolean hasSell(ItemModel item) {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public boolean hasBuy(ItemModel item) {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public double getDistance(StationModel other) {
            throw  new UnsupportedOperationException("Is fake system, unsupported");
        }

        @Override
        public String toString() {
            return "";
        }
    }
}
