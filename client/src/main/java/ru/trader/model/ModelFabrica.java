package ru.trader.model;

import ru.trader.core.*;
import ru.trader.store.simple.SimpleItem;
import ru.trader.store.simple.SimpleOffer;
import ru.trader.store.simple.SimpleVendor;

import java.lang.ref.WeakReference;
import java.util.HashMap;

public class ModelFabrica {

    private static final HashMap<Item, WeakReference<ItemModel>> items = new HashMap<>();
    private static final HashMap<Vendor, WeakReference<VendorModel>> vendors = new HashMap<>();
    private static final HashMap<Offer, WeakReference<OfferModel>> offers = new HashMap<>();

    private static final HashMap<ItemStat, WeakReference<ItemStatModel>> stats = new HashMap<>();

    public static ItemModel buildItemModel(String name, MarketModel market){
        return getModel(new SimpleItem(name), market);
    }

    public static VendorModel buildModel(String name, MarketModel market){
        return getModel(new SimpleVendor(name), market);
    }

    public static OfferModel buildModel(OFFER_TYPE type, ItemModel item, double price, MarketModel market) {
        return getModel(new SimpleOffer(type, item.getItem(), price), market);
    }

    public static ItemDescModel buildModel(ItemModel item, ItemStat sell, ItemStat buy, MarketModel market) {
        return new ItemDescModelImpl(item, getModel(sell, market), getModel(buy, market));
    }

    public static OfferDescModel buildModel(OfferModel offer, ItemStat sell, ItemStat buy, MarketModel market){
        return new OfferDescModel(offer, getModel(sell, market), getModel(buy, market));
    }


    public static VendorModel getModel(Vendor vendor, MarketModel market){
        if (vendor == null) return null;
        VendorModel res=null;
        WeakReference<VendorModel> ref = vendors.get(vendor);
        if (ref != null){
            res = ref.get();
        }
        if (res == null){
            res = new VendorModel(vendor, market);
            vendors.put(vendor, new WeakReference<>(res));
        }
        return res;
    }


    public static ItemModel getModel(Item item, MarketModel market){
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

    public static OfferModel getModel(Offer offer, MarketModel market){
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

    public static ItemStatModel getModel(ItemStat itemStat, MarketModel market){
        if (itemStat == null) return null;
        ItemStatModel res = null;
        WeakReference<ItemStatModel> ref = stats.get(itemStat);
        if (ref != null){
            res = ref.get();
        }
        if (res == null){
            res = new ItemStatModel(itemStat, market);
            stats.put(itemStat, new WeakReference<>(res));
        }
        return res;
    }

    public static void clear(){
        items.clear();
        vendors.clear();
        offers.clear();
        stats.clear();
    }

}
