package ai.ui;

import ai.models.Side;
import ai.models.UnitState;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

final class UnitStatusPanel extends JPanel {
    private final JLabel nameLabel = new JLabel();
    private final JLabel statusLabel = new JLabel();
    private final JProgressBar healthBar = new JProgressBar(0, 100);
    private final Side side;

    UnitStatusPanel(Side side) {
        this.side = side;
        setOpaque(true);
        setBackground(new Color(29, 35, 48));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(67, 77, 96)),
                new EmptyBorder(10, 12, 10, 12)));
        setLayout(new BorderLayout(8, 6));

        nameLabel.setForeground(new Color(235, 239, 246));
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 15f));
        statusLabel.setForeground(new Color(190, 198, 212));
        statusLabel.setFont(statusLabel.getFont().deriveFont(12f));

        healthBar.setStringPainted(true);
        healthBar.setBorderPainted(false);
        healthBar.setBackground(new Color(43, 50, 64));
        healthBar.setForeground(side == Side.HUMAN ? new Color(65, 150, 255) : new Color(236, 83, 83));
        healthBar.setUI(new BasicProgressBarUI() {
            @Override protected Color getSelectionBackground() { return new Color(242, 245, 250); }
            @Override protected Color getSelectionForeground() { return new Color(242, 245, 250); }
        });

        JPanel labels = new JPanel(new GridLayout(2, 1, 0, 2));
        labels.setOpaque(false);
        labels.add(nameLabel);
        labels.add(statusLabel);
        add(labels, BorderLayout.CENTER);
        add(healthBar, BorderLayout.SOUTH);
    }

    void setUnit(UnitState unit, boolean activeTurn) {
        nameLabel.setText((side == Side.HUMAN ? "HUMAN  •  " : "AI  •  ") + unit.name());
        String state;
        if (unit.hidden()) state = "HIDDEN IN COVER";
        else if (unit.defending()) state = "Defending";
        else state = "Ready";
        if (activeTurn) state += "  •  ACTIVE TURN";
        statusLabel.setText(state + "  •  Tile " + unit.position().row() + "," + unit.position().col());
        statusLabel.setForeground(unit.hidden() ? new Color(106, 214, 164) : new Color(190, 198, 212));
        healthBar.setValue(unit.healthPercent());
        healthBar.setString(unit.health() + " / " + unit.maxHealth() + " HP");
    }
}
