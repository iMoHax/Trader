package ru.trader.model;

import ru.trader.analysis.CrawlerSpecificator;
import ru.trader.analysis.RouteReserve;
import ru.trader.core.Offer;
import ru.trader.store.simple.SimpleOffer;

import java.util.Collection;
import java.util.Objects;

public class MissionModel {
    private final StationModel target;
    private final ItemModel item;
    private final long count;
    private final double profit;
    private final Offer offer;
    private long need;
    private Collection<RouteReserve> reserves;

    public MissionModel(StationModel target, double profit) {
        this(target, null, 0, profit);
    }

    public MissionModel(StationModel target, long count, double profit) {
        this(target, null, count, profit);
    }


    public MissionModel(StationModel target, ItemModel item, long count, double profit) {
        this.target = target;
        this.item = item;
        this.count = count;
        this.profit = profit;
        if (item != null) {
            offer = SimpleOffer.fakeBuy(target.getStation(), item.getItem(), profit / count, count);
            need = count;
        } else {
            need = 0;
            offer = null;
        }
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
        return item == null && count > 0;
    }

    public boolean isCourier(){
        return item == null && count == 0;
    }

    @Override
    public String toString() {
        if (isDelivery()){
            return String.format("Deliver %d items to %s", count, target.getName());
        }
        if (isCourier()){
            return String.format("Deliver message to %s", target.getName());
        }
        if (isSupply()){
            return String.format("Supply %d %s to %s", count, item.getName(), target.getName());
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MissionModel that = (MissionModel) o;
        return Objects.equals(count, that.count) &&
                Objects.equals(profit, that.profit) &&
                Objects.equals(target, that.target) &&
                Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, item, count);
    }



    public MissionModel getCopy(){
        return new MissionModel(target, item, count, profit);
    }
}
