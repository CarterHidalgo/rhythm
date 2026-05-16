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
    private int halfmoves;
    private short move;

    public GameState(short move) {
        this.from = Move.getFrom(move);
        this.piece = Board.get(this.from);
        this.to = Move.getTo(move);
        this.target = Board.get(this.to);
        this.move = move;
        this.halfmoves = GameInfo.getHalfmoves();
    }

    public final int getFrom() {
        return this.from;
    }

    public final int getPiece() {
        return this.piece;
    }

    public final int getTo() {
        return this.to;
    }

    public final int getTarget() {
        return this.target;
    }

    public final int getHalfmoves() {
        return this.halfmoves;
    }

    public final short getMove() {
        return this.move;
    }
}
