package ru.trader.analysis;

import org.jetbrains.annotations.NotNull;
import ru.trader.core.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Scorer {
    private final Map<Item, Offer> sellOffers;
    private final Map<Item, Offer> buyOffers;
    private final FilteredMarket market;
    private final Profile profile;

    private final double avgProfit;
    private final double avgDistance;

    private int ordersCount = 5;
    private double distanceRate = 1;

    public Scorer(FilteredMarket market, Profile profile) {
        this.market = market;
        this.profile = profile;
        sellOffers = new HashMap<>(100, 0.9f);
        buyOffers = new HashMap<>(100, 0.9f);
        market.getItems().parallelStream().forEach(this::fillOffers);
        avgProfit = computeAvgProfit();
        avgDistance = computeAvgDistance();
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

    private double computeAvgProfit(){
        OptionalDouble avg = sellOffers.values().stream()
                .flatMap(this::mapToOrder)
                .mapToDouble(o -> o.getProfit() / profile.getShip().getCargo())
                .average();
        return avg.orElse(0);
    }

    private double computeAvgDistance(){
        OptionalDouble res = market.getVendors().mapToDouble(Vendor::getDistance).average();
        return res.orElse(0);
    }

    public void setOrdersCount(int ordersCount) {
        this.ordersCount = ordersCount;
    }

    public void setDistanceRate(double distanceRate) {
        this.distanceRate = distanceRate;
    }

    public double getAvgProfit() {
        return avgProfit;
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
                   .limit(ordersCount)
                   .collect(Collectors.summarizingDouble(o -> o.getProfit() / profile.getShip().getCargo()));
        }

        private void computeScore(){
            score = (getSellProfit() + getBuyProfit())/2;
            score -= distanceRate * avgProfit * (vendor.getDistance() - avgDistance) / avgDistance;
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
