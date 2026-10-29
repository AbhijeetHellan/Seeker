package ai.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Button that paints its own dark surface so Windows look-and-feel cannot turn it white. */
final class DarkButton extends JButton {
    enum Style { NORMAL, PRIMARY, DANGER, STEALTH }

    private final Style style;

    DarkButton(String text, Style style) {
        super(text);
        this.style = style;
        setForeground(new Color(232, 236, 244));
        setFont(getFont().deriveFont(Font.BOLD, 12f));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorder(new EmptyBorder(9, 12, 9, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill;
        Color border;
        if (!isEnabled()) {
            fill = new Color(30, 36, 47);
            border = new Color(48, 57, 71);
        } else {
            fill = switch (style) {
                case PRIMARY -> new Color(40, 101, 183);
                case DANGER -> new Color(142, 54, 63);
                case STEALTH -> new Color(55, 92, 79);
                case NORMAL -> new Color(37, 45, 59);
            };
            border = switch (style) {
                case PRIMARY -> new Color(77, 143, 232);
                case DANGER -> new Color(201, 81, 92);
                case STEALTH -> new Color(81, 145, 119);
                case NORMAL -> new Color(71, 83, 103);
            };
            if (getModel().isPressed()) fill = fill.darker();
            else if (getModel().isRollover()) fill = fill.brighter();
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        g2.setColor(border);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        g2.dispose();

        Color old = getForeground();
        if (!isEnabled()) setForeground(new Color(103, 112, 128));
        super.paintComponent(g);
        setForeground(old);
    }
}
