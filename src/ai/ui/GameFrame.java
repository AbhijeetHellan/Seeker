package ai.ui;

import ai.api.AiTurnResult;
import ai.api.DefaultGameApi;
import ai.api.GameApi;
import ai.engine.*;
import ai.experiment.ComparisonSample;
import ai.experiment.CsvRecorder;
import ai.models.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class GameFrame extends JFrame {
    private static final Color BG = new Color(14, 19, 29);
    private static final Color PANEL = new Color(23, 29, 41);
    private static final Color PANEL_2 = new Color(29, 36, 49);
    private static final Color TEXT = new Color(228, 232, 240);
    private static final Color MUTED = new Color(157, 166, 183);

    private final GameApi game;
    private GameState state;
    private final BoardPanel boardPanel;
    private final UnitStatusPanel humanStatus = new UnitStatusPanel(Side.HUMAN);
    private final UnitStatusPanel aiStatus = new UnitStatusPanel(Side.AI);
    private final EfficiencyChartPanel chartPanel = new EfficiencyChartPanel();
    private final List<ComparisonSample> samples = new ArrayList<>();

    private final JLabel turnLabel = label("", 18, Font.BOLD, TEXT);
    private final JLabel arenaLabel = label("", 11, Font.PLAIN, MUTED);
    private final JLabel aStarLabel = label("A*: waiting for a path", 12, Font.PLAIN, MUTED);
    private final JLabel minimaxStats = label("Minimax: —", 12, Font.PLAIN, TEXT);
    private final JLabel alphaStats = label("Alpha-Beta: —", 12, Font.PLAIN, TEXT);
    private final JLabel sameMoveLabel = label("Live comparison starts on the AI turn", 11, Font.BOLD, MUTED);
    private final JLabel depthLabel = label("Depth 5", 12, Font.BOLD, TEXT);
    private final JLabel tacticalHint = label(
            "Human damage 5–15 • AI damage 8–20 • every 6 human attacks unlocks a 15-damage Power Strike",
            11,
            Font.PLAIN,
            new Color(109, 205, 158)
    );
    private final JTextArea logArea = new JTextArea();

    // Manual movement controls.
    private final DarkButton upButton = button("W  ↑", DarkButton.Style.NORMAL);
    private final DarkButton leftButton = button("A  ←", DarkButton.Style.NORMAL);
    private final DarkButton downButton = button("S  ↓", DarkButton.Style.NORMAL);
    private final DarkButton rightButton = button("D  →", DarkButton.Style.NORMAL);

    // Tactical actions.
    private final DarkButton attackButton = button("F  Attack", DarkButton.Style.DANGER);
    private final DarkButton powerStrikeButton = button("G  Power Strike (0/6)", DarkButton.Style.PRIMARY);
    private final DarkButton defendButton = button("E  Defend", DarkButton.Style.NORMAL);
    private final DarkButton hideButton = button("H  Hide", DarkButton.Style.STEALTH);
    private final DarkButton approachButton = button("R  A* Move", DarkButton.Style.PRIMARY);
    private final DarkButton retreatButton = button("X  Retreat", DarkButton.Style.NORMAL);
    private final DarkButton holdButton = button("Space  Hold", DarkButton.Style.NORMAL);

    private final DarkButton newArenaButton = button("New Random Arena", DarkButton.Style.PRIMARY);
    private final DarkButton resetButton = button("Reset Match", DarkButton.Style.NORMAL);
    private final DarkButton depthMinusButton = button("−", DarkButton.Style.NORMAL);
    private final DarkButton depthPlusButton = button("+", DarkButton.Style.NORMAL);

    private int depth = 5;
    private int aiTurnCounter = 0;
    private boolean aiThinking = false;

    public GameFrame() {
        super("Seeker — A* / Minimax / Alpha-Beta");
        game = new DefaultGameApi(newSeed());
        state = game.state();
        boardPanel = new BoardPanel(state);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1240, 820));
        setSize(1500, 940);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMain(), BorderLayout.CENTER);
        wireActions();
        installKeyBindings();
        CsvRecorder.reset();
        refreshUi();
        appendLog("New arena generated automatically. Use W/A/S/D to move one tile per turn.");
        appendLog("Human attacks roll 5–15 damage; AI attacks roll 8–20 damage.");
        appendLog("After 6 human attacks, press G for a guaranteed 15-damage Power Strike.");
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(18, 24, 35));
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(label("SEEKER", 24, Font.BOLD, new Color(244, 247, 252)));
        titles.add(label("A* Manhattan  •  Minimax vs Alpha-Beta  •  WASD tactical movement  •  randomized combat",
                12, Font.PLAIN, MUTED));

        JPanel tools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        tools.setOpaque(false);
        tools.add(resetButton);
        tools.add(newArenaButton);
        header.add(titles, BorderLayout.WEST);
        header.add(tools, BorderLayout.EAST);
        return header;
    }

    private JSplitPane buildMain() {
        JPanel left = new JPanel(new BorderLayout(10, 10));
        left.setBackground(BG);
        left.setBorder(new EmptyBorder(14, 14, 14, 7));
        left.add(boardPanel, BorderLayout.CENTER);
        left.add(buildActionBar(), BorderLayout.SOUTH);

        JScrollPane rightScroll = new JScrollPane(buildRightPanel());
        styleScrollPane(rightScroll);
        rightScroll.getVerticalScrollBar().setUnitIncrement(18);
        rightScroll.getViewport().setBackground(BG);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, rightScroll);
        split.setResizeWeight(0.56);
        split.setDividerLocation(0.56);
        split.setDividerSize(5);
        split.setBorder(null);
        split.setBackground(BG);
        return split;
    }

    private JPanel buildActionBar() {
        JPanel outer = cardPanel(new BorderLayout(12, 10));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JPanel labels = new JPanel(new GridLayout(2, 1));
        labels.setOpaque(false);
        labels.add(label("HUMAN CONTROLS", 11, Font.BOLD, MUTED));
        labels.add(tacticalHint);
        top.add(labels, BorderLayout.WEST);
        top.add(aStarLabel, BorderLayout.EAST);

        JPanel movement = new JPanel(new GridLayout(3, 3, 5, 5));
        movement.setOpaque(false);
        movement.setPreferredSize(new Dimension(215, 128));
        movement.add(blankCell());
        movement.add(upButton);
        movement.add(blankCell());
        movement.add(leftButton);
        JLabel wasd = label("WASD", 12, Font.BOLD, new Color(122, 132, 149));
        wasd.setHorizontalAlignment(SwingConstants.CENTER);
        movement.add(wasd);
        movement.add(rightButton);
        movement.add(blankCell());
        movement.add(downButton);
        movement.add(blankCell());

        JPanel tactics = new JPanel(new GridLayout(2, 4, 7, 7));
        tactics.setOpaque(false);
        tactics.add(attackButton);
        tactics.add(powerStrikeButton);
        tactics.add(defendButton);
        tactics.add(hideButton);
        tactics.add(approachButton);
        tactics.add(retreatButton);
        tactics.add(holdButton);
        JLabel turnHelp = label("6 attacks unlock Power Strike", 11, Font.PLAIN, MUTED);
        turnHelp.setHorizontalAlignment(SwingConstants.CENTER);
        tactics.add(turnHelp);

        JPanel center = new JPanel(new BorderLayout(12, 0));
        center.setOpaque(false);
        center.add(movement, BorderLayout.WEST);
        center.add(tactics, BorderLayout.CENTER);

        outer.add(top, BorderLayout.NORTH);
        outer.add(center, BorderLayout.CENTER);
        return outer;
    }

    private JPanel buildRightPanel() {
        JPanel right = new JPanel();
        right.setBackground(BG);
        right.setBorder(new EmptyBorder(14, 8, 20, 14));
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JPanel match = cardPanel(new BorderLayout(8, 8));
        JPanel matchLabels = new JPanel(new GridLayout(2, 1));
        matchLabels.setOpaque(false);
        matchLabels.add(turnLabel);
        matchLabels.add(arenaLabel);
        match.add(matchLabels, BorderLayout.CENTER);
        match.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        right.add(match);
        right.add(Box.createVerticalStrut(10));

        humanStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        aiStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        right.add(humanStatus);
        right.add(Box.createVerticalStrut(8));
        right.add(aiStatus);
        right.add(Box.createVerticalStrut(10));

        JPanel settings = cardPanel(new BorderLayout(10, 8));
        settings.add(label("SEARCH SETTINGS", 11, Font.BOLD, MUTED), BorderLayout.WEST);

        JPanel searchControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchControls.setOpaque(false);
        searchControls.add(label("A*: Manhattan", 11, Font.BOLD, new Color(109, 205, 158)));

        depthMinusButton.setPreferredSize(new Dimension(38, 30));
        depthPlusButton.setPreferredSize(new Dimension(38, 30));
        depthLabel.setPreferredSize(new Dimension(66, 30));
        depthLabel.setHorizontalAlignment(SwingConstants.CENTER);
        searchControls.add(depthMinusButton);
        searchControls.add(depthLabel);
        searchControls.add(depthPlusButton);
        settings.add(searchControls, BorderLayout.EAST);

        JLabel settingsInfo = label("Alpha-Beta controls the AI; Minimax evaluates the same state for a fair live comparison.",
                11, Font.PLAIN, MUTED);
        settings.add(settingsInfo, BorderLayout.SOUTH);
        settings.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));
        right.add(settings);
        right.add(Box.createVerticalStrut(10));

        JPanel comparison = cardPanel(new BorderLayout(8, 8));
        comparison.add(label("LIVE ALGORITHM COMPARISON", 11, Font.BOLD, MUTED), BorderLayout.NORTH);
        JPanel metrics = new JPanel(new GridLayout(3, 1, 0, 4));
        metrics.setOpaque(false);
        metrics.add(minimaxStats);
        metrics.add(alphaStats);
        metrics.add(sameMoveLabel);
        comparison.add(metrics, BorderLayout.CENTER);
        comparison.setMaximumSize(new Dimension(Integer.MAX_VALUE, 116));
        right.add(comparison);
        right.add(Box.createVerticalStrut(10));

        chartPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 275));
        right.add(chartPanel);
        right.add(Box.createVerticalStrut(10));

        JPanel stealthCard = cardPanel(new BorderLayout(8, 6));
        stealthCard.add(label("COVER / DEFENSE", 11, Font.BOLD, new Color(109, 205, 158)), BorderLayout.NORTH);
        JTextArea stealthText = new JTextArea(
                "1. Move beside a rock using W/A/S/D.\n" +
                "2. Press H to hide in cover and reduce incoming damage.\n" +
                "3. Hold position to stay hidden.\n" +
                "4. Moving, attacking, retreating, or defending reveals you.\n" +
                "Use E to defend when you expect a direct attack.\n" +
                "Normal human attacks charge Power Strike; the special move is ready after 6 attacks."
        );
        styleReadOnlyText(stealthText);
        stealthCard.add(stealthText, BorderLayout.CENTER);
        stealthCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        right.add(stealthCard);
        right.add(Box.createVerticalStrut(10));

        JPanel logCard = cardPanel(new BorderLayout(8, 8));
        logCard.add(label("EVENT LOG", 11, Font.BOLD, MUTED), BorderLayout.NORTH);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setRows(8);
        logArea.setBackground(new Color(18, 23, 33));
        logArea.setForeground(new Color(201, 209, 222));
        logArea.setCaretColor(new Color(226, 232, 240));
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        JScrollPane logScroll = new JScrollPane(logArea);
        styleScrollPane(logScroll);
        logScroll.setBorder(BorderFactory.createLineBorder(new Color(58, 67, 83)));
        logCard.add(logScroll, BorderLayout.CENTER);
        logCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        right.add(logCard);
        return right;
    }

    private void wireActions() {
        upButton.addActionListener(e -> performHumanMove(-1, 0));
        downButton.addActionListener(e -> performHumanMove(1, 0));
        leftButton.addActionListener(e -> performHumanMove(0, -1));
        rightButton.addActionListener(e -> performHumanMove(0, 1));

        attackButton.addActionListener(e -> performHumanAction(ActionType.ATTACK));
        powerStrikeButton.addActionListener(e -> performHumanAction(ActionType.POWER_STRIKE));
        defendButton.addActionListener(e -> performHumanAction(ActionType.DEFEND));
        hideButton.addActionListener(e -> performHumanAction(ActionType.HIDE));
        approachButton.addActionListener(e -> performHumanAction(ActionType.APPROACH));
        retreatButton.addActionListener(e -> performHumanAction(ActionType.RETREAT));
        holdButton.addActionListener(e -> performHumanAction(ActionType.HOLD));

        depthMinusButton.addActionListener(e -> setDepth(depth - 1));
        depthPlusButton.addActionListener(e -> setDepth(depth + 1));
        resetButton.addActionListener(e -> resetSameArena());
        newArenaButton.addActionListener(e -> newArena());
    }

    private void installKeyBindings() {
        JRootPane root = getRootPane();
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_W, 0), "moveUp", () -> performHumanMove(-1, 0));
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_A, 0), "moveLeft", () -> performHumanMove(0, -1));
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "moveDown", () -> performHumanMove(1, 0));
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_D, 0), "moveRight", () -> performHumanMove(0, 1));
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_F, 0), "attack", () -> performHumanAction(ActionType.ATTACK));
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_G, 0), "powerStrike",
                () -> performHumanAction(ActionType.POWER_STRIKE));
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_E, 0), "defend", () -> performHumanAction(ActionType.DEFEND));
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_H, 0), "hide", () -> performHumanAction(ActionType.HIDE));
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_R, 0), "approach", () -> performHumanAction(ActionType.APPROACH));
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_X, 0), "retreat", () -> performHumanAction(ActionType.RETREAT));
        bind(root, KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "hold", () -> performHumanAction(ActionType.HOLD));
    }

    private void bind(JRootPane root, KeyStroke stroke, String name, Runnable action) {
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, name);
        root.getActionMap().put(name, new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    private void performHumanMove(int dRow, int dCol) {
        if (!canHumanAct()) return;
        ActionOutcome outcome = game.moveHuman(dRow, dCol);
        if (outcome.state() == state) {
            Toolkit.getDefaultToolkit().beep();
            appendLog(outcome.description());
            return;
        }
        state = game.state();
        boardPanel.setPath(List.of());
        aStarLabel.setText("Manual movement: W/A/S/D");
        appendLog(outcome.description());
        refreshUi();
        if (state.isTerminal()) finishGame();
        else runAiTurn();
    }

    private void performHumanAction(ActionType type) {
        if (!canHumanAct()) return;
        if (!game.isLegal(type)) {
            Toolkit.getDefaultToolkit().beep();
            appendLog(type + " is not legal right now.");
            return;
        }

        ActionOutcome outcome = game.performHumanAction(type);
        state = game.state();
        showPath(outcome.pathResult());
        appendLog(outcome.description());
        refreshUi();

        if (state.isTerminal()) finishGame();
        else runAiTurn();
    }

    private boolean canHumanAct() {
        if (aiThinking || state.turn() != Side.HUMAN) return false;
        if (state.isTerminal()) {
            JOptionPane.showMessageDialog(this, state.winnerText(), "Match finished", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }

    private void runAiTurn() {
        aiThinking = true;
        setHumanControlsEnabled(false);
        setDepthControlsEnabled(false);
        int searchDepth = depth;
        int gameTurnBeforeAi = state.turnNumber();
        appendLog("AI is evaluating the same state with Minimax and Alpha-Beta at depth " + searchDepth + "...");

        SwingWorker<AiTurnResult, Void> worker = new SwingWorker<>() {
            @Override
            protected AiTurnResult doInBackground() {
                return game.runAiTurn(searchDepth);
            }

            @Override
            protected void done() {
                try {
                    AiTurnResult comparison = get();
                    state = game.state();
                    aiTurnCounter++;
                    ComparisonSample sample = new ComparisonSample(
                            aiTurnCounter,
                            gameTurnBeforeAi,
                            comparison.minimax(),
                            comparison.alphaBeta()
                    );
                    samples.add(sample);
                    CsvRecorder.append(sample);
                    chartPanel.setSamples(samples);
                    showComparison(comparison);

                    DecisionResult chosen = comparison.alphaBeta();
                    ActionOutcome outcome = comparison.appliedOutcome();
                    if (chosen.action() == null || outcome == null) {
                        appendLog("AI had no legal action.");
                    } else {
                        showPath(outcome.pathResult());
                        appendLog("Alpha-Beta selected " + chosen.action().label() + ". " + outcome.description());
                    }
                    refreshUi();
                    if (state.isTerminal()) finishGame();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(GameFrame.this,
                            "AI search failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    aiThinking = false;
                    setDepthControlsEnabled(!state.isTerminal());
                    refreshUi();
                }
            }
        };
        worker.execute();
    }

    private void showComparison(AiTurnResult c) {
        DecisionResult mm = c.minimax();
        DecisionResult ab = c.alphaBeta();
        minimaxStats.setText(String.format(java.util.Locale.US,
                "Minimax       • action %-8s • nodes %,d • %.3f ms",
                actionName(mm), mm.nodesVisited(), mm.runtimeMillis()));
        alphaStats.setText(String.format(java.util.Locale.US,
                "Alpha-Beta    • action %-8s • nodes %,d • %.3f ms • cutoffs %,d",
                actionName(ab), ab.nodesVisited(), ab.runtimeMillis(), ab.cutoffs()));
        boolean same = mm.action() != null && ab.action() != null && mm.action().type() == ab.action().type();
        sameMoveLabel.setForeground(same ? new Color(91, 211, 145) : new Color(255, 187, 92));
        sameMoveLabel.setText(same
                ? "✓ Same decision — Alpha-Beta preserves the move with fewer/equal searched branches"
                : "⚠ Different tie/decision observed — inspect evaluation and search depth");
    }

    private String actionName(DecisionResult r) {
        return r.action() == null ? "NONE" : r.action().label();
    }

    private void showPath(PathResult path) {
        if (path == null) {
            boardPanel.setPath(List.of());
            return;
        }
        boardPanel.setPath(path.path());
        if (path.found()) {
            aStarLabel.setText(String.format(java.util.Locale.US,
                    "A* Manhattan: path %.0f • expanded %,d • %.3f ms",
                    path.pathCost(), path.nodesExpanded(), path.runtimeMillis()));
        } else {
            aStarLabel.setText("A*: no path found");
        }
    }

    private void refreshUi() {
        boardPanel.setState(state);
        humanStatus.setUnit(state.human(), state.turn() == Side.HUMAN && !aiThinking);
        aiStatus.setUnit(state.ai(), state.turn() == Side.AI || aiThinking);
        String turnText = state.turn() + " TURN  •  Turn " + state.turnNumber()
                + "/" + GameState.MAX_TURNS + "  •  Round " + ((state.turnNumber() + 1) / 2);
        turnLabel.setText(state.isTerminal() ? state.winnerText() : turnText);
        arenaLabel.setText("Arena seed " + state.arenaSeed() + "  •  20% auto obstacles  •  A* Manhattan");

        boolean humanTurn = !aiThinking && !state.isTerminal() && state.turn() == Side.HUMAN;
        setHumanControlsEnabled(humanTurn);
        attackButton.setEnabled(humanTurn && game.isLegal(ActionType.ATTACK));
        powerStrikeButton.setEnabled(humanTurn && game.isLegal(ActionType.POWER_STRIKE));
        powerStrikeButton.setText(state.powerStrikeReady()
                ? "G  Power Strike READY (15)"
                : "G  Power Strike (" + state.humanAttackCharge() + "/6)");
        hideButton.setEnabled(humanTurn && game.isLegal(ActionType.HIDE));
        approachButton.setEnabled(humanTurn && game.isLegal(ActionType.APPROACH));
        retreatButton.setEnabled(humanTurn && game.isLegal(ActionType.RETREAT));
        defendButton.setEnabled(humanTurn && game.isLegal(ActionType.DEFEND));
        holdButton.setEnabled(humanTurn && game.isLegal(ActionType.HOLD));
        setDepthControlsEnabled(!aiThinking && !state.isTerminal());
    }

    private void setDepth(int value) {
        depth = Math.max(1, Math.min(6, value));
        depthLabel.setText("Depth " + depth);
        depthMinusButton.setEnabled(depth > 1 && !aiThinking);
        depthPlusButton.setEnabled(depth < 6 && !aiThinking);
    }

    private void setDepthControlsEnabled(boolean enabled) {
        depthMinusButton.setEnabled(enabled && depth > 1);
        depthPlusButton.setEnabled(enabled && depth < 6);
    }

    private void resetSameArena() {
        if (aiThinking) return;
        game.resetArena();
        state = game.state();
        resetUiForArena();
        appendLog("Match reset on the same generated arena.");
    }

    private void newArena() {
        if (aiThinking) return;
        game.newArena(newSeed());
        state = game.state();
        resetUiForArena();
        appendLog("Generated a new random arena. A reachable path between both units is guaranteed.");
    }

    private void resetUiForArena() {
        samples.clear();
        aiTurnCounter = 0;
        CsvRecorder.reset();
        chartPanel.setSamples(samples);
        boardPanel.setPath(List.of());
        minimaxStats.setText("Minimax: —");
        alphaStats.setText("Alpha-Beta: —");
        sameMoveLabel.setForeground(MUTED);
        sameMoveLabel.setText("Live comparison starts on the AI turn");
        aStarLabel.setText("A* Manhattan: waiting for a path");
        logArea.setText("");
        refreshUi();
    }

    private void finishGame() {
        setHumanControlsEnabled(false);
        appendLog("MATCH COMPLETE: " + state.winnerText());
        JOptionPane.showMessageDialog(this, state.winnerText(), "Match Complete", JOptionPane.INFORMATION_MESSAGE);
    }

    private void appendLog(String message) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        logArea.append("[" + time + "] " + message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void setHumanControlsEnabled(boolean enabled) {
        upButton.setEnabled(enabled);
        downButton.setEnabled(enabled);
        leftButton.setEnabled(enabled);
        rightButton.setEnabled(enabled);
        attackButton.setEnabled(enabled);
        powerStrikeButton.setEnabled(enabled);
        approachButton.setEnabled(enabled);
        retreatButton.setEnabled(enabled);
        defendButton.setEnabled(enabled);
        hideButton.setEnabled(enabled);
        holdButton.setEnabled(enabled);
    }

    private static long newSeed() {
        return ThreadLocalRandom.current().nextLong(100_000, 9_999_999);
    }

    private JPanel cardPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(PANEL);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(57, 66, 81)),
                new EmptyBorder(11, 12, 11, 12)));
        return p;
    }

    private static JLabel label(String text, int size, int style, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(label.getFont().deriveFont(style, (float) size));
        return label;
    }

    private static DarkButton button(String text, DarkButton.Style style) {
        return new DarkButton(text, style);
    }

    private static JPanel blankCell() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        return p;
    }

    private static void styleReadOnlyText(JTextArea area) {
        area.setEditable(false);
        area.setFocusable(false);
        area.setOpaque(false);
        area.setForeground(new Color(190, 198, 212));
        area.setFont(area.getFont().deriveFont(11.5f));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(null);
    }

    private static void styleScrollPane(JScrollPane pane) {
        pane.setBorder(null);
        pane.setBackground(BG);
        pane.getViewport().setBackground(BG);
        pane.getVerticalScrollBar().setBackground(new Color(17, 22, 32));
        pane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(64, 74, 91);
                trackColor = new Color(17, 22, 32);
            }
            @Override protected JButton createDecreaseButton(int orientation) { return zeroButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return zeroButton(); }
            private JButton zeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                b.setMinimumSize(new Dimension(0, 0));
                b.setMaximumSize(new Dimension(0, 0));
                return b;
            }
        });
    }

}
