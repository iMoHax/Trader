package ru.trader.store.berkeley.entities;

import com.sleepycat.persist.model.*;

@Entity(version = 1)
public class BDBItem {
    @PrimaryKey(sequence="I_ID")
    private long id;

    private String name;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE,
            relatedEntity = BDBGroup.class,
            onRelatedEntityDelete = DeleteAction.CASCADE)
    private String groupId;

    private BDBItem() {
    }

    public BDBItem(String name, String groupId) {
        this.name = name;
        this.groupId = groupId;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupId() {
        return groupId;
    }

}
