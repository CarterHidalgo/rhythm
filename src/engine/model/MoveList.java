package engine.model;

public class MoveList {
    private short[] moves = new short[218];
    private int size = 0;

    public final void add(short move) {
        moves[size++] = move;
    }

    public final short get(int index) {
        return moves[index];
    }

    public final int size() {
        return size;
    }

    public final void clear() {
        size = 0;
    }

    @Override
    public final String toString() {
        if(size == 0) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();

        for(int i = 0; i < size-1; i++) {
            result.append(Move.getAlgebraic(moves[i]) + " ");
        }
        result.append(Move.getAlgebraic(moves[size - 1]));

        return result.toString();
    }
}
