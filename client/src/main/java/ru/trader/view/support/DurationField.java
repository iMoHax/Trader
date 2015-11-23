package ru.trader.view.support;

import javafx.beans.property.*;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.util.StringConverter;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DurationField extends TextField {
    private final StringConverter<Duration> converter;
    private final Tooltip tooltip = new Tooltip();

    private final ObjectProperty<Duration> duration = new SimpleObjectProperty<>(Duration.ZERO);
    private final BooleanProperty wrong = new SimpleBooleanProperty(false);

    public ReadOnlyObjectProperty<Duration> durationProperty() {
        return duration;
    }

    public Duration getValue(){
        return duration.get();
    }

    public void setValue(Duration value){
        duration.setValue(value);
        setText(converter.toString(value));
    }

    public boolean isWrong() {
        return wrong.get();
    }

    public BooleanProperty wrongProperty() {
        return ReadOnlyBooleanWrapper.booleanProperty(wrong);
    }

    public DurationField() {
        super();
        converter = new DurationStringConverter();
        tooltip.setText("Wrong duration format, use like 1w1d1h1m1s");
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
        Duration d = converter.fromString(text);
        if (d != null){
            duration.setValue(d);
            wrong.setValue(false);
        } else {
            wrong.setValue(true);
            selectAll();
            requestFocus();
        }
    }

    private static class DurationStringConverter extends StringConverter<Duration> {
        private static final Pattern PATTERN = Pattern.compile("^(?:([0-9]+)w)?(?:([0-9]+)d)?(?:([0-9]+)h)?(?:([0-9]+)m)?(?:([0-9]+)s)?$", Pattern.CASE_INSENSITIVE);

        private static long parseLong(String value){
            if (value == null) return 0;
            return Long.valueOf(value);
        }

        @Override
        public String toString(Duration dur) {
            StringBuilder res = new StringBuilder();
            long days = dur.toDays();
            dur = dur.minusDays(days);
            if (days >= 7){
                long weeks = days/7;
                res.append(weeks).append("w");
                days = days%7;
            }
            if (days > 0){
                res.append(days).append("d");
            }
            long hours = dur.toHours();
            if (hours > 0){
                res.append(hours).append("h");
                dur = dur.minusHours(hours);
            }
            long minutes = dur.toMinutes();
            if (minutes > 0){
                res.append(minutes).append("m");
                dur = dur.minusMinutes(minutes);
            }
            long seconds = dur.getSeconds();
            if (seconds > 0){
                res.append(seconds).append("s");
            }
            return res.toString();
        }

        @Override
        public Duration fromString(String string) {
            Matcher matcher = PATTERN.matcher(string);
            if (matcher.matches()) {
                long weeks = parseLong(matcher.group(1));
                long days = parseLong(matcher.group(2));
                long hours = parseLong(matcher.group(3));
                long minutes = parseLong(matcher.group(4));
                long seconds = parseLong(matcher.group(5));
                Duration res = Duration.ZERO;
                if (weeks > 0) days += weeks*7;
                return res.plusDays(days)
                        .plusHours(hours)
                        .plusMinutes(minutes)
                        .plusSeconds(seconds);
            } else {
                return null;
            }
        }
    }
}
