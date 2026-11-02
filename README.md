# Seeker

Seeker is a Java 21 tactical AI game built with Swing. The project combines A* pathfinding, Minimax, and Alpha-Beta pruning in one playable grid game.

## Main features

- A* pathfinding using **Manhattan distance only**
- Minimax baseline search
- Alpha-Beta pruning used by the AI
- Automatic random obstacle generation with guaranteed reachability
- WASD human movement
- Attack, Defend, Hide, A* Move, Retreat, and Hold actions
- Human normal attack damage is randomized from **5 to 15**
- AI damage varies by difficulty: **5–12 (Easy), 8–20 (Medium), 12–24 (Hard)**
- After **5 human normal attacks**, a **Power Strike** button unlocks
- Power Strike deals a guaranteed **15 damage** and then resets the charge
- One **two-line live graph** showing Minimax and Alpha-Beta nodes visited on the same axes, with efficiency savings shown below
- Live search statistics and CSV result logging
- 200-turn match limit
- Dark Swing interface

## Difficulty selection (before each match)

Select **Easy**, **Medium**, or **Hard** in the difficulty dropdown **before taking your first turn**.
The dropdown becomes **locked after the first human move/action** and stays locked for the entire match.
The selected difficulty sets both AI search depth and AI damage range. Human attacks and Power Strike remain the same at every difficulty.

| Difficulty | AI search depth | AI attack damage | Human attack damage | Power Strike |
|---|---:|---:|---:|---:|
| Easy | 2 | 5–12 | 5–15 | 15 |
| Medium (default) | 3 | 8–20 | 5–15 | 15 |
| Hard | 4 | 12–24 | 5–15 | 15 |

**Reset Match** restarts the same arena, retains the selected difficulty, and unlocks the dropdown.
**New Random Arena** generates a new reachable map, retains the chosen difficulty, and unlocks the dropdown.
You can change difficulty again before the new match's first move. The current difficulty is displayed beside the turn and recorded in each row of the CSV file.

Minimax and Alpha-Beta always run with the **same difficulty-derived search depth** for each AI turn, so their comparison stays fair.
The actual damage rolled during gameplay is random, while search simulations use deterministic expected damage.

## Controls

- `W` — move up
- `A` — move left
- `S` — move down
- `D` — move right
- `F` — normal attack
- `G` — Power Strike after 5 normal attacks
- `E` — defend
- `H` — hide beside cover
- `R` — A* Move
- `X` — retreat
- `Space` — hold position

## Damage rules

Normal attack damage is randomized only during the real game:

- Human: 5–15 damage
- AI: Easy 5–12, Medium 8–20, Hard 12–24 damage

Minimax and Alpha-Beta use fixed expected damage values while simulating future states. This keeps both algorithms deterministic and makes their comparison fair.

Defending and hiding can reduce incoming damage.

## Power Strike

Every successful human normal attack adds one charge. After 5 attacks:

- the Power Strike button becomes available,
- it deals exactly 15 damage,
- and the charge resets to 0 after use.

## Live Minimax vs Alpha-Beta chart

Seeker displays **one chart with two lines** rather than separate runtime and nodes charts:

- **Orange line:** Minimax nodes visited per AI turn.
- **Green line:** Alpha-Beta nodes visited for that same AI turn.

Both lines use the same scale, game state, evaluation function, and search depth. Lower is better for search work.
The chart footer also shows the latest efficiency percentage:

`nodes_saved_percent = (minimax_nodes - alphabeta_nodes) / minimax_nodes × 100`

Runtime, cutoffs, and selected moves remain visible in the text comparison. The `results/live_comparison.csv` output includes the difficulty, depth, node counts, execution times, pruning data, and percentage saved.

## Project structure

```text
Seeker/
├── .github/
│   ├── CODEOWNERS
│   └── workflows/
│       └── gradle-check.yml
├── config/
│   └── checkstyle/
│       └── checkstyle.xml
├── src/
│   └── ai/
│       ├── App.java
│       ├── api/
│       ├── engine/
│       ├── experiment/
│       ├── models/
│       └── ui/
├── test/
│   └── ai/
│       └── SelfTest.java
├── build.gradle
├── settings.gradle
├── .gitignore
├── run.bat
├── run_tests.bat
└── README.md
```

## Run in Eclipse

Import the folder as an **Existing Gradle Project** and run:

```text
src/ai/App.java
```

as a Java Application.

## Run on Windows without Gradle

Double-click:

```text
run.bat
```

## Run tests

```text
run_tests.bat
```

or with Gradle:

```text
gradle clean check selfTest
```

## GitHub verification

The included GitHub Actions workflow runs the required status check:

```text
Build and Checkstyle
```

It is intended to be used with the protected `main` ruleset.
