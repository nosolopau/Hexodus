# H-Search Optimization Variants Experiment

## Summary

Three optimization ideas were implemented behind runtime flags and benchmarked
against the baseline with a deterministic scaffold:

| Variant | Idea | Result |
|---|---|---|
| `bitpath` | Represent `Path` as a 128-bit mask (two longs) instead of `ArrayList<Cell>` | **~5-7.5x faster** — the clear winner |
| `reuse` | Reuse scratch buffers instead of allocating per evaluation | Neutral (±1%) |
| `presort` | Order root moves by shallow evaluation before full search | Slower at depth 2, **~12% faster at depth 3** (on top of bitpath) |

All variants produce **bit-identical evaluation values** to the baseline
(verified position-by-position on 5x5, 7x7 and 9x9 boards).

## The Variants

All flags live in `src/heuristics/OptConfig.java` and default to **off**, so
default behavior is unchanged. Set them before creating any
`Heuristic`/`Simulation` object.

### 1. `OptConfig.USE_BITPATH` — bitmask paths

The innermost H-search loop is dominated by `Path` set operations:
`hasEmptyIntersection`, `contains`, `union`, `intersection`, `equals`
(the last one called by `Route.add` for de-duplication, previously an
O(n²) list scan). A path becomes a 128-bit mask held in two primitive
`long` fields (`maskLo` for ids 0-63, `maskHi` for 64-127) — two fields
rather than a `long[]` so no array allocation comes back per Path — and
each operation becomes a couple of bitwise instructions:

- `hasEmptyIntersection` → `(aLo & bLo) == 0 && (aHi & bHi) == 0`
- `union` → `|` on both words
- `contains` → bit test on the right word
- `equals` → `==` on both words
- `getLength` → `Long.bitCount(lo) + Long.bitCount(hi)`

It also eliminates the per-Path `ArrayList` allocation entirely. 128 bits
cover boards up to 11x11 (121 squares + 4 borders = 125 ids), i.e. the
standard Hex board; a larger board fails loudly in `Path.add` instead of
silently colliding bits. Measured cost of the second word vs the original
single-long prototype: none (5x5 level 2: 862ms vs 839ms, within noise).
Limitation: paths are no longer enumerable (`getIterator` returns an empty
iterator), which only affects the debug display helpers.

### 2. `OptConfig.USE_REUSE` — scratch buffer reuse

Per-thread scratch buffers (`src/heuristics/Scratch.java`) replace the
per-evaluation allocations: the `connections.clone()` of the full
`Route[][]` matrix (recycling Route objects through a pool), the `M`/`N`/`B`
conductance matrices (cached by node count), the dead `C[i]` allocation in
the Simulation constructor, and the expiring-list clone in the H-search loop.

### 3. `OptConfig.USE_ROOT_PRESORT` — shallow-eval root ordering

At the root of the alpha-beta search, instead of ordering moves by killer
moves + proximity, every candidate move gets a cheap depth-0 evaluation and
moves are sorted best-first. Costs F extra leaf evaluations (F = free cells)
to improve cutoffs in the F subtrees below.

## Benchmark Scaffold

- `test/heuristics/OptimizationBenchmark.java` — times `chooseMove` over a
  **fixed scripted game** (cells ordered center-out), so every variant
  evaluates exactly the same positions. Self-play was rejected because
  decided positions trigger a `Math.random()` fallback in `chooseMove`,
  making runs non-comparable. Includes JIT warmup and repetitions; reports
  wall time and leaf-eval counts (`OptConfig.evalCount`).

  ```
  java heuristics.OptimizationBenchmark <variant> [dim] [level] [moves] [reps]
  # variant: baseline | all | comma-list of bitpath,reuse,presort
  ```

- `test/heuristics/OptimizationEquivalence.java` — replays scripted
  positions under every flag combination and verifies `calculateValue` is
  identical to baseline.

## Results

Machine: Apple Silicon Mac, single-threaded engine (`SingleThread`), swap off.
Median of reps, leaf evals per game in parentheses.

### 5x5, level 2, 10 scripted moves, 3 reps

| Variant | Median | vs baseline | Leaf evals |
|---|---|---|---|
| baseline | 4510 ms | — | 536 |
| **bitpath** | **839 ms** | **5.4x faster** | 536 (identical) |
| reuse | 4562 ms | ~1% slower (noise) | 536 (identical) |
| presort | 5978 ms | 32% slower | 571 (≈390 search + ≈180 presort) |
| bitpath+reuse | 864 ms | 5.2x faster | 536 |

