package ru.trader.analysis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/* times:
*   26810 - 7:15  - 435
*   1980 - 3:46 - 226
*   1330 - 2:47 - 167
*   1380 - 2:32 - 152
*   780 - 2:10 - 130
*   430 - 2:04 - 124
*   306 - 1:54 - 116
*   88 - 1:25 - 85
*
*  launch_to_start_jmp - 0:40, 0:43, 0:40
*  jmp - 0:33, 0:33, 0:30, 0:32, 0:32, 0:32
*  recharge - 0:12, 0:14, 0:12, 0:12, 0:12
*  lading - 1:04, 1:08, 1:28
*
* */
public class Scorer {
    private final static Logger LOG = LoggerFactory.getLogger(Scorer.class);

    private final Map<Item, Offer> sellOffers;
    private final Map<Item, Offer> buyOffers;
    private final FilteredMarket market;
    private final Profile profile;

    private final double avgProfit;
    private final double minProfit;
    private final double maxProfit;
    private final double maxScore;
    private final double avgDistance;

    public Scorer(FilteredMarket market, Profile profile) {
        this.market = market;
        this.profile = profile;
        sellOffers = new HashMap<>(100, 0.9f);
        buyOffers = new HashMap<>(100, 0.9f);
        market.getItems().forEach(this::fillOffers);
        DoubleSummaryStatistics statProfit = computeProfit();
        minProfit = statProfit.getMin() / profile.getShip().getCargo();
        avgProfit = statProfit.getAverage() / profile.getShip().getCargo();
        maxProfit = statProfit.getMax() / profile.getShip().getCargo();

        avgDistance = computeAvgDistance();
        maxScore = getScore(1, statProfit.getMax(), 0, 1, 0);
    }

    public Profile getProfile() {
        return profile;
    }

    private void fillOffers(Item item){
        Optional<Offer> offer = market.getSell(item).findFirst();
        if (offer.isPresent()){
            sellOffers.put(item, offer.get());
        }
        offer = market.getBuy(item).findFirst();
        if (offer.isPresent()){
            buyOffers.put(item, offer.get());
        }
    }

    private DoubleSummaryStatistics computeProfit(){
        return sellOffers.values().stream()
               .flatMap(this::mapToOrder)
               .collect(Collectors.summarizingDouble(Order::getProfit));
    }

    private double computeAvgDistance(){
        OptionalDouble res = market.getVendors().mapToDouble(Vendor::getDistance).average();
        return res.orElse(0);
    }

    public double getAvgProfit() {
        return avgProfit * profile.getShip().getCargo();
    }

    public double getMaxProfit() {
        return maxProfit;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public double getAvgDistance() {
        return avgDistance;
    }

    private double getTime(double distance){
        double a = 6000;
        double b = 673;
        double c = 1670;
        return Math.log(distance + a)*b*profile.getDistanceTime() - c;
    }

    public double getProfitByTonne(double profit, double fuel){
        return getProfit(profit, fuel) / profile.getShip().getCargo();
    }

    public double getProfit(double profit, double fuel){
        profit -= profile.getFuelPrice() * fuel;
        return profit;
    }

    public long getTime(RouteEntry entry, RouteEntry prev) {
        if (prev == null) return 0;
        int lands = entry.isLand() ? 1 : 0;
        int jumps = prev.getVendor().getPlace().equals(entry.getVendor().getPlace()) ? 0 : 1;
        double time = getTime(entry.getVendor().getDistance(), jumps, lands);
        if (!prev.isLand()){
            time = time - profile.getTakeoffTime() + profile.getRechargeTime();
        }
        return Math.round(time);
    }

    public long getTime(double distance, int jumps, int lands){
        double time = profile.getTakeoffTime();
        if (jumps > 0){
            time += profile.getJumpTime() + (jumps-1) * (profile.getRechargeTime() + profile.getJumpTime());
        }
        if (profile.getLandingTime() > 0 & lands > 0){
            time += (lands-1)*(getTime(avgDistance) + profile.getLandingTime() + profile.getTakeoffTime()) + getTime(distance) + profile.getLandingTime();
        }
        return Math.round(time);
    }

    public double getScore(RouteEntry entry, RouteEntry prev) {
        int lands = prev.isLand() ? 1 : 0;
        int jumps = prev.getVendor().getPlace().equals(entry.getVendor().getPlace())? 0 : 1;
        return getScore(prev.getVendor(), prev.getProfit(), jumps, lands, prev.getFuel());
    }

    public double getScore(Vendor vendor, double profit, int jumps, int lands, double fuel) {
        return getScore(vendor.getDistance(), profit, jumps, lands, fuel);
    }

    public double getScore(double distance, double profit, int jumps, int lands, double fuel){
        LOG.trace("Compute score distance={}, profit={}, jumps={}, lands={}, fuel={}", distance, profit, jumps, lands, fuel);
        double score = getProfitByTonne(profit, fuel)/getTime(distance, jumps, lands);
        LOG.trace("score={}", score);
        return score;
    }

    private Stream<Order> mapToOrder(Offer offer) {
        Offer sell;
        Offer buy;
        if (offer.getType() == OFFER_TYPE.SELL){
            sell = offer;
            buy =  buyOffers.get(offer.getItem());
        } else {
            sell = sellOffers.get(offer.getItem());
            buy = offer;
        }
        if (sell == null || buy == null) return Stream.empty();
        Order order = new Order(sell, buy, profile.getBalance(), profile.getShip().getCargo());
        if (order.getProfit() <= 0) return Stream.empty();
        return Stream.of(order);
    }
}
