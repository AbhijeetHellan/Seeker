package ai.experiment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/** Saves live comparison data so it can be used later in the report. */
public final class CsvRecorder {
    private static final Path OUTPUT = Path.of("results", "live_comparison.csv");

    private CsvRecorder() {
    }

    public static void reset() {
        try {
            Files.createDirectories(OUTPUT.getParent());
            Files.writeString(
                    OUTPUT,
                    "ai_turn,game_turn,depth,minimax_action,minimax_nodes,minimax_ms,alphabeta_action,"
                            + "alphabeta_nodes,alphabeta_ms,cutoffs,pruned_siblings,efficiency_gain_percent,same_action\n",
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException ignored) {
            // The game still works if the report CSV cannot be written.
        }
    }

    public static void append(ComparisonSample sample) {
        try {
            Files.createDirectories(OUTPUT.getParent());

            String mmAction = sample.minimax().action() == null
                    ? "NONE"
                    : sample.minimax().action().type().name();
            String abAction = sample.alphaBeta().action() == null
                    ? "NONE"
                    : sample.alphaBeta().action().type().name();
            boolean same = mmAction.equals(abAction);

            String line = String.format(
                    java.util.Locale.US,
                    "%d,%d,%d,%s,%d,%.6f,%s,%d,%.6f,%d,%d,%.2f,%s%n",
                    sample.aiTurn(),
                    sample.gameTurn(),
                    sample.minimax().depth(),
                    mmAction,
                    sample.minimax().nodesVisited(),
                    sample.minimax().runtimeMillis(),
                    abAction,
                    sample.alphaBeta().nodesVisited(),
                    sample.alphaBeta().runtimeMillis(),
                    sample.alphaBeta().cutoffs(),
                    sample.alphaBeta().prunedSiblingBranches(),
                    sample.alphaBetaEfficiencyGainPercent(),
                    same
            );

            Files.writeString(
                    OUTPUT,
                    line,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException ignored) {
            // Data logging is optional for gameplay.
        }
    }
}
