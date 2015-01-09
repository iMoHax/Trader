package ru.trader.core;

import ru.trader.graph.Connectable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class VendorsIterator implements Iterator<Vendor> {

    private final Iterator<Place> places;
    private Iterator<Vendor> vendors;
    private Vendor next;

    public VendorsIterator(Collection<Place> places) {
        this.places = places.iterator();
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
                next = new TransitVendor(place);
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


    private class TransitVendor implements Vendor {

        private Place place;

        private TransitVendor(Place place) {
            this.place = place;
        }

        @Override
        public String getName() {
            return "";
        }

        @Override
        public void setName(String name) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public Place getPlace() {
            return place;
        }

        @Override
        public double getDistance() {
            return 0;
        }

        @Override
        public void setDistance(double distance) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public void add(SERVICE_TYPE service) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public void remove(SERVICE_TYPE service) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public boolean has(SERVICE_TYPE service) {
            return false;
        }

        @Override
        public Collection<SERVICE_TYPE> getServices() {
            return Collections.emptyList();
        }

        @Override
        public Offer addOffer(OFFER_TYPE type, Item item, double price, long count) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public void add(Offer offer) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public void remove(Offer offer) {
            throw new UnsupportedOperationException("Is fake vendor, change unsupported");
        }

        @Override
        public Collection<Offer> get(OFFER_TYPE type) {
            return Collections.emptyList();
        }

        @Override
        public Offer get(OFFER_TYPE type, Item item) {
            return null;
        }

        @Override
        public boolean has(OFFER_TYPE type, Item item) {
            return false;
        }

        @Override
        public int compareTo(Connectable<Vendor> o) {
            double d = getDistance((Vendor)o);
            return d == 0 ? 0 : d > 0 ? 1 : -1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TransitVendor)) return false;
            TransitVendor that = (TransitVendor) o;
            return place.equals(that.place);
        }

        @Override
        public int hashCode() {
            return place.hashCode();
        }

        @Override
        public String toString() {
            return "Transit";
        }
    }

}
