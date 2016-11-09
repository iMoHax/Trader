package ru.trader.view.support;



import javafx.beans.NamedArg;
import javafx.util.converter.NumberStringConverter;


public class CustomNumberStringConverter extends NumberStringConverter {

    public CustomNumberStringConverter(@NamedArg("pattern")String pattern) {
        super(pattern);
    }
}
