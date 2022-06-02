package com.lawmillenium.intellij.plugins.narutoprogress;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import com.intellij.openapi.ui.GraphicsConfig;
import com.intellij.ui.JBColor;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.ui.GraphicsUtil;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;
import com.lawmillenium.intellij.plugins.narutoprogress.configuration.NarutoProgressState;
import com.lawmillenium.intellij.plugins.narutoprogress.model.Shinobi;
import com.lawmillenium.intellij.plugins.narutoprogress.theme.ColorScheme;
import com.lawmillenium.intellij.plugins.narutoprogress.theme.ColorSchemes;
import com.lawmillenium.intellij.plugins.narutoprogress.theme.PaintTheme;
import com.lawmillenium.intellij.plugins.narutoprogress.theme.PaintThemes;
import org.jetbrains.annotations.NotNull;

public class NarutoProgressBarUi extends BasicProgressBarUI {
    private static final String DEBUGGING_ENV_VAR = "NARUTO_PROGRESS_DEBUG";
    private final Shinobi shinobi;
    private final ImageIcon iconForward;
    private final ImageIcon iconReversed;
    private final Supplier<Float> initialVelocity;
    private final Supplier<Float> acceleration;
    private final Supplier<PaintTheme> theme;
    private final Supplier<ColorScheme> colorScheme;
    private final BooleanSupplier transparencyOnIndeterminate;
    private final BooleanSupplier transparencyOnDeterminate;
    private final BooleanSupplier drawSprites;
    private final BooleanSupplier addToolTips;
    private final BooleanSupplier restrictMaxHeight;
    private final IntSupplier maxHeight;
    private final BooleanSupplier restrictMinHeight;
    private final IntSupplier minHeight;
    private ImageIcon iconForwardScaled = null;
    private ImageIcon iconReversedScaled = null;
    private volatile int pos = 0;
    private volatile float velocity = 0;

    public NarutoProgressBarUi(final Shinobi shinobi) {
        this(shinobi, () -> NarutoProgressState.getInstance().initialVelocity, () -> NarutoProgressState.getInstance().acceleration,
            () -> PaintThemes.getByIdOrDefault(NarutoProgressState.getInstance().theme),
            () -> ColorSchemes.getByIdOrDefault(NarutoProgressState.getInstance().colorScheme),
            () -> NarutoProgressState.getInstance().transparencyOnIndeterminate, () -> NarutoProgressState.getInstance().transparencyOnDeterminate,
            () -> NarutoProgressState.getInstance().drawSprites, () -> NarutoProgressState.getInstance().addToolTips,
            () -> NarutoProgressState.getInstance().restrictMaximumHeight, () -> NarutoProgressState.getInstance().maximumHeight,
            () -> NarutoProgressState.getInstance().restrictMinimumHeight, () -> NarutoProgressState.getInstance().minimumHeight);
    }

    public NarutoProgressBarUi(final Shinobi shinobi, final Supplier<Float> initialVelocity, final Supplier<Float> acceleration,
        final Supplier<PaintTheme> theme, final Supplier<ColorScheme> colorScheme, final BooleanSupplier transparencyOnIndeterminate,
        final BooleanSupplier transparencyOnDeterminate, final BooleanSupplier drawSprites, final BooleanSupplier addToolTips,
        final BooleanSupplier restrictMaxHeight, final IntSupplier maxHeight, final BooleanSupplier restrictMinHeight, final IntSupplier minHeight) {
        super();
        this.shinobi = shinobi;
        this.initialVelocity = initialVelocity;
        this.acceleration = acceleration;
        this.theme = theme;
        this.colorScheme = colorScheme;
        this.transparencyOnIndeterminate = transparencyOnIndeterminate;
        this.transparencyOnDeterminate = transparencyOnDeterminate;
        this.drawSprites = drawSprites;
        this.addToolTips = addToolTips;
        this.restrictMaxHeight = restrictMaxHeight;
        this.maxHeight = maxHeight;
        this.restrictMinHeight = restrictMinHeight;
        this.minHeight = minHeight;
        velocity = initialVelocity.get();

        iconForward = ShinobiResourceLoader.getIcon(shinobi);
        iconReversed = ShinobiResourceLoader.getReversedIcon(shinobi);
        if (restrictMaxHeight.getAsBoolean() || restrictMinHeight.getAsBoolean()) {
            computeScaledIcons();
        }
    }

