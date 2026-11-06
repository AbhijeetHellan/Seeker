# Seeker

**Seeker** is a tactical grid-based game built in **Java 21 and Swing**. A human player battles an AI opponent on an arena with automatically generated obstacles. The project demonstrates three AI search techniques: **A\*** pathfinding, **Minimax**, and **Alpha-Beta Pruning**.

This project is being developed as a three-person AI course project. The goal is to demonstrate a playable game **and** compare how efficiently Minimax and Alpha-Beta make decisions.

## Features

- Dark graphical interface with a human character, AI opponent, health bars, and turn counter
- Automatically generated obstacles and a reachable arena
- **A\*** navigation using **Manhattan distance only** (four-directional movement)
- **Minimax** and **Alpha-Beta Pruning** evaluating the same game position
- One live comparison graph with **two lines**: Minimax nodes visited and Alpha-Beta nodes visited
- Efficiency percentage showing how many search nodes Alpha-Beta saves
- **Easy, Medium, and Hard** difficulties that change AI search depth and attack damage
- Difficulty selection **before the first move**, locked during the match
- Random attack damage, defending, retreating, holding position, and A\*-assisted movement
- Human-only **Power Strike**, charged by five normal attacks
- CSV export/logging for algorithm comparison and later analysis
- Gradle, Checkstyle, and GitHub Actions for code-quality checks

**Note:** Ambush, Hide, and the Euclidean heuristic are **not** part of Seeker.

## Difficulty and damage

Choose a difficulty before your first move. The dropdown locks after the match begins so that gameplay and recorded comparisons use consistent settings.

| Difficulty | AI search depth | AI normal attack | Human normal attack |
|---|---:|---:|---:|
| Easy | **4** | **4–9** damage | **5–15** damage |
| Medium | **5** | **6–12** damage | **5–15** damage |
| Hard | **6** | **8–16** damage | **5–15** damage |

- **Medium** is the default difficulty.
- The selected difficulty changes **AI damage and search depth**, not human damage.
- **Reset Match** keeps the current arena and unlocks the difficulty selector for a new match.
- **New Random Arena** generates a new map and unlocks the difficulty selector.
- The search depth appears in the UI as **AI Depth: 4**, **AI Depth: 5**, or **AI Depth: 6**.
- Higher depths require much more search work. **Hard (depth 6)** can make AI turns noticeably slower, especially because both algorithms are evaluated for comparison.

## How to play

You control the human character. After each valid human action, the AI takes its turn using Alpha-Beta decision-making. A\* is used to find paths around obstacles when automatic movement is needed.

### Keyboard controls

| Key | Action |
|---|---|
| `W` | Move up one tile |
| `A` | Move left one tile |
| `S` | Move down one tile |
| `D` | Move right one tile |
| `F` | Normal attack (when in range) |
| `G` | Power Strike (when charged and in range) |
| `E` | Defend |
| `R` | A\* Move toward the AI |
| `X` | Retreat |
| `Space` | Hold position |

These actions are also available through the game's on-screen buttons when legal.

### Combat rules

- A successful **human normal attack** deals a random **5–15 damage**.
- An **AI normal attack** deals random damage based on the selected difficulty.
- **Defend** reduces incoming normal-attack damage.
- **Power Strike** is for the human player only: after **five normal attacks**, the Power Strike button becomes available. It deals a **guaranteed 15 damage**, then the charge resets to zero.
- A match ends when a character is defeated or the **200-turn limit** is reached.

## AI algorithms

### A\* search

A\* handles automatic movement around obstacles. Seeker uses **Manhattan distance** as its only heuristic because the grid permits movement in four directions.

### Minimax

Minimax searches possible future turns and assigns scores to game positions using factors such as health, distance, attack opportunities, defense, and available movement.

### Alpha-Beta Pruning

Alpha-Beta uses the same rules, evaluation function, and search depth as Minimax, but skips branches that cannot change the final decision. **Alpha-Beta controls the AI's actual move**; Minimax is run on the same position for comparison.

To make the comparison repeatable, simulations use **expected damage values**. Random damage is rolled only when an actual attack happens in gameplay.

## Live algorithm comparison

Seeker has **one live graph containing two lines**:

- **Minimax** — nodes visited for each AI decision
- **Alpha-Beta** — nodes visited for the same decision

The horizontal axis shows AI turns, and the vertical axis shows search nodes visited. The interface also reports the latest node-saving efficiency:

```text
Efficiency (%) = (Minimax nodes - Alpha-Beta nodes) / Minimax nodes × 100
```

The comparison panel includes search depth, selected actions, execution times, and pruning statistics. There are **no separate runtime or nodes graphs**.

Experimental results are recorded in `results/live_comparison.csv`, including difficulty, turn, search depth, nodes visited, execution times, and Alpha-Beta pruning results.

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
├── docs/
├── src/
│   └── ai/
│       ├── App.java
│       ├── api/          # Interface between the UI and game logic
│       ├── engine/       # A*, Minimax, Alpha-Beta, evaluation
│       ├── experiment/   # Comparison data and CSV recording
│       ├── models/       # Board, game state, rules, actions, difficulty
│       └── ui/           # Swing screens, arena and live graph
├── test/
│   └── ai/
├── build.gradle
├── settings.gradle
├── .gitignore
├── run.bat
├── run_tests.bat
└── README.md
```

The source code uses `src/ai/` rather than the default `src/main/java/`; this is configured in `build.gradle`.

## Requirements

- **JDK 21** (the full JDK, including `javac`)
- **Gradle** for Gradle commands, or the included Windows scripts for direct compilation
- **Eclipse IDE** or another Java IDE (optional)

No JavaFX installation is needed because the user interface uses **Java Swing**.

## Run the game

### Eclipse

1. Import this folder using **File → Import → Gradle → Existing Gradle Project**.
2. Check that the project uses **JDK 21**.
3. Open `src/ai/App.java`.
4. Right-click the file and choose **Run As → Java Application**.

### Windows Command Prompt

From the project root, run:

```bat
run.bat
```

Or, if Gradle is installed:

```bat
gradle run
```

## Run the tests and code checks

On Windows:

```bat
run_tests.bat
```

With Gradle:

```bat
gradle clean check selfTest
```

GitHub Actions runs the **Build and Checkstyle** verification job for pushes and pull requests targeting `main`. The project includes a `.github/CODEOWNERS` file for reviews; branch protection and approval requirements must be enabled in the GitHub repository's settings.

## Team workflow

The GitHub repository may retain the name **AI-Fight-Me**, while the game and Gradle project are named **Seeker**.

1. Create a feature branch from the latest `main`.
2. Make and test your changes locally.
3. Push your branch and open a pull request into `main`.
4. Make sure Gradle/Checkstyle checks pass.
5. Request the project owner's review before merging, according to the repository ruleset.

Do not commit generated `.gradle/`, `build/`, `bin/`, or IDE metadata files.

## Graduate-level evaluation

The final analysis will compare Minimax and Alpha-Beta at identical depths and game states, measure search-node savings and execution times, test different arenas and difficulties, and discuss limitations such as search complexity and heuristic evaluation quality.

## Keyboard focus and Hold

- **Space** performs **Hold**, not Reset Match. Game buttons do not capture keyboard focus, so Space still works after clicking them.
- Reset Match and New Random Arena remain on-screen controls.
- **Hide has been removed** for both the human and the AI. Rocks still block movement, and Defend reduces incoming normal-attack damage.
