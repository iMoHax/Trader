package ru.trader.view.support;


import javafx.util.StringConverter;
import ru.trader.core.POWER;


public class PowerStringConverter extends StringConverter<POWER> {

    @Override
    public String toString(POWER power) {
        return toLocalizationString(power);
    }

    @Override
    public POWER fromString(String power) {
        return POWER.valueOf(power);
    }

    public static String toLocalizationString(POWER power){
        if (power == null) return null;
        return Localization.getString("power." + power.toString(), power.toString());
    }
}
