package ai.api;

import ai.models.ActionOutcome;
import ai.models.Difficulty;
import ai.models.ActionType;
import ai.models.GameState;

/** Small API between the Swing interface and the game/search code. */
public interface GameApi {
    GameState state();

    void newArena(long seed);

    void resetArena();

    /** Change difficulty only before the first move of a match. */
    void setDifficulty(Difficulty difficulty);

    boolean isLegal(ActionType actionType);

    ActionOutcome moveHuman(int dRow, int dCol);

    ActionOutcome performHumanAction(ActionType actionType);

    /** AI search depth comes from the difficulty chosen before the match. */
    AiTurnResult runAiTurn();
}
