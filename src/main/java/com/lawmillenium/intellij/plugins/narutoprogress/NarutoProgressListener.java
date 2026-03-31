package com.lawmillenium.intellij.plugins.narutoprogress;

import com.intellij.ide.plugins.DynamicPluginListener;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.lawmillenium.intellij.plugins.narutoprogress.configuration.NarutoProgressState;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;

public class NarutoProgressListener implements LafManagerListener, DynamicPluginListener {
    private static final String PROGRESS_BAR_UI_KEY = "ProgressBarUI";
    private static final String NARUTO_PROGRESS_BAR_UI_IMPLEMENTATION_NAME = NarutoProgressBarUi.class.getName();
    private volatile static Object previousProgressBar = null;
    private volatile static PluginId pluginId = null;
    private static boolean initialized = false;

    public NarutoProgressListener() {
        if (!initialized) {
            updateProgressBarUi();
            pluginId = PluginId.getId("com.lawmillenium.narutoprogress");
            initialized = true;
        }
    }

    static void updateProgressBarUi() {
        ApplicationManager.getApplication().invokeLater(() -> {
            final Object prev = UIManager.get(PROGRESS_BAR_UI_KEY);
            if (!Objects.equals(NARUTO_PROGRESS_BAR_UI_IMPLEMENTATION_NAME, prev)) {
                previousProgressBar = prev;
            }
            Optional.ofNullable(NarutoProgressState.getInstance()).ifPresent(s -> ShurikenLoaderIconReplacer.updateSpinner(NarutoProgressState.getInstance().isReplaceLoaderIcon()));
            UIManager.put(PROGRESS_BAR_UI_KEY, NARUTO_PROGRESS_BAR_UI_IMPLEMENTATION_NAME);
            UIManager.getDefaults().put(NARUTO_PROGRESS_BAR_UI_IMPLEMENTATION_NAME, NarutoProgressBarUi.class);
        });
    }

    static void resetProgressBarUi() {
        ApplicationManager.getApplication().invokeLater(() -> {
            UIManager.put(PROGRESS_BAR_UI_KEY, previousProgressBar);
            ShurikenLoaderIconReplacer.updateSpinner(false);
        });
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

    /**
     * StartupActivity to ensure the plugin is properly initialized after IDE startup
     */
    public static class NarutoProgressStartupActivity implements StartupActivity.DumbAware {
        @Override
        public void runActivity(@NotNull Project project) {
            updateProgressBarUi();
        }
    }
}
