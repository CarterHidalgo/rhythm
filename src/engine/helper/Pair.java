package engine.helper;

import java.util.List;

public class Pair implements Comparable<Pair> {
    private List<String> moves;
    private String move;
    private int nodes;

    public Pair(List<String> moves, int nodes) {
        this.moves = moves;
        this.nodes = nodes;
    }

    public Pair(String move, int nodes) {
        this.move = move;
        this.nodes = nodes;
    }

    public List<String> getMoves() {
        return this.moves;
    }

    public String getMove() {
        return this.move;
    }

    public int getNodes() {
        return this.nodes;
    }

    @Override
    public String toString() {
        return "(" + this.move + ", " + this.nodes + ")";
    }

    @Override
    public int compareTo(Pair o) {
        if(this.move != null) {
            return this.move.compareTo(o.getMove());
        } else {
            return this.compareTo(o);
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }

        if(!(o instanceof Pair)) {
            return false;
        }

        Pair p = (Pair) o;

        return this.move.equals(p.move) && nodes == p.getNodes();
    }
}
