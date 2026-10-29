package ai.models;

/** Immutable model for one combatant. */
public record UnitState(
        String name,
        Side side,
        Position position,
        int health,
        int maxHealth,
        boolean defending,
        boolean hidden
) {
    public UnitState {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Unit name is required.");
        if (side == null || position == null) throw new IllegalArgumentException("Side and position are required.");
        if (maxHealth <= 0) throw new IllegalArgumentException("maxHealth must be positive.");
        health = Math.max(0, Math.min(health, maxHealth));
    }

    public UnitState moveTo(Position next) {
        return new UnitState(name, side, next, health, maxHealth, false, false);
    }

    public UnitState withDefending(boolean value) {
        return new UnitState(name, side, position, health, maxHealth, value, value ? false : hidden);
    }

    public UnitState withHidden(boolean value) {
        return new UnitState(name, side, position, health, maxHealth, value ? false : defending, value);
    }

    public UnitState receiveDamage(int amount) {
        return new UnitState(name, side, position, Math.max(0, health - Math.max(0, amount)), maxHealth, defending, hidden);
    }

    public UnitState reveal() {
        return hidden ? new UnitState(name, side, position, health, maxHealth, defending, false) : this;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public int healthPercent() {
        return (int) Math.round(health * 100.0 / maxHealth);
    }
}
