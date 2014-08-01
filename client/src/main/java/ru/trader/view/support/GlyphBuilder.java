package ru.trader.view.support;

import javafx.util.Builder;
import org.controlsfx.glyphfont.Glyph;
import org.controlsfx.glyphfont.GlyphFontRegistry;

public class GlyphBuilder implements Builder<Glyph> {

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    private String text;


    @Override
    public Glyph build() {
        return (Glyph) GlyphFontRegistry.glyph(text);
    }
}
