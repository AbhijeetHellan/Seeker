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

/** Draws both algorithms on the same chart using the nodes they visited per AI turn. */
public final class EfficiencyChartPanel extends JPanel {
    private static final Color MINIMAX_COLOR = new Color(248, 168, 95);
    private static final Color ALPHA_BETA_COLOR = new Color(91, 211, 145);

    private List<ComparisonSample> samples = List.of();

    public EfficiencyChartPanel() {
        setPreferredSize(new Dimension(560, 285));
        setBackground(new Color(22, 28, 39));
    }

    public void setSamples(List<ComparisonSample> samples) {
        this.samples = new ArrayList<>(samples);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 18;
        int y = 12;
        int width = getWidth() - 36;
        int height = getHeight() - 24;

        g.setColor(new Color(29, 36, 49));
        g.fillRoundRect(x, y, width, height, 18, 18);
        g.setColor(new Color(65, 74, 91));
        g.drawRoundRect(x, y, width, height, 18, 18);

        g.setFont(getFont().deriveFont(Font.BOLD, 12f));
        g.setColor(new Color(221, 226, 235));
        g.drawString("LIVE SEARCH COMPARISON", x + 14, y + 21);

        g.setFont(getFont().deriveFont(11f));
        g.setColor(new Color(159, 169, 187));
        g.drawString("Nodes visited per AI turn — lower is better", x + 14, y + 39);
        drawLegend(g, x + 15, y + 54, MINIMAX_COLOR, "Minimax");
        drawLegend(g, x + 113, y + 54, ALPHA_BETA_COLOR, "Alpha-Beta");

        if (samples.isEmpty()) {
            g.setColor(new Color(159, 169, 187));
            g.drawString("Play a turn to see both search algorithms on this chart.", x + 14, y + 87);
            g.dispose();
            return;
        }

        int left = x + 61;
        int right = x + width - 20;
        int top = y + 78;
        int bottom = y + height - 44;
        int plotWidth = Math.max(1, right - left);
        int plotHeight = Math.max(1, bottom - top);
        long maxNodes = 1;
        for (ComparisonSample sample : samples) {
            maxNodes = Math.max(maxNodes, sample.minimax().nodesVisited());
            maxNodes = Math.max(maxNodes, sample.alphaBeta().nodesVisited());
        }

        g.setFont(getFont().deriveFont(10f));
        for (int tick = 0; tick <= 4; tick++) {
            int yy = bottom - (int) (plotHeight * tick / 4.0);
            g.setColor(new Color(79, 88, 105, 90));
            g.drawLine(left, yy, right, yy);
            g.setColor(new Color(152, 162, 179));
            g.drawString(formatCount(Math.round(maxNodes * tick / 4.0)), x + 10, yy + 4);
        }

        drawSeries(g, left, bottom, plotWidth, plotHeight, maxNodes, MINIMAX_COLOR, true);
        drawSeries(g, left, bottom, plotWidth, plotHeight, maxNodes, ALPHA_BETA_COLOR, false);

        g.setColor(new Color(164, 175, 191));
        g.drawString("AI turn 1", left, bottom + 15);
        if (samples.size() > 1) {
            String lastTurn = "Turn " + samples.get(samples.size() - 1).aiTurn();
            g.drawString(lastTurn, right - g.getFontMetrics().stringWidth(lastTurn), bottom + 15);
        }
        ComparisonSample latest = samples.get(samples.size() - 1);
        String summary = String.format(Locale.US,
                "Latest: Minimax %,d  •  Alpha-Beta %,d  •  %.1f%% fewer nodes",
                latest.minimax().nodesVisited(), latest.alphaBeta().nodesVisited(),
                latest.alphaBetaEfficiencyGainPercent());
        g.setColor(new Color(205, 214, 224));
        g.drawString(summary, x + 14, y + height - 13);
        g.dispose();
    }

    private void drawSeries(Graphics2D g, int left, int bottom, int width, int height,
                            long maxNodes, Color color, boolean minimax) {
        g.setColor(color);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int prevX = -1;
        int prevY = -1;
        for (int i = 0; i < samples.size(); i++) {
            long count = minimax ? samples.get(i).minimax().nodesVisited()
                    : samples.get(i).alphaBeta().nodesVisited();
            int xx = left + (samples.size() == 1 ? width / 2
                    : (int) Math.round(width * i / (double) (samples.size() - 1)));
            int yy = bottom - (int) Math.round(height * count / (double) maxNodes);
            if (prevX >= 0) {
                g.drawLine(prevX, prevY, xx, yy);
            }
            g.fillOval(xx - 3, yy - 3, 6, 6);
            prevX = xx;
            prevY = yy;
        }
    }

    private static void drawLegend(Graphics2D g, int x, int y, Color color, String text) {
        g.setColor(color);
        g.fillRoundRect(x, y - 7, 16, 5, 4, 4);
        g.setColor(new Color(205, 212, 224));
        g.drawString(text, x + 21, y);
    }

    private static String formatCount(long count) {
        if (count >= 1_000_000) {
            return String.format(Locale.US, "%.1fM", count / 1_000_000.0);
        }
        if (count >= 1_000) {
            return String.format(Locale.US, "%.1fk", count / 1_000.0);
        }
        return Long.toString(count);
    }
}
