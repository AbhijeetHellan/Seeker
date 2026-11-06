package ai.engine;

import ai.models.GameState;
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

        // Winning the fight matters most: damage dealt and health preserved.
        score += (ai.health() - human.health()) * 5.5;

        // This boss ALWAYS wants to close the gap, even when badly hurt.
        // Previously low health rewarded increasing the distance (fleeing).
        score -= distance * 5.0;

        // Once close, stay engaged rather than backing away from an attack.
        if (distance == 1) {
            score += 18.0;
        }

        // A charged Power Strike matters, but must not make the boss flee.
        score -= state.humanAttackCharge() * 2.0;
        if (state.powerStrikeReady() && distance == 1) {
            score -= 12.0;
        }

        // Guarding is useful only in melee range. Defending far away should
        // never compete with pursuing the player.
        if (ai.defending() && distance == 1) {
            score += ai.healthPercent() <= 30 ? 12.0 : 4.0;
        }
        if (human.defending() && distance == 1) {
            score -= 4.0;
        }

        // Slight bonus for having space to maneuver near the player.
        if (distance <= 2) {
            score += state.board().neighbors(ai.position()).size();
        }
        return score;
    }
}