### 5x5, level 3, 6 scripted moves, 2 reps

| Variant | Median | vs baseline | Leaf evals |
|---|---|---|---|
| baseline | 25440 ms | — | 1406 |
| bitpath | 4325 ms | 5.9x faster | 1406 (identical) |
| presort | 21980 ms | 14% faster | 1237 (12% fewer) |
| **bitpath+presort** | **3820 ms** | **6.7x faster** | 1237 (12% fewer) |

### 7x7, level 2, 4 scripted moves, 1 rep

| Variant | Time | vs baseline | Leaf evals |
|---|---|---|---|
| baseline | 93481 ms | — | 522 |
| **bitpath** | **12448 ms** | **7.5x faster** | 522 (identical, same chosen moves) |

The bitpath speedup grows with board size: paths are longer on bigger
boards, so the O(path-length) list scans it replaces cost proportionally
more.

### 9x9, level 1, 3 scripted moves, 1 rep (high mask word exercised)

Cell ids reach 84 on 9x9, so this run exercises the `maskHi` word.

| Variant | Time | vs baseline | Leaf evals |
|---|---|---|---|
| baseline | 35162 ms | — | 159 |
| **bitpath** | **7201 ms** | **4.9x faster** | 159 (identical, same chosen moves) |

### Follow-up: locality move ordering (`localorder`)

`OptConfig.USE_LOCAL_ORDERING` sorts candidates at every tree level by
distance to the nearest stone (killers still first, distance to the last
move as tiebreak) instead of distance to the last move only. Ordering
only — every legal move is still searched.

| Configuration | Without | With localorder | Leaf evals |
|---|---|---|---|
| 5x5 L2 (no bitpath) | 5077 ms | 4622 ms (9% faster) | 536 → 551 |
| 5x5 L2 + bitpath | 893 ms | 969 ms (wash/slower) | 536 → 551 |
| 5x5 L3 + bitpath | 4507 ms | **3877 ms (14% faster)** | 1406 → 1264 |
| 7x7 L2 + bitpath | 11034 ms | **14972 ms (36% SLOWER)** | 522 → 656 |

**Verdict: not adopted.** It matches presort's depth-3 gain (3877 vs
3820 ms) but is clearly worse on 7x7, where distance-to-any-stone
front-loads cells near old, strategically dead stones while the existing
proximity-to-last-move ordering already points at the live action. Chosen
moves were identical or equal-valued in all runs.

**Notable side-finding:** root scores are NOT bit-identical across
orderings (differences up to ~0.7% at a few positions, e.g. 0.679095 vs
0.681934). In theory alpha-beta's root value is ordering-invariant; here
the evaluation itself is slightly order-sensitive, because the board graph
mutates through simulate/restore cycles (neighbor-list order depends on
history) and H-search combines that iteration order with the 20-path cap
per Route and path expiration. This is pre-existing behavior, not
introduced by localorder — bitpath (which preserves visit order exactly)
reproduces baseline scores to the last bit, and each ordering reproduces
its own scores deterministically across reps.

### Phase profiling (where the time goes)

