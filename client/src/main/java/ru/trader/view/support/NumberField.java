package ru.trader.view.support;

import javafx.beans.NamedArg;
import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.util.converter.NumberStringConverter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;


public class NumberField extends TextField {
    private final NumberStringConverter converter;
    private final Tooltip tooltip = new Tooltip();

    private final ObjectProperty<Number> number = new SimpleObjectProperty<Number>(0);
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

    public void add(Number value){
        setValue(number.getValue().doubleValue() + value.doubleValue());
    }

    public void sub(Number value){
        setValue(number.getValue().doubleValue() - value.doubleValue());
    }

    public boolean isWrong() {
        return wrong.get();
    }

    public BooleanProperty wrongProperty() {
        return ReadOnlyBooleanWrapper.booleanProperty(wrong);
    }

    public NumberField() {
        this("0.####");
    }

    public NumberField(@NamedArg("format") String format) {
        super();
        NumberFormat f = new DecimalFormat(format, new DecimalFormatSymbols(Locale.ENGLISH));
        f.setGroupingUsed(false);
        converter = new NumberStringConverter(f);
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
        setOnKeyPressed((e) -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                editCancel();
                e.consume();
            }
        });
        setOnAction((e) -> parseNumber());
        focusedProperty().addListener((ob, o, n) -> {if (o) parseNumber();});
        setAlignment(Pos.BASELINE_RIGHT);
    }

    private void editCancel() {
        setText(converter.toString(getValue()));
       getParent().requestFocus();
    }

    private void parseNumber(){
        String text = getText();
        if (text == null || text.isEmpty()) {
            number.setValue(0);
            return;
        }

        if (text.matches("^-?\\d+([,\\.]\\d+)?([eE]-?\\d+)?$")){
            text = text.replace(',','.');
            number.setValue(converter.fromString(text));

            wrong.setValue(false);
        } else {
                wrong.setValue(true);
                selectAll();
                requestFocus();
            }
    }


}
