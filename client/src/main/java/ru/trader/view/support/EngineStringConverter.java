package ru.trader.view.support;

import javafx.util.StringConverter;
import ru.trader.core.Engine;
import ru.trader.core.ModEngine;

public class EngineStringConverter extends StringConverter<Engine> {
    @Override
    public String toString(Engine engine) {
        return engine instanceof ModEngine ? "Custom" : ""+engine.getClazz()+engine.getRating();
    }

    @Override
    public Engine fromString(String string) {
        throw new UnsupportedOperationException();
    }
}