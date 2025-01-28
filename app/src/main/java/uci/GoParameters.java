/*
 * Name: GoParameters.java
 * Author: FrequentlyMissedDeadlines (https://github.com/FrequentlyMissedDeadlines/Chess-UCI) Thanks! =]
 * 
 * Purpose: Contains parameters and getters for the UCI 'go' command
 */

package uci;

import java.util.Optional;

public class GoParameters {
    private final Optional<Integer> whiteTime;
    private final Optional<Integer> whiteTimeIncrement;
    private final Optional<Integer> blackTime;
    private final Optional<Integer> blackTimeIncrement;
    private final Optional<Integer> movesToGo;
    private final Optional<Integer> depth;
    private final Optional<Integer> nodes;
    private final Optional<Integer> mate;
    private final Optional<Integer> moveTime;
    private final Optional<Integer> perft;
    private final Boolean infinite;

    public GoParameters(
        Optional<Integer> whiteTime, Optional<Integer> whiteTimeIncrement,
        Optional<Integer> blackTime, Optional<Integer> blackTimeIncrement,
        Optional<Integer> movesToGo, Optional<Integer> depth, Optional<Integer> nodes,
        Optional<Integer> mate, Optional<Integer> moveTime, Optional<Integer> perft, Boolean infinite
    ) {
        this.whiteTime = whiteTime;
        this.whiteTimeIncrement = whiteTimeIncrement;
        this.blackTime = blackTime;
        this.blackTimeIncrement = blackTimeIncrement;
        this.movesToGo = movesToGo;
        this.depth = depth;
        this.nodes = nodes;
        this.mate = mate;
        this.moveTime = moveTime;
        this.perft = perft;
        this.infinite = infinite;
    }

    public Optional<Integer> getWhiteTime() {
        return whiteTime;
    }

    public Optional<Integer> getWhiteTimeIncrement() {
        return whiteTimeIncrement;
    }

    public Optional<Integer> getBlackTime() {
        return blackTime;
    }

    public Optional<Integer> getBlackTimeIncrement() {
        return blackTimeIncrement;
    }

    public Optional<Integer> getMovesToGo() {
        return movesToGo;
    }

    public Optional<Integer> getDepth() {
        return depth;
    }

    public Optional<Integer> getNodes() {
        return nodes;
    }

    public Optional<Integer> getMate() {
        return mate;
    }

    public Optional<Integer> getMoveTime() {
        return moveTime;
    }

    public Optional<Integer> getPerft() {
        return perft;
    }

    public Boolean getInfinite() {
        return infinite;
    }
}