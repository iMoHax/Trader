package ru.trader.graph;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Point implements Connectable<Point> {
    private int x;
    private String name;
    private boolean refill;

    public Point(String name, int x) {
        this(name, x, false);
    }

    public Point(String name, int x, boolean refill) {
        this.x = x;
        this.name = name;
        this.refill = refill;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x;
    }

    @Override
    public int hashCode() {
        return x;
    }

    @Override
    public double getDistance(Point other) {
        return Math.abs(x-other.x);
    }

    @Override
    public boolean canRefill() {
        return refill;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NotNull Connectable<Point> o) {
        Objects.requireNonNull(o, "Not compare with null");
        Point other =(Point)o;
        return Integer.compare(hashCode(), other.hashCode());
    }
}
