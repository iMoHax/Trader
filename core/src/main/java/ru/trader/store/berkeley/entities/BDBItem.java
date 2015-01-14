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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BDBItem)) return false;

        BDBItem bdbItem = (BDBItem) o;

        if (id != bdbItem.id) return false;
        if (!groupId.equals(bdbItem.groupId)) return false;
        if (!name.equals(bdbItem.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
