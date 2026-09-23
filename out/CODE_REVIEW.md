# Rhythm Chess Engine: Code Review and Completion Plan

## Context
You asked for a review-and-planning pass (see `prompts/1-code-review.md`) on the unfinished Java engine "Rhythm". I read all ~3.4k lines under `src/engine/**` and the perft suite, and traced move generation, make/unmake, legality filtering, UCI parsing and the perft/bisect tooling. **I edited no files.** Findings come from reading the code, not from running it. Each finding has a concrete repro that the first testing step turns into a test.

---

## 1. Executive Summary

**What's implemented:** bitboard board representation (19 bitboards plus a 64-square mailbox), compacted magic bitboards for sliders, pseudo-legal move generation for every piece (including castling, en passant and promotions), legality filtering by make/unmake plus a king-attack test, a packed `short` move encoding, an `int`-packed undo stack, FEN parsing, a UCI loop with `go perft`, and a perft harness (`autoperft`) with a 129-position suite and teacher-engine bisection.

**What's unfinished:** search, evaluation, `bestmove`, `stop`, `ucinewgame`, `setoption`, time management, checkmate/stalemate detection, repetition and fifty-move handling, Zobrist hashing, and automated tests (there's no build file and no unit tests).

**Most important issues:**
- **Move generation looks correct.** The standard suite passes. But **perft can't see state corruption that doesn't change move counts**, and I found several such bugs:
  - Unmaking a promotion leaves a phantom pawn bit.
  - The en-passant state isn't restored on unmake.
  - The fullmove counter drifts.
- **Parsing UCI moves (`Move.create(String)`) guesses flags from square geometry and gets it badly wrong.** Any king step up or down the board is treated as castling. A rook moving two ranks is treated as a double pawn push. Any piece landing on the en-passant square is treated as an en-passant capture. Knight promotion (`n`) isn't recognized. **In a real GUI game, these corrupt the board.**
- **The undo stack is never cleared and holds only 64 entries.** A GUI re-sends `position startpos moves …` before every move, so the engine crashes with `ArrayIndexOutOfBoundsException` within the first few moves of a real game.
- **All game state is global and static, and threads are already in play.** The UCI thread starts before `Model.init()`, and `go` spawns threads. This has to be resolved before a search thread exists.

**Overall:** the move-generation core is solid. The code around it (UCI input, undo-state bookkeeping, FEN output, tooling) has real correctness bugs that perft doesn't catch. Fix those and put tests around them before starting on search.

---

## 2. Architecture

| Component | Responsibility | Notes |
|---|---|---|
| `base/App` | Entry point: starts the UCI thread, then `Model.init()` | Race: UCI can read input before magic tables and board exist |
| `model/Bitboard` | Static `long[19]`: 12 piece boards, 2 color boards, EMPTY, OCCUPIED, 2 PROMO-rank constants, EP; plus rank/file/ray constants | The PROMO and EP entries mix constants with game state in one array |
| `model/Board` | Static `int[64]` mailbox | Kept in sync with the bitboards by hand in `Move.make/unmake` |
| `model/GameInfo` | Static side to move, castling rights, ep, halfmove and fullmove counters | `ep` is set only from FEN and never updated (see C4) |
| `model/StateStack` | Static `int[64]` of packed undo records | Never cleared; fixed size |
| `model/Move` | Move encoding (chessprogramming 16-bit), make/unmake, legality check, UCI string ↔ move | Mixes encoding, board mutation and UCI parsing |
| `model/MoveGeneration` | Pseudo-legal generation, then legal filtering via `Move.isLegal` (make → `King.inCheck` → unmake) | Clear and correct; uses a shared static `temp` list |
| `model/AttackTable`, `King` | Knight/king/pawn attack tables, `squareIsAttacked` | Correct; always reads global occupancy |
| `model/magics/*` | Compacted magic tables (Houdini-style byte index into the unique-attack table) | Correct for the given magics; no collision self-check |
| `model/Model` | Facade: `setPosition`, perft, perft map | |
| `uci/UCI`, `GoParameters` | Command dispatch, regex-based parsing | `go` has no search behind it |
| `helper/*` | FEN, Enum codes, Autoperft/Teacher tooling, Coord, Printer | `Printer` sits in `model` and calls `System.exit` |

