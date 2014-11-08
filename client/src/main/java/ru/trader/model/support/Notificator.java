package ru.trader.model.support;

import ru.trader.model.ItemModel;
import ru.trader.model.OfferModel;
import ru.trader.model.StationModel;
import ru.trader.model.SystemModel;

import java.util.ArrayList;
import java.util.Collection;

public class Notificator {

    private final Collection<ChangeMarketListener> listener = new ArrayList<>();
    private boolean alert = true;

    public void setAlert(boolean alert) {
        this.alert = alert;
    }

    public void add(ChangeMarketListener listener){
        synchronized (this.listener){
            this.listener.add(listener);
        }
    }

    public void addAll(Collection<? extends ChangeMarketListener> listener){
        synchronized (this.listener){
            this.listener.addAll(listener);
        }
    }

    public void clear() {
        synchronized (listener){
            listener.clear();
        }
    }

    public void sendAdd(ItemModel item) {
        if (alert) listener.forEach((c) -> c.add(item));
    }

    public void sendRemove(ItemModel item) {
        if (alert) listener.forEach((c) -> c.remove(item));
    }

    public void sendAdd(SystemModel system) {
        if (alert) listener.forEach((c) -> c.add(system));
    }

    public void sendRemove(SystemModel system) {
        if (alert) listener.forEach((c) -> c.remove(system));
    }

    public void sendAdd(StationModel station) {
        if (alert) listener.forEach((c) -> c.add(station));
    }

    public void sendRemove(StationModel station) {
        if (alert) listener.forEach((c) -> c.remove(station));
    }

    public void sendAdd(OfferModel offer) {
        if (alert) listener.forEach((c) -> c.add(offer));
    }

    public void sendRemove(OfferModel offer) {
        if (alert) listener.forEach((c) -> c.remove(offer));
    }

    public void sendPriceChange(OfferModel offer, double oldPrice, double newPrice) {
        if (alert) listener.forEach((c) -> c.priceChange(offer, oldPrice, newPrice));
    }


}
