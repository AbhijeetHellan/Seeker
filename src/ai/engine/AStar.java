package ai.engine;

import ai.models.Board;
import ai.models.Position;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/** A* pathfinding for a four-direction grid using Manhattan distance. */
public final class AStar {
    private AStar() {
    }

    private record Node(Position position, double g, double h) implements Comparable<Node> {
        double f() {
            return g + h;
        }

        @Override
        public int compareTo(Node other) {
            int byF = Double.compare(f(), other.f());
            if (byF != 0) {
                return byF;
            }

            int byH = Double.compare(h, other.h);
            if (byH != 0) {
                return byH;
            }

            int byRow = Integer.compare(position.row(), other.position.row());
            if (byRow != 0) {
                return byRow;
            }
            return Integer.compare(position.col(), other.position.col());
        }
    }

    public static PathResult findPath(Board board, Position start, Position goal) {
        long startTime = System.nanoTime();

        if (!board.isWalkable(start) || !board.isWalkable(goal)) {
            return PathResult.notFound(0, System.nanoTime() - startTime);
        }

        PriorityQueue<Node> open = new PriorityQueue<>();
        Map<Position, Double> bestKnownCost = new HashMap<>();
        Map<Position, Position> parent = new HashMap<>();
        Set<Position> closed = new HashSet<>();

        bestKnownCost.put(start, 0.0);
        open.add(new Node(start, 0.0, estimate(start, goal)));

        long expanded = 0;

        while (!open.isEmpty()) {
            Node current = open.poll();
            if (!closed.add(current.position())) {
                continue;
            }
            expanded++;

            if (current.position().equals(goal)) {
                return new PathResult(
                        true,
                        rebuildPath(parent, goal),
                        current.g(),
                        expanded,
                        System.nanoTime() - startTime
                );
            }

            for (Position next : board.neighbors(current.position())) {
                if (closed.contains(next)) {
                    continue;
                }

                double newCost = current.g() + 1.0;
                double oldCost = bestKnownCost.getOrDefault(next, Double.POSITIVE_INFINITY);
                if (newCost >= oldCost) {
                    continue;
                }

                bestKnownCost.put(next, newCost);
                parent.put(next, current.position());
                open.add(new Node(next, newCost, estimate(next, goal)));
            }
        }

        return PathResult.notFound(expanded, System.nanoTime() - startTime);
    }

    private static double estimate(Position from, Position goal) {
        return from.manhattanDistance(goal);
    }

    private static List<Position> rebuildPath(Map<Position, Position> parent, Position goal) {
        LinkedList<Position> path = new LinkedList<>();
        Position current = goal;
        path.addFirst(current);

        while (parent.containsKey(current)) {
            current = parent.get(current);
            path.addFirst(current);
        }
        return path;
    }
}
