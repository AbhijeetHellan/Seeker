package ai.engine;

import ai.models.GameAction;
import ai.models.GameRules;
import ai.models.GameState;
import ai.models.Side;

import java.util.List;

/** Minimax with Alpha-Beta pruning. This is the AI used during gameplay. */
public final class AlphaBeta {
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
        double alpha = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        for (GameAction action : actions) {
            GameState child = GameRules.applyForSearch(state, action).state();
            double score = search(child, depth - 1, alpha, beta, metrics);

            if (maximizing) {
                if (score > bestScore) {
                    bestScore = score;
                    bestAction = action;
                }
                alpha = Math.max(alpha, bestScore);
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestAction = action;
                }
                beta = Math.min(beta, bestScore);
            }
        }

        return new DecisionResult(
                bestAction,
                bestScore,
                metrics.nodesVisited(),
                metrics.cutoffs(),
                metrics.prunedSiblingBranches(),
                System.nanoTime() - start,
                depth
        );
    }

    private double search(
            GameState state,
            int depth,
            double alpha,
            double beta,
            SearchMetrics metrics
    ) {
        metrics.visitNode();

        if (depth == 0 || state.isTerminal()) {
            return Evaluator.evaluate(state);
        }

        List<GameAction> actions = GameRules.legalActions(state);
        if (actions.isEmpty()) return Evaluator.evaluate(state);

        if (state.turn() == Side.AI) {
            double best = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < actions.size(); i++) {
                GameState child = GameRules.applyForSearch(state, actions.get(i)).state();
                best = Math.max(best, search(child, depth - 1, alpha, beta, metrics));
                alpha = Math.max(alpha, best);

                if (alpha >= beta) {
                    metrics.cutoff(actions.size() - i - 1L);
                    break;
                }
            }
            return best;
        }

        double best = Double.POSITIVE_INFINITY;

        for (int i = 0; i < actions.size(); i++) {
            GameState child = GameRules.applyForSearch(state, actions.get(i)).state();
            best = Math.min(best, search(child, depth - 1, alpha, beta, metrics));
            beta = Math.min(beta, best);

            if (beta <= alpha) {
                metrics.cutoff(actions.size() - i - 1L);
                break;
            }
        }
        return best;
    }
}
