package ai;

import ai.api.DefaultGameApi;
import ai.api.GameApi;
import ai.engine.AStar;
import ai.engine.AlphaBeta;
import ai.engine.DecisionResult;
import ai.engine.Minimax;
import ai.engine.PathResult;
import ai.models.ActionType;
import ai.models.Difficulty;
import ai.models.Board;
import ai.models.GameAction;
import ai.models.GameRules;
import ai.models.GameState;
import ai.models.Position;
import ai.models.Side;
import ai.models.UnitState;

public final class SelfTest {
    private SelfTest() {
    }

    public static void main(String[] args) {
        testAStarManhattan();
        testRandomArenaReachable();
        testRandomDamageRanges();
        testPowerStrikeAfterFiveAttacks();
        testDifficultyAndLock();
        testDifficultyDamageRanges();
        testComparisonData();
        testWasdMovement();
        testHoldDoesNotReset();
        testNoHiddenState();
        testApiReset();
        testMinimaxAlphaBetaConsistency();
        System.out.println("All Seeker self-tests passed.");
    }

    private static void testAStarManhattan() {
        Board board = Board.empty(5, 5);
        Position start = new Position(0, 0);
        Position goal = new Position(4, 4);

        PathResult result = AStar.findPath(board, start, goal);
        require(result.found(), "A* should find a path.");
        require(Math.abs(result.pathCost() - 8.0) < 0.0001, "Expected path cost 8.");
    }

    private static void testRandomArenaReachable() {
        GameState state = GameState.newRandomArena(12345L);
        PathResult path = AStar.findPath(
                state.board(),
                state.human().position(),
                state.ai().position()
        );
        require(path.found(), "Generated arena must keep human and AI connected.");
    }

    private static void testRandomDamageRanges() {
        Board board = Board.empty(4, 4);
        UnitState human = unit("H", Side.HUMAN, new Position(1, 1));
        UnitState ai = unit("A", Side.AI, new Position(1, 2));

        for (int i = 0; i < 100; i++) {
            GameState humanTurn = state(board, human, ai, Side.HUMAN, 0);
            GameState afterHuman = GameRules.apply(humanTurn, GameAction.attack()).state();
            int humanDamage = 100 - afterHuman.ai().health();
            require(humanDamage >= 5 && humanDamage <= 15,
                    "Human damage must stay between 5 and 15.");

            GameState aiTurn = state(board, human, ai, Side.AI, 0);
            GameState afterAi = GameRules.apply(aiTurn, GameAction.attack()).state();
            int aiDamage = 100 - afterAi.human().health();
            require(aiDamage >= Difficulty.MEDIUM.minAiDamage()
                    && aiDamage <= Difficulty.MEDIUM.maxAiDamage(),
                    "AI damage must follow the Medium difficulty damage range.");
        }
    }

    private static void testPowerStrikeAfterFiveAttacks() {
        Board board = Board.empty(4, 4);
        UnitState human = unit("H", Side.HUMAN, new Position(1, 1));
        UnitState ai = unit("A", Side.AI, new Position(1, 2));
        GameState current = state(board, human, ai, Side.HUMAN, 0);

        for (int i = 0; i < 5; i++) {
            current = GameRules.applyForSearch(current, GameAction.attack()).state();
            if (i < 4) {
                require(!current.powerStrikeReady(), "Power Strike should not be ready early.");
            }
            current = current.withTurn(Side.HUMAN);
        }

        require(current.powerStrikeReady(), "Power Strike should unlock after five attacks.");
        require(GameRules.isLegal(current, ActionType.POWER_STRIKE),
                "Power Strike should be legal when charged and adjacent.");

        int healthBefore = current.ai().health();
        GameState after = GameRules.apply(current, GameAction.powerStrike()).state();
        require(healthBefore - after.ai().health() == 15,
                "Power Strike should deal a guaranteed 15 damage.");
        require(after.humanAttackCharge() == 0, "Power Strike should reset the charge.");
    }