    @SuppressWarnings({ "MethodOverridesStaticMethodOfSuperclass", "UnusedDeclaration" })
    public static ComponentUI createUI(final JComponent jComponent) {
        jComponent.setBorder(JBUI.Borders.empty().asUIResource());
        return new NarutoProgressBarUi(ShinobiPicker.get());
    }

    private static Paint getTransparencyPaint(final Color backgroundColor, final int width, final boolean movingRight) {
        final JBColor transparent = new JBColor(new Color(0, 0, 0, 0), new Color(0, 0, 0, 0));
        return new LinearGradientPaint(0, JBUIScale.scale(2f), width, JBUIScale.scale(2f), new float[]{ 0, 1 },
            new Color[]{ movingRight ? backgroundColor : transparent, movingRight ? transparent : backgroundColor });
    }

    private static boolean isEven(final int n) {
        return (n & 1) == 0;
    }

    @Override
    protected int getBoxLength(final int availableLength, final int otherDimension) {
        return availableLength;
    }

    @Override
    public Dimension getPreferredSize(final JComponent jComponent) {
        int height = restrictMaxHeight.getAsBoolean() ? Math.min(maxHeight.getAsInt(), shinobi.getHeight()) : shinobi.getHeight();
        height = restrictMinHeight.getAsBoolean() ? Math.max(minHeight.getAsInt(), height) : height;
        return new Dimension(super.getPreferredSize(jComponent).width, JBUI.scale(height));
    }

    @Override
    protected void paintIndeterminate(final Graphics graphics, final JComponent jComponent) {
        paint(graphics, jComponent, false);
        updatePosition();
    }

    @Override
    protected void paintDeterminate(final Graphics graphics, final JComponent jComponent) {
        resetPositionAndVelocity();
        paint(graphics, jComponent, true);
    }

    public void computeScaledIcons() {
        iconForwardScaled = new ImageIcon(
            iconForward.getImage().getScaledInstance(-1, scaleToHeightRestrictions(iconForward.getIconHeight()), Image.SCALE_DEFAULT));
        iconReversedScaled = new ImageIcon(
            iconReversed.getImage().getScaledInstance(-1, scaleToHeightRestrictions(iconReversed.getIconHeight()), Image.SCALE_DEFAULT));
    }

    private void paint(final Graphics graphics, final JComponent jComponent, final boolean determinate) {
        if (isUnsupported(graphics, jComponent)) {
            if (determinate) {
                super.paintDeterminate(graphics, jComponent);
            } else {
                super.paintIndeterminate(graphics, jComponent);
            }
            return;
        }
        setToolTipText();

        final GraphicsConfig config = GraphicsUtil.setupAAPainting(graphics);
        final Graphics2D graphics2D = (Graphics2D) graphics;

        final Insets border = progressBar.getInsets(); // area for border
        final int width = progressBar.getWidth();
        int height = progressBar.getPreferredSize().height;

        if (!isEven(jComponent.getHeight() - height)) {
            height++;
        }

        final int barRectWidth = width - (border.right + border.left);
        final int barRectHeight = height - (border.top + border.bottom);

        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }

        final int amountFull;
        if (System.getenv().containsKey(DEBUGGING_ENV_VAR)) {
            amountFull = barRectWidth / 2;
        } else {
            amountFull = determinate ? getAmountFull(border, barRectWidth, barRectHeight) : pos;
        }

        final Container parent = jComponent.getParent();
        final Color background = parent != null ? parent.getBackground() : UIUtil.getPanelBackground();

        graphics2D.setColor(background);
        if (jComponent.isOpaque()) {
            graphics2D.fillRect(0, 0, width, height);
        }

        final RoundRectangle2D rectangle = getRoundRectangle(width, height);

        drawBackgroundPaint(width, height, amountFull, graphics2D, rectangle);
        drawShinobiIcon(amountFull, graphics2D, rectangle);
        drawBorder(rectangle, graphics2D);
        // Deal with possible text painting
        if (progressBar.isStringPainted()) {
            graphics2D.translate(0, -(jComponent.getHeight() - height) / 2);
            paintString(graphics2D, border.left, border.top, barRectWidth, barRectHeight, amountFull, border);
        }

