package com.lawmillenium.intellij.plugins.narutoprogress.theme;

import java.awt.*;

public record BackgroundColor(Color colorLight, Color color, Color colorDark) {
    public BackgroundColor(String colorLight, final String color, final String colorDark) {
        this(Color.decode(colorLight), Color.decode(color), Color.decode(colorDark));
    }
}