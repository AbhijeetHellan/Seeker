package ai.engine;

final class SearchMetrics {
    private long nodesVisited;
    private long cutoffs;
    private long prunedSiblingBranches;

    void visitNode() { nodesVisited++; }

    void cutoff(long prunedSiblings) {
        cutoffs++;
        prunedSiblingBranches += Math.max(0, prunedSiblings);
    }

    long nodesVisited() { return nodesVisited; }
    long cutoffs() { return cutoffs; }
    long prunedSiblingBranches() { return prunedSiblingBranches; }
}
