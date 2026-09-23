You are reviewing an unfinished Java chess engine.

Your job is to perform a thorough code review of the existing implementation and produce an actionable plan for completing and improving the engine.

## Primary goals

Evaluate the codebase for the following items (if present):

1. **Correctness**

   * Board representation and state management
   * Move generation for every piece
   * Legal vs. pseudo-legal moves
   * Check, checkmate, and stalemate detection
   * Castling
   * En passant
   * Pawn promotion
   * Captures
   * Side-to-move handling
   * Make/unmake move logic
   * Detection of illegal moves
   * FEN parsing/generation, if present
   * Any other chess-rule edge cases

2. **Engine architecture**

   * Separation of responsibilities
   * Board/game-state design
   * Move representation
   * Move generation architecture
   * Search architecture
   * Evaluation architecture
   * Transposition-table design, if present
   * Time management, if present
   * UCI/CLI integration, if present
   * Extensibility and testability

3. **Search and playing strength**
   If search code exists, review:

   * Minimax / negamax correctness
   * Alpha-beta pruning
   * Quiescence search
   * Move ordering
   * Iterative deepening
   * Transposition tables
   * Repetition/draw handling
   * Mate scoring
   * Evaluation function
   * Search termination and time controls
   * Potential horizon, null-move, or other search issues

4. **Java-specific issues**
   Look for:

   * Incorrect equality/hashCode behavior
   * Mutable-state bugs
   * Unnecessary object allocation
   * Excessive copying
   * Boxing/unboxing
   * Collections used in performance-critical paths
   * Concurrency/thread-safety issues
   * Exceptions and error handling
   * Poor encapsulation
   * API/design problems
   * Code that is difficult to test or reason about

5. **Performance**
   Identify likely bottlenecks, especially in:

   * Move generation
   * Make/unmake
   * Search
   * Evaluation
   * Board copying
   * Allocation/GC pressure
   * Repeated calculations

   Distinguish between performance problems that matter now and optimizations that should wait until correctness is established.

6. **Testing**
   Assess the existing tests and identify missing coverage.

   In particular, determine whether the engine should have tests for:

   * Perft positions
   * Legal move generation
   * Check/evasion positions
   * Castling
   * En passant
   * Promotion
   * Checkmate/stalemate
   * FEN round-tripping
   * Make/unmake reversibility
   * Search correctness
   * Regression cases

## Important review rules

* **Do not assume the existing architecture is correct.**
* Trace important execution paths through the actual code before making recommendations.
* Do not recommend rewriting components merely because you would personally design them differently.
* Prioritize concrete correctness issues over stylistic preferences.
* Separate **bugs**, **design weaknesses**, **performance concerns**, and **optional improvements**.
* When identifying a problem, reference the relevant file/class/method and explain why it is a problem.
* If you are uncertain whether something is a bug, explicitly say so and explain what would need to be verified.
* Do not make broad claims without tying them to the code.
* Preserve working functionality wherever practical.
* Assume the goal is a maintainable, correct chess engine rather than merely making the code compile.

## Chess-engine-specific review

Pay particular attention to subtle state bugs. Verify how the implementation handles:

* Side to move
* King locations
* Castling rights
* En-passant target squares
* Halfmove/fullmove counters
* Captured pieces
* Promotions
* Check status
* Pinned pieces
* Discovered checks
* Double check
* King captures
* Moves that expose the moving side's king
* Castling through or into check
* En passant exposing a discovered check
* Repeated positions
* Fifty-move rule
* Insufficient material, if implemented

If the project contains a perft implementation or test suite, inspect it carefully and identify whether it is trustworthy.

## Review process

Before producing the final plan:

1. Inspect the repository structure.
2. Identify the main execution path.
3. Identify the core chess-state representation.
4. Trace move generation.
5. Trace make/unmake or state transitions.
6. Trace legal-move filtering.
7. Trace search/evaluation, if implemented.
8. Inspect existing tests.
9. Identify unfinished/TODO areas.
10. Look for interactions between components where bugs are likely to occur.

Do not stop after finding the first few issues. Build a coherent understanding of the implementation first.

## Final deliverable

Produce a structured review with these sections:

### 1. Executive Summary

Briefly describe:

* What is currently implemented
* What appears unfinished
* The most important technical issues
* The overall state of the engine

Do not give a simplistic overall score.

### 2. Architecture

Describe the current architecture and the responsibilities of the major components. Note architectural strengths and weaknesses.

### 3. Findings

Create a prioritized list using:

* **Critical** — correctness issue that can cause illegal chess behavior, crashes, corrupted state, or fundamentally incorrect search
* **High** — important correctness/design issue that should be addressed before substantial feature work
* **Medium** — meaningful issue but not immediately blocking
* **Low** — cleanup, maintainability, or minor optimization

For each finding include:

* Severity
* File/class
* Method or relevant code location
* Problem
* Why it matters
* Concrete recommendation

### 4. Missing Functionality

List functionality that is incomplete or absent, separating:

* Required for a correct chess engine
* Required for a usable UCI engine
* Search-strength improvements
* Optional enhancements

### 5. Testing Gaps

Explain what should be tested and why. Include specific chess positions or test categories where useful.

### 6. Performance Review

Identify actual or likely bottlenecks. Separate:

* Fix now
* Fix after correctness
* Defer until profiling demonstrates a need

Do not recommend premature optimization.

### 7. Recommended Implementation Order

Create a dependency-aware sequence of work. For example:

1. Fix foundational state/correctness issues
2. Establish reliable move-generation tests/perft
3. Complete legal move handling
4. Stabilize make/unmake
5. Complete search
6. Add evaluation
7. Add UCI/time management
8. Optimize based on profiling

Adjust this order based on what you actually find in the repository.

### 8. Concrete Implementation Plan

For each planned step, specify:

* Files/classes likely affected
* What should change
* Why it should change
* Dependencies on previous steps
* Tests that should be added or updated
* Any risks or compatibility considerations

Do not write the implementation yet.

### 9. Questions / Ambiguities

List anything you need clarified before implementation, but only ask questions that genuinely affect the implementation plan.

## Important constraint

This is a **code review and planning task, not an implementation task**.

Do not edit files, generate a large patch, or start implementing fixes. The desired output is a technically detailed, prioritized plan that I can review.
