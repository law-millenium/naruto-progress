package com.lawmillenium.intellij.plugins.narutoprogress.configuration;

import javax.swing.*;
import java.util.Objects;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;

public class NarutoProgressConfigurable implements Configurable {
    public static final float HUNDRED_PERCENT = 100f;
    private NarutoProgressConfigurationComponent component;

    @Nls
    @Override
    public String getDisplayName() {
        return "Naruto Progress";
    }

    @Override
    public JComponent createComponent() {
        component = new NarutoProgressConfigurationComponent();
        return component.getPanel();
    }

    @Override
    public boolean isModified() {
        final NarutoProgressState state = NarutoProgressState.getInstance();
        return component != null && (!state.enabledShinobisNames.equals(component.getEnabledIdMap()) || !Objects.equals(state.theme,
            component.getTheme().getItemAt(component.getTheme().getSelectedIndex()).getId()) || !Objects.equals(state.colorScheme,
            component.getColorScheme().getItemAt(component.getColorScheme().getSelectedIndex()).getId()) ||
            state.drawSprites != component.getDrawSprites().isSelected() || state.addToolTips != component.getAddToolTips().isSelected() ||
            state.transparencyOnIndeterminate != component.getIndeterminateTransparency().isSelected() ||
            state.transparencyOnDeterminate != component.getDeterminateTransparency().isSelected() ||
            state.initialVelocity != component.getInitialVelocity().getValue() / HUNDRED_PERCENT ||
            state.acceleration != component.getAcceleration().getValue() / HUNDRED_PERCENT ||
            NarutoProgressState.getInstance().isReplaceLoaderIcon() != component.getReplaceLoaderIcon().isSelected() ||
            state.showUpdateNotification != component.getShowUpdateNotification().isSelected() ||
            state.restrictMaximumHeight != component.getRestrictMaxHeight().isSelected() ||
            state.maximumHeight != component.getMaxHeight().getValue() ||
            state.restrictMinimumHeight != component.getRestrictMinHeight().isSelected() ||
            state.minimumHeight != component.getMinHeight().getValue());
    }

    @Override
    public void apply() {
        final NarutoProgressState state = NarutoProgressState.getInstance();
        state.enabledShinobisNames = component.getEnabledIdMap();
        state.theme = component.getTheme().getItemAt(component.getTheme().getSelectedIndex()).getId();
        state.colorScheme = component.getColorScheme().getItemAt(component.getColorScheme().getSelectedIndex()).getId();
        state.drawSprites = component.getDrawSprites().isSelected();
        state.addToolTips = component.getAddToolTips().isSelected();
        state.transparencyOnIndeterminate = component.getIndeterminateTransparency().isSelected();
        state.transparencyOnDeterminate = component.getDeterminateTransparency().isSelected();
        state.initialVelocity = component.getInitialVelocity().getValue() / HUNDRED_PERCENT;
        state.acceleration = component.getAcceleration().getValue() / HUNDRED_PERCENT;
        NarutoProgressState.getInstance().setReplaceLoaderIcon(component.getReplaceLoaderIcon().isSelected());
        state.showUpdateNotification = component.getShowUpdateNotification().isSelected();
        state.restrictMaximumHeight = component.getRestrictMaxHeight().isSelected();
        state.restrictMinimumHeight = component.getRestrictMinHeight().isSelected();
        NarutoProgressState.getInstance().setHeightLimits(component.getMaxHeight().getValue(), component.getMinHeight().getValue());
    }

    @Override
    public void reset() {
        final NarutoProgressState state = NarutoProgressState.getInstance();
        component.updateUi(state);
    }

    @Override
    public void disposeUIResources() {
        component = null;
    }
}
