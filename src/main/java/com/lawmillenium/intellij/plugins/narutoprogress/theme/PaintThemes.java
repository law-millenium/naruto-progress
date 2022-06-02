package com.lawmillenium.intellij.plugins.narutoprogress.theme;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

import java.util.Arrays;

import com.google.common.collect.ImmutableMap;

public final class PaintThemes {
    private static final ImmutableMap<String, PaintTheme> ALL_THEMES;
    private static final PaintTheme DEFAULT_THEME;

    static {
        DEFAULT_THEME = new SmoothTheme("smooth", "Smooth");

        final PaintTheme[] themes = { DEFAULT_THEME, new FlatTheme("flat", "Flat", BackgroundColor::getColor), new FlatTheme("flat_light", "Flat (Light)",
            BackgroundColor::getColorLight), new FlatTheme("flat_dark", "Flat (Dark)", BackgroundColor::getColorDark), };

        ALL_THEMES = Arrays.stream(themes).collect(toImmutableMap(PaintTheme::getId, identity()));
    }

    private PaintThemes() {
    }

    public static PaintTheme[] getAll() {
        return ALL_THEMES.values().toArray(new PaintTheme[0]); // ImmutableMap preserves insertion order
    }

    public static PaintTheme getByIdOrDefault(final String id) {
        return ALL_THEMES.getOrDefault(id, DEFAULT_THEME);
    }

    public static PaintTheme getDefaultTheme() {
        return DEFAULT_THEME;
    }
}
