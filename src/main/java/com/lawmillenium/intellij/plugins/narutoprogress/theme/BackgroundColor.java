package com.lawmillenium.intellij.plugins.narutoprogress.theme;

import java.awt.*;

public class BackgroundColor {
    private final Color colorLight;
    private final Color color;
    private final Color colorDark;

    public BackgroundColor(final String colorLight, final String color, final String colorDark) {
        this.colorLight = Color.decode(colorLight);
        this.color = Color.decode(color);
        this.colorDark = Color.decode(colorDark);
    }

    public Color getColorLight() {
        return colorLight;
    }

    public Color getColor() {
        return color;
    }

    public Color getColorDark() {
        return colorDark;
    }
}
