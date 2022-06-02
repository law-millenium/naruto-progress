package com.lawmillenium.intellij.plugins.narutoprogress.theme;

import java.awt.*;

import com.lawmillenium.intellij.plugins.narutoprogress.model.Shinobi;

public abstract class PaintTheme {
    private final String id;
    private final String title;

    protected PaintTheme(final String id, final String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return title;
    }

    public abstract Paint getPaint(final Shinobi shinobi, final ColorScheme colorScheme, final int startY, final int height);
}
