package ru.trader.view.support;


import javafx.util.StringConverter;
import ru.trader.core.GOVERNMENT;


public class GovernmentStringConverter extends StringConverter<GOVERNMENT> {

    @Override
    public String toString(GOVERNMENT government) {
        return toLocalizationString(government);
    }

    @Override
    public GOVERNMENT fromString(String government) {
        return GOVERNMENT.valueOf(government);
    }

    public static String toLocalizationString(GOVERNMENT government){
        if (government == null) return null;
        return Localization.getString("government." + government.toString(), government.toString());
    }
}
