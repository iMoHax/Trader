package ru.trader.core;

import java.util.Collection;
import java.util.Iterator;

public class VendorsIterator implements Iterator<Vendor> {

    private final Iterator<Place> places;
    private final boolean includeTransit;
    private Iterator<Vendor> vendors;
    private Vendor next;

    public VendorsIterator(Iterator<Place> places, boolean includeTransit) {
        this.places = places;
        this.includeTransit = includeTransit;
        nextPlace();
    }

    public VendorsIterator(Collection<Place> places, boolean includeTransit) {
        this.places = places.iterator();
        this.includeTransit = includeTransit;
        nextPlace();
    }

    private void nextPlace(){
        if (places.hasNext()){
            Place place = places.next();
            Collection<Vendor> v = place.get();
            if (place.count() > 0){
                vendors = v.iterator();
                nextVendor();
            } else {
                if (includeTransit)
                    next = new TransitVendor(place);
                else
                    nextPlace();
            }
        } else {
            next = null;
        }
    }

    private void nextVendor(){
        if (vendors != null && vendors.hasNext()) {
            next = vendors.next();
        } else {
            next = null;
        }
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public Vendor next() {
        Vendor current = next;
        nextVendor();
        if (next == null){
            nextPlace();
        }
        return current;
    }
}
