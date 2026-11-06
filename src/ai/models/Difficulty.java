package ai.models;

/** Game difficulty: how far the AI looks ahead and how much damage it can deal. */
public enum Difficulty {
    EASY("Easy", 4, 4, 9),
    MEDIUM("Medium", 5, 6, 12),
    HARD("Hard", 6, 8, 16);
	
    private final String label;
    private final int searchDepth;
    private final int minAiDamage;
    private final int maxAiDamage;

    Difficulty(String label, int searchDepth, int minAiDamage, int maxAiDamage) {
        this.label = label;
        this.searchDepth = searchDepth;
        this.minAiDamage = minAiDamage;
        this.maxAiDamage = maxAiDamage;
    }

    public int searchDepth() {
        return searchDepth;
    }

    public int minAiDamage() {
        return minAiDamage;
    }

    public int maxAiDamage() {
        return maxAiDamage;
    }

    public int expectedAiDamage() {
        return (minAiDamage + maxAiDamage) / 2;
    }

    @Override
    public String toString() {
        return label;
    }
}
