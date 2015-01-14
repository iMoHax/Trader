package ru.trader.store.berkeley.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import ru.trader.core.GROUP_TYPE;
import ru.trader.core.Group;


@Entity(version = 1)
public class BDBGroup implements Group {

    @PrimaryKey
    private String name;

    private GROUP_TYPE type;

    private BDBGroup() {
    }

    public BDBGroup(String name, GROUP_TYPE type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public GROUP_TYPE getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BDBGroup)) return false;

        BDBGroup bdbGroup = (BDBGroup) o;

        if (!name.equals(bdbGroup.name)) return false;
        if (type != bdbGroup.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
