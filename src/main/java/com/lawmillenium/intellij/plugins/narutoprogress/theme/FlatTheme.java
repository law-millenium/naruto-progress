package com.lawmillenium.intellij.plugins.narutoprogress.theme;

import java.awt.*;
import java.util.function.Function;

import com.lawmillenium.intellij.plugins.narutoprogress.model.Shinobi;

public class FlatTheme extends PaintTheme {

    private final Function<BackgroundColor, Color> getColorFromShinobi;

    public FlatTheme(final String id, final String title, final Function<BackgroundColor, Color> getColorFromShinobi) {
        super(id, title);
        this.getColorFromShinobi = getColorFromShinobi;
    }

    @Override
    public Paint getPaint(final Shinobi shinobi, final ColorScheme colorScheme, final int startY, final int height) {
        return getColorFromShinobi.apply(colorScheme.get(shinobi));
    }
}
