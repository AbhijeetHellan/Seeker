package ai.models;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

/** Immutable obstacle map. */
public final class Board {
    private final int rows;
    private final int cols;
    private final boolean[][] blocked;

    public Board(boolean[][] blocked) {
        if (blocked == null || blocked.length == 0 || blocked[0].length == 0) {
            throw new IllegalArgumentException("Board cannot be empty.");
        }
        this.rows = blocked.length;
        this.cols = blocked[0].length;
        this.blocked = new boolean[rows][cols];
        for (int r = 0; r < rows; r++) {
            if (blocked[r].length != cols) throw new IllegalArgumentException("Board rows must be equal length.");
            System.arraycopy(blocked[r], 0, this.blocked[r], 0, cols);
        }
    }

    public static Board empty(int rows, int cols) {
        return new Board(new boolean[rows][cols]);
    }

    /**
     * Generates random obstacles automatically and retries until the two start
     * positions are connected and each start has room to move.
     */
    public static Board randomReachable(
            int rows,
            int cols,
            double obstacleDensity,
            Position humanStart,
            Position aiStart,
            long seed
    ) {
        if (obstacleDensity < 0 || obstacleDensity > 0.45) {
            throw new IllegalArgumentException("Obstacle density must be between 0 and 0.45.");
        }

        for (int attempt = 0; attempt < 500; attempt++) {
            Random random = new Random(seed + attempt * 9_973L);
            boolean[][] walls = new boolean[rows][cols];

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    Position p = new Position(r, c);
                    boolean safe = p.manhattanDistance(humanStart) <= 1
                            || p.manhattanDistance(aiStart) <= 1;
                    if (!safe && random.nextDouble() < obstacleDensity) {
                        walls[r][c] = true;
                    }
                }
            }

            Board candidate = new Board(walls);
            if (candidate.hasPath(humanStart, aiStart)
                    && candidate.openNeighborCount(humanStart) >= 2
                    && candidate.openNeighborCount(aiStart) >= 2) {
                return candidate;
            }
        }

        // Very unlikely fallback: an empty board is guaranteed to be playable.
        return empty(rows, cols);
    }

    public int rows() { return rows; }
    public int cols() { return cols; }

    public boolean inBounds(Position p) {
        return p.row() >= 0 && p.row() < rows && p.col() >= 0 && p.col() < cols;
    }

    public boolean isBlocked(Position p) {
        return !inBounds(p) || blocked[p.row()][p.col()];
    }

    public boolean isWalkable(Position p) {
        return inBounds(p) && !blocked[p.row()][p.col()];
    }

    public List<Position> neighbors(Position p) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        List<Position> result = new ArrayList<>(4);
        for (int[] d : directions) {
            Position next = new Position(p.row() + d[0], p.col() + d[1]);
            if (isWalkable(next)) result.add(next);
        }
        return result;
    }

    private int openNeighborCount(Position p) {
        return neighbors(p).size();
    }

    private boolean hasPath(Position start, Position goal) {
        if (!isWalkable(start) || !isWalkable(goal)) return false;
        boolean[][] seen = new boolean[rows][cols];
        Queue<Position> queue = new ArrayDeque<>();
        queue.add(start);
        seen[start.row()][start.col()] = true;

        while (!queue.isEmpty()) {
            Position current = queue.remove();
            if (current.equals(goal)) return true;
            for (Position next : neighbors(current)) {
                if (!seen[next.row()][next.col()]) {
                    seen[next.row()][next.col()] = true;
                    queue.add(next);
                }
            }
        }
        return false;
    }
}