**Strengths:** fast, idiomatic bitboard generation (set-wise pawn shifts, magics); compact move and undo encodings; a legality method (full make/unmake) that is simple and handles pins, discovered checks, en-passant discovered checks and double check without special cases; a real perft suite and bisect tooling.

**Weaknesses:**
- **Global static state everywhere.** There's one implicit position. Tests can't build independent positions, a search thread can't own its state, and "ep" has two sources of truth (`GameInfo.ep` and `Bitboard[EP]`).
- **Two representations kept in sync by hand.** Six `toggle` calls per piece movement are scattered across make/unmake, and nothing checks that the two agree.
- **Tooling and debug output are mixed into the engine**, including ANSI colors on stdout and `System.exit` calls.

---

## 3. Findings

### Critical

**C1: The undo stack is never reset and overflows after 64 entries**
- *Where:* `StateStack` (`data = new int[64]`), `Model.setPosition(String)` and `setPosition(String, List)`.
- *Problem:* `StateStack.clear()` is never called anywhere (confirmed by grep). Every `position … moves m1..mN` pushes N more entries on top of the previous ones.
- *Why it matters:* a GUI sends the full move list before every `go`. After about 64 plies in total across commands (roughly the third or fourth `position` of a game), `data[++top]` throws `ArrayIndexOutOfBoundsException`, and that kills the UCI thread (see H2). A single game longer than 64 plies overflows even with clearing.
- *Recommendation:*
  - Clear the stack in `setPosition`.
  - Size it for the longest game plus the maximum search depth (for example 1024), or make it growable.
  - Later, this stack becomes the game history used for repetition detection (see M8).

**C2: `Move.create(String)` infers move flags from geometry and corrupts the board**
- *Where:* `Move.create(String)` (`Move.java:250-295`). It's used by `Model.setPosition(fen, moves)`, which means every UCI `position … moves` command and the bisect tool.
- *Problems, each confirmed by trace:*
  - (a) `self == turn+KING && diff > 1` is true for any king step up, down or diagonally (diff 7, 8 or 9). For example, `e1e2` gets `KING_CASTLE`, and make then toggles rook bitboards on f2/d2 even though no rook is there.
  - (b) `diff == 16` sets `DOUBLE_PAWN` for **any** piece moving two ranks straight, such as a rook or queen `a2a4`. That produces a bogus ep square (a3). A black pawn on b4 can then "capture en passant" on a3, which removes the "pawn" on a4. That's actually the rook, and afterwards `Board` and the bitboards disagree.
  - (c) `to == ep square` sets `EP_CAPTURE` for **any** piece landing there, not just pawns. For example, after `e2e4` a black knight or bishop moving to e3 removes the white pawn on e4.
  - (d) The knight promotion letter is matched as `'k'`. UCI uses `'n'`, so `e7e8n` becomes a quiet move and leaves a pawn on the 8th rank.
- *Why it matters:* perft never goes through this function, since autoperft calls `setPosition(fen)` with no moves. So these bugs are invisible to the current tests, but they appear in normal GUI play.
- *Recommendation:* replace flag inference with matching against the legal move list. Generate legal moves, find the one whose from, to and promotion piece match the string, and reject the input if nothing matches. This removes the whole class of bugs and also validates GUI input.

**C3: Unmaking a promotion leaves a phantom pawn on the promotion square**
- *Where:* `Move.unmake`, promotion branch (`Move.java:161-166`).
- *Problem:* the generic from/to toggle with `piece = pawn` sets the pawn bit on `to`. The branch then clears `self` (the color bitboard, code 0 or 8) instead of `self + PAWN`. Result: the pawn bitboard keeps a bit on the 8th rank (white) or 1st rank (black) after every promotion make/unmake.
- *Why it matters:* perft doesn't notice, because pawns on the last rank generate no pushes or captures and attack nothing on the board. But evaluation (material, pawn structure), Zobrist hashing and any "count pawns" logic will read corrupted data, and the phantoms accumulate during search.
- *Recommendation:* clear `to` on the `self + PAWN` bitboard, or restructure the branch so the promotion undo is symmetric with make. Add the invariant test from §5.

**C4: En-passant state is not restored by unmake**
- *Where:* `GameInfo.ep` (set only in `setWithFEN`, never updated); `StateStack.push` (saves `GameInfo.getEP()`); `Move.unmake` lines 144, 168-170, 182.
- *Problems:*
  - (a) The stack saves the **FEN's** ep square, not the current one.
  - (b) Unmake **ORs** that value into `Bitboard[EP]`. When there's no ep square, that's index 0, so a1 gets set.
  - (c) Unmaking a double push then clears the ep bitboard entirely, wiping anything restored in step (b).
  - (d) Unmaking an ep capture sets the ep bit on the captured pawn's square (rank 4 or 5), not the target square.
