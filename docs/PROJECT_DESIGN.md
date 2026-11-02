# Seeker Project Design

## Game flow

1. The human takes a turn using movement or a tactical action.
2. The same resulting game state is sent to Minimax and Alpha-Beta.
3. Both algorithms use the same legal actions and evaluation function.
4. Alpha-Beta's selected action controls the AI.
5. If movement is needed, A* uses Manhattan distance to find a path.
6. The single two-line graph shows nodes visited by both algorithms at each AI turn.

## Main AI algorithms

### A*
A* is used for grid movement. Seeker uses only Manhattan distance because movement is limited to up, down, left, and right.

### Minimax
Minimax is the baseline adversarial search. It explores future game states without pruning.

### Alpha-Beta
Alpha-Beta searches the same game tree but skips branches that cannot change the final decision. The gameplay AI uses this result.

## Combat

- Human normal attack: random 5–15 damage.
- AI normal attack: difficulty-dependent (Easy 5–12, Medium 8–20, Hard 12–24).
- Search simulation uses fixed expected damage so comparisons remain deterministic.
- Defend reduces damage.
- Hide can be used beside an obstacle and reduces incoming damage.
- Every 5 human normal attacks unlocks a Power Strike for guaranteed 15 damage.

## AI evaluation

The AI considers:

- AI health vs human health
- distance to the human
- immediate attack opportunities
- the human's Power Strike charge
- defensive state
- hidden/cover state
- nearby obstacles
- available movement options

## Graduate-level comparison

The program records:

- Minimax action
- Alpha-Beta action
- nodes visited
- runtime
- Alpha-Beta cutoffs
- pruned branches
- Alpha-Beta efficiency percentage
- whether both algorithms selected the same action

## Difficulty and experimental consistency

- Easy: depth 2, AI damage 5–12. Medium: depth 3, AI damage 8–20. Hard: depth 4, AI damage 12–24.
- Human normal attacks always do random 5–15 damage and Power Strike always deals 15.
- The difficulty dropdown is editable until the first human action; afterward it locks until Reset Match or New Random Arena.
- The selected difficulty is a field in each immutable `GameState`, so simulated and real game transitions keep it.
- Both Minimax and Alpha-Beta use that difficulty's exact same search depth.
- Each CSV comparison row records the difficulty to avoid mixing experiment conditions.