        config.restore();
    }

    @NotNull
    private RoundRectangle2D getRoundRectangle(final int width, final int height) {
        final float arcLength = JBUIScale.scale(9f);
        final float offset = JBUIScale.scale(2f);

        return new RoundRectangle2D.Float(JBUIScale.scale(1f), JBUIScale.scale(1f), width - offset, height - offset, arcLength, arcLength);
    }

    private void drawBackgroundPaint(final int width, final int height, final int progress, final Graphics2D graphics2D, final RoundRectangle2D rectangle) {
        final Paint paint = graphics2D.getPaint();
        final Shape clip = graphics2D.getClip();
        final boolean movingRight = velocity >= 0;

        graphics2D.setPaint(theme.get().getPaint(shinobi, colorScheme.get(), 0, height));
        graphics2D.setClip(movingRight ? new Rectangle(progress, height) : new Rectangle(progress, 0, progressBar.getWidth(), height));
        graphics2D.fill(rectangle);

        if ((progressBar.isIndeterminate() && transparencyOnIndeterminate.getAsBoolean()) ||
            (!progressBar.isIndeterminate() && transparencyOnDeterminate.getAsBoolean())) {
            graphics2D.setPaint(getTransparencyPaint(progressBar.getBackground(), width, movingRight));
            graphics2D.setClip(movingRight ? new Rectangle(progress, height) : new Rectangle(progress, 0, progressBar.getWidth(), height));
            graphics2D.fill(rectangle);
        }

        graphics2D.setPaint(paint);
        graphics2D.setClip(clip);
    }

    private void setToolTipText() {
        if (addToolTips.getAsBoolean()) {
            progressBar.setToolTipText(shinobi.getNameWithNumber());
        }
    }

    private void drawBorder(final RoundRectangle2D rectangle, final Graphics2D graphics2D) {
        final Color color = graphics2D.getColor();
        final Stroke stroke = graphics2D.getStroke();

        graphics2D.setColor(progressBar.getForeground());
        graphics2D.setStroke(new BasicStroke(2));
        graphics2D.draw(rectangle);

        graphics2D.setColor(color);
        graphics2D.setStroke(stroke);
    }

    private void drawShinobiIcon(final int amountFull, final Graphics2D graphics2D, final Shape clip) {
        if (!drawSprites.getAsBoolean()) {
            return;
        }
        final Shape previousClip = graphics2D.getClip();

        graphics2D.setClip(clip);
        final Icon icon;
        if (restrictMaxHeight.getAsBoolean() || restrictMinHeight.getAsBoolean()) {
            icon = velocity >= 0 ? iconForwardScaled : iconReversedScaled;
        } else {
            icon = velocity >= 0 ? iconForward : iconReversed;
        }
        if (icon != null) {
            icon.paintIcon(progressBar, graphics2D, amountFull + (velocity >= 0 ? JBUI.scale(scaleToHeightRestrictions(shinobi.getXShift())) :
                    JBUI.scale(-icon.getIconWidth() - scaleToHeightRestrictions(shinobi.getXShift()))),
                JBUI.scale(scaleToHeightRestrictions(shinobi.getYShift())));
        }
        graphics2D.setClip(previousClip);
    }

    private boolean isUnsupported(final Graphics graphics, final JComponent jComponent) {
        return !(graphics instanceof Graphics2D) || progressBar.getOrientation() != SwingConstants.HORIZONTAL ||
            !jComponent.getComponentOrientation().isLeftToRight();
    }

    private void updatePosition() {
        final float v = velocity;
        final int p = pos;
        if (velocity < 0) {
            if (pos <= 0) {
                velocity = initialVelocity.get();
                pos = 0;
            } else {
                pos = p + (int) JBUIScale.scale(velocity);
                velocity = v - acceleration.get();
            }
        } else if (velocity > 0) {
            if (pos >= progressBar.getWidth()) {
                velocity = -initialVelocity.get();
                pos = progressBar.getWidth();
            } else {
                pos = p + (int) JBUIScale.scale(velocity);
                velocity = v + acceleration.get();
            }
        }
    }

    private int scaleToHeightRestrictions(final int value) {
        if (restrictMaxHeight.getAsBoolean() && shinobi.getHeight() > maxHeight.getAsInt()) {
            return Math.round(((float) maxHeight.getAsInt() / shinobi.getHeight()) * value);
        }
        if (restrictMinHeight.getAsBoolean() && shinobi.getHeight() < minHeight.getAsInt()) {
            return Math.round(((float) minHeight.getAsInt() / shinobi.getHeight()) * value);
        }
        return value;
    }

    private void resetPositionAndVelocity() {
        velocity = 1;
        pos = 0;
    }
}
