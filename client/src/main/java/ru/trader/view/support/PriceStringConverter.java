package ru.trader.view.support;

import javafx.util.converter.DoubleStringConverter;

public class PriceStringConverter extends DoubleStringConverter {
    @Override
    public String toString(Double value) {
        return String.format("%.0f", value);
    }
}
