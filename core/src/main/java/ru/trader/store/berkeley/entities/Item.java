package ru.trader.store.berkeley.entities;

import com.sleepycat.persist.model.*;

@Entity
public class Item {
    @PrimaryKey(sequence="I_ID")
    private long id;

    private String name;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE,
            relatedEntity = Group.class,
            onRelatedEntityDelete = DeleteAction.CASCADE)
    private String groupId;

    private Item() {
    }

    public Item(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }
}
