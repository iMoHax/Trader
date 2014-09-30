package ru.trader.store.simple;

import ru.trader.core.Group;
import ru.trader.core.Item;

public class SimpleItem extends Item {
    private String name;
    private Group group;

    public SimpleItem(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    protected void setName(String name) {
        this.name = name;
    }

    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public void setGroup(Group group) {
        this.group = group;
    }
}
