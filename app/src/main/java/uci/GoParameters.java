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
    private final Boolean infinite;

    public GoParameters(
        Optional<Integer> whiteTime, Optional<Integer> whiteTimeIncrement,
        Optional<Integer> blackTime, Optional<Integer> blackTimeIncrement,
        Optional<Integer> movesToGo, Optional<Integer> depth, Optional<Integer> nodes,
        Optional<Integer> mate, Optional<Integer> moveTime, Boolean infinite
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
        this.infinite = infinite;
    }

}