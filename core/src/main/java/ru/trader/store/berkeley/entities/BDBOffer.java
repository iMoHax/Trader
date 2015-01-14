package ru.trader.store.berkeley.entities;

import com.sleepycat.persist.model.*;
import ru.trader.core.OFFER_TYPE;

@Entity(version = 1)
public class BDBOffer {

    @PrimaryKey(sequence = "O_ID")
    private long id;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE,
            relatedEntity = BDBItem.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private long itemId;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE,
            relatedEntity = BDBVendor.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private long vendorId;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private OFFER_TYPE type;

    private double price;
    private long count;

    private BDBOffer() {
    }

    public BDBOffer(OFFER_TYPE type, long itemId, double price, long count, long vendorId) {
        this.type = type;
        this.itemId = itemId;
        this.price = price;
        this.count = count;
        this.vendorId = vendorId;
    }

    public long getId() {
        return id;
    }

    public OFFER_TYPE getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getItemId() {
        return itemId;
    }

    public long getVendorId() {
        return vendorId;
    }

    public void setVendor(long vendorId) {
        this.vendorId = vendorId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BDBOffer)) return false;

        BDBOffer bdbOffer = (BDBOffer) o;

        if (count != bdbOffer.count) return false;
        if (id != bdbOffer.id) return false;
        if (itemId != bdbOffer.itemId) return false;
        if (Double.compare(bdbOffer.price, price) != 0) return false;
        if (type != bdbOffer.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
