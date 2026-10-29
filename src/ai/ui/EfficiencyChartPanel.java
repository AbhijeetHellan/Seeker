package ai.ui;

import ai.experiment.ComparisonSample;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Live chart showing how many search nodes Alpha-Beta saves compared with Minimax. */
public final class EfficiencyChartPanel extends JPanel {
    private List<ComparisonSample> samples = List.of();

    public EfficiencyChartPanel() {
        setPreferredSize(new Dimension(560, 260));
        setBackground(new Color(22, 28, 39));
    }

    public void setSamples(List<ComparisonSample> samples) {
        this.samples = new ArrayList<>(samples);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 18;
        int y = 12;
        int width = getWidth() - 36;
        int height = getHeight() - 24;

        g2.setColor(new Color(29, 36, 49));
        g2.fillRoundRect(x, y, width, height, 18, 18);
        g2.setColor(new Color(65, 74, 91));
        g2.drawRoundRect(x, y, width, height, 18, 18);

        g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
        g2.setColor(new Color(221, 226, 235));
        g2.drawString("LIVE ALPHA-BETA EFFICIENCY VS MINIMAX", x + 14, y + 22);

        g2.setFont(getFont().deriveFont(11f));
        g2.setColor(new Color(145, 155, 173));
        g2.drawString("Higher % means Alpha-Beta evaluated fewer nodes than Minimax.", x + 14, y + 40);

        if (samples.isEmpty()) {
            g2.drawString("Play a turn to generate live efficiency data.", x + 14, y + 68);
            g2.dispose();
            return;
        }

        int left = x + 48;
        int right = x + width - 18;
        int top = y + 58;
        int bottom = y + height - 38;
        int plotWidth = Math.max(1, right - left);
        int plotHeight = Math.max(1, bottom - top);

        g2.setFont(getFont().deriveFont(10f));
        for (int i = 0; i <= 4; i++) {
            int gridY = bottom - (int) (plotHeight * i / 4.0);
            int percent = i * 25;
            g2.setColor(new Color(77, 86, 103, 80));
            g2.drawLine(left, gridY, right, gridY);
            g2.setColor(new Color(148, 158, 176));
            g2.drawString(percent + "%", x + 10, gridY + 4);
        }

        g2.setColor(new Color(91, 211, 145));
        g2.setStroke(new BasicStroke(2.4f));

        int lastX = -1;
        int lastY = -1;
        for (int i = 0; i < samples.size(); i++) {
            ComparisonSample sample = samples.get(i);
            double value = sample.alphaBetaEfficiencyGainPercent();
            int pointX = left + (samples.size() == 1
                    ? plotWidth / 2
                    : (int) (plotWidth * i / (double) (samples.size() - 1)));
            int pointY = bottom - (int) (plotHeight * value / 100.0);

            if (lastX >= 0) {
                g2.drawLine(lastX, lastY, pointX, pointY);
            }
            g2.fillOval(pointX - 4, pointY - 4, 8, 8);
            lastX = pointX;
            lastY = pointY;
        }

        ComparisonSample latest = samples.get(samples.size() - 1);
        String latestText = String.format(
                Locale.US,
                "Latest: Alpha-Beta saved %.1f%% of Minimax's node work",
                latest.alphaBetaEfficiencyGainPercent()
        );
        g2.setColor(new Color(200, 207, 220));
        g2.drawString(latestText, x + 14, y + height - 14);
        g2.dispose();
    }
}
