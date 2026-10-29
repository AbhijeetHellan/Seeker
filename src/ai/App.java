package ai;

import ai.ui.GameFrame;

import javax.swing.*;
import java.awt.*;

public final class App {
    private App() {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Custom-painted controls still keep the application usable and dark.
            }
            UIManager.put("OptionPane.background", new Color(23, 29, 41));
            UIManager.put("Panel.background", new Color(23, 29, 41));
            UIManager.put("OptionPane.messageForeground", new Color(228, 232, 240));
            new GameFrame().setVisible(true);
        });
    }
}
