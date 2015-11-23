package ru.trader.model;

import ru.trader.analysis.CrawlerSpecificator;
import ru.trader.analysis.RouteReserve;
import ru.trader.core.Offer;
import ru.trader.store.simple.SimpleOffer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.Objects;

public class MissionModel {
    private final StationModel target;
    private final ItemModel item;
    private final long count;
    private final double profit;
    private final Offer offer;
    private final LocalDateTime time;
    private long need;
    private Collection<RouteReserve> reserves;

    public MissionModel(StationModel target, Duration duration, double profit) {
        this(target, null, 0, duration, profit);
    }

    public MissionModel(StationModel target, long count, Duration duration, double profit) {
        this(target, null, count, duration, profit);
    }


    public MissionModel(StationModel target, ItemModel item, long count, Duration duration, double profit) {
        this.target = target;
        this.item = item;
        this.count = count;
        this.time = LocalDateTime.now().plus(duration);
        this.profit = profit;
        if (item != null) {
            offer = SimpleOffer.fakeBuy(ModelFabric.get(target), ModelFabric.get(item), profit / count, count);
            need = count;
        } else {
            need = 0;
            offer = null;
        }
    }

    protected MissionModel(MissionModel mission, boolean includeReserves){
        this.target = mission.target;
        this.item = mission.item;
        this.count = mission.count;
        this.time = mission.time;
        this.profit = mission.profit;
        this.offer = mission.offer;
        this.need = mission.need;
        this.reserves = includeReserves ? mission.reserves : null;
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
        String t = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).format(time);
        if (isDelivery()){
            return String.format("Deliver %d items to %s at %s", count, target.getName(), t);
        }
        if (isCourier()){
            return String.format("Deliver message to %s at %s", target.getName(), t);
        }
        if (isSupply()){
            return String.format("Supply %d %s to %s at %s", count, item.getName(), target.getName(), t);
        }
        return "MissionModel{" +
                "target=" + target +
                ", item=" + item +
                ", count=" + count +
                ", time=" + time +
                ", profit=" + profit +
                "} ";
    }

    public void toSpecification(CrawlerSpecificator specificator){
        long interval = Duration.between(LocalDateTime.now(), time).getSeconds();
        if (isSupply()){
            if (isCompleted()){
                if (interval <= 0) specificator.add(ModelFabric.get(target), true);
                 else specificator.add(ModelFabric.get(target), interval, true);
            } else {
                if (interval <= 0) specificator.buy(offer);
                 else specificator.buy(offer, interval);
            }
        } else
        if (isCourier() || isDelivery()){
            if (interval <= 0) specificator.add(ModelFabric.get(target), true);
             else specificator.add(ModelFabric.get(target), interval, true);
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
                        if (ModelFabric.get(order.getOffer()).equals(reserve.getOrder().getSell())){
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
                Objects.equals(time, that.time) &&
                Objects.equals(target, that.target) &&
                Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, item, count);
    }

    public static MissionModel copy(MissionModel mission){
        return new MissionModel(mission, false);
    }

    public static MissionModel clone(MissionModel mission){
        return mission != null ? new MissionModel(mission, true) : null;
    }
}
