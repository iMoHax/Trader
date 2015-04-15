package ru.trader.analysis;

import ru.trader.core.*;

import java.util.*;
import java.util.stream.Stream;

public class Scorer {
    private final Map<Item, Offer> sellOffers;
    private final Map<Item, Offer> buyOffers;
    private final FilteredMarket market;
    private final Profile profile;

    private int ordersCount = 5;

    private double avgProfit;

    public Scorer(FilteredMarket market, Profile profile) {
        this.market = market;
        this.profile = profile;
        sellOffers = new HashMap<>(100, 0.9f);
        buyOffers = new HashMap<>(100, 0.9f);
        market.getItems().parallelStream().forEach(this::fillOffers);
        avgProfit = computeAvgProfit();
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

    public void setOrdersCount(int ordersCount) {
        this.ordersCount = ordersCount;
    }

    private double computeAvgProfit(){
        OptionalDouble avg = sellOffers.values().stream()
                .flatMap(this::mapToOrder)
                .mapToDouble(Order::getProfit)
                .average();
        return avg.orElse(0);
    }

    public Score getScore(Vendor vendor){
        Stream<Order> sellOrders = vendor.getAllSellOffers().stream().flatMap(this::mapToOrder);
        Stream<Order> buyOrders = vendor.getAllBuyOffers().stream().flatMap(this::mapToOrder);
        return new Score(sellOrders, buyOrders);
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

    public class Score {
        private double sellProfit;
        private double buyProfit;
        private double score;

        public Score(Stream<Order> sell, Stream<Order> buy) {
            sellProfit = computeProfits(sell);
            buyProfit = computeProfits(buy);

            long count = sell.limit(ordersCount).count();
            computeScore(count);
        }

        private double computeProfits(Stream<Order> orders) {
            OptionalDouble profit = orders.sorted(Comparator.<Order>reverseOrder())
                    .limit(ordersCount)
                    .mapToDouble(Order::getProfit)
                    .average();
            return profit.orElse(0);
        }

        private void computeScore(long sellOrdersCount){
            score = (sellProfit + buyProfit)/2;
            if (sellOrdersCount < ordersCount){
                score =- Math.abs(avgProfit-sellProfit) * (ordersCount - sellOrdersCount) / score;
            }

        }
    }

}