- *Why perft passes:* each node generates its pseudo-legal moves before any child make/unmake, and every make clears the ep bitboard first. The corrupted value is never read in perft. The `RANK_3`/`RANK_6` masks in pawn generation also hide the junk bits.
- *Repro now:* `position startpos moves e2e4 d7d5 e4e5 f7f5`, then `go perft 1` twice. The second run is missing `e5f6`. `FEN.create()` also prints `-` for ep after any moves.
- *Why it matters:* iterative deepening regenerates root moves after unmaking, so it will lose the ep capture. Hashing, repetition detection and FEN output will all be wrong.
- *Recommendation:*
  - Keep a single ep square (`int`, -1 for none) in the game state and update it in make.
  - Push the **current** value onto the undo stack and restore it with plain assignment.
  - Derive the ep bitboard from that square (or drop the bitboard).

### High

**H1: The fullmove counter is decremented on the wrong side**
- *Where:* `GameInfo.unmake`.
- *Problem:* make increments when black moves. Unmake tests `turn == BLACK` **before** flipping the side, which means it decrements when white moved. After make/unmake of `e2e4` from the start position, the fullmove counter is 0.
- *Why it matters:* FEN output is wrong, and anything that uses the ply count derived from it is wrong too.
- *Recommendation:* save the fullmove counter on the undo stack or restore it symmetrically. The simplest option is to restore halfmove, fullmove, castling and ep all from the stack (see M4).

**H2: One exception anywhere permanently kills the UCI loop**
- *Where:* `UCI.run`.
- *Problem:* nothing catches exceptions around `commands.get(command).accept(params)`, and several inputs throw:
  - a malformed move string, including an empty move list after `moves`
  - C1's stack overflow
  - `FEN.create()` (M1)
  - `autoperft 130` (M5: division by zero)
  - EOF on stdin (`NoSuchElementException`)
- *Why it matters:* when the thread dies, the engine goes silent and the GUI hangs.
- *Recommendation:*
  - Wrap dispatch in a try/catch that reports the error as `info string` and continues.
  - Handle EOF by treating it as `quit`.

**H3: Global static state is shared across threads, with a startup race**
- *Where:*
  - `App.main` starts `UCI.start()` before `Model.init()`. Only `isready` waits for init; `position` or `go perft` sent early runs before the magic tables exist.
  - `UCI.go` spawns a new `Thread` for each `go`.
  - `MoveGeneration.temp` is a static shared list.
  - `Model.isReady` is written outside the lock.
- *Why it matters:* once search runs on its own thread, a `position` or `ucinewgame` arriving mid-search mutates the same static arrays. There's also no stop flag.
- *Recommendation:*
  - Initialize before starting UCI.
  - Run search on one dedicated worker with a `volatile boolean stop`, and join it before handling `position`, `ucinewgame` or `quit`.
  - Move position state into an instance (see step 4 in §7) so the searcher owns its copy.

**H4: There are no automated tests, no build, and perft checks only node counts**
- *Where:* the repository has no Gradle or Maven file and no JUnit. `Autoperft` runs only through an interactive command.
- *Problem:* perft validates counts only. It doesn't check that state is restored (C3, C4, H1), doesn't exercise UCI move parsing (C2), and doesn't check FEN output (M1).
- *Why it matters:* every Critical finding above slipped past the existing harness.
- *Recommendation:* see §5 and step 0 in §7.

**H5: FEN input is not validated**
- *Where:* `FEN`, `Board.setWithFEN`, `Bitboard.setWithFEN`, `GameInfo.setWithFEN`, `UCI.patternPosition`.
- *Problems:*
  - An invalid piece character makes `stringToCode` return -1, and then `bitboards[-1]` throws.
  - A missing king makes `numberOfTrailingZeros` return 64, and then `KNIGHT_ATTACKS[64]` throws.
  - A castling right without the king or rook on its home square generates castling, and make toggles rook bits that don't exist.
  - A bogus ep square (no capturable pawn) lets an ep capture remove a pawn that isn't there.
  - The UCI regex requires all 6 FEN fields, but `FEN` itself has defaults for missing fields.
