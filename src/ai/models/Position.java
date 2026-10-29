package ai.models;

/** Row/column location on the tactical grid. */
public record Position(int row, int col) {
    public int manhattanDistance(Position other) {
        return Math.abs(row - other.row) + Math.abs(col - other.col);
    }
}
