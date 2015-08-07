package ru.trader.edce.entities;

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
}