- *Recommendation:*
  - Validate the FEN strictly: 8 ranks of 8 squares, exactly one king per side, no pawns on the back ranks, the side not to move is not in check.
  - Sanitize castling rights and the ep square against the piece placement.
  - Accept 4-field FENs.

**H6: ANSI escape codes go to stdout in UCI mode**
- *Where:* `App.main` calls `Printer.white("")`, which prints `ESC[37mESC[37m` with no newline. Colors are also used in perft and bisect output.
- *Why it matters:* the first line the GUI reads becomes `\e[37m\e[37mid name rhythm`. Many GUIs match `startsWith("id")` or `uciok`, so the handshake can fail.
- *Recommendation:* print nothing that isn't UCI on stdout by default. Allow colors only in an explicit debug or CLI mode.

### Medium

- **M1: `FEN.create()` crashes and emits stale fields.** It calls `fen.charAt(fen.length()-1)` on an empty builder when a8 is empty, which throws `StringIndexOutOfBoundsException`. That covers most suite positions, and it breaks `bisect`. Its ep (C4) and fullmove (H1) fields are also wrong. *Fix:* use a standard run-length writer, and read the fields from the real state.
- **M2: Magic tables aren't self-checked and assume global occupancy.**
  - `initLookups` never detects two configurations mapping to the same slot with different attacks. The perft pass suggests the current magics are fine, but nothing enforces it.
  - `getRook/BishopMoves` always read `Bitboard[OCCUPIED]`, so pins, x-rays, SEE and "attacks with this piece removed" can't be computed.
  - *Fix:* verify every configuration after init (or in a test), and add overloads that take an occupancy argument.
- **M3: Castling legality is correct but implicit.**
  - `Move.isLegal` checks the transit squares **after** make, which relies on non-obvious equivalences: the king's origin square stands in for "not in check", and the moved rook doesn't change attacks on d1.
  - `addKingMoves` assumes the king is on e1/e8 whenever the rights are set.
  - *Fix:* check "not in check, transit squares not attacked" before make. That's clearer and cheaper, and it pairs with H5's sanitized castling rights.
- **M4: Halfmove clock bookkeeping.** It's stored in 7 bits, so a FEN clock above 127 is truncated. Unmake sometimes decrements and sometimes restores. *Fix:* restore all irreversible state from the stack, and widen the field or use separate arrays.
- **M5: Autoperft and Teacher tooling bugs.**
  - `TEST_SIZE = 130` but the suite has 129 lines. `autoperft 130` runs zero tests and then divides by `nps.size()==0`.
  - Per-test NPS uses integer seconds + 1.
  - `Teacher()` sets `loaded = true` even when the process failed to start, which leads to a later `NullPointerException`.
  - `waitForMatch` busy-spins, creates a thread on every call, and leaves stale lines in an instance queue.
  - The teacher path is a Windows-only `src/resources/teacher.exe`, and `*.exe` is gitignored.
  - Paths are relative to the working directory.
- **M6: `go` parsing bugs.**
  - `patternPerft` contains `\\\\d`, which is a literal backslash, so `go perft 10` runs **depth 1**.
  - `patternMate` requires a trailing space.
  - Parsing applies 10 regexes to each `go`.
  - *Fix:* tokenize instead.
- **M7: `MoveList` capacity of 218 is the maximum for legal moves.** Pseudo-legal lists can, in contrived positions, exceed it. *Fix:* use 256.
- **M8: There's no position history or hashing.** Repetition, the fifty-move draw and a transposition table all need Zobrist keys plus a history stack.

### Low
- **L1:** `Bitboard.getOp(int code)` returns `bitboards[turn + code]`, which is the side to move's bitboard, not the opponent's. It's unused today but will bite later.
- **L2:** Dead or unused code:
  - `helper/Offset`, `Bitboard.genSliderMasks`, `AttackTable.genPawnAttacks`
  - unused `empty`/`promo` parameters in the knight, bishop, rook, queen and king generators
  - the redundant outer `while (pawns != 0)` loop in the pawn generators (the inner loops drain everything on the first pass)
