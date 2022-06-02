package com.lawmillenium.intellij.plugins.narutoprogress;

import javax.swing.*;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.lawmillenium.intellij.plugins.narutoprogress.model.Shinobi;

public final class ShinobiResourceLoader {
    private static final String SPRITE_RESOURCE_PATH = "com/lawmillenium/intellij/plugins/narutoprogress/sprites/";

    private static final Cache<String, ImageIcon> CACHE = CacheBuilder.newBuilder().maximumSize(100L).build();
    private static final Pattern SLASH_PATTERN = Pattern.compile("/");

    private ShinobiResourceLoader() {
    }

    public static ImageIcon getIcon(final Shinobi shinobi) {
        return getIconInternal(getIconPath(shinobi));
    }

    public static ImageIcon getReversedIcon(final Shinobi shinobi) {
        return getIconInternal(getReversedIconPath(shinobi));
    }

    public static String getIconPath(final Shinobi shinobi) {
        return SPRITE_RESOURCE_PATH + shinobi.getName().replace(' ', '_') + ".gif";
    }

    public static String getReversedIconPath(final Shinobi shinobi) {
        return SPRITE_RESOURCE_PATH + shinobi.getName().replace(' ', '_') + "_r.gif";
    }

    public static Optional<URL> getResource(final String resourceName) {
        return Optional.ofNullable(ShinobiResourceLoader.class.getClassLoader().getResource(resourceName)).or(() -> {
            if (resourceName.startsWith("/")) {
                return Optional.ofNullable(
                    ShinobiResourceLoader.class.getClassLoader().getResource(SLASH_PATTERN.matcher(resourceName).replaceFirst("")));
            }
            return Optional.ofNullable(ShinobiResourceLoader.class.getClassLoader().getResource('/' + resourceName));
        });
    }

    public static Optional<InputStream> getResourceAsStream(final String resourceName) {
        return Optional.ofNullable(ShinobiResourceLoader.class.getClassLoader().getResourceAsStream(resourceName)).or(() -> {
            if (resourceName.startsWith("/")) {
                return Optional.ofNullable(
                    ShinobiResourceLoader.class.getClassLoader().getResourceAsStream(SLASH_PATTERN.matcher(resourceName).replaceFirst("")));
            }
            return Optional.ofNullable(ShinobiResourceLoader.class.getClassLoader().getResourceAsStream('/' + resourceName));
        });
    }

    private static ImageIcon getIconInternal(final String resourceName) {
        try {
            return CACHE.get(resourceName, () -> getResource(resourceName).map(ImageIcon::new).orElseGet(ImageIcon::new));
        } catch (final ExecutionException e) {
            return new ImageIcon();
        }
    }
}
