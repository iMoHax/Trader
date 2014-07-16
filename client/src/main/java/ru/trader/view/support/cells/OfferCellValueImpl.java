package ru.trader.view.support.cells;

import javafx.beans.NamedArg;
import javafx.beans.value.ObservableValue;
import ru.trader.model.OfferModel;
import ru.trader.model.support.ModelBindings;


public class OfferCellValueImpl<T> extends PropertyCellValueFactory<OfferModel, T, String>{

    public OfferCellValueImpl(@NamedArg("property") String property) {
        super(property);
    }

    @Override
    ObservableValue<String> format(ObservableValue<OfferModel> value) {
        return ModelBindings.asString(value);
    }
}