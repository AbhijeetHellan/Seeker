package ai.models;

import ai.engine.AStar;
import ai.engine.PathResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** Central rules used by the UI, Minimax, and Alpha-Beta. */
public final class GameRules {
    public static final int HUMAN_MIN_DAMAGE = 5;
    public static final int HUMAN_MAX_DAMAGE = 15;
    public static final int AI_MIN_DAMAGE = 8;
    public static final int AI_MAX_DAMAGE = 20;
    public static final int POWER_STRIKE_DAMAGE = 15;

    private static final int HUMAN_SEARCH_DAMAGE = 10;
    private static final int AI_SEARCH_DAMAGE = 14;

    private GameRules() {
    }

    /** Returns legal actions in an order that also helps Alpha-Beta pruning. */
    public static List<GameAction> legalActions(GameState state) {
        if (state.isTerminal()) {
            return List.of();
        }

        UnitState actor = state.unit(state.turn());
        UnitState opponent = state.unit(state.turn().opposite());
        int distance = actor.position().manhattanDistance(opponent.position());

        List<GameAction> actions = new ArrayList<>();

        if (distance == 1) {
            if (actor.side() == Side.HUMAN && state.powerStrikeReady()) {
                actions.add(GameAction.powerStrike());
            }
            actions.add(GameAction.attack());
        }

        if (actor.healthPercent() <= 35) {
            actions.add(GameAction.defend());
            actions.add(GameAction.retreat());
        }

        if (!actor.hidden() && canHide(state.board(), actor.position(), opponent.position())) {
            actions.add(GameAction.hide());
        }

        if (distance > 1) {
            actions.add(GameAction.approach());
        }

        addIfMissing(actions, GameAction.retreat());
        addIfMissing(actions, GameAction.defend());
        actions.add(GameAction.hold());
        return actions;
    }

    private static void addIfMissing(List<GameAction> actions, GameAction action) {
        boolean exists = actions.stream().anyMatch(existing -> existing.type() == action.type());
        if (!exists) {
            actions.add(action);
        }
    }

    public static boolean isLegal(GameState state, ActionType type) {
        return legalActions(state).stream().anyMatch(action -> action.type() == type);
    }

    /** One-tile W/A/S/D movement for the human player. */
    public static ActionOutcome moveHuman(GameState state, int dRow, int dCol) {
        if (state.isTerminal()) {
            return new ActionOutcome(state, null, "The match is already finished.");
        }
        if (state.turn() != Side.HUMAN) {
            return new ActionOutcome(state, null, "Wait for the AI turn to finish.");
        }
        if (Math.abs(dRow) + Math.abs(dCol) != 1) {
            return new ActionOutcome(state, null, "Movement must be exactly one tile.");
        }

        UnitState human = state.human().withDefending(false);
        Position destination = new Position(
                human.position().row() + dRow,
                human.position().col() + dCol
        );

        if (!state.board().isWalkable(destination)) {
            return new ActionOutcome(state, null, "That direction is blocked by an obstacle or arena edge.");
        }
        if (destination.equals(state.ai().position())) {
            return new ActionOutcome(state, null, "The AI occupies that tile. Use Attack instead.");
        }

        boolean wasHidden = human.hidden();
        human = human.moveTo(destination);

        GameState next = new GameState(
                state.board(),
                human,
                state.ai(),
                Side.AI,
                state.turnNumber() + 1,
                state.arenaSeed(),
                state.humanAttackCharge()
        );

        String message = "Human Ranger moved to tile " + destination.row() + "," + destination.col() + ".";
        if (wasHidden) {
            message += " Moving revealed your position.";
        }
        return new ActionOutcome(next, null, message);
    }

    /** Applies a real game action. Normal attack damage is randomized here. */
    public static ActionOutcome apply(GameState state, GameAction action) {
        return applyInternal(state, action, false);
    }

    /** Deterministic version used only by Minimax and Alpha-Beta while searching. */
    public static ActionOutcome applyForSearch(GameState state, GameAction action) {
        return applyInternal(state, action, true);
    }

