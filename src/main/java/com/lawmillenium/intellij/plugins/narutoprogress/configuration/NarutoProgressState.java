package com.lawmillenium.intellij.plugins.narutoprogress.configuration;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import com.lawmillenium.intellij.plugins.narutoprogress.ShurikenLoaderIconReplacer;
import com.lawmillenium.intellij.plugins.narutoprogress.model.Shinobi;
import org.jetbrains.annotations.NotNull;

@State(name = "com.lawmillenium.intellij.plugins.narutoprogress.configuration.NarutoProgressState", storages = { @Storage("NarutoProgress.xml") })
public class NarutoProgressState implements PersistentStateComponent<NarutoProgressState> {
    public String version = "";
    public float initialVelocity = 1.0f;
    public float acceleration = 0.4f;
    public Map<String, Boolean> enabledShinobisNames = Shinobi.DEFAULT_SHINOBIS.keySet().stream().collect(
        Collectors.toMap(Function.identity(), p -> true));
    public String theme;
    public boolean drawSprites = true;
    public boolean addToolTips = true;
    public boolean transparencyOnIndeterminate = true;
    public boolean transparencyOnDeterminate = false;
    public String colorScheme;
    public boolean showUpdateNotification = true;
    public boolean restrictMaximumHeight = false;
    public int maximumHeight = 20;
    public boolean restrictMinimumHeight = false;
    public int minimumHeight = 20;
    private boolean replaceLoaderIcon = true;

    public static NarutoProgressState getInstance() {
        return ApplicationManager.getApplication().getService(NarutoProgressState.class);
    }

    public boolean isReplaceLoaderIcon() {
        return replaceLoaderIcon;
    }

    public void setReplaceLoaderIcon(final boolean updated) {
        replaceLoaderIcon = updated;
        ShurikenLoaderIconReplacer.updateSpinner(updated);
    }

    public void setHeightLimits(final int newMaxHeight, final int newMinHeight) {
        if (newMinHeight > newMaxHeight) {
            minimumHeight = newMaxHeight;
            maximumHeight = newMaxHeight;
        } else {
            minimumHeight = newMinHeight;
            maximumHeight = newMaxHeight;
        }
    }

    @Override
    public NarutoProgressState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull final NarutoProgressState state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
