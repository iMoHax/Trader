package ru.trader.store.simple;

import ru.trader.core.GROUP_TYPE;
import ru.trader.core.Group;

public class SimpleGroup implements Group {
    private final String name;
    private final GROUP_TYPE type;

    public SimpleGroup(String name, GROUP_TYPE type) {
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
}
