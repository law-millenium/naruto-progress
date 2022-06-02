package com.lawmillenium.intellij.plugins.narutoprogress.theme;

import java.util.Map;

import com.lawmillenium.intellij.plugins.narutoprogress.model.Shinobi;

public class ColorScheme {
    private final String id;
    private final String name;
    private final Map<Shinobi, BackgroundColor> values;

    public ColorScheme(final String id, final String name, final Map<Shinobi, BackgroundColor> values) {
        this.id = id;
        this.name = name;
        this.values = values;
    }

    public BackgroundColor get(final Shinobi shinobi) {
        return values.getOrDefault(shinobi, values.values().stream().findFirst().orElse(null));
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
