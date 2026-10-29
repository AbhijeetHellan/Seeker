package ai.ui;

import ai.models.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Graphical arena view with custom-drawn human, robot, obstacles, cover, and A* path. */
public final class BoardPanel extends JPanel {
    private GameState state;
    private List<Position> path = List.of();

    public BoardPanel(GameState initialState) {
        this.state = initialState;
        setPreferredSize(new Dimension(680, 680));
        setBackground(new Color(13, 18, 28));
        setFocusable(true);
    }

    public void setState(GameState state) {
        this.state = state;
        repaint();
    }

    public void setPath(List<Position> path) {
        this.path = path == null ? List.of() : List.copyOf(path);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (state == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padding = 22;
        int usableW = getWidth() - padding * 2;
        int usableH = getHeight() - padding * 2;
        int cell = Math.min(usableW / state.board().cols(), usableH / state.board().rows());
        int boardW = cell * state.board().cols();
        int boardH = cell * state.board().rows();
        int x0 = (getWidth() - boardW) / 2;
        int y0 = (getHeight() - boardH) / 2;

        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRoundRect(x0 - 10, y0 - 8, boardW + 20, boardH + 20, 28, 28);
        g2.setColor(new Color(28, 36, 49));
        g2.fillRoundRect(x0 - 5, y0 - 5, boardW + 10, boardH + 10, 22, 22);

        Set<Position> pathCells = new HashSet<>(path);
        for (int r = 0; r < state.board().rows(); r++) {
            for (int c = 0; c < state.board().cols(); c++) {
                Position p = new Position(r, c);
                int x = x0 + c * cell;
                int y = y0 + r * cell;

                boolean alternate = (r + c) % 2 == 0;
                g2.setColor(alternate ? new Color(46, 57, 70) : new Color(41, 51, 64));
                g2.fillRect(x, y, cell, cell);
                g2.setColor(new Color(255, 255, 255, 16));
                g2.drawRect(x, y, cell, cell);

                if (pathCells.contains(p) && !p.equals(state.human().position()) && !p.equals(state.ai().position())) {
                    drawPathMarker(g2, x, y, cell);
                }
                if (state.board().isBlocked(p)) {
                    drawRock(g2, x, y, cell, r * 31L + c * 17L + state.arenaSeed());
                }
            }
        }

        drawCoverHalo(g2, state.human(), x0, y0, cell);
        drawCoverHalo(g2, state.ai(), x0, y0, cell);
        drawUnit(g2, state.human(), x0, y0, cell, state.turn() == Side.HUMAN);
        drawUnit(g2, state.ai(), x0, y0, cell, state.turn() == Side.AI);
        g2.dispose();
    }

    private void drawPathMarker(Graphics2D g2, int x, int y, int cell) {
        int d = Math.max(8, cell / 5);
        int cx = x + cell / 2;
        int cy = y + cell / 2;
        g2.setColor(new Color(255, 211, 79, 65));
        g2.fill(new Ellipse2D.Double(cx - d, cy - d, d * 2.0, d * 2.0));
        g2.setColor(new Color(255, 220, 94, 220));
        g2.fillOval(cx - d / 2, cy - d / 2, d, d);
    }

    private void drawRock(Graphics2D g2, int x, int y, int cell, long seed) {
        int pad = Math.max(5, cell / 8);
        int wobble = (int) (Math.abs(seed) % Math.max(2, cell / 9));
        g2.setColor(new Color(24, 29, 38));
        g2.fillRoundRect(x + pad + 2, y + pad + 4, cell - pad * 2, cell - pad * 2, 12, 12);
        g2.setColor(new Color(92, 101, 113));
        g2.fillRoundRect(x + pad, y + pad + wobble / 2, cell - pad * 2, cell - pad * 2 - wobble / 2, 12, 12);
        g2.setColor(new Color(132, 140, 151));
        g2.drawLine(x + pad + 6, y + pad + 7, x + cell - pad - 8, y + cell - pad - 9);
    }

    private void drawCoverHalo(Graphics2D g2, UnitState unit, int x0, int y0, int cell) {
        if (!unit.hidden()) return;
        int x = x0 + unit.position().col() * cell;
        int y = y0 + unit.position().row() * cell;
        g2.setColor(new Color(72, 207, 139, 42));
        g2.fillRoundRect(x + 2, y + 2, cell - 4, cell - 4, 14, 14);
        g2.setStroke(new BasicStroke(Math.max(2f, cell / 20f)));
        g2.setColor(new Color(99, 230, 164, 185));
        g2.drawRoundRect(x + 5, y + 5, cell - 10, cell - 10, 12, 12);
    }

    private void drawUnit(Graphics2D g2, UnitState unit, int x0, int y0, int cell, boolean active) {
        int x = x0 + unit.position().col() * cell;
        int y = y0 + unit.position().row() * cell;
        int margin = Math.max(5, cell / 10);

        if (active) {
            g2.setStroke(new BasicStroke(Math.max(2f, cell / 18f)));
            g2.setColor(unit.side() == Side.HUMAN ? new Color(80, 170, 255, 200) : new Color(255, 92, 92, 200));
            g2.drawOval(x + 3, y + 3, cell - 6, cell - 6);
        }

        Composite oldComposite = g2.getComposite();
        if (unit.hidden()) g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        if (unit.side() == Side.HUMAN) drawHuman(g2, x, y, cell, margin);
        else drawRobot(g2, x, y, cell, margin);
        g2.setComposite(oldComposite);

        if (unit.defending()) {
            g2.setStroke(new BasicStroke(3f));
            g2.setColor(new Color(112, 214, 255, 220));
            g2.drawArc(x + margin / 2, y + margin / 2, cell - margin, cell - margin, 210, 240);
        }
        if (unit.hidden()) {
            g2.setFont(getFont().deriveFont(Font.BOLD, Math.max(8f, cell / 7f)));
            String tag = "HIDDEN";
            int tw = g2.getFontMetrics().stringWidth(tag);
            int th = g2.getFontMetrics().getHeight();
            int tx = x + (cell - tw) / 2;
            int ty = y + Math.max(th, cell / 5);
            g2.setColor(new Color(10, 22, 20, 205));
            g2.fillRoundRect(tx - 5, ty - th + 3, tw + 10, th, 8, 8);
            g2.setColor(new Color(112, 232, 174));
            g2.drawString(tag, tx, ty - 2);
        }
    }

    private void drawHuman(Graphics2D g2, int x, int y, int cell, int m) {
        int cx = x + cell / 2;
        int head = Math.max(10, cell / 4);
        g2.setColor(new Color(20, 30, 48, 130));
        g2.fillOval(cx - cell / 3 + 2, y + m + 3, cell * 2 / 3, cell * 2 / 3);
        g2.setColor(new Color(53, 139, 232));
        g2.fill(new RoundRectangle2D.Double(cx - cell * 0.27, y + cell * 0.42, cell * 0.54, cell * 0.40, 12, 12));
        g2.setColor(new Color(240, 202, 166));
        g2.fillOval(cx - head / 2, y + cell / 5, head, head);
        g2.setColor(new Color(34, 41, 55));
        g2.fillArc(cx - head / 2, y + cell / 5 - 2, head, head * 2 / 3, 0, 180);
        g2.setColor(new Color(241, 245, 251));
        g2.setFont(getFont().deriveFont(Font.BOLD, Math.max(9f, cell / 5f)));
        g2.drawString("H", cx - g2.getFontMetrics().stringWidth("H") / 2, y + cell - m / 2);
    }

    private void drawRobot(Graphics2D g2, int x, int y, int cell, int m) {
        int cx = x + cell / 2;
        int headW = (int) (cell * 0.58);
        int headH = (int) (cell * 0.42);
        int hx = cx - headW / 2;
        int hy = y + cell / 5;

        g2.setColor(new Color(225, 69, 78));
        g2.fill(new RoundRectangle2D.Double(hx, hy, headW, headH, 10, 10));
        g2.setColor(new Color(47, 54, 67));
        g2.fill(new RoundRectangle2D.Double(cx - cell * 0.25, y + cell * 0.58, cell * 0.50, cell * 0.25, 8, 8));
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(213, 220, 229));
        g2.drawLine(cx, hy, cx, hy - Math.max(5, cell / 8));
        g2.fillOval(cx - 3, hy - Math.max(8, cell / 8) - 3, 6, 6);
        g2.setColor(new Color(255, 237, 114));
        int eye = Math.max(4, cell / 10);
        g2.fillOval(cx - headW / 4 - eye / 2, hy + headH / 2 - eye / 2, eye, eye);
        g2.fillOval(cx + headW / 4 - eye / 2, hy + headH / 2 - eye / 2, eye, eye);
        g2.setColor(new Color(241, 245, 251));
        g2.setFont(getFont().deriveFont(Font.BOLD, Math.max(9f, cell / 5f)));
        g2.drawString("AI", cx - g2.getFontMetrics().stringWidth("AI") / 2, y + cell - m / 2);
    }
}
