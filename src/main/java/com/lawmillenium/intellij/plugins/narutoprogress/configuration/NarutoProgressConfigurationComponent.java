package com.lawmillenium.intellij.plugins.narutoprogress.configuration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.AnimatedIcon;
import com.intellij.ui.components.DefaultLinkButtonUI;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.roots.ScalableIconComponent;
import com.intellij.uiDesigner.core.Spacer;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.ThreeStateCheckBox;
import com.intellij.util.ui.ThreeStateCheckBox.State;
import com.lawmillenium.intellij.plugins.narutoprogress.NarutoProgressBarUi;
import com.lawmillenium.intellij.plugins.narutoprogress.NarutoProgressChangenotesDialog;
import com.lawmillenium.intellij.plugins.narutoprogress.ShinobiPicker;
import com.lawmillenium.intellij.plugins.narutoprogress.ShinobiResourceLoader;
import com.lawmillenium.intellij.plugins.narutoprogress.ShurikenLoaderIconReplacer;
import com.lawmillenium.intellij.plugins.narutoprogress.UpdateNotificationActivity;
import com.lawmillenium.intellij.plugins.narutoprogress.model.Shinobi;
import com.lawmillenium.intellij.plugins.narutoprogress.model.ShinobiGroup;
import com.lawmillenium.intellij.plugins.narutoprogress.theme.ColorScheme;
import com.lawmillenium.intellij.plugins.narutoprogress.theme.ColorSchemes;
import com.lawmillenium.intellij.plugins.narutoprogress.theme.PaintTheme;
import com.lawmillenium.intellij.plugins.narutoprogress.theme.PaintThemes;
import org.jetbrains.annotations.NotNull;

public class NarutoProgressConfigurationComponent {
    public static final float HUNDRED_PERCENT = 100f;
    final JLabel title = new JLabel("Naruto Progress");
    final JProgressBar determinateProgressBar = new JProgressBar(0, 2);
    final JProgressBar indeterminateProgressBar = new JProgressBar();
    final JLabel loader = new JLabel(new AnimatedIcon.Default());
    private final JComboBox<PaintTheme> theme = new ComboBox<>(PaintThemes.getAll());
    private final JComboBox<ColorScheme> colorScheme = new ComboBox<>(ColorSchemes.getAll());
    private final JBCheckBox replaceLoaderIcon = new JBCheckBox("Replace loader icon with shuriken");
    private final JBCheckBox drawSprites = new JBCheckBox("Draw sprites");
    private final JBCheckBox addToolTips = new JBCheckBox("Add tool tips");
    private final JBCheckBox indeterminateTransparency = new JBCheckBox("Transparency on indeterminate");
    private final JBCheckBox determinateTransparency = new JBCheckBox("Transparency on determinate");
    private final JBCheckBox showUpdateNotification = new JBCheckBox("Show update notification");
    private final Map<String, JBCheckBox> checkboxes = new HashMap<>();
    private final Multimap<ShinobiGroup, JCheckBox> checkboxesByShinobiGroup = ArrayListMultimap.create();
    private final Map<ShinobiGroup, ThreeStateCheckBox> toggleCheckBoxesByShinobiGroup = new EnumMap<>(ShinobiGroup.class);
    private final JButton selectAll = new JButton("Select all");
    private final JButton deselectAll = new JButton("Deselect all");
    private final JLabel label = new JLabel("?/? shinobis selected");
    private final AtomicInteger numSelected = new AtomicInteger(0);
    private final JSlider initialVelocity = new JSlider(1, 500, 100);
    private final JSlider acceleration = new JSlider(1, 500, 40);
    private final JBCheckBox restrictMaxHeight = new JBCheckBox("Restrict max height");
    private final JSlider maxHeight = new JSlider(8, 64, 20);
    private final JBCheckBox restrictMinHeight = new JBCheckBox("Restrict min height");
    private final JSlider minHeight = new JSlider(8, 64, 20);
    private JPanel mainPanel;
    private NarutoProgressBarUi determinateUi;
    private NarutoProgressBarUi indeterminateUi;

