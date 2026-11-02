# Seeker: Difficulty and live comparison

## Difficulty is selected before the match

The player chooses difficulty **before the first human action**. Once the first move/action is taken, the selector locks for the whole game. The choice controls both AI search depth and AI damage range, but does **not** change human damage or Power Strike. The selected setting is carried in every `GameState` and captured in comparison CSV output.

| Mode | Search depth | AI damage per normal hit | Human normal hit | Power Strike |
|---|---:|---:|---:|---:|
| Easy | 2 | 5–12 | 5–15 | 15 |
| Medium | 3 | 8–20 | 5–15 | 15 |
| Hard | 4 | 12–24 | 5–15 | 15 |

- **Reset Match:** restore the initial state using the **same arena** and current difficulty; unlock the selector.
- **New Random Arena:** create a new reachable map using the current difficulty; unlock the selector.
- After another first move, the selector locks again.
- Human Power Strike unlocks after **five** normal human attacks, must be used when adjacent, does **15** damage, and resets its charge.

## One graph, two lines

The live comparison graph now uses **AI turn** on the horizontal axis and **search nodes visited** on the vertical axis. Orange is **Minimax** and green is **Alpha-Beta**. The footer shows the latest percentage of nodes saved by pruning. Separate nodes and runtime graphs are not shown. Both algorithms use the exact same starting state, legal actions, evaluation function, and difficulty-controlled search depth.

The text panel still displays selected moves, runtime, visited nodes, and pruning cutoffs. The CSV saves difficulty and search depth alongside both results, allowing the group to analyze Easy, Medium, and Hard matches separately.
