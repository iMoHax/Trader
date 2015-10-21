package ru.trader.edce.entities;

import java.util.Objects;

public class ShipyardItem {
    private long id;
    private String name;
    private long basevalue;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBasevalue() {
        return basevalue;
    }

    public void setBasevalue(long basevalue) {
        this.basevalue = basevalue;
    }

    @Override
    public String toString() {
        return "ShipyardItem{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", basevalue=" + basevalue +
                "} ";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShipyardItem that = (ShipyardItem) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(basevalue, that.basevalue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
