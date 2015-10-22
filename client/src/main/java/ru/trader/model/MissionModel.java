package ru.trader.model;

import ru.trader.analysis.CrawlerSpecificator;
import ru.trader.analysis.RouteReserve;
import ru.trader.core.Offer;
import ru.trader.store.simple.SimpleOffer;

import java.util.Collection;

public class MissionModel {
    private final StationModel target;
    private final ItemModel item;
    private final long count;
    private final double profit;
    private final Offer offer;
    private long need;
    private Collection<RouteReserve> reserves;

    public MissionModel(StationModel target, double profit) {
        this.target = target;
        this.profit = profit;
        item = null;
        count = 0;
        offer = null;
        need = 0;
    }

    public MissionModel(StationModel target, long count, double profit) {
        this.target = target;
        this.count = count;
        this.profit = profit;
        this.item = null;
        offer = null;
        need = 0;
    }


    public MissionModel(StationModel target, ItemModel item, long count, double profit) {
        this.target = target;
        this.item = item;
        this.count = count;
        this.profit = profit;
        offer = SimpleOffer.fakeBuy(target.getStation(), item.getItem(), profit/count, count);
        need = count;
    }

    public StationModel getTarget() {
        return target;
    }

    public ItemModel getItem() {
        return item;
    }

    public long getCount() {
        return count;
    }

    public double getProfit() {
        return profit;
    }

    public boolean isSupply(){
        return offer != null;
    }

    public boolean isDelivery(){
        return count > 0;
    }

    public boolean isCourier(){
        return count == 0;
    }

    @Override
    public String toString() {
        return "MissionModel{" +
                "target=" + target +
                ", item=" + item +
                ", count=" + count +
                ", profit=" + profit +
                "} ";
    }

    public void toSpecification(CrawlerSpecificator specificator){
        if (isSupply()){
            specificator.buy(offer);
        } else
        if (isCourier() || isDelivery()){
            specificator.add(target.getStation(), true);
        }
    }

    Offer getOffer(){
        return offer;
    }

    Collection<RouteReserve> getReserves() {
        return reserves;
    }

    void setReserves(Collection<RouteReserve> reserves) {
        assert this.reserves == null;
        this.reserves = reserves;
    }

    void complete(Collection<OrderModel> orders){
        if (isSupply()){
            for (OrderModel order : orders) {
                if (item.equals(order.getOffer().getItem()) && target.equals(order.getBuyer())){
                    for (RouteReserve reserve : reserves) {
                        if (order.getOffer().getOffer().equals(reserve.getOrder().getSell())){
                            need -= order.getCount();
                        }
                    }
                }
            }
        }
    }

    public boolean isCompleted(){
        return need <= 0;
    }
}
