/*
 * Name: FEN.java
 * Author: Pigpen
 * 
 * Purpose: Provides methods for safely parsing data out of a FEN string; if no value is found a default is provided
 */

package helper;

public class FEN {
    public static final String START_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";

    public static final String[] COMMON_FENS = {
        "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1" // [0] kiwipete
    };

    private StringBuilder fen;
    private StringBuilder turn;
    private StringBuilder castling;
    private StringBuilder ep;
    private int halfmoves = 0;
    private int fullmoves = 1;

    public FEN(String fen) {
        String[] fields = fen.split(" ");

        if(fields.length > 0) {
            this.fen = new StringBuilder(fields[0]);
        } else {
            this.fen = new StringBuilder(START_FEN);
        }

        if(fields.length > 1) {
            this.turn = new StringBuilder(fields[1]);
        } else {
            this.turn = new StringBuilder("w");
        }

        if(fields.length > 2) {
            this.castling = new StringBuilder(fields[2]);
        } else {
            this.castling = new StringBuilder("KQkq");
        }

        if(fields.length > 3) {
            this.ep = new StringBuilder(fields[3]);
        } else {
            this.ep = new StringBuilder("-");
        }

        if(fields.length > 4) {
            this.halfmoves = Integer.valueOf(fields[4]);
        } else {
            this.halfmoves = 0;
        }

        if(fields.length > 5) {
            this.fullmoves = Integer.valueOf(fields[5]);
        } else {
            this.fullmoves = 1;
        }
    }

    public FEN() {
        this.fen = new StringBuilder(START_FEN);
        this.turn = new StringBuilder("w");
        this.castling = new StringBuilder("KQkq");
        this.ep = new StringBuilder("-");
        this.halfmoves = 0;
        this.fullmoves = 1;
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
}
