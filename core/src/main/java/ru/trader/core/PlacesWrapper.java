package ru.trader.core;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

public class PlacesWrapper extends AbstractCollection<Vendor> {
    private final Collection<Place> places;
    private final boolean includeTransit;
    private int size;

    public PlacesWrapper(Collection<Place> places, boolean includeTransit) {
        this.places = places;
        this.includeTransit = includeTransit;
        size = 0;
        for (Place place : places) {
            int count = place.count();
            size += count > 0 ? count : includeTransit ? 1 : 0;
        }
    }

    @NotNull
    @Override
    public Iterator<Vendor> iterator() {
        return new VendorsIterator(places, includeTransit);
    }

    @Override
    public int size() {
        return size;
    }
}
