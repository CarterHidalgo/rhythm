/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Provides methods for safely parsing data out of a FEN string; if no value is found a default is provided
 */

package engine.helper;

import engine.model.Board;
import engine.model.GameInfo;

public class FEN {
    public static final String[] COMMON_FENS = {
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", // [0] starting position
        "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1" // [1] kiwipete
    };

    public static final String START_FEN = COMMON_FENS[0];

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

    public static final FEN create() {
        StringBuilder fen = new StringBuilder();

        int index = 0;
        int space = 0;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                index = (7-i) * 8 + j;
                char c = Enum.codeToString(Board.get(index)).charAt(0);
                
                if(c == 'e') {
                    if(Character.isDigit(fen.charAt(fen.length() - 1))) {
                        fen.replace(fen.length() - 1, fen.length(), String.valueOf(++space));
                    } else {
                        fen.append(String.valueOf(++space));
                    }
                } else {
                    fen.append(c);
                    space = 0;
                }
            }

            space = 0;
            fen.append("/");
        }

        fen.deleteCharAt(fen.length() - 1);
        fen.append(" " + GameInfo.getTurnString().charAt(0) + " " + GameInfo.getCastlingString() + " " + GameInfo.getEPString() + " " + GameInfo.getHalfmoves() + " " + GameInfo.getFullmoves());

        return new FEN(fen.toString());
    }

    public final String getFEN() {
        return fen.toString();
    }

    public final String getTurn() {
        return turn.toString();
    }

    public final String getCastling() {
        return castling.toString();
    }

    public final String getEp() {
        return ep.toString();
    }

    public final int getHalfmoves() {
        return halfmoves;
    }

    public final int getFullmoves() {
        return fullmoves;
    }

    public final String get() {
        return fen.toString() + " " + turn.toString() + " " + castling.toString() + " " + ep.toString() + " "
                + halfmoves + " " + fullmoves;
    }

    @Override
    public final String toString() {
        return fen.toString() + " " + turn.toString() + " " + castling.toString() + " " + ep.toString() + " "
                + halfmoves + " " + fullmoves;
    }
}
