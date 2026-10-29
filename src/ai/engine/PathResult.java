package ai.engine;

import ai.models.Position;
import java.util.List;

public record PathResult(
        boolean found,
        List<Position> path,
        double pathCost,
        long nodesExpanded,
        long runtimeNanos
) {
    public PathResult {
        path = List.copyOf(path);
    }

    public static PathResult notFound(long expanded, long runtimeNanos) {
        return new PathResult(false, List.of(), Double.POSITIVE_INFINITY, expanded, runtimeNanos);
    }

    public double runtimeMillis() {
        return runtimeNanos / 1_000_000.0;
    }
}
