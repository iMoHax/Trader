package ru.trader.analysis.graph;

public class PathEntry<T> {
    private final T entry;
    private boolean refill;

    public PathEntry(T entry, boolean refill) {
        this.entry = entry;
        this.refill = refill;
    }

    public T getEntry() {
        return entry;
    }

    public boolean isRefill() {
        return refill;
    }
}
