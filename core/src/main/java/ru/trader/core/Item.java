package ru.trader.core;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class Item implements Comparable<Item> {

    public abstract String getName();
    protected abstract void setName(String name);

    public abstract Group getGroup();
    public abstract void setGroup(Group group);

    @Override
    public String toString(){
        return getName();
    }

    @Override
    public int compareTo(@NotNull Item other){
        Objects.requireNonNull(other, "Not compare with null");
        if (this == other) return 0;
        String name = getName();
        String otherName = other.getName();
        return name != null ? otherName != null ? name.compareTo(otherName) : -1 : 0;
    }

}