- **L3:** `MagicBitboardMask.ROOK_BLOCKER_MASKS` is `Long[]` (boxed), so it's unboxed on every rook lookup in the hottest path. It's trivial to fix (see §6).
- **L4:** `Printer` lives in `model` and calls `System.exit(1)` on an inconsistency. Magic init also calls `System.exit`. Throw exceptions instead.
- **L5:** `helper.Enum` shadows `java.lang.Enum`. `Piece.color` returns BLACK for EMPTY and other non-piece codes. `Move.getIndex("-")` returns 0, which is also a1.
- **L6:** `Autoperft` uses `List.removeLast()`, so the code requires Java 21. Document this in the build.

---

## 4. Missing Functionality

**Required for a correct chess engine**
- Checkmate and stalemate detection: no legal moves plus in check, or no legal moves plus not in check.
- Threefold repetition, which needs Zobrist keys and a history stack.
- The fifty-move rule.
- Insufficient material: KK, KNK, KBK and same-colored bishops.
- Validated FEN input (H5) and legal-move-matched UCI input (C2).
- A single source of truth for irreversible state (C4, H1, M4).

**Required for a usable UCI engine**
- A `go` that searches and prints `bestmove`, including `bestmove 0000` or a sensible fallback when there are no legal moves.
- `stop`, and `isready` that answers during search.
- `ucinewgame` that resets the history stack and the transposition table.
- `quit` that stops the search.
- Time management: `wtime`/`btime`/`winc`/`binc`/`movestogo`/`movetime`/`depth`/`nodes`/`infinite`.
- `info depth … score cp|mate … nodes … nps … pv …` output.
- `setoption name Hash`, which is optional but expected.
- stdout that carries only UCI output (H6).

**Search-strength improvements (in order)**
1. Alpha-beta negamax with mate scores adjusted by ply.
2. Quiescence search on captures and promotions.
3. Iterative deepening.
4. MVV-LVA capture ordering, then the PV move.
5. Transposition table.
6. Killer moves and history heuristic.
7. Check extension.
8. Principal variation search (PVS).
9. Null-move pruning, guarded for zugzwang.
10. Late move reductions (LMR).
11. Aspiration windows.
12. Static exchange evaluation (SEE) for ordering and pruning captures.

**Evaluation (none exists)**
1. Material plus piece-square tables, tapered between middlegame and endgame.
2. Then mobility, pawn structure (passed, doubled, isolated), king safety and bishop pair.

**Optional enhancements:** opening book, pondering, MultiPV, Syzygy endgame tables, NNUE, multithreaded search, a `bench` command.

---

## 5. Testing Gaps

Add JUnit 5 tests. Each item says what it catches.

1. **Perft suite as parameterized tests.** Run the shallow depths (≤ ~1M nodes) on every build and put the deep ones behind a `@Tag("slow")`. Keep the standard named positions explicit: start position, Kiwipete, CPW positions 3–6.
2. **Board-state invariant checker**, run after every make and unmake during a perft walk:
   - the `Board` mailbox agrees with the piece bitboards
   - each color bitboard is the union of that color's pieces
   - OCCUPIED is the union of both colors, and EMPTY is `~OCCUPIED`
   - exactly one king per side
   - no pawns on ranks 1 or 8
   - the ep square is empty and sits on rank 3 or 6

   This catches C3 immediately.
3. **Make/unmake reversibility.** Snapshot the full state (all bitboards, board, side, castling, ep, halfmove, fullmove, stack depth) before make, compare after unmake, at every node to depth 3–4 across the suite. This catches C3, C4, H1 and M4.
4. **Move-string round trip.** For every legal move `m` in every suite position, check `parse(toUci(m)) == m`. Add explicit cases:
   - `e1e2` (king step; C2a)
   - `a2a4` with a rook (C2b)
   - a non-pawn moving onto the ep square (C2c)
   - `e7e8n` (C2d)
   - `e1g1` and `e1c1`
   - an ep capture and a capture-promotion
   - illegal and malformed strings, which must be rejected rather than crash
5. **UCI session tests.**
   - Repeated `position startpos moves …` growing past 64 plies (C1).
   - Run `go perft 1` twice on a position with an ep square (C4 repro above).
   - `go perft 10` parsing (M6).
   - EOF and garbage input don't kill the loop (H2).
   - The first stdout line is exactly `id name …` (H6).
6. **FEN.** Parse and emit each suite FEN and check they match. Check the emitted FEN after a sequence of moves (ep, counters). Invalid FENs must be rejected. A 4-field FEN must be accepted.
7. **Rules edge cases** (moves must be exactly right, not just counts):
   - En passant that exposes a rank check: `8/8/8/KPp4r/8/8/8/7k w - c6 0 1` must **not** generate `b5c6`.
   - Double check allows only king moves.
   - Castling out of check, through check and into check.
   - Losing castling rights on a king move, a rook move, or a rook being captured on its corner.
   - All four under-promotions.
