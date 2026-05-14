/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Data structure to store game state information to help reset during unmake
 */

package engine.model;

public class GameState {
    private int from;
    private int piece;
    private int to;
    private int target;

    public GameState(short move) {
        this.from = Move.getFrom(move);
        this.piece = Board.get(this.from);
        this.to = Move.getTo(move);
        this.target = Board.get(this.to);
    }

    public int getFrom() {
        return this.from;
    }

    public int getPiece() {
        return this.piece;
    }

    public int getTo() {
        return this.to;
    }

    public int getTarget() {
        return this.target;
    }
}
