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

    private final FilteredMarket market;
    private final Profile profile;

    private final double avgDistance;

    public Scorer(FilteredMarket market, Profile profile) {
        this.market = market;
        this.profile = profile;
        avgDistance = computeAvgDistance();
    }

    public Profile getProfile() {
        return profile;
    }

    private double computeAvgDistance(){
        OptionalDouble res = market.getVendors().mapToDouble(Vendor::getDistance).average();
        return res.orElse(0);
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
        STATION_TYPE t = entry.getVendor().getType();
        int planetLands = t != null && t.isPlanetary() ? 1 : 0;
        double time = getTime(entry.getVendor().getDistance(), jumps, lands, planetLands);
        if (!prev.isLand()){
            time = time - profile.getTakeoffTime() + profile.getRechargeTime();
        }
        return Math.round(time);
    }

    public long getTime(double distance, int jumps, int lands, int planetLands){
        double time = profile.getTakeoffTime();
        if (jumps > 0){
            time += profile.getJumpTime() + (jumps-1) * (profile.getRechargeTime() + profile.getJumpTime());
        }
        if (profile.getLandingTime() > 0 & lands > 0){
            time += (lands-1)*(getTime(avgDistance) + profile.getLandingTime() + profile.getTakeoffTime()) + getTime(distance) + profile.getLandingTime();
        }
        if (profile.getOrbitalTime() > 0 & planetLands > 0){
            time += planetLands * profile.getOrbitalTime();
        }
        return Math.round(time);
    }

    public double getScore(RouteEntry entry, RouteEntry prev) {
        int lands = prev.isLand() ? 1 : 0;
        int jumps = prev.getVendor().getPlace().equals(entry.getVendor().getPlace())? 0 : 1;
        return getScore(prev.getVendor(), prev.getProfit(), jumps, lands, prev.getFuel());
    }

    public double getScore(Vendor vendor, double profit, int jumps, int lands, double fuel) {
        STATION_TYPE t = vendor.getType();
        int planetLands = t != null && t.isPlanetary() ? 1 : 0;
        return getScore(vendor.getDistance(), profit, jumps, lands, planetLands, fuel);
    }

    public double getScore(double distance, double profit, int jumps, int lands, int planetLands, double fuel){
        LOG.trace("Compute score distance={}, profit={}, jumps={}, lands={}, planetary lands = {}, fuel={}", distance, profit, jumps, lands, planetLands, fuel);
        double score = getProfitByTonne(profit, fuel)/getTime(distance, jumps, lands, planetLands);
        LOG.trace("score={}", score);
        return score;
    }


    public List<Order> getOrders(Vendor seller, Vendor buyer){
        FilteredVendor fSeller = market.getFiltered(seller);
        FilteredVendor fBuyer = market.getFiltered(buyer);
        return MarketUtils.getOrders(fSeller, fBuyer);
    }

    public RatingComputer getRatingComputer(final Set<Vendor> vendors){
        return new RatingComputer(vendors);
    }

    public class RatingComputer {
        private final Map<Item, Offer> sellOffers;
        private final Map<Item, Offer> buyOffers;

        private final DoubleSummaryStatistics globalStat;
        private final double avgDistance;

        private RatingComputer(final Set<Vendor> vendors) {
            sellOffers = new HashMap<>(100, 0.9f);
            buyOffers = new HashMap<>(100, 0.9f);
            market.getItems().forEach(i -> fillOffers(i, vendors));
            globalStat = computeProfit();
            avgDistance = vendors.stream().mapToDouble(Vendor::getDistance).average().orElse(0);
        }

        private void fillOffers(Item item, Set<Vendor> vendors){
            Optional<Offer> offer = market.getSell(item).filter(o -> vendors.contains(o.getVendor())).findFirst();
            if (offer.isPresent()){
                sellOffers.put(item, offer.get());
            }
            offer = market.getBuy(item).filter(o -> vendors.contains(o.getVendor())).findFirst();
            if (offer.isPresent()){
                buyOffers.put(item, offer.get());
            }
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

        private DoubleSummaryStatistics computeProfit(){
            return sellOffers.values().stream()
                    .flatMap(this::mapToOrder)
                    .collect(Collectors.summarizingDouble(Order::getProfit));
        }

        private DoubleSummaryStatistics computeProfits(Stream<Order> orders) {
            return orders.sorted(Comparator.<Order>reverseOrder())
                    .limit(4)
                    .filter(o -> o.getProfit() > 0)
                    .collect(Collectors.summarizingDouble(Order::getProfit));
        }

        public Rating getRating(Vendor vendor){
            Stream<Order> sell = vendor.getAllSellOffers().stream().flatMap(this::mapToOrder);
            Stream<Order> buy = vendor.getAllBuyOffers().stream().flatMap(this::mapToOrder);

            DoubleSummaryStatistics sellStat = computeProfits(sell);
            DoubleSummaryStatistics buyStat = computeProfits(buy);

            double sellRate = 0.5 * sellStat.getMax() / globalStat.getMax() + 2.5 * sellStat.getAverage() / globalStat.getAverage();
            double buyRate = 0.5 * buyStat.getMax() / globalStat.getMax() + 2 * buyStat.getAverage() / globalStat.getAverage();
            double distRate = 0.5 * (vendor.getDistance() > 0 ? (vendor.getDistance() < avgDistance ? 1-vendor.getDistance()/avgDistance : -1+avgDistance/vendor.getDistance()) : 0.0);

            LOG.trace("Computed rate for {} = {}", vendor.getFullName(), sellRate + buyRate + distRate);
            LOG.trace("global - max: {} avg: {} min: {}", globalStat.getMax(), globalStat.getAverage(), globalStat.getMin());
            LOG.trace("sell - max: {} avg: {} min: {} rate: {}", sellStat.getMax(), sellStat.getAverage(), sellStat.getMin(), sellRate);
            LOG.trace("buy  - max: {} avg: {} min: {} rate: {}", buyStat.getMax(), buyStat.getAverage(), buyStat.getMin(), buyRate);
            LOG.trace("distance: {} avg: {} rate: {}", vendor.getDistance(), avgDistance, distRate);

            return new Rating(vendor, sellRate + buyRate + distRate);
        }
    }

    public class Rating implements Comparable<Rating> {
        private final Vendor vendor;
        private final double rate;

        public Rating(Vendor vendor, double rate) {
            this.vendor = vendor;
            this.rate = rate;
        }

        public Vendor getVendor() {
            return vendor;
        }

        public double getRate() {
            return rate;
        }

        @Override
        public int compareTo(Rating o) {
            return Double.compare(rate, o.rate);
        }
    }
}
