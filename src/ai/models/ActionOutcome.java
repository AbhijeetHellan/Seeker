package ai.models;

import ai.engine.PathResult;

public record ActionOutcome(GameState state, PathResult pathResult, String description) {}
