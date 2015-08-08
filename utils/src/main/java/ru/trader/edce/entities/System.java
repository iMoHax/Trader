package ru.trader.edce.entities;

import java.util.Objects;

public class System {
    private long id;
    private String name;
    private String faction;

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

    public String getFaction() {
        return faction;
    }

    public void setFaction(String faction) {
        this.faction = faction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        System system = (System) o;
        return Objects.equals(id, system.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
