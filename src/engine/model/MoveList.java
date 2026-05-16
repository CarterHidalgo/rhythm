package engine.model;

public class MoveList {
    short[] moves = new short[256];
    short reset = 0;
    int size = 0;

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
        StringBuilder result = new StringBuilder();

        for(int i = 0; i < size-1; i++) {
            result.append(Move.getAlgebraic(moves[i]) + " ");
        }
        result.append(Move.getAlgebraic(moves[size - 1]));

        return result.toString();
    }
}
