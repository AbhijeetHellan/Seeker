package ai.api;

import ai.engine.AlphaBeta;
import ai.engine.DecisionResult;
import ai.engine.Minimax;
import ai.models.ActionOutcome;
import ai.models.ActionType;
import ai.models.GameAction;
import ai.models.GameRules;
import ai.models.GameState;

/** Default in-memory implementation used by the desktop application. */
public final class DefaultGameApi implements GameApi {
    private GameState state;

    public DefaultGameApi(long seed) {
        state = GameState.newRandomArena(seed);
    }

    @Override
    public GameState state() {
        return state;
    }

    @Override
    public void newArena(long seed) {
        state = GameState.newRandomArena(seed);
    }

    @Override
    public void resetArena() {
        state = GameState.newRandomArena(state.arenaSeed());
    }

    @Override
    public boolean isLegal(ActionType actionType) {
        return GameRules.isLegal(state, actionType);
    }

    @Override
    public ActionOutcome moveHuman(int dRow, int dCol) {
        ActionOutcome outcome = GameRules.moveHuman(state, dRow, dCol);
        state = outcome.state();
        return outcome;
    }

    @Override
    public ActionOutcome performHumanAction(ActionType actionType) {
        ActionOutcome outcome = GameRules.apply(state, actionFor(actionType));
        state = outcome.state();
        return outcome;
    }

    @Override
    public AiTurnResult runAiTurn(int depth) {
        GameState snapshot = state;
        DecisionResult minimax = new Minimax().chooseAction(snapshot, depth);
        DecisionResult alphaBeta = new AlphaBeta().chooseAction(snapshot, depth);

        ActionOutcome outcome = null;
        if (alphaBeta.action() != null) {
            outcome = GameRules.apply(state, alphaBeta.action());
            state = outcome.state();
        }

        return new AiTurnResult(minimax, alphaBeta, outcome);
    }

    private static GameAction actionFor(ActionType type) {
        return switch (type) {
            case ATTACK -> GameAction.attack();
            case POWER_STRIKE -> GameAction.powerStrike();
            case APPROACH -> GameAction.approach();
            case RETREAT -> GameAction.retreat();
            case DEFEND -> GameAction.defend();
            case HIDE -> GameAction.hide();
            case HOLD -> GameAction.hold();
        };
    }
}
