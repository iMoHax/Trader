package ru.trader.core;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

public class PlacesWrapper extends AbstractCollection<Vendor> {
    private final Collection<Place> places;
    private int size;

    public PlacesWrapper(Collection<Place> places) {
        this.places = places;
        size = 0;
        for (Place place : places) {
            int count = place.count();
            size += count > 0 ? count : 1;
        }
    }

    @NotNull
    @Override
    public Iterator<Vendor> iterator() {
        return new VendorsIterator(places);
    }

    @Override
    public int size() {
        return size;
    }
}
