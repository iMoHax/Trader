package ru.trader.model;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ru.trader.analysis.Route;
import ru.trader.core.*;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
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

    public static Order get(OrderModel order){
        return order.getOrder();
    }

    public RouteModel get(Route route) {
        return new RouteModel(route, market);
    }

    public static Route get(RouteModel route){
        return route.getRoute();
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

    public static Place get(SystemModel model){
        if (isFake(model)) return null;
        return model.getSystem();
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

    public static Vendor get(StationModel model){
        if (isFake(model)) return null;
        return model.getStation();
    }

    public GroupModel get(Group group){
        if (group == null) return null;
        return new GroupModel(group);
    }

    public static Group get(GroupModel group){
        return group.getGroup();
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

    public static Item get(ItemModel item){
        if (isFake(item)) return null;
        return item.getItem();
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

    public static Offer get(OfferModel offer){
        return offer.getOffer();
    }

    public OfferModel get(Offer offer, ItemModel item){
        if (offer == null) return null;
        //always create new offer model
        OfferModel res = new OfferModel(offer, item, market);
        offers.put(offer, new WeakReference<>(res));
        return res;
    }

    public ProfileModel get(Profile profile){
        return new ProfileModel(profile, market);
    }

    public static Profile get(ProfileModel profile){
        return profile.getProfile();
    }

    public void clear(){
        items.clear();
        systems.clear();
        stations.clear();
        offers.clear();
    }

    public static SystemModel NONE_SYSTEM = new FAKE_SYSTEM_MODEL();
    public static StationModel NONE_STATION = new FAKE_STATION_MODEL();
    public static ItemModel NONE_ITEM = new FAKE_ITEM_MODEL();

    public static boolean isFake(StationModel station) {
        return station == null || station instanceof FAKE_STATION_MODEL;
    }

    public static boolean isFake(SystemModel system) {
        return system == null || system instanceof FAKE_SYSTEM_MODEL;
    }

    public static boolean isFake(ItemModel item) {
        return item == null || item instanceof FAKE_ITEM_MODEL;
    }

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
            return "";
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
        public StationModel get(String name) {
            return ModelFabric.NONE_STATION;
        }

        @Override
        public List<String> getStationNames() {
            return Collections.emptyList();
        }

        @Override
        public ObservableList<String> getStationNamesList() {
            return FXCollections.observableArrayList(ModelFabric.NONE_STATION.getName());
        }

        @Override
        public List<String> getStationNames(SERVICE_TYPE service) {
            return Collections.emptyList();
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
            throw new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public String getFullName() {
            return "";
        }

        @Override
        public FACTION getFaction() {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public void setFaction(FACTION faction) {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public GOVERNMENT getGovernment() {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public void setGovernment(GOVERNMENT government) {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public void setName(String value) {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public double getDistance() {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public void setDistance(double value) {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public boolean hasService(SERVICE_TYPE service) {
            return false;
        }

        @Override
        public Collection<SERVICE_TYPE> getServices() {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public void addService(SERVICE_TYPE service) {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public void removeService(SERVICE_TYPE service) {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public SystemModel getSystem() {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public List<OfferModel> getSells() {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public List<OfferModel> getBuys() {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public OfferModel add(OFFER_TYPE type, ItemModel item, double price, long count) {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public void remove(OfferModel offer) {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public boolean hasSell(ItemModel item) {
            return false;
        }

        @Override
        public boolean hasBuy(ItemModel item) {
            return false;
        }

        @Override
        public double getDistance(StationModel other) {
            throw  new UnsupportedOperationException("Is fake station, unsupported");
        }

        @Override
        public String toString() {
            return "";
        }
    }

    public static class FAKE_ITEM_MODEL extends ItemModel {

        FAKE_ITEM_MODEL() {
            super();
        }

        FAKE_ITEM_MODEL(Item item, MarketModel market) {
            super(item, market);
        }

        @Override
        Item getItem() {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public String getId() {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void setName(String value) {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public ReadOnlyStringProperty nameProperty() {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public ReadOnlyDoubleProperty avgBuyProperty() {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public ReadOnlyObjectProperty<OfferModel> minBuyProperty() {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public ReadOnlyObjectProperty<OfferModel> maxBuyProperty() {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public ReadOnlyObjectProperty<OfferModel> bestBuyProperty() {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public ReadOnlyDoubleProperty avgSellProperty() {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public ReadOnlyObjectProperty<OfferModel> minSellProperty() {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public ReadOnlyObjectProperty<OfferModel> maxSellProperty() {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public ReadOnlyObjectProperty<OfferModel> bestSellProperty() {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public List<OfferModel> getSeller() {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public List<OfferModel> getBuyer() {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public boolean isMarketItem() {
            return false;
        }

        @Override
        public void refresh() {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public void refresh(OFFER_TYPE type) {
            throw new UnsupportedOperationException("Is fake item, unsupported");
        }

        @Override
        public String toString() {
            return "";
        }
    }
}
