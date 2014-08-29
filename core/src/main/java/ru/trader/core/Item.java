package ru.trader.core;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Item implements Comparable<Item>{
    private String name;
    private Group group;

    public Item(String name) {
        setName(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(@NotNull Item other) {
        Objects.requireNonNull(other, "Not compare with null");
        if (this == other) return 0;
        return name != null ? other.name != null ? name.compareTo(other.name) : -1 : 0;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
