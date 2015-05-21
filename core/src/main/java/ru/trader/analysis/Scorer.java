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
    private final double maxScore;
    private final double avgDistance;

    public Scorer(FilteredMarket market, Profile profile) {
        this.market = market;
        this.profile = profile;
        sellOffers = new HashMap<>(100, 0.9f);
        buyOffers = new HashMap<>(100, 0.9f);
        market.getItems().parallelStream().forEach(this::fillOffers);
        DoubleSummaryStatistics statProfit = computeProfit();
        avgProfit = statProfit.getAverage();
        avgDistance = computeAvgDistance();
        maxScore = getScore(0, statProfit.getMax()*2, 0,0,0);
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
        return avgProfit;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public double getFuel(double distance){
        return profile.getShip().getFuelCost(distance);
    }

    public double getScore(Vendor vendor, double profit, int jumps, int lands, double fuel) {
        return getScore(vendor.getDistance(), profit, jumps, lands, fuel);
    }

    public double getScore(double distance, double profit, int jumps, int lands, double fuel){
        LOG.trace("Compute score distance={}, profit={}, jumps={}, lands={}, fuel={}", distance, profit, jumps, lands, fuel);
        double score = profit;
        if (avgDistance > 0 && profit > 0) {
            score -= profile.getDistanceMult() * getAvgProfit() * (distance - avgDistance) / avgDistance;
        }
        score -= profile.getLandMult() * lands * getAvgProfit();
        score -= profile.getFuelPrice() * fuel;
        score -= profile.getJumpMult() * jumps * getAvgProfit();
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
