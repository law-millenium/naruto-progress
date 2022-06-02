package com.lawmillenium.intellij.plugins.narutoprogress;

import javax.swing.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;

import com.intellij.openapi.application.ApplicationInfo;
import com.intellij.ui.AnimatedIcon;
import icons.ShurikenIcons;
import org.jetbrains.annotations.NotNull;

public class ShurikenLoaderIconReplacer {
    private static List<Icon> originalIcons;
    private static AnimatedIcon originalInstance;
    private static List<Icon> shurikenIcons;
    private static AnimatedIcon.Frame[] originalFrames;

    private static boolean replaced;
    private static boolean reflectionFailed;

    @SuppressWarnings("unchecked")
    public static void updateSpinner(final boolean useShuriken) {
        if (reflectionFailed || useShuriken == replaced) {
            return;
        }
        try {
            final Class<AnimatedIcon.Default> defaultClass = AnimatedIcon.Default.class;
            final Field iconsField = defaultClass.getDeclaredField("ICONS");
            final Field instanceField = defaultClass.getDeclaredField("INSTANCE");
            Field defaultFramesField = null;
            if (isIdeaMajorVersionOver2022()) {
                defaultFramesField = defaultClass.getDeclaredField("DEFAULT_FRAMES");
            }
            makeFieldNonFinal(iconsField, instanceField, defaultFramesField);
            iconsField.setAccessible(true);
            instanceField.setAccessible(true);
            if (originalIcons == null) {
                originalIcons = (List<Icon>) iconsField.get(null);
            }
            if (originalInstance == null) {
                originalInstance = (AnimatedIcon) instanceField.get(null);
            }
            iconsField.set(null, useShuriken ? getShurikenIcons() : originalIcons);
            instanceField.set(null, useShuriken ? new AnimatedIcon.Default() : originalInstance);
            if (isIdeaMajorVersionOver2022()) {
                Objects.requireNonNull(defaultFramesField).setAccessible(true);
                if (originalFrames == null) {
                    originalFrames = (AnimatedIcon.Frame[]) defaultFramesField.get(null);
                }
                if (useShuriken) {
                    defaultFramesField.set(null, getFrames(125, getShurikenIcons().toArray(Icon[]::new)));
                } else {
                    defaultFramesField.set(null, originalFrames);
                }
            }
            replaced = useShuriken;
        } catch (final IllegalAccessException | NoSuchFieldException exception) {
            reflectionFailed = true;
            exception.printStackTrace();
        }
    }

    private static boolean isIdeaMajorVersionOver2022() {
        return ApplicationInfo.getInstance().getMajorVersion().compareTo("2022") >= 0;
    }

    private static void makeFieldNonFinal(final Field... fields) throws NoSuchFieldException, IllegalAccessException {
        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        for (final Field field : fields) {
            if (field != null) {
                modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            }
        }
    }

    private static List<Icon> getShurikenIcons() {
        if (shurikenIcons == null) {
            shurikenIcons = ShurikenIcons.getShurikensIcons();
        }
        return shurikenIcons;
    }

    private static AnimatedIcon.Frame[] getFrames(final int delay, final Icon @NotNull ... icons) {
        int length = icons.length;
        assert length > 0 : "empty array";
        AnimatedIcon.Frame[] frames = new AnimatedIcon.Frame[length];
        for (int i = 0; i < length; i++) {
            Icon icon = icons[i];
            assert icon != null : "null icon";
            frames[i] = new AnimatedIcon.Frame() {
                @NotNull
                @Override
                public Icon getIcon() {
                    return icon;
                }

                @Override
                public int getDelay() {
                    return delay;
                }
            };
        }
        return frames;
    }
}
