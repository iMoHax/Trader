package ru.trader.edce.entities;

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

}
