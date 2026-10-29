package ai.models;

/** Immutable snapshot used by the UI and the search algorithms. */
public record GameState(
        Board board,
        UnitState human,
        UnitState ai,
        Side turn,
        int turnNumber,
        long arenaSeed,
        int humanAttackCharge
) {
    public static final int MAX_TURNS = 200;
    public static final int POWER_STRIKE_REQUIRED_ATTACKS = 6;

    public GameState {
        if (board == null || human == null || ai == null || turn == null) {
            throw new IllegalArgumentException("GameState fields cannot be null.");
        }
        if (human.side() != Side.HUMAN || ai.side() != Side.AI) {
            throw new IllegalArgumentException("Human and AI units must have the correct sides.");
        }
        humanAttackCharge = Math.max(0, Math.min(humanAttackCharge, POWER_STRIKE_REQUIRED_ATTACKS));
    }

    public static GameState newRandomArena(long seed) {
        int rows = 12;
        int cols = 12;
        Position humanStart = new Position(rows - 2, 1);
        Position aiStart = new Position(1, cols - 2);
        Board board = Board.randomReachable(rows, cols, 0.20, humanStart, aiStart, seed);

        UnitState human = new UnitState("Human Ranger", Side.HUMAN, humanStart, 100, 100, false, false);
        UnitState ai = new UnitState("AI Sentinel", Side.AI, aiStart, 100, 100, false, false);
        return new GameState(board, human, ai, Side.HUMAN, 1, seed, 0);
    }

    public UnitState unit(Side side) {
        return side == Side.HUMAN ? human : ai;
    }

    public Position positionOf(Side side) {
        return unit(side).position();
    }

    public boolean powerStrikeReady() {
        return humanAttackCharge >= POWER_STRIKE_REQUIRED_ATTACKS;
    }

    public boolean isTerminal() {
        return !human.isAlive() || !ai.isAlive() || turnNumber > MAX_TURNS;
    }

    public String winnerText() {
        if (human.health() <= 0 && ai.health() <= 0) {
            return "Draw";
        }
        if (human.health() <= 0) {
            return "AI Sentinel wins";
        }
        if (ai.health() <= 0) {
            return "Human Ranger wins";
        }
        if (turnNumber > MAX_TURNS) {
            return "Turn limit reached - draw";
        }
        return "Match in progress";
    }

    public GameState withTurn(Side nextTurn) {
        return new GameState(board, human, ai, nextTurn, turnNumber, arenaSeed, humanAttackCharge);
    }
}