    private static ActionOutcome applyInternal(GameState state, GameAction action, boolean searchMode) {
        if (state.isTerminal()) {
            return new ActionOutcome(state, null, "The match is already finished.");
        }
        if (!isLegal(state, action.type())) {
            return new ActionOutcome(state, null, "Illegal action: " + action.label());
        }

        Side actorSide = state.turn();
        Side targetSide = actorSide.opposite();
        UnitState actor = state.unit(actorSide).withDefending(false);
        UnitState target = state.unit(targetSide);
        PathResult pathResult = null;
        String description;
        int humanCharge = state.humanAttackCharge();

        switch (action.type()) {
            case ATTACK -> {
                int rawDamage = attackDamage(actorSide, searchMode);
                int damage = reducedDamage(rawDamage, target);
                actor = actor.reveal();
                target = target.receiveDamage(damage);

                if (actorSide == Side.HUMAN) {
                    humanCharge = Math.min(GameState.POWER_STRIKE_REQUIRED_ATTACKS, humanCharge + 1);
                }

                description = actor.name() + " attacked " + target.name() + " for " + damage + " damage.";
                if (actorSide == Side.HUMAN && humanCharge >= GameState.POWER_STRIKE_REQUIRED_ATTACKS) {
                    description += " Power Strike is ready.";
                }
            }
            case POWER_STRIKE -> {
                actor = actor.reveal();
                target = target.receiveDamage(POWER_STRIKE_DAMAGE);
                humanCharge = 0;
                description = actor.name() + " used Power Strike for a guaranteed "
                        + POWER_STRIKE_DAMAGE + " damage.";
            }
            case APPROACH -> {
                pathResult = AStar.findPath(state.board(), actor.position(), target.position());
                if (pathResult.found() && pathResult.path().size() >= 3) {
                    actor = actor.moveTo(pathResult.path().get(1));
                    description = actor.name() + " advanced one tile using A* with Manhattan distance.";
                } else {
                    description = actor.name() + " could not advance and held position.";
                }
            }
            case RETREAT -> {
                Position retreat = chooseRetreat(state.board(), actor.position(), target.position());
                if (!retreat.equals(actor.position())) {
                    actor = actor.moveTo(retreat);
                    description = actor.name() + " retreated to create distance.";
                } else {
                    description = actor.name() + " had no safer retreat tile.";
                }
            }
            case DEFEND -> {
                actor = actor.withHidden(false).withDefending(true);
                description = actor.name() + " raised a defensive guard.";
            }
            case HIDE -> {
                actor = actor.withHidden(true);
                description = actor.name() + " slipped behind cover for protection.";
            }
            case HOLD -> description = actor.name() + (actor.hidden()
                    ? " stayed hidden and held position."
                    : " held position.");
            default -> throw new IllegalStateException("Unexpected action: " + action.type());
        }

        UnitState nextHuman = actorSide == Side.HUMAN ? actor : target;
        UnitState nextAi = actorSide == Side.AI ? actor : target;

        GameState next = new GameState(
                state.board(),
                nextHuman,
                nextAi,
                state.turn().opposite(),
                state.turnNumber() + 1,
                state.arenaSeed(),
                humanCharge
        );

        return new ActionOutcome(next, pathResult, description);
    }

    private static int attackDamage(Side attacker, boolean searchMode) {
        if (searchMode) {
            return attacker == Side.HUMAN ? HUMAN_SEARCH_DAMAGE : AI_SEARCH_DAMAGE;
        }
        if (attacker == Side.HUMAN) {
            return ThreadLocalRandom.current().nextInt(HUMAN_MIN_DAMAGE, HUMAN_MAX_DAMAGE + 1);
        }
        return ThreadLocalRandom.current().nextInt(AI_MIN_DAMAGE, AI_MAX_DAMAGE + 1);
    }

    private static int reducedDamage(int rawDamage, UnitState target) {
        if (target.defending()) {
            return Math.max(1, (int) Math.round(rawDamage * 0.50));
        }
        if (target.hidden()) {
            return Math.max(1, (int) Math.round(rawDamage * 0.65));
        }
        return rawDamage;
    }

    public static boolean canHide(Board board, Position actor, Position opponent) {
        if (actor.manhattanDistance(opponent) <= 1) {
            return false;
        }
        return hasAdjacentObstacle(board, actor);
    }

    public static boolean hasAdjacentObstacle(Board board, Position position) {
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] direction : directions) {
            Position next = new Position(
                    position.row() + direction[0],
                    position.col() + direction[1]
            );
            if (board.inBounds(next) && board.isBlocked(next)) {
                return true;
            }
        }
        return false;
    }

    private static Position chooseRetreat(Board board, Position actor, Position opponent) {
        return board.neighbors(actor).stream()
                .filter(position -> !position.equals(opponent))
                .max(Comparator
                        .comparingInt((Position position) -> position.manhattanDistance(opponent))
                        .thenComparingInt(position -> -position.row())
                        .thenComparingInt(position -> -position.col()))
                .orElse(actor);
    }
}