Nanosecond timers around each phase of the evaluation pipeline
(`OptConfig.ns*`, reported by the benchmark's PHASE BREAKDOWN):

| Phase | baseline 5x5 L2 | bitpath 5x5 L2 | bitpath 7x7 L2 |
|---|---|---|---|
| **H-search loop (AND/OR rules)** | **98.2%** | **90.1%** | **97.5%** |
| Sim build (graph surgery) | 0.3% | 1.9% | 0.2% |
| Eval setup (copy connections + G) | 0.3% | 1.1% | 0.1% |
| Matrix build | 0.2% | 1.3% | 0.8% |
| Gauss solve | 0.1% | 0.7% | 0.2% |
| Restore (undo surgery) | 0.2% | 1.2% | 0.1% |
| Other (sort, book, misc) | 0.9% | 3.8% | 1.1% |

**Conclusion: the H-search generation loop is 90-98% of all time in every
configuration.** This settles several open questions at once: a faster or
incremental matrix solve is worthless (solve is under 1%), buffer reuse
could never have mattered (setup is ~1%, consistent with the neutral
`reuse` result), and the only single-threaded optimization with real
headroom is restructuring the H-search loop itself (the dirty-pair
worklist: only re-examine cell triples touching a connection discovered in
the previous generation). Parallelism is the other lever, since it
multiplies whatever the loop costs.

### Path-cap experiment (`capN`)

`Route` historically stores at most 20 alternative paths per cell pair
(now configurable via `OptConfig.maxPathsPerRoute`; benchmark token
`capN`). Since the H-search inner loop combines paths pairwise, the cap
bounds its cost quadratically.

| Cap | 5x5 L2 | 5x5 L3 | 7x7 L2 | Scores vs cap 20 |
|---|---|---|---|---|
| 20 (default) | 870 ms | 4500 ms | 11413 ms | — |
| 10 | 853 ms | 4469 ms | 11918 ms | **bit-identical everywhere** |
| 5 | 592 ms (-32%) | 2846 ms (-37%) | 5662 ms (-50%) | drift 1.5-14% |

**Findings:**

1. **Cap 10 never binds** — identical values, identical times. Routes in
   practice hold well under 10 paths, so the historic 20 was never the
   active constraint and lowering it to 10 is free but useless.
2. **Cap 5 binds hard**: up to 2x faster on 7x7, but evaluation genuinely
   changes (one 7x7 position shifted 14%). Win/loss detections were
   unchanged in these runs, but adopting cap 5 would require self-play
   strength testing, not just benchmarks. Not adopted.
3. Typical route sizes are therefore ~5-10 paths; the dominant H-search
   cost is the sheer number of (triple x generation) visits, not
   overstuffed routes — further confirmation that the dirty-pair worklist
   is the right structural fix.

**Possible follow-up:** a smarter cap policy — keep maximally *disjoint*
paths instead of the first N found. The OR rule only needs non-overlapping
alternatives, so 5 diverse paths may preserve evaluation quality where 5
near-duplicates do not, making a low cap safe.

### Dirty-skip experiment (`dirtyskip`)

`OptConfig.USE_DIRTY_SKIP`: each Route keeps an exact "contains a new
path" flag (recomputed at each generation start, set on insert), and the
triple loop skips the pair scan when neither route is flagged — the AND
rule needs a new ingredient, so the skip is provably observation-free and
results are bit-identical (verified).

| Config | Without | With dirtyskip | Triples skipped |
|---|---|---|---|
| 5x5 L2 + bitpath | 1178 ms | 1048 ms (-11%) | 33.8% |
| 5x5 L3 + bitpath | 5917 ms | 5877 ms (wash) | 35.1% |
| 7x7 L2 + bitpath | 14954 ms | 16208 ms (+8% SLOWER) | 51.9% |

**Verdict: not adopted (kept as a flag).** Two reasons:

1. Evaluations only run ~2-3 generations, so a large share of triples are
   visited while most paths are genuinely new — there is less "clean
   rescan" waste than the frontier model assumed.
2. More importantly, **bitpath already made rejecting a clean pair nearly
   free** (three mask operations), so the work the skip avoids is tiny,
   while the skip adds per-triple flag checks, counters, and a
   per-generation flag refresh. Pre-bitpath this optimization would have
   been a win; post-bitpath the remaining H-search time must live in the
   pairs that FIRE (path unions, Route.add de-dup, OR-rule recursion with
   cloneWithoutPath, iterator churn) — not in scanning clean ones.

**Implication:** the next profiling step is method-level (e.g. Java
Flight Recorder) to split the 90% H-search slice into scan machinery vs
rule application, before investing in a stage-2 worklist that would
attack the (apparently small) scan share.

### Method-level profiling and the lean OR rule (`leanor`) — ADOPTED

Java Flight Recorder (2615 samples, 7x7 L2 + bitpath) split the 90%
H-search slice by method:

| Method | Self | Inclusive |
|---|---|---|
| AplicarReglaOR (OR-rule recursion) | 10% | **54%** |
| Route.cloneWithoutPath | 1.5% | **40%** |
| Route.add (+ Path.equals dedup) | 17% + 18% | 36% |
| calculateResistance (scan machinery) | 32% | — |

The OR rule clones the route at every recursion level, and the clone
rebuilt via add() — an equals-dedup scan plus a hasDirectPath scan per
path, then an ArrayList.remove. All wasted: the source route is already
de-duplicated. `OptConfig.USE_LEAN_OR` copies the path list directly
(skipping the excluded path by identity, exactly as ArrayList.remove
matched it) and caches the direct-path flag on Route.

| Config | bitpath | bitpath+leanor | |
|---|---|---|---|
| 5x5 L2 | 918 ms | 648 ms | -29% |
| 5x5 L3 | 4672 ms | 3534 ms | -24% |
| 7x7 L2 | 13051 ms | 8136 ms | **-38%** |

Bit-identical values (verified 5x5 and 7x7), identical eval counts,
faster everywhere — **adopted, on by default** (independent of the
algorithm selector; benchmarks pass `baseline` to measure the historic
behavior). This also explains the dirty-skip failure: the time was never
in scanning clean triples but in the OR-rule's support structures.

### Strength comparison (full games, all four engine combinations)

`test/heuristics/StrengthComparison.java` plays complete self-play games
through the real game stack (Match, win detection) for the four selector
combinations (OO / Bitmap x lean-OR off/on) at each level, recording
moves, root scores, winner and random-fallback moves.

- **Level 1:** all four combinations play the identical 13-move game,
  identical scores, identical winner. Only time differs (2465 -> 788 ms).
- **Level 3:** all four play the identical first **17 moves** with
  identical scores, diverging only at move 18 — exactly where the first
  random fallback triggers. Same winner. Times 58.1s / 42.4s / 10.8s /
  7.6s (7.7x for the shipped combo).
- **Level 2:** all four diverge from move 2 — again exactly at the first
  fallback, which at this level fires immediately.

**Conclusion: the four combinations are equally strong.** Divergence is
entirely caused by a pre-existing (2007) behavior: when every move at the
root fails to improve the search bounds — i.e. H-search proves the
position decided (all replies evaluate to Infinity for the losing side) —
`chooseMove` plays a **random** move. From that point on, the losing side
plays random cells for the rest of the game.

This is also why stronger settings can LOOK weaker: at level 1 the search
cannot prove the win, so both sides play sensible moves; at level 2 the
defender's eval horizon ends on the opponent's reply (even depth), the
loss is proven from move 2, and the defender flails immediately. Perceived
"weak play" is the random fallback, not the optimizations.

**Fix (ADOPTED): smart fallback.** `OptConfig.USE_SMART_FALLBACK` (on by
default) replaces the random choice: when no move improves the search
bounds, the engine ranks the free cells by the player's OWN resistance
(`Simulation.calculateOwnResistance`) and plays the most connective one —
the losing side keeps building its best threat instead of playing
randomly. (The full evaluation is Infinity for every move in a proven-lost
position, so own-resistance is the discriminating metric.) Applied to both
SingleThread and MultiThread; benchmarks disable it to keep timings
comparable to the historic engine.

**Verification after the fix:** all four engine combinations now play
IDENTICAL, fully deterministic games at every level — 13 moves at level 1,
11 at level 2, 19 at level 3 — with zero random fallbacks, and the level-2
"defense" changed from random corners to contact and blocking moves.
Shipped-combo full-game times: 275 ms (L1), 1013 ms (L2), 7329 ms (L3) vs
2288 / 6823 / 45974 ms for the historic OO engine.

## Interpretation

1. **The H-search loop is compute-bound on Path set operations, not
   allocation-bound.** `bitpath` alone gives ~5-6x; `reuse` on top of it
   adds nothing measurable. The JVM's allocator/GC handles the object churn
   fine at this board size; it was the O(path-length) list scans (and the
   O(n²) `Route.add` de-dup) that hurt.

2. **Presort is a depth trade-off.** At level 2 the subtrees below each root
   move are too small for better cutoffs to repay F extra evaluations. At
   level 3 it cuts total leaf evals by ~12% net of its own cost. Expert
   mode (level 3) benefits; normal mode (level 2) should keep
   proximity+killer ordering.

3. **Recommended combination:** `bitpath` always (boards ≤ 11x11, i.e. all
   practical sizes), plus `presort` only when level ≥ 3.

## Future Work

- More mask words (3-4 longs, or `long[]`) to support bitpath beyond
  11x11 — academic for now, since H-Search's own O(n³) generation loop is
  the wall at those sizes.
- Transposition table keyed by Zobrist hash: identical positions recur in
  the alpha-beta tree and each one currently re-runs the full H-search.
- Dirty-pair worklist for H-search: instead of re-scanning all O(n³)
  (g, g1, g2) triples every generation, only process triples touching a
  connection discovered in the previous generation.
- Scratch-based Gaussian solve: `Matrix.solve` still copies the matrix and
  allocates internally (only relevant if allocation ever becomes visible).
- Candidate-move pruning: only consider free cells within distance k of an
  occupied cell, cutting the branching factor directly.

## Date

August 29, 2026