8. **Game-end and draw detection.**
   - Checkmate: `rnb1kbnr/pppp1ppp/8/4p3/6Pq/5P2/PPPPP2P/RNBQKBNR w KQkq - 1 3`.
   - Stalemate: `k7/8/1Q6/8/8/8/8/7K b - - 0 1`.
   - Threefold via knight shuffles.
   - Fifty-move at halfmove 100.
   - Insufficient material.
9. **Search.**
   - Finds mate-in-1 and mate-in-2 with correct mate scores.
   - Always returns a legal move.
   - Handles a mated or stalemated root.
   - Respects `stop`, `movetime` and `depth`.
   - Scores a repetition as a draw.
   - Evaluation symmetry: a color-flipped position gets the negated score.
10. **Magic tables.** For every square and every blocker subset, the magic lookup equals slow ray-casting (`createAttack`).

---

## 6. Performance Review

**Fix now** (trivial, and no risk to correctness)
- Change `Long[] ROOK_BLOCKER_MASKS` to `long[]` (L3).

Nothing else is urgent: perft already passes at usable speed.

**Fix after correctness**
- Legal filtering does a full make, check test and unmake for every pseudo-legal move. `squareIsAttacked` also computes **both** rook and bishop magic lookups through `getQueenMoves`, even though the two are then separated by mask. Two fixes:
  - (a) Look up rook and bishop attacks separately.
  - (b) Later, generate legal moves directly with a check mask and pin masks, keeping make/unmake validation only for king moves and en passant.
