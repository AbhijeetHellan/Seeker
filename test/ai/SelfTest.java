package ai;

import ai.api.DefaultGameApi;
import ai.api.GameApi;
import ai.engine.AStar;
import ai.engine.AlphaBeta;
import ai.engine.DecisionResult;
import ai.engine.Minimax;
import ai.engine.PathResult;
import ai.models.ActionType;
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
        testPowerStrikeAfterSixAttacks();
        testWasdMovement();
        testHideBehindCover();
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
            require(aiDamage >= 8 && aiDamage <= 20,
                    "AI damage must stay between 8 and 20.");
        }
    }

    private static void testPowerStrikeAfterSixAttacks() {
        Board board = Board.empty(4, 4);
        UnitState human = unit("H", Side.HUMAN, new Position(1, 1));
        UnitState ai = unit("A", Side.AI, new Position(1, 2));
        GameState current = state(board, human, ai, Side.HUMAN, 0);

        for (int i = 0; i < 6; i++) {
            current = GameRules.applyForSearch(current, GameAction.attack()).state();
            if (i < 5) {
                require(!current.powerStrikeReady(), "Power Strike should not be ready early.");
            }
            current = current.withTurn(Side.HUMAN);
        }

        require(current.powerStrikeReady(), "Power Strike should unlock after six attacks.");
        require(GameRules.isLegal(current, ActionType.POWER_STRIKE),
                "Power Strike should be legal when charged and adjacent.");

        int healthBefore = current.ai().health();
        GameState after = GameRules.apply(current, GameAction.powerStrike()).state();
        require(healthBefore - after.ai().health() == 15,
                "Power Strike should deal a guaranteed 15 damage.");
        require(after.humanAttackCharge() == 0, "Power Strike should reset the charge.");
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

    private static void testHideBehindCover() {
        boolean[][] walls = new boolean[6][6];
        walls[3][1] = true;
        Board board = new Board(walls);

        UnitState human = unit("H", Side.HUMAN, new Position(3, 2));
        UnitState ai = unit("A", Side.AI, new Position(0, 5));
        GameState state = state(board, human, ai, Side.HUMAN, 0);

        require(GameRules.isLegal(state, ActionType.HIDE),
                "Human beside cover should be able to hide.");

        GameState after = GameRules.apply(state, GameAction.hide()).state();
        require(after.human().hidden(), "Hide should mark the human as hidden.");
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
        return new UnitState(name, side, position, 100, 100, false, false);
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
