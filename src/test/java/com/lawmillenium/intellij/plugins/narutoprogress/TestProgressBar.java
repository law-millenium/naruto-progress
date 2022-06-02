package com.lawmillenium.intellij.plugins.narutoprogress;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;

import com.intellij.ide.ui.laf.darcula.DarculaLaf;
import com.intellij.ide.ui.laf.darcula.ui.DarculaTextBorder;
import com.intellij.mock.MockApplication;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.ui.components.fields.IntegerField;
import com.intellij.util.ReflectionUtil;
import com.lawmillenium.intellij.plugins.narutoprogress.configuration.NarutoProgressState;
import com.lawmillenium.intellij.plugins.narutoprogress.model.Shinobi;
import com.lawmillenium.intellij.plugins.narutoprogress.theme.ColorScheme;
import com.lawmillenium.intellij.plugins.narutoprogress.theme.ColorSchemes;
import com.lawmillenium.intellij.plugins.narutoprogress.theme.PaintTheme;
import com.lawmillenium.intellij.plugins.narutoprogress.theme.PaintThemes;

public class TestProgressBar {
    private static final int MAX_SHIFT_VALUE = 900;
    private NarutoProgressState state;

    private JFrame frame;
    private IntegerField xShift;
    private IntegerField yShift;
    private IntegerField height;
    private JProgressBar progressBar;

    private Shinobi selectedShinobi;
    private int originalXShift = 0;
    private int originalYShift = 0;
    private int originalHeight = 20;

    @SuppressWarnings("FieldCanBeLocal")
    private final boolean useDarkMode = true;

    @SuppressWarnings("FieldCanBeLocal")
    private final Shinobi target = null;

    @SuppressWarnings("ConstantConditions")
    public TestProgressBar() {
        setUpMockApplication();
        setLookAndFeel();
        updateSelectedShinobi(Optional.ofNullable(target).orElseGet(ShinobiPicker::get));
        initializeFrame();
        addShutdownHook();
    }

    private void setUpMockApplication() {
        state = new NarutoProgressState();
        state.drawSprites = true;
        state.addToolTips = false;
        final Disposable parent = () -> { /*do nothing*/ };
        final MockApplication application = MockApplication.setUp(parent);
        application.registerService(NarutoProgressState.class, state);
        ApplicationManager.setApplication(application, parent);
    }

    private void setLookAndFeel() {
        if (useDarkMode) {
            final DarculaLaf darkMode = new DarculaLaf();
            try {
                UIManager.setLookAndFeel(darkMode);
            } catch (Exception e) {
                System.out.println("unable to set look and feel");
                e.printStackTrace();
            }
        }
    }

    private void initializeFrame() {
        final JPanel contentPanel = createContentPanel();
        frame = new JFrame();
        frame.setTitle("Naruto ProgressBar TestUI");
        frame.setContentPane(contentPanel);
        frame.setSize(contentPanel.getPreferredSize());
        frame.setResizable(false);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
    }

    private JPanel createContentPanel() {
        final JPanel contentPanel = new JPanel();
        contentPanel.setPreferredSize(new Dimension(400, 250));
        contentPanel.setLayout(new GridLayout(5, 1));

        contentPanel.add(createShinobiComboBox());
        contentPanel.add(createProgressBar());
        contentPanel.add(createThemePanel());
        contentPanel.add(createShiftPanel());
        contentPanel.add(createButtonPanel());

        return contentPanel;
    }

    private JPanel createThemePanel() {
        final JPanel themePanel = new JPanel();
        themePanel.setLayout(new GridLayout(1, 2));
        final ComboBox<PaintTheme> paintThemeComboBox = new ComboBox<>(PaintThemes.getAll());
        final ComboBox<ColorScheme> colorSchemeComboBox = new ComboBox<>(ColorSchemes.getAll());
        paintThemeComboBox.addActionListener(e -> {
            state.theme = ((PaintTheme) ((ComboBox<?>) e.getSource()).getSelectedItem()).getId();
            updatePositionAndUI(e);
        });
        colorSchemeComboBox.addActionListener(e -> {
            state.colorScheme = ((ColorScheme) ((ComboBox<?>) e.getSource()).getSelectedItem()).getId();
            updatePositionAndUI(e);
        });

        themePanel.add(LabeledComponent.create(paintThemeComboBox, "Paint theme", BorderLayout.NORTH));
        themePanel.add(LabeledComponent.create(colorSchemeComboBox, "Color theme", BorderLayout.NORTH));
        return themePanel;
    }

