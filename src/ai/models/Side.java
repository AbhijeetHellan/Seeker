package ai.models;

public enum Side {
    HUMAN,
    AI;

    public Side opposite() {
        return this == HUMAN ? AI : HUMAN;
    }
}
