package com.lawmillenium.intellij.plugins.narutoprogress;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;

import com.intellij.ide.plugins.DynamicPluginListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.extensions.PluginId;
import com.lawmillenium.intellij.plugins.narutoprogress.configuration.NarutoProgressState;
import org.jetbrains.annotations.NotNull;

public class NarutoProgressListener implements LafManagerListener, DynamicPluginListener {
    private static final String PROGRESS_BAR_UI_KEY = "ProgressBarUI";
    private static final String NARUTO_PROGRESS_BAR_UI_IMPLEMENTATION_NAME = NarutoProgressBarUi.class.getName();
    private volatile static Object previousProgressBar = null;
    private volatile static PluginId pluginId = null;

    public NarutoProgressListener() {
        updateProgressBarUi();
        pluginId = PluginId.getId("com.lawmillenium.narutoprogress");
    }

    static void updateProgressBarUi() {
        final Object prev = UIManager.get(PROGRESS_BAR_UI_KEY);
        if (!Objects.equals(NARUTO_PROGRESS_BAR_UI_IMPLEMENTATION_NAME, prev)) {
            previousProgressBar = prev;
        }
        Optional.ofNullable(NarutoProgressState.getInstance()).ifPresent(s -> ShurikenLoaderIconReplacer.updateSpinner(NarutoProgressState.getInstance().isReplaceLoaderIcon()));
        UIManager.put(PROGRESS_BAR_UI_KEY, NARUTO_PROGRESS_BAR_UI_IMPLEMENTATION_NAME);
        UIManager.getDefaults().put(NARUTO_PROGRESS_BAR_UI_IMPLEMENTATION_NAME, NarutoProgressBarUi.class);
    }

    static void resetProgressBarUi() {
        UIManager.put(PROGRESS_BAR_UI_KEY, previousProgressBar);
        ShurikenLoaderIconReplacer.updateSpinner(false);
    }

    @Override
    public void lookAndFeelChanged(@NotNull final LafManager lafManager) {
        updateProgressBarUi();
    }

    @Override
    public void pluginLoaded(@NotNull final IdeaPluginDescriptor pluginDescriptor) {
        if (Objects.equals(pluginId, pluginDescriptor.getPluginId())) {
            updateProgressBarUi();
        }
    }

    @Override
    public void beforePluginUnload(@NotNull final IdeaPluginDescriptor pluginDescriptor, final boolean isUpdate) {
        if (Objects.equals(pluginId, pluginDescriptor.getPluginId())) {
            resetProgressBarUi();
        }
    }
}