    @SuppressWarnings("unchecked")
    private ComboBox<Shinobi> createShinobiComboBox() {
        final ComboBox<Shinobi> shinobisComboBox = new ComboBox<>(Shinobi.values());
        shinobisComboBox.setSelectedItem(selectedShinobi);
        shinobisComboBox.addActionListener(e -> {
            printIfShiftUpdated();
            updateSelectedShinobi(Objects.requireNonNull((Shinobi) ((ComboBox<Shinobi>) e.getSource()).getSelectedItem()));
            resetShifts();
            updatePositionAndUI(e);
        });
        return shinobisComboBox;
    }

    private JProgressBar createProgressBar() {
        progressBar = new JProgressBar();
        progressBar.setUI(new NarutoProgressBarUi(selectedShinobi));
        progressBar.setMinimum(0);
        progressBar.setMaximum(2);
        progressBar.setValue(1);
        return progressBar;
    }

    private JPanel createShiftPanel() {
        final JPanel shiftPanel = new JPanel();
        shiftPanel.setLayout(new GridLayout(1, 3));
        xShift = createIntegerFieldTextBox("xShift");
        yShift = createIntegerFieldTextBox("yShift");
        height = createIntegerFieldTextBox("height", 5, 64);

        shiftPanel.add(LabeledComponent.create(xShift, "X shift", BorderLayout.NORTH));
        shiftPanel.add(LabeledComponent.create(yShift, "Y shift", BorderLayout.NORTH));
        shiftPanel.add(LabeledComponent.create(height, "Height", BorderLayout.NORTH));
        resetShifts();
        return shiftPanel;
    }

    private IntegerField createIntegerFieldTextBox(final String valueName) {
        return createIntegerFieldTextBox(valueName, -MAX_SHIFT_VALUE, MAX_SHIFT_VALUE);
    }

    private IntegerField createIntegerFieldTextBox(final String valueName, final int min, final int max) {
        final IntegerField shiftField = new IntegerField(valueName, min, max);
        shiftField.setBorder(new DarculaTextBorder());
        shiftField.addActionListener(this::updatePositionAndUI);
        return shiftField;
    }

    private JPanel createButtonPanel() {
        final JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2));

        final JButton updateButton = new JButton("Update");
        updateButton.addActionListener(this::updatePositionAndUI);

        final JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            resetShifts();
            updatePositionAndUI(e);
        });

        buttonPanel.add(updateButton);
        buttonPanel.add(resetButton);

        return buttonPanel;
    }

    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                printIfShiftUpdated();
                super.run();
            }
        });
    }

    private void updateSelectedShinobi(final Shinobi newShinobi) {
        if (selectedShinobi != null) {
            setSelectedShinobiIntField("xShift", originalXShift);
            setSelectedShinobiIntField("yShift", originalYShift);
            setSelectedShinobiIntField("height", originalHeight);
        }
        selectedShinobi = newShinobi;
        originalXShift = newShinobi.getXShift();
        originalYShift = newShinobi.getYShift();
        originalHeight = newShinobi.getHeight();
    }

    private void resetShifts() {
        xShift.setValue(originalXShift);
        yShift.setValue(originalYShift);
        height.setValue(originalHeight);
        xShift.setDefaultValue(originalXShift);
        yShift.setDefaultValue(originalYShift);
        height.setDefaultValue(originalHeight);
    }

    private void updatePositionAndUI(final ActionEvent e) {
        if (e.getID() == ActionEvent.ACTION_PERFORMED) {
            handleShiftChange(xShift);
            handleShiftChange(yShift);
            handleShiftChange(height);
            progressBar.setUI(new NarutoProgressBarUi(selectedShinobi));
            frame.repaint();
        }
    }

    private void handleShiftChange(final IntegerField field) {
        try {
            field.validateContent();
        } catch (final ConfigurationException ex) {
            field.setToolTipText(ex.getMessage());
            return;
        }
        field.setToolTipText(null);
        setSelectedShinobiIntField(field.getValueName(), field.getValue());
    }

    private void setSelectedShinobiIntField(final String name, final int amount) {
        try {
            final Field field = ReflectionUtil.findField(Shinobi.class, Integer.TYPE, name);
            if ((int) field.get(selectedShinobi) != amount) {
                field.setAccessible(true);
                field.setInt(selectedShinobi, amount);
                field.setAccessible(false);
            }
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void printIfShiftUpdated() {
        if (shiftUpdated()) {
            System.out.printf("%nUpdated shift for %s: %d, %d, %d%n", selectedShinobi.getNameWithNumber(), xShift.getValue(), yShift.getValue(),
                height.getValue());
        }
    }

    private boolean shiftUpdated() {
        return !Objects.equals(originalXShift, selectedShinobi.getXShift()) || !Objects.equals(originalYShift, selectedShinobi.getYShift()) ||
            !Objects.equals(originalHeight, selectedShinobi.getHeight());
    }

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(TestProgressBar::new);
    }
}
