package ai.api;

import ai.engine.DecisionResult;
import ai.models.ActionOutcome;

/** Result of one complete AI turn, including both search comparisons. */
public record AiTurnResult(
        DecisionResult minimax,
        DecisionResult alphaBeta,
        ActionOutcome appliedOutcome
) {}
