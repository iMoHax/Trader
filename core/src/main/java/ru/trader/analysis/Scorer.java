package ru.trader.analysis;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public double getMaxScore() {
        return maxScore;
    }

    public double getAvgDistance() {
        return avgDistance;
    }

    public double getFuel(double distance){
        return profile.getShip().getFuelCost(distance);
    }

    public double getScore(RouteEntry entry, int jumps) {
        int lands = entry.isLand() ? 1 : 0;
        return getScore(entry.getVendor(), entry.getProfit(), jumps, lands, entry.getFuel());
    }

    public double getScore(Vendor vendor, double profit, int jumps, int lands, double fuel) {
        return getScore(vendor.getDistance(), profit, jumps, lands, fuel);
    }

    public double getTransitScore(double fuel){
        LOG.trace("Compute transit score fuel={}", fuel);
        double profit = maxProfit;
        profit -= profile.getFuelPrice() * fuel / profile.getShip().getCargo();
        double score = 1;
        score -= profile.getLandMult();
        score -= profile.getJumpMult();
        score = score * profit;
        if (avgDistance > 0) {
            score -= - avgProfit * profile.getDistanceMult();
        }
        LOG.trace("score={}", score);
        return score;
    }

    public double getScore(double distance, double profit, int jumps, int lands, double fuel){
        LOG.trace("Compute score distance={}, profit={}, jumps={}, lands={}, fuel={}", distance, profit, jumps, lands, fuel);
        profit -= profile.getFuelPrice() * fuel;
        profit = profit / profile.getShip().getCargo();
        double score = 1;
        if (profit > 0) {
            score -= profile.getJumpMult() * (jumps - 1);
            score -= profile.getLandMult() * lands;
            if (score == 0) {
                score = profit;
            } else {
                score = score * profit;
            }
            if (avgDistance > 0) {
                score -= avgProfit * profile.getDistanceMult() * (distance - avgDistance) / avgDistance;
            }
        } else {
            score = profit;
        }
        LOG.trace("score={}", score);
        return score;
    }

    public Score getScore(Vendor vendor){
        return new Score(vendor);
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

    public class Score implements Comparable<Score> {
        private final Vendor vendor;
        private final DoubleSummaryStatistics sellStat;
        private final DoubleSummaryStatistics buyStat;
        private double score;

        public Score(Vendor vendor) {
            this.vendor = vendor;
            Stream<Order> sell = vendor.getAllSellOffers().stream().flatMap(Scorer.this::mapToOrder);
            Stream<Order> buy = vendor.getAllBuyOffers().stream().flatMap(Scorer.this::mapToOrder);

            sellStat = computeProfits(sell);
            buyStat = computeProfits(buy);

            computeScore();
        }

        public double getSellProfit() {
            return sellStat.getAverage();
        }

        public double getBuyProfit() {
            return buyStat.getAverage();
        }

        public double getScore() {
            return score;
        }

        private DoubleSummaryStatistics computeProfits(Stream<Order> orders) {
            return orders.sorted(Comparator.<Order>reverseOrder())
                   .limit(profile.getScoreOrdersCount())
                   .collect(Collectors.summarizingDouble(Order::getProfit));
        }

        private void computeScore(){
            score = (getSellProfit() + getBuyProfit())/2;
            score = Scorer.this.getScore(vendor, score, 0, 0, 0);
        }

        @Override
        public String toString() {
            return "Score{" +
                    "vendor=" + vendor.getPlace()+"("+vendor+")"+
                    ", sellStat=" + sellStat +
                    ", buyStat=" + buyStat +
                    ", score=" + score +
                    '}';
        }

        @Override
        public int compareTo(@NotNull Score other) {
            return Double.compare(score, other.score);
        }
    }

}
