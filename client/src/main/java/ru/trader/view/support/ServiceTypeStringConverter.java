package ru.trader.view.support;


import javafx.util.StringConverter;
import ru.trader.core.SERVICE_TYPE;


public class ServiceTypeStringConverter extends StringConverter<SERVICE_TYPE> {

    @Override
    public String toString(SERVICE_TYPE type) {
        return toLocalizationString(type);
    }

    @Override
    public SERVICE_TYPE fromString(String type) {
        return SERVICE_TYPE.valueOf(type);
    }

    public static String toLocalizationString(SERVICE_TYPE type){
        if (type == null) return null;
        return Localization.getString("services." + type.toString(), type.toString());
    }
}