    private static void testDifficultyAndLock() {
        GameApi api = new DefaultGameApi(101L);
        api.setDifficulty(Difficulty.EASY);
        require(api.state().difficulty() == Difficulty.EASY, "Difficulty must be selectable before play.");
        require(api.state().turnNumber() == 1, "Selecting difficulty must not consume a turn.");

        api.performHumanAction(ActionType.HOLD);
        require(api.state().turnNumber() > 1, "Human action should start the match.");
        boolean rejected = false;
        try {
            api.setDifficulty(Difficulty.HARD);
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        require(rejected, "Difficulty must be locked once a match has started.");
        require(api.state().difficulty() == Difficulty.EASY, "Rejected change must preserve difficulty.");
        require(api.runAiTurn().alphaBeta().depth() == Difficulty.EASY.searchDepth(),
                "AI search must use the locked difficulty depth.");

        api.resetArena();
        require(api.state().turnNumber() == 1, "Reset must return to pre-game state.");
        require(api.state().difficulty() == Difficulty.EASY, "Reset should retain chosen difficulty.");
        api.setDifficulty(Difficulty.HARD);
        require(api.state().difficulty() == Difficulty.HARD, "Difficulty should unlock after Reset.");
        api.newArena(202L);
        require(api.state().difficulty() == Difficulty.HARD, "New arena should retain selected difficulty.");
        api.setDifficulty(Difficulty.MEDIUM);
        require(api.state().difficulty() == Difficulty.MEDIUM, "Difficulty should unlock on new arena.");
    }

    private static void testDifficultyDamageRanges() {
        Board board = Board.empty(4, 4);
        UnitState human = unit("H", Side.HUMAN, new Position(1, 1));
        UnitState ai = unit("A", Side.AI, new Position(1, 2));
        for (Difficulty difficulty : Difficulty.values()) {
            for (int i = 0; i < 100; i++) {
                GameState starting = new GameState(board, human, ai, Side.AI, 1, 12L, 0, difficulty);
                GameState after = GameRules.apply(starting, GameAction.attack()).state();
                int damage = 100 - after.human().health();
                require(damage >= difficulty.minAiDamage() && damage <= difficulty.maxAiDamage(),
                        "AI damage must follow the chosen difficulty.");
            }
        }
    }

    private static void testComparisonData() {
        GameState starting = GameState.newRandomArena(9988L, Difficulty.EASY).withTurn(Side.AI);
        DecisionResult mm = new Minimax().chooseAction(starting, Difficulty.EASY.searchDepth());
        DecisionResult ab = new AlphaBeta().chooseAction(starting, Difficulty.EASY.searchDepth());
        ai.experiment.ComparisonSample sample = new ai.experiment.ComparisonSample(1, 2, Difficulty.EASY, mm, ab);
        require(sample.difficulty() == Difficulty.EASY, "CSV comparison needs match difficulty.");
        require(sample.minimax().nodesVisited() >= sample.alphaBeta().nodesVisited(),
                "Two-line comparison should show Alpha-Beta explores no more nodes.");
        require(sample.alphaBetaEfficiencyGainPercent() >= 0.0, "Efficiency percentage must be nonnegative.");
    }

    private static void testWasdMovement() {
        Board board = Board.empty(4, 4);
        UnitState human = unit("H", Side.HUMAN, new Position(2, 1));
        UnitState ai = unit("A", Side.AI, new Position(0, 3));
        GameState state = state(board, human, ai, Side.HUMAN, 0);

        GameState after = GameRules.moveHuman(state, -1, 0).state();
        require(after.human().position().equals(new Position(1, 1)),
                "W should move the human up one tile.");
        require(after.turn() == Side.AI, "Manual movement should consume the human turn.");
    }

    /** Holding consumes a turn but never resets the arena or difficulty. */
    private static void testHoldDoesNotReset() {
        GameApi api = new DefaultGameApi(2026L);
        api.setDifficulty(Difficulty.HARD);
        GameState before = api.state();
        api.performHumanAction(ActionType.HOLD);
        GameState after = api.state();
        require(after.turnNumber() == before.turnNumber() + 1,
                "Hold should advance one turn, not reset the match.");
        require(after.arenaSeed() == before.arenaSeed(),
                "Hold should not generate another arena.");
        require(after.difficulty() == Difficulty.HARD,
                "Hold should preserve the selected difficulty.");
    }

    private static void testNoHiddenState() {
        for (ActionType type : ActionType.values()) {
            require(!type.name().equals("HIDE"), "Hide must not be available as an action.");
        }
        GameState state = GameState.newRandomArena(2026L);
        require(GameRules.legalActions(state).stream().noneMatch(a -> a.label().equalsIgnoreCase("Hide")),
                "Neither player should have Hide in the legal action list.");
        require(GameRules.legalActions(state.withTurn(Side.AI)).stream()
                .noneMatch(a -> a.label().equalsIgnoreCase("Hide")),
                "The AI must not be offered a Hide action.");
    }

    private static void testApiReset() {
        GameApi api = new DefaultGameApi(777L);
        long seed = api.state().arenaSeed();
        api.resetArena();
        require(api.state().arenaSeed() == seed, "Reset should keep the same arena seed.");
    }

    private static void testMinimaxAlphaBetaConsistency() {
        GameState state = GameState.newRandomArena(9988L).withTurn(Side.AI);
        DecisionResult minimax = new Minimax().chooseAction(state, 4);
        DecisionResult alphaBeta = new AlphaBeta().chooseAction(state, 4);

        require(minimax.action() != null && alphaBeta.action() != null,
                "Both searches must return an action.");
        require(minimax.action().type() == alphaBeta.action().type(),
                "Minimax and Alpha-Beta should choose the same move.");
        require(Math.abs(minimax.score() - alphaBeta.score()) < 0.0001,
                "Search scores should match.");
        require(alphaBeta.nodesVisited() <= minimax.nodesVisited(),
                "Alpha-Beta should not visit more nodes than Minimax.");
    }

    private static UnitState unit(String name, Side side, Position position) {
        return new UnitState(name, side, position, 100, 100, false);
    }

    private static GameState state(
            Board board,
            UnitState human,
            UnitState ai,
            Side turn,
            int attackCharge
    ) {
        return new GameState(board, human, ai, turn, 1, 1L, attackCharge);
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
