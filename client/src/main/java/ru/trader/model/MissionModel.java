package ru.trader.model;

import ru.trader.analysis.CrawlerSpecificator;
import ru.trader.core.Offer;
import ru.trader.store.simple.SimpleOffer;

public class MissionModel {
    private final StationModel target;
    private final ItemModel item;
    private final long count;
    private final double profit;
    private final Offer offer;

    public MissionModel(StationModel target, double profit) {
        this.target = target;
        this.profit = profit;
        item = null;
        count = 0;
        offer = null;
    }

    public MissionModel(StationModel target, long count, double profit) {
        this.target = target;
        this.count = count;
        this.profit = profit;
        this.item = null;
        offer = null;
    }


    public MissionModel(StationModel target, ItemModel item, long count, double profit) {
        this.target = target;
        this.item = item;
        this.count = count;
        this.profit = profit;
        offer = SimpleOffer.fakeBuy(target.getStation(), item.getItem(), profit/count, count);
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
}
