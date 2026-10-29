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
- AI normal attack damage is randomized from **8 to 20**
- After **6 human normal attacks**, a **Power Strike** button unlocks
- Power Strike deals a guaranteed **15 damage** and then resets the charge
- One live graph showing **Alpha-Beta efficiency compared with Minimax**
- Live search statistics and CSV result logging
- 200-turn match limit
- Dark Swing interface

## Controls

- `W` — move up
- `A` — move left
- `S` — move down
- `D` — move right
- `F` — normal attack
- `G` — Power Strike after 6 normal attacks
- `E` — defend
- `H` — hide beside cover
- `R` — A* Move
- `X` — retreat
- `Space` — hold position

## Damage rules

Normal attack damage is randomized only during the real game:

- Human: 5–15 damage
- AI: 8–20 damage

Minimax and Alpha-Beta use fixed expected damage values while simulating future states. This keeps both algorithms deterministic and makes their comparison fair.

Defending and hiding can reduce incoming damage.

## Power Strike

Every successful human normal attack adds one charge. After 6 attacks:

- the Power Strike button becomes available,
- it deals exactly 15 damage,
- and the charge resets to 0 after use.

## Live efficiency graph

The old separate nodes and runtime graphs are removed. Seeker now shows one live efficiency graph.

The graph displays how much node-search work Alpha-Beta saves compared with Minimax:

`efficiency = (Minimax nodes - Alpha-Beta nodes) / Minimax nodes × 100`

A higher percentage means Alpha-Beta avoided more search work while preserving the same decision.

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