- Preallocate one `MoveList` per ply in search. Today perft allocates one per node, and `getPerftMap` uses boxed `HashMap<String, Long>` (that one is tooling only, so it's fine).
- Remove the redundant pawn outer loop and the unused parameters (L2).
- For search, incremental Zobrist and evaluation terms updated inside make/unmake.

**Defer until profiling shows a need**
- The compacted magic scheme adds a dependent load (lookup table → offset → attack). Switching to plain fancy magics (one table, one load, about 800 KB) *may* be faster, but measure first.
- Staged move generation, bulk-count tricks, multithreading.
- Replacing the mailbox or unifying the dual representation. Only do this if profiling blames synchronization.

---

## 7. Recommended Implementation Order

0. **Build and test harness.** Nothing else can be verified without it.
1. **Fix irreversible-state bugs:** C4 (ep), C3 (promotion unmake), H1 and M4 (counters), C1 (stack reset and size).
2. **Fix the input boundary:** C2 (parse against legal moves), H5 (FEN validation), M1 (FEN output).
3. **Make UCI robust:** H2 (exception guard, EOF), H6 (clean stdout), M6 (tokenized `go`), H3 part 1 (init before UCI), M5 (tooling).
4. **Encapsulate state:** move the static `Bitboard`/`Board`/`GameInfo`/`StateStack` into a `Position` instance, protected by the tests from steps 0–1. (See Q2.)
5. **Rules completion:** Zobrist hashing plus history, repetition, fifty-move, insufficient material, mate and stalemate detection.
6. **Minimal search:** negamax alpha-beta with mate scores, quiescence, iterative deepening, a search thread with a stop flag, and `bestmove`/`info`.
7. **Evaluation:** material plus tapered piece-square tables.
8. **UCI completeness:** time management, `ucinewgame`, `setoption Hash`.
9. **Strength:** transposition table, move ordering (PV, MVV-LVA, killers, history), then PVS, null move, LMR and aspiration windows. Validate each change with self-play (for example fastchess or cutechess-cli with SPRT).
10. **Profiling-driven optimization** (the §6 items listed after correctness).

---

## 8. Concrete Implementation Plan

### Step 0: Build and test harness
- **Files:** new `build.gradle` (or `pom.xml`; see Q1) using the current `src/` layout: `sourceSets.main.java.srcDirs = ['src']`, resources from `src/resources`, tests in `test/`. Target Java 21. New `test/engine/…` test classes. A test-only `PositionSnapshot` helper that reads the static state.
- **What:** move the perft suite into a JUnit parameterized test. Add the invariant checker (§5 item 2) and the reversibility walk (item 3). Add the move-string round trip (item 4). Load the suite from the classpath instead of a cwd-relative path.
- **Why:** makes the bugs reproducible and guards every later change.
- **Depends on:** nothing.
- **Tests:** these are the tests. Several will fail on purpose, documenting C1–C4 and H1. Mark them `@Disabled("C3")`, or let them fail until step 1.
- **Risk:** none to runtime behavior. The perft suite's deep entries (up to about 200M nodes) must be tagged `slow`.

### Step 1: Irreversible-state correctness
- **Files:** `GameInfo`, `StateStack`, `Move` (make/unmake), `Bitboard` (EP), `Model.setPosition`.
- **What:**
  - Keep a single `epSquare` int (-1 for none) in `GameInfo`, set in make (double push → the square behind; otherwise -1).
  - Have `StateStack.push` store the current ep, castling rights, halfmove clock and captured piece.
  - Have unmake **assign** all of them back: no ORing, no conditional decrement. Restore fullmove symmetrically.
  - Derive the EP bitboard from the ep square, or drop it.
  - Fix the promotion undo to clear `self + PAWN` on `to`.
  - Size the stack to 1024 (or make it growable) and clear it in `setPosition`.
- **Why:** C1, C3, C4, H1, M4.
- **Depends on:** step 0.
- **Tests:** the invariant and reversibility tests pass. The perft suite still passes. The C4 repro gives the same result twice. Add a test that `position startpos moves …` over 100 plies works.
- **Risk:** low, as long as perft plus the reversibility tests guard it.

### Step 2: Input boundary
- **Files:** `Move.create(String)` (replace it with something like `Move.parseUci(String, MoveList legal)`), `Model.setPosition`, `FEN`, `Board/Bitboard/GameInfo.setWithFEN`, `UCI.position`.
- **What:**
  - Parse from, to and promotion (`n`/`b`/`r`/`q`), match against `genLegal`, and throw a descriptive exception if nothing matches.
  - Validate FENs strictly and sanitize castling rights and the ep square.
  - Accept 4-field FENs.
  - Rewrite `FEN.create` with a standard run-length writer that reads the real state.
- **Why:** C2, H5, M1.
- **Depends on:** step 1 (the parser uses genLegal, which needs correct state; FEN output needs a correct ep).
- **Tests:** round trip, the explicit C2 cases, FEN round trip, rejection of invalid FENs.
- **Risk:** stricter FEN parsing may reject a suite line. Run the whole suite through the validator.

### Step 3: UCI robustness and tooling
- **Files:** `UCI`, `App`, `Printer`, `Autoperft`, `Teacher`, `GoParameters`.
- **What:**
  - Call `Model.init()` before `UCI.start()`, or run UCI on the main thread.
  - Wrap dispatch in try/catch and report errors as `info string`.
  - Treat EOF as `quit`.
  - Replace the ANSI/`Printer` output on stdout with plain text, and gate colors behind a debug flag.
  - Tokenize `go` arguments.
  - Fix `TEST_SIZE` (derive it from the file) and the NPS math.
  - Make the Teacher path configurable (for example a `setoption` or a system property pointing at a Stockfish on PATH), make `loaded` reflect the real process state, and replace the busy-wait with blocking reads.
- **Why:** H2, H3 (part 1), H6, M5, M6.
- **Depends on:** step 2 for safe `position` handling.
- **Tests:** UCI session tests (§5 item 5), driving `UCI` with injected in/out streams. That means making `in` and `out` injectable.
- **Risk:** changing stdout output affects anyone relying on colored CLI output. Keep a `debug on` toggle.

### Step 4: Encapsulate position state (recommended; see Q2)
- **Files:** new `model/Position` holding bitboards, board, side to move, castling, ep, clocks, undo stack and history. `Move.make/unmake`, `MoveGeneration`, `AttackTable`, `King`, `MagicBitboard` (add occupancy arguments) and `Model` take a `Position` argument.
- **What:** do it mechanically. Keep the static facade delegating to a default `Position` during the transition so each commit stays green. Turn `MoveGeneration.temp` into a per-position or per-ply buffer.
- **Why:** needed so the search thread owns its state (H3), so tests can build independent positions, and so M2's occupancy-parameter attacks exist. This is driven by concrete needs, not style.
- **Depends on:** steps 0–1, since the tests are the safety net.
- **Tests:** perft plus reversibility, unchanged. Also check that perft speed doesn't regress by more than a few percent.
- **Risk:** the largest diff in the plan. Doing it after step 1 means the invariants are already enforced.

### Step 5: Rules completion
- **Files:** new `Zobrist`, `Position`/`GameInfo` (hash field updated incrementally in make/unmake), `StateStack` (store the hash), and new `GameRules` (`isCheckmate`, `isStalemate`, `isRepetition`, `isFiftyMove`, `isInsufficientMaterial`).
- **What:** incremental Zobrist hashing (pieces, side, castling, ep file), with a test that the incremental hash equals one recomputed from scratch. For repetition, scan back through the history only as far as the halfmove clock goes.
- **Why:** M8 and the correctness items in §4.
- **Depends on:** step 1, since the ep and castling state must be correct before it can be hashed.
- **Tests:** §5 item 8; the incremental hash equals the from-scratch hash at every node of a perft walk.
- **Risk:** hashing the ep square only when a capture is possible is a convention choice. Pick one and test it.

### Step 6: Minimal search
- **Files:** new `search/Search` (negamax, alpha-beta, quiescence, iterative deepening), `search/SearchLimits` (built from `GoParameters`), `search/SearchInfo` (reporting). `UCI.go` and `UCI.stop`.
- **What:**
  - Mate score = `MATE - ply`. Return a draw score for repetition, fifty-move and insufficient material.
  - Check a `volatile stop` flag every N nodes.
  - Run on one worker thread. Always emit `bestmove`: keep the best move from the last completed iteration, or the first legal move as a fallback.
  - Order moves at first by PV move, then MVV-LVA.
- **Depends on:** steps 3–5.
- **Tests:** §5 item 9.
- **Risk:** stopping in the middle of an iteration. Only trust results from completed iterations, or from a partial one that has a PV move.

### Step 7: Evaluation
- **Files:** new `eval/Evaluator`. Material plus tapered piece-square tables, returned from the side to move's perspective.
- **Depends on:** step 6 for integration, but it can be built in parallel.
- **Tests:** color-flip symmetry, known material imbalances.

### Step 8: UCI completeness and time management
- **Files:** `UCI`, `search/TimeManager`.
- **What:** implement `ucinewgame` (reset the transposition table and history), `setoption Hash`, and `info` lines. Budget time as roughly `remaining/movesToGo + inc*k`, with a soft and a hard limit.
- **Tests:** timing tests with `movetime`; a cutechess or fastchess smoke run with no time losses or illegal moves.

### Step 9: Strength
- Transposition table (fixed-size array of longs, replacement by depth or age), killer moves and history heuristic, PVS, null move, LMR, aspiration windows.
- Validate each feature individually with an SPRT self-play run.

### Step 10: Profiling-driven optimization
- The "after correctness" items in §6, driven by a `bench` command (fixed positions and depth, printing nodes and NPS). Its node count also works as a regression checksum.

### Verification (end-to-end)
- `./gradlew test` runs perft (fast depths), invariants, reversibility, round trip, FEN, rules and UCI-session tests.
- `./gradlew test -Pslow` runs the full perft suite.
- Manual: `echo -e "uci\nisready\nposition startpos moves e2e4 d7d5 e4e5 f7f5\ngo perft 1\ngo perft 1\nquit" | java -cp build/classes engine.base.App` prints two identical counts that include `e5f6`, and the first line is a clean `id name rhythm`.
- After step 6: a 100-game fastchess or cutechess match against itself or a weak engine with no illegal moves, crashes or time forfeits.

---

## 9. Questions / Ambiguities
1. **Build tool:** Gradle, Maven, or keep plain `javac` plus a script? The code already needs **Java 21** (`List.removeLast`). Is that the intended minimum?
2. **State refactor (step 4):** are you OK moving from static globals to a `Position` instance? It's the largest structural change. I recommend it before search because of threading and testability, but steps 0–3 don't depend on it.
3. **Teacher engine:** what are `teacher.exe` and its platform? You're on macOS. Should bisect use a configurable path (for example Stockfish on PATH)?
4. **CLI commands:** should `print`, `autoperft` and `bisect` stay in the UCI command loop, or move behind a `debug` mode or a separate entry point?
5. **Goals and constraints:** target strength or time controls, single- versus multi-threaded search, and any rule against dependencies beyond JUnit. These affect step 9 priorities and the transposition table design.