    public NarutoProgressConfigurationComponent() {
        createUi();
    }

    void createUi() {
        final FormBuilder formBuilder = FormBuilder.createFormBuilder();
        formBuilder.addComponent(createTitlePanel());
        formBuilder.addVerticalGap(5);
        formBuilder.addLabeledComponent("Preview", createPreviewPanel(), true);
        formBuilder.addSeparator();
        formBuilder.addComponent(createIndeterminatePanel());
        formBuilder.addSeparator();
        final JPanel themes = new JPanel();
        themes.setLayout(new GridLayout(1, 2));
        themes.add(LabeledComponent.create(theme, "Theme:"));
        themes.add(LabeledComponent.create(colorScheme, "Color scheme:"));
        formBuilder.addComponent(themes);
        formBuilder.addComponent(createCheckboxPanel());
        formBuilder.addComponent(createHeightPanel());

        formBuilder.addSeparator();
        formBuilder.addVerticalGap(5);
        formBuilder.addComponent(label);
        final JPanel selectButtonsPanel = new JPanel();
        selectButtonsPanel.setLayout(new GridLayout(1, 2));
        selectButtonsPanel.add(selectAll);
        selectButtonsPanel.add(deselectAll);
        selectAll.addActionListener(a -> {
            if (a.getID() == ActionEvent.ACTION_PERFORMED) {
                checkboxes.values().forEach(checkBox -> checkBox.setSelected(true));
            }
        });
        deselectAll.addActionListener(a -> {
            if (a.getID() == ActionEvent.ACTION_PERFORMED) {
                checkboxes.values().forEach(checkBox -> checkBox.setSelected(false));
            }
        });
        formBuilder.addComponent(selectButtonsPanel);

        final Set<ShinobiGroup> addedShinobiGroups = EnumSet.noneOf(ShinobiGroup.class);

        Shinobi.DEFAULT_SHINOBIS.values().stream().sorted(Comparator.comparing(Shinobi::getShinobiGroup)).forEach(shinobi -> {
            final ShinobiGroup shinobiGroup = shinobi.getShinobiGroup();

            if (addedShinobiGroups.add(shinobiGroup)) {
                final ThreeStateCheckBox shinobiGroupToggleCheckBox = new ThreeStateCheckBox(String.valueOf(shinobiGroup));
                shinobiGroupToggleCheckBox.setThirdStateEnabled(false);
                shinobiGroupToggleCheckBox.addItemListener(itemEvent -> {
                    final boolean isSelected = shinobiGroupToggleCheckBox.getState() == State.SELECTED;
                    checkboxesByShinobiGroup.get(shinobiGroup).forEach(cb -> cb.setSelected(isSelected));
                    refreshSelectAllButtons();
                });
                formBuilder.addComponent(shinobiGroupToggleCheckBox);
                toggleCheckBoxesByShinobiGroup.put(shinobiGroup, shinobiGroupToggleCheckBox);
            }

            final JBCheckBox checkBox = new JBCheckBox(shinobi.getNameWithNumber(), true);
            checkBox.addItemListener(itemEvent -> {
                if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                    numSelected.incrementAndGet();
                } else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
                    numSelected.decrementAndGet();
                }
                refreshShinobiGroupCheckBoxes(shinobiGroup);
                refreshSelectAllButtons();
            });

            formBuilder.addLabeledComponent(new ScalableIconComponent(ShinobiResourceLoader.getIcon(shinobi)), checkBox);
            checkboxes.put(shinobi.getName(), checkBox);
            checkboxesByShinobiGroup.put(shinobiGroup, checkBox);
            numSelected.incrementAndGet();
        });

        addedShinobiGroups.forEach(this::refreshShinobiGroupCheckBoxes);
        refreshSelectAllButtons();
        mainPanel = formBuilder.getPanel();
    }

    private JPanel createTitlePanel() {
        final JButton changenotes = new JButton("Changenotes");
        changenotes.setUI(DefaultLinkButtonUI.createUI(changenotes));
        changenotes.addActionListener(a -> new NarutoProgressChangenotesDialog(null).show());
        final JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new GridBagLayout());
        final GridBagConstraints left = new GridBagConstraints();
        left.anchor = GridBagConstraints.WEST;
        left.weightx = 0.5;
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        titlePanel.add(title, left);
        final GridBagConstraints right = new GridBagConstraints();
        right.anchor = GridBagConstraints.EAST;
        right.weightx = 0.5;
        titlePanel.add(changenotes, right);
        return titlePanel;
    }

    private JPanel createHeightPanel() {
        final JPanel heightPanel = new JPanel();
        heightPanel.setLayout(new GridLayout(2, 2));
        heightPanel.add(restrictMaxHeight);
        heightPanel.add(restrictMinHeight);
        heightPanel.add(maxHeight);
        heightPanel.add(minHeight);
        setupHeightConfig(restrictMaxHeight, maxHeight, "Restrict max height");
        setupHeightConfig(restrictMinHeight, minHeight, "Restrict min height");
        maxHeight.addChangeListener(changeEvent -> {
            if (maxHeight.getValue() < minHeight.getValue()) {
                minHeight.setValue(maxHeight.getValue());
            }
        });
        minHeight.addChangeListener(changeEvent -> {
            if (minHeight.getValue() > maxHeight.getValue()) {
                maxHeight.setValue(minHeight.getValue());
            }
        });
        return heightPanel;
    }

    private void setupHeightConfig(final JBCheckBox checkbox, final JSlider slider, final String text) {
        checkbox.addItemListener(itemEvent -> {
            if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
                slider.setEnabled(true);
                checkbox.setText(text + ": " + slider.getValue() + "px");
                if (determinateUi != null) {
                    determinateUi.computeScaledIcons();
                }
                if (indeterminateUi != null) {
                    indeterminateUi.computeScaledIcons();
                }
            } else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
                slider.setEnabled(false);
                checkbox.setText(text);
            }
            determinateProgressBar.setUI(determinateUi);
            indeterminateProgressBar.setUI(indeterminateUi);
        });
        slider.setEnabled(false);
        slider.addChangeListener(changeEvent -> {
            if (slider.isEnabled()) {
                checkbox.setText(text + ": " + slider.getValue() + "px");
            }
            if (determinateUi != null) {
                determinateUi.computeScaledIcons();
                determinateProgressBar.setUI(determinateUi);
            }
            if (indeterminateUi != null) {
                indeterminateUi.computeScaledIcons();
                indeterminateProgressBar.setUI(indeterminateUi);
            }
        });
    }

    @NotNull
    private JPanel createCheckboxPanel() {
        final JPanel checkboxPanel = new JPanel();
        checkboxPanel.setLayout(new GridLayout(3, 2));

        checkboxPanel.add(drawSprites);
        drawSprites.setToolTipText("If disabled, progress bars will just show the background colors");
        checkboxPanel.add(indeterminateTransparency);

        checkboxPanel.add(addToolTips);
        addToolTips.setToolTipText("Whether or not to add a naruto tool tip (hover text) on the progress bars");
        checkboxPanel.add(determinateTransparency);

        replaceLoaderIcon.addActionListener(a -> {
            if (a.getID() == ActionEvent.ACTION_PERFORMED) {
                ShurikenLoaderIconReplacer.updateSpinner(replaceLoaderIcon.isSelected());
                loader.setIcon(new AnimatedIcon.Default());
            }
        });
        checkboxPanel.add(replaceLoaderIcon);

        showUpdateNotification.setToolTipText("Turn on or off the notification when the plugin has been updated");
        checkboxPanel.add(showUpdateNotification);

        return checkboxPanel;
    }

    void updateUi(final NarutoProgressState state) {
        if (state != null) {
            Optional.ofNullable(UpdateNotificationActivity.getPluginDescriptor()).ifPresent(
                desc -> title.setText("Naruto Progress " + desc.getVersion()));
            initialVelocity.setValue((int) (state.initialVelocity * 100));
            acceleration.setValue((int) (state.acceleration * 100));
            theme.setSelectedItem(PaintThemes.getByIdOrDefault(state.theme));
            colorScheme.setSelectedItem(ColorSchemes.getByIdOrDefault(state.colorScheme));
            drawSprites.setSelected(state.drawSprites);
            addToolTips.setSelected(state.addToolTips);
            indeterminateTransparency.setSelected(state.transparencyOnIndeterminate);
            determinateTransparency.setSelected(state.transparencyOnDeterminate);
            state.enabledShinobisNames.forEach((id, enabled) -> checkboxes.computeIfPresent(id, (p, check) -> {
                check.setSelected(enabled);
                return check;
            }));
            replaceLoaderIcon.setSelected(NarutoProgressState.getInstance().isReplaceLoaderIcon());
            showUpdateNotification.setSelected(state.showUpdateNotification);
            maxHeight.setValue(state.maximumHeight);
            minHeight.setValue(state.minimumHeight);
            restrictMaxHeight.setSelected(state.restrictMaximumHeight);
            restrictMinHeight.setSelected(state.restrictMinimumHeight);
        }
    }

    public JPanel getPanel() {
        return mainPanel;
    }

    public Map<String, Boolean> getEnabledIdMap() {
        return checkboxes.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().isSelected()));
    }

    public JBCheckBox getDrawSprites() {
        return drawSprites;
    }

    public JBCheckBox getAddToolTips() {
        return addToolTips;
    }

    public JSlider getInitialVelocity() {
        return initialVelocity;
    }

    public JSlider getAcceleration() {
        return acceleration;
    }

    public JComboBox<PaintTheme> getTheme() {
        return theme;
    }

    public JComboBox<ColorScheme> getColorScheme() {
        return colorScheme;
    }

    public JBCheckBox getIndeterminateTransparency() {
        return indeterminateTransparency;
    }

    public JBCheckBox getDeterminateTransparency() {
        return determinateTransparency;
    }

    public JBCheckBox getReplaceLoaderIcon() {
        return replaceLoaderIcon;
    }

    public JBCheckBox getShowUpdateNotification() {
        return showUpdateNotification;
    }

    public JBCheckBox getRestrictMaxHeight() {
        return restrictMaxHeight;
    }

    public JSlider getMaxHeight() {
        return maxHeight;
    }

    public JBCheckBox getRestrictMinHeight() {
        return restrictMinHeight;
    }

    public JSlider getMinHeight() {
        return minHeight;
    }

    private void refreshSelectAllButtons() {
        final int i = numSelected.get();
        final int size = checkboxes.size();
        deselectAll.setEnabled(i > 0);
        selectAll.setEnabled(i < size);
        label.setText(i + "/" + size + " shinobis selected");
    }

    private void refreshShinobiGroupCheckBoxes(final ShinobiGroup shinobiGroup) {
        final ThreeStateCheckBox toggleCheckBox = toggleCheckBoxesByShinobiGroup.get(shinobiGroup);
        final Collection<JCheckBox> shinobisCheckBoxes = checkboxesByShinobiGroup.get(shinobiGroup);
        final boolean areAllSelected = shinobisCheckBoxes.stream().allMatch(JCheckBox::isSelected);
        final boolean areNoneSelected = shinobisCheckBoxes.stream().noneMatch(JCheckBox::isSelected);

        if (areAllSelected) {
            toggleCheckBox.setState(State.SELECTED);
        } else if (areNoneSelected) {
            toggleCheckBox.setState(State.NOT_SELECTED);
        } else {
            toggleCheckBox.setState(State.DONT_CARE);
        }
    }

    private JPanel createPreviewPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());

        determinateProgressBar.setIndeterminate(false);
        determinateProgressBar.setValue(1);
        determinateUi = createProgressBarUi();
        determinateProgressBar.setUI(determinateUi);

        indeterminateProgressBar.setIndeterminate(true);
        indeterminateUi = createProgressBarUi();
        indeterminateProgressBar.setUI(indeterminateUi);

        final JButton randomizeButton = new JButton(AllIcons.Actions.Refresh);
        randomizeButton.setToolTipText("Randomize");
        randomizeButton.addActionListener(actionEvent -> {
            if (actionEvent.getID() == ActionEvent.ACTION_PERFORMED) {
                determinateUi = createProgressBarUi();
                indeterminateUi = createProgressBarUi();
                determinateProgressBar.setUI(determinateUi);
                indeterminateProgressBar.setUI(indeterminateUi);
            }
        });

        loader.setToolTipText("loader/spinner icon");

        final GridBagConstraints buttonConstraints = new GridBagConstraints();
        buttonConstraints.gridx = 0;
        buttonConstraints.gridy = 0;
        buttonConstraints.gridwidth = 1;
        buttonConstraints.gridheight = 1;
        buttonConstraints.weightx = 0;
        panel.add(randomizeButton, buttonConstraints);
        final GridBagConstraints loaderConstraints = new GridBagConstraints();
        loaderConstraints.gridx = GridBagConstraints.RELATIVE;
        loaderConstraints.gridy = 0;
        loaderConstraints.gridwidth = 1;
        loaderConstraints.gridheight = 1;
        loaderConstraints.weightx = 0.1;
        panel.add(LabeledComponent.create(loader, "Loader", BorderLayout.NORTH), loaderConstraints);
        final GridBagConstraints progressBarConstraints = new GridBagConstraints();
        progressBarConstraints.gridx = GridBagConstraints.RELATIVE;
        progressBarConstraints.gridy = 0;
        progressBarConstraints.gridwidth = 3;
        progressBarConstraints.gridheight = 1;
        progressBarConstraints.weightx = 0.45;
        progressBarConstraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(LabeledComponent.create(determinateProgressBar, "Determinate", BorderLayout.NORTH), progressBarConstraints);
        panel.add(LabeledComponent.create(indeterminateProgressBar, "Indeterminate", BorderLayout.NORTH), progressBarConstraints);
        return panel;
    }

    private NarutoProgressBarUi createProgressBarUi() {
        return new NarutoProgressBarUi(ShinobiPicker.get(), () -> initialVelocity.getValue() / HUNDRED_PERCENT,
            () -> acceleration.getValue() / HUNDRED_PERCENT, () -> theme.getItemAt(theme.getSelectedIndex()),
            () -> colorScheme.getItemAt(colorScheme.getSelectedIndex()), indeterminateTransparency::isSelected, determinateTransparency::isSelected,
            drawSprites::isSelected, addToolTips::isSelected, restrictMaxHeight::isSelected, maxHeight::getValue, restrictMinHeight::isSelected,
            minHeight::getValue);
    }

    private JPanel createIndeterminatePanel() {
        final JPanel indeterminatePanel = new JPanel();
        indeterminatePanel.setLayout(new GridLayout(2, 2));
        final LabeledComponent<JSlider> labeledInitVelocity = LabeledComponent.create(initialVelocity,
            String.format("Indeterminate initial velocity (%d/%d)", initialVelocity.getValue(), initialVelocity.getMaximum()));
        indeterminatePanel.add(labeledInitVelocity);
        indeterminatePanel.add(new Spacer());
        final LabeledComponent<JSlider> labeledAccel = LabeledComponent.create(acceleration,
            String.format("Indeterminate acceleration (%d/%d)", acceleration.getValue(), acceleration.getMaximum()));
        indeterminatePanel.add(labeledAccel);
        final JButton resetIndeterminateButton = new JButton("Reset to defaults");
        resetIndeterminateButton.addActionListener(a -> {
            if (a.getID() == ActionEvent.ACTION_PERFORMED) {
                acceleration.setValue(40);
                initialVelocity.setValue(100);
            }
        });
        initialVelocity.addChangeListener(e -> labeledInitVelocity.getLabel()
            .setText(String.format("Indeterminate initial velocity (%d/%d)", initialVelocity.getValue(), initialVelocity.getMaximum())));
        acceleration.addChangeListener(e -> labeledAccel.getLabel()
            .setText(String.format("Indeterminate acceleration (%d/%d)", acceleration.getValue(), acceleration.getMaximum())));
        indeterminatePanel.add(resetIndeterminateButton);
        return indeterminatePanel;
    }
}
