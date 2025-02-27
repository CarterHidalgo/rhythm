/*
 * Name: FEN.java
 * Author: Pigpen
 * 
 * Purpose: Provides methods for safely parsing data out of a FEN string; if no value is found a default is provided
 */

package engine.helper;

public class FEN {
    public static final String[] COMMON_FENS = {
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", // [0] starting position
            "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1" // [1] kiwipete
    };

    public static final String START_FEN = COMMON_FENS[1];

    private StringBuilder fen;
    private StringBuilder turn;
    private StringBuilder castling;
    private StringBuilder ep;
    private int halfmoves = 0;
    private int fullmoves = 1;

    public FEN(String fen) {
        String[] fields = fen.split(" ");

        if (fields.length > 0) {
            this.fen = new StringBuilder(fields[0]);
        } else {
            this.fen = new StringBuilder("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR");
        }

        if (fields.length > 1) {
            this.turn = new StringBuilder(fields[1]);
        } else {
            this.turn = new StringBuilder("w");
        }

        if (fields.length > 2) {
            this.castling = new StringBuilder(fields[2]);
        } else {
            this.castling = new StringBuilder("KQkq");
        }

        if (fields.length > 3) {
            this.ep = new StringBuilder(fields[3]);
        } else {
            this.ep = new StringBuilder("-");
        }

        if (fields.length > 4) {
            this.halfmoves = Integer.valueOf(fields[4]);
        } else {
            this.halfmoves = 0;
        }

        if (fields.length > 5) {
            this.fullmoves = Integer.valueOf(fields[5]);
        } else {
            this.fullmoves = 1;
        }
    }

    public String getFEN() {
        return fen.toString();
    }

    public String getTurn() {
        return turn.toString();
    }

    public String getCastling() {
        return castling.toString();
    }

    public String getEp() {
        return ep.toString();
    }

    public int getHalfmoves() {
        return halfmoves;
    }

    public int getFullmoves() {
        return fullmoves;
    }

    public String get() {
        return fen.toString() + " " + turn.toString() + " " + castling.toString() + " " + ep.toString() + " "
                + halfmoves + " " + fullmoves;
    }
}
