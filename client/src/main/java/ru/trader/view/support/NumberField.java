package ru.trader.view.support;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.converter.NumberStringConverter;


public class NumberField extends TextField {
    private final static NumberStringConverter converter = new NumberStringConverter("#0.#");
    private final Tooltip tooltip = new Tooltip();

    private final ObjectProperty<Number> number = new SimpleObjectProperty<>(0);
    private final BooleanProperty wrong = new SimpleBooleanProperty(false);

    public ObjectProperty<Number> numberProperty() {
        return number;
    }

    public Number getValue(){
        return number.get();
    }

    public void setValue(Number value){
        number.setValue(value);
        setText(converter.toString(value));
    }

    public boolean isWrong() {
        return wrong.get();
    }

    public BooleanProperty wrongProperty() {
        return ReadOnlyBooleanWrapper.booleanProperty(wrong);
    }

    public NumberField() {
        super();
        tooltip.setText("Wrong number");
        tooltip.setAutoHide(true);
        wrong.addListener((ob, o ,n) -> {
            if (n) {
                setTooltip(tooltip);
                Point2D p = this.localToScene(0.0, 0.0);
                tooltip.show(this, getScene().getWindow().getX() + getScene().getX() + p.getX(),
                                   getScene().getWindow().getY() + getScene().getY() + p.getY() + getHeight() + 2);
            }
            else {
                tooltip.hide();
                setTooltip(null);
            }
        });

        setOnAction((e) -> parseNumber());
        focusedProperty().addListener((ob, o, n) -> {if (o) parseNumber();});
    }

    private void parseNumber(){
        String text = getText();
        if (text == null || text.isEmpty()) {
            number.setValue(0);
            return;
        }

        if (text.matches("^-?\\d+([,\\.]\\d+)?([eE]-?\\d+)?$")){
            number.setValue(converter.fromString(text));
            wrong.setValue(false);
        } else {
                wrong.setValue(true);
                selectAll();
                requestFocus();
            }
    }


}
