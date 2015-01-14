package ru.trader.store.berkeley;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.trader.core.*;

import java.util.Collections;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAdder;


public class BDBItemStat extends AbstractItemStat {
    private final static Logger LOG = LoggerFactory.getLogger(BDBItemStat.class);

    private final ItemProxy item;
    private final OFFER_TYPE type;
    private final BDBStore store;
    private NavigableSet<Offer> offers;
    private volatile double sum;
    private volatile double avg;
    private AtomicLong count;


    public BDBItemStat(ItemProxy item, OFFER_TYPE type, BDBStore store) {
        this.item = item;
        this.type = type;
        this.store = store;
        offers = new TreeSet<>(store.getOfferAccessor().getAllByItem(item.getId(), type));
        this.sum = 0;
        this.avg = Double.NaN;
        this.count = new AtomicLong(0);
        init();
    }

    void init(){
        sum = 0;
        count.set(0);
        offers.forEach(o -> {
            sum += o.getPrice();
            count.incrementAndGet();
        });
        avg = sum / count.get();
    }

    synchronized void put(Offer offer){
        LOG.trace("Put offer {} to item stat {}", offer, this);
        assert offer.hasType(type) && offer.hasItem(item);
        double price = offer.getPrice();
        sum += price;
        avg = sum / count.incrementAndGet();
        if (offers != null){
            offers.add(offer);
        }
        LOG.trace("After this = {}", this);
    }

    synchronized void remove(Offer offer){
        LOG.trace("Remove offer {} from item stat {}", offer, this);
        assert offer.hasType(type) && offer.hasItem(item);
        double price = offer.getPrice();
        sum -= price;
        if (count.decrementAndGet() > 0){
            avg = sum / count.get();
        } else {
            sum = 0; avg = Double.NaN;
        }
        if (offers != null){
            offers.remove(offer);
        }
        LOG.trace("After this = {}", this);
    }

    @Override
    protected synchronized void updatePrice(AbstractOffer offer, double price) {
        LOG.trace("Update offer {} from item stat {}", offer, this);
        assert offer.hasType(type) && offer.hasItem(item);
        double oldPrice = offer.getPrice();
        if (offers != null){
            offers.remove(offer);
        }
        super.updatePrice(offer, price);
        if (offers != null){
            offers.add(offer);
        }
        sum += price - oldPrice;
        avg = sum / offers.size();
        LOG.trace("After update this = {}", this);
    }

    @Override
    public OFFER_TYPE getType() {
        return type;
    }

    @Override
    public Item getItem() {
        return item;
    }

    @Override
    public Offer getMin() {
        if (count.get() == 0) return getFake();
        return offers.first();
    }

    @Override
    public double getAvg() {
        return avg;
    }

    @Override
    public Offer getMax() {
        if (offers.isEmpty()) return getFake();
        return offers.last();
    }

    @Override
    public Offer getBest() {
        if (offers.isEmpty()) return getFake();
        return type.getOrder() > 0 ? offers.first() : offers.last();
    }

    @Override
    public NavigableSet<Offer> getOffers() {
        return Collections.unmodifiableNavigableSet(offers);
    }

    @Override
    public boolean isEmpty() {
        return count.get() == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BDBItemStat)) return false;

        BDBItemStat itemStat = (BDBItemStat) o;
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
        if (BDBItemStat.LOG.isTraceEnabled()){
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
