package ru.trader.store.berkeley.entities;

import com.sleepycat.persist.model.*;
import ru.trader.core.OFFER_TYPE;

@Entity
public class Offer {

    @PrimaryKey(sequence = "O_ID")
    private long id;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE,
            relatedEntity = Item.class,
            onRelatedEntityDelete = DeleteAction.NULLIFY)
    private long itemId;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE,
            relatedEntity = Vendor.class,
            onRelatedEntityDelete = DeleteAction.NULLIFY)
    private long vendorId;

    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private OFFER_TYPE type;

    private double price;

    public Offer(Item item, OFFER_TYPE type, double price) {
        this.itemId = item.getId();
        this.type = type;
        this.price = price;
    }
}
