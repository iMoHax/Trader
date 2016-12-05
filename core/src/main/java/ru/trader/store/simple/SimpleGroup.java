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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleGroup)) return false;

        SimpleGroup that = (SimpleGroup) o;

        if (!name.equals(that.name)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
