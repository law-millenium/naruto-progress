package com.lawmillenium.intellij.plugins.narutoprogress;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import com.lawmillenium.intellij.plugins.narutoprogress.configuration.NarutoProgressState;
import com.lawmillenium.intellij.plugins.narutoprogress.model.Shinobi;

public class ShinobiPicker {
    private static final String TARGET_ENV_VAR = "NARUTO_PROGRESS_TARGET";

    private static final Random RANDOM = new Random();

    public static Shinobi get() {
        final String target = System.getenv().get(TARGET_ENV_VAR);
        if (target != null) {
            return Shinobi.getByName(target);
        }
        final List<String> enabledShinobisNames = Optional.ofNullable(NarutoProgressState.getInstance()).map(ShinobiPicker::getEnabledShinobisNames)
            .orElse(null);
        if (enabledShinobisNames == null || enabledShinobisNames.isEmpty()) {
            return Shinobi.MISSINGNO;
        }
        return Shinobi.getByName(enabledShinobisNames.get(RANDOM.nextInt(enabledShinobisNames.size())));
    }

    private static List<String> getEnabledShinobisNames(final NarutoProgressState state) {
        return state.enabledShinobisNames.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).collect(Collectors.toList());
    }
}
