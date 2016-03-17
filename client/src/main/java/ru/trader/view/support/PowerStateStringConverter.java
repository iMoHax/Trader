package ru.trader.view.support;


import javafx.util.StringConverter;
import ru.trader.core.POWER_STATE;


public class PowerStateStringConverter extends StringConverter<POWER_STATE> {

    @Override
    public String toString(POWER_STATE powerState) {
        return toLocalizationString(powerState);
    }

    @Override
    public POWER_STATE fromString(String powerState) {
        return POWER_STATE.valueOf(powerState);
    }

    public static String toLocalizationString(POWER_STATE powerState){
        if (powerState == null) return null;
        return Localization.getString("power.states." + powerState.toString(), powerState.toString());
    }
}
