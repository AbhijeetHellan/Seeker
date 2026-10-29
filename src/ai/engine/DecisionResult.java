package ai.engine;

import ai.models.GameAction;

public record DecisionResult(
        GameAction action,
        double score,
        long nodesVisited,
        long cutoffs,
        long prunedSiblingBranches,
        long runtimeNanos,
        int depth
) {
    public double runtimeMillis() { return runtimeNanos / 1_000_000.0; }
}
