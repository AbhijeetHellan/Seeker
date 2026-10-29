package ai.api;

import ai.models.ActionOutcome;
import ai.models.ActionType;
import ai.models.GameState;

/** Small API between the Swing interface and the game/search code. */
public interface GameApi {
    GameState state();

    void newArena(long seed);

    void resetArena();

    boolean isLegal(ActionType actionType);

    ActionOutcome moveHuman(int dRow, int dCol);

    ActionOutcome performHumanAction(ActionType actionType);

    AiTurnResult runAiTurn(int depth);
}
