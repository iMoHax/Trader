package ru.trader.edce.entities;

import java.util.Objects;

public class Commander {
    private String name;
    private long credits;
    private boolean docked;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCredits() {
        return credits;
    }

    public void setCredits(long credits) {
        this.credits = credits;
    }

    public boolean isDocked() {
        return docked;
    }

    public void setDocked(boolean docked) {
        this.docked = docked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Commander commander = (Commander) o;
        return Objects.equals(credits, commander.credits) &&
                Objects.equals(docked, commander.docked) &&
                Objects.equals(name, commander.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(credits, docked);
    }
}
