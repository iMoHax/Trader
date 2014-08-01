package ru.trader.view.support;

import javafx.fxml.JavaFXBuilderFactory;
import javafx.util.Builder;
import javafx.util.BuilderFactory;
import org.controlsfx.glyphfont.Glyph;

public class CustomBuilderFactory implements BuilderFactory {
    private BuilderFactory baseFactory;

    public CustomBuilderFactory() {
        baseFactory = new JavaFXBuilderFactory();
    }

    @Override
    public Builder<?> getBuilder(Class<?> aClass) {
        if (Glyph.class.equals(aClass)) {
            return new GlyphBuilder();
        } else {
            return baseFactory.getBuilder(aClass);
        }
    }
}
