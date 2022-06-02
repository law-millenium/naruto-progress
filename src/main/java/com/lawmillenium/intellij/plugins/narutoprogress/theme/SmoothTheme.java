package com.lawmillenium.intellij.plugins.narutoprogress.theme;

import java.awt.*;

import com.intellij.ui.scale.JBUIScale;
import com.lawmillenium.intellij.plugins.narutoprogress.model.Shinobi;

public class SmoothTheme extends PaintTheme {

    private static final float ONE_HALF = 0.5f;

    public SmoothTheme(final String id, final String name) {
        super(id, name);
    }

    private static Paint getPaintSingleColor(final Shinobi shinobi, final ColorScheme colorScheme, final int startY, final int height) {
        final BackgroundColor backgroundColor = colorScheme.get(shinobi);
        return new LinearGradientPaint(0, (float) startY + JBUIScale.scale(2f), 0, (float) startY + (float) height - JBUIScale.scale(2f),
            new float[]{ 0f, ONE_HALF, 1f }, new Color[]{ backgroundColor.getColorLight(), backgroundColor.getColor(), backgroundColor.getColorDark() });
    }

    @Override
    public Paint getPaint(final Shinobi shinobi, final ColorScheme colorScheme, final int startY, final int height) {
        return getPaintSingleColor(shinobi, colorScheme, startY, height);
    }
}
