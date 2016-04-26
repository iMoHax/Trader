package ru.trader.store.simple;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import java.util.Collection;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.NavigableSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

public class SimpleItemStat extends AbstractItemStat {
    private final static Logger LOG = LoggerFactory.getLogger(SimpleItemStat.class);

    private final Item item;
    private final OFFER_TYPE type;
    private final NavigableSet<Offer> offers;
    private volatile double sum;
    private volatile double avg;


    public SimpleItemStat(Item item, OFFER_TYPE offerType) {
        this.offers = new ConcurrentSkipListSet<>();
        this.item = item;
        this.type = offerType;
        this.sum = 0;
        this.avg = Double.NaN;
    }

    synchronized void put(Offer offer){
        LOG.trace("Put offer {} to item stat {}", offer, this);
        assert offer.hasType(type) && offer.hasItem(item);
        if (offers.add(offer)){
            double price = offer.getPrice();
            sum += price;
            avg = sum / offers.size();
            LOG.trace("After this = {}", this);
        }
    }

    synchronized void putAll(Collection<Offer> offers){
        LOG.trace("Put offers to item stat {}", this);
        if (this.offers.addAll(offers)){
            DoubleSummaryStatistics stat = this.offers.stream().collect(Collectors.summarizingDouble(Offer::getPrice));
            sum = stat.getSum();
            avg = stat.getAverage();
            LOG.trace("After this = {}", this);
        }
    }

    synchronized void remove(Offer offer){
        LOG.trace("Remove offer {} from item stat {}", offer, this);
        assert offer.hasType(type) && offer.hasItem(item);
        if (offers.remove(offer)){
            if (offers.size()>0){
                double price = offer.getPrice();
                sum -= price;
                avg = sum / offers.size();
            } else {
                sum = 0; avg = Double.NaN;
            }
            LOG.trace("After this = {}", this);
        }
    }

    @Override
    protected synchronized void updatePrice(AbstractOffer offer, double price) {
        LOG.trace("Update offer {} from item stat {}", offer, this);
        assert offer.hasType(type) && offer.hasItem(item) && offers.contains(offer);
        double oldPrice = offer.getPrice();
        offers.remove(offer);

        super.updatePrice(offer, price);

        offers.add(offer);
        sum += price - oldPrice;
        avg = sum / offers.size();
        LOG.trace("After update this = {}", this);
    }

    @Override
    public OFFER_TYPE getType(){
        return type;
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public synchronized Offer getMin() {
        if (offers.isEmpty()) return getFake();
        return offers.first();
    }

    @Override
    public synchronized double getAvg(){
        return avg;
    }

    @Override
    public synchronized Offer getMax() {
        if (offers.isEmpty()) return getFake();
        return offers.last();
    }

    @Override
    public synchronized Offer getBest() {
        if (offers.isEmpty()) return getFake();
        return type.getOrder() > 0 ? offers.first() : offers.last();
    }

    @Override
    public synchronized NavigableSet<Offer> getOffers() {
        return Collections.unmodifiableNavigableSet(offers);
    }

    @Override
    public synchronized boolean isEmpty() {
        return offers.isEmpty();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleItemStat)) return false;

        SimpleItemStat itemStat = (SimpleItemStat) o;
        return type == itemStat.type && item.equals(itemStat.item);
    }

    @Override
    public int hashCode() {
        int result = item.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append(getItem());
        sb.append(", ").append(getType());
        if (SimpleItemStat.LOG.isTraceEnabled()){
            sb.append(", count=").append(offers.size());
            sb.append(", sum=").append(sum);
        }
        sb.append(", avg=").append(getAvg());
        sb.append(", best=").append(getBest());
        sb.append(", min=").append(getMin());
        sb.append(", max=").append(getMax());

        sb.append("}");
        return sb.toString();
    }



}
