package ai.experiment;

import ai.engine.DecisionResult;

/** One live Minimax-vs-Alpha-Beta measurement from the same game state. */
public record ComparisonSample(
        int aiTurn,
        int gameTurn,
        DecisionResult minimax,
        DecisionResult alphaBeta
) {
    public double alphaBetaEfficiencyGainPercent() {
        long minimaxNodes = minimax.nodesVisited();
        long alphaBetaNodes = alphaBeta.nodesVisited();
        if (minimaxNodes <= 0) {
            return 0.0;
        }
        double saved = (minimaxNodes - alphaBetaNodes) * 100.0 / minimaxNodes;
        return Math.max(0.0, Math.min(100.0, saved));
    }
}
