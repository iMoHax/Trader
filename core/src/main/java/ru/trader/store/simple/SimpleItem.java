package ru.trader.store.simple;

import ru.trader.core.AbstractItem;
import ru.trader.core.Group;

public class SimpleItem extends AbstractItem {
    private String name;
    private Group group;

    public SimpleItem(String name) {
        this.name = name;
    }

    public SimpleItem(String name, Group group) {
        this.name = name;
        setGroup(group);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void updateName(String name) {
        this.name = name;
    }

    @Override
    public Group getGroup() {
        return group;
    }

    protected void setGroup(Group group) {
        this.group = group;
    }
}
