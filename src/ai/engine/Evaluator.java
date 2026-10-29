package ai.engine;

import ai.models.GameRules;
import ai.models.GameState;
import ai.models.Side;
import ai.models.UnitState;

/** Scores a position from the AI's point of view. */
public final class Evaluator {
    private Evaluator() {
    }

    public static double evaluate(GameState state) {
        if (state.ai().health() <= 0) {
            return -100_000;
        }
        if (state.human().health() <= 0) {
            return 100_000;
        }

        UnitState ai = state.ai();
        UnitState human = state.human();
        int distance = ai.position().manhattanDistance(human.position());

        double score = 0;

        // Health difference is the main goal.
        score += (ai.health() - human.health()) * 5.5;

        // Healthy AI pressures the player. Low-health AI values distance more.
        if (ai.healthPercent() > 40) {
            score -= distance * 1.8;
        } else {
            score += distance * 1.6;
        }

        // Being next to the human is valuable when the AI can attack.
        if (distance == 1) {
            score += state.turn() == Side.AI ? 34 : -24;
        }

        // The human's charged Power Strike is a real threat at close range.
        score -= state.humanAttackCharge() * 2.5;
        if (state.powerStrikeReady() && distance == 1) {
            score -= 32;
        }

        // Cover and defense matter, especially at lower health.
        if (ai.hidden()) {
            score += 11;
        }
        if (human.hidden()) {
            score -= 14;
        }
        if (GameRules.hasAdjacentObstacle(state.board(), ai.position())) {
            score += 6;
        }
        if (GameRules.hasAdjacentObstacle(state.board(), human.position())) {
            score -= 5;
        }
        if (ai.defending()) {
            score += ai.healthPercent() <= 40 ? 18 : 8;
        }
        if (human.defending()) {
            score -= human.healthPercent() <= 40 ? 16 : 7;
        }

        // More open neighboring tiles means more tactical flexibility.
        score += state.board().neighbors(ai.position()).size() * 2.0;
        score -= state.board().neighbors(human.position()).size() * 1.4;

        // Small tempo bonus so the AI prefers useful actions on its own turn.
        score += state.turn() == Side.AI ? 1.0 : -1.0;
        return score;
    }
}
