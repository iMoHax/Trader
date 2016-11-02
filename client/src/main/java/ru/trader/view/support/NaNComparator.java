package ru.trader.view.support;


import java.util.Comparator;

public class NaNComparator<T extends Number> implements Comparator<T> {

    @Override
    public int compare(Number n1, Number n2) {
        if (n1 == null && n2 == null) return 0;
        if (n1 == null) return -1;
        if (n2 == null) return 1;
        double d1 = n1.doubleValue();
        double d2 = n2.doubleValue();
        boolean isNaN1 = Double.isNaN(d1);
        boolean isNaN2 = Double.isNaN(d2);
        if (isNaN1 && isNaN2) return 0;
        if (isNaN1) return -1;
        if (isNaN2) return 1;

        return Double.compare(d1, d2);

    }

}
