package ai.engine;

import ai.models.GameAction;
import ai.models.GameRules;
import ai.models.GameState;
import ai.models.Side;

import java.util.List;

/** Straightforward Minimax used as the baseline for comparison. */
public final class Minimax {
    public DecisionResult chooseAction(GameState state, int depth) {
        if (depth < 1) throw new IllegalArgumentException("Depth must be at least 1.");

        long start = System.nanoTime();
        SearchMetrics metrics = new SearchMetrics();
        List<GameAction> actions = GameRules.legalActions(state);

        if (actions.isEmpty()) {
            return new DecisionResult(
                    null,
                    Evaluator.evaluate(state),
                    0,
                    0,
                    0,
                    System.nanoTime() - start,
                    depth
            );
        }

        boolean maximizing = state.turn() == Side.AI;
        GameAction bestAction = actions.get(0);
        double bestScore = maximizing ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        for (GameAction action : actions) {
            GameState child = GameRules.applyForSearch(state, action).state();
            double score = search(child, depth - 1, metrics);

            if (maximizing && score > bestScore) {
                bestScore = score;
                bestAction = action;
            } else if (!maximizing && score < bestScore) {
                bestScore = score;
                bestAction = action;
            }
        }

        return new DecisionResult(
                bestAction,
                bestScore,
                metrics.nodesVisited(),
                0,
                0,
                System.nanoTime() - start,
                depth
        );
    }

    private double search(GameState state, int depth, SearchMetrics metrics) {
        metrics.visitNode();

        if (depth == 0 || state.isTerminal()) {
            return Evaluator.evaluate(state);
        }

        List<GameAction> actions = GameRules.legalActions(state);
        if (actions.isEmpty()) return Evaluator.evaluate(state);

        if (state.turn() == Side.AI) {
            double best = Double.NEGATIVE_INFINITY;
            for (GameAction action : actions) {
                GameState child = GameRules.applyForSearch(state, action).state();
                best = Math.max(best, search(child, depth - 1, metrics));
            }
            return best;
        }

        double best = Double.POSITIVE_INFINITY;
        for (GameAction action : actions) {
            GameState child = GameRules.applyForSearch(state, action).state();
            best = Math.min(best, search(child, depth - 1, metrics));
        }
        return best;
    }
}
