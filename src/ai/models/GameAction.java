package ai.models;

public record GameAction(ActionType type, String label) {
    public static GameAction attack() {
        return new GameAction(ActionType.ATTACK, "Attack");
    }

    public static GameAction powerStrike() {
        return new GameAction(ActionType.POWER_STRIKE, "Power Strike");
    }

    public static GameAction approach() {
        return new GameAction(ActionType.APPROACH, "A* Move");
    }

    public static GameAction retreat() {
        return new GameAction(ActionType.RETREAT, "Retreat");
    }

    public static GameAction defend() {
        return new GameAction(ActionType.DEFEND, "Defend");
    }

    public static GameAction hide() {
        return new GameAction(ActionType.HIDE, "Hide");
    }

    public static GameAction hold() {
        return new GameAction(ActionType.HOLD, "Hold");
    }

    @Override
    public String toString() {
        return label;
    }
}
