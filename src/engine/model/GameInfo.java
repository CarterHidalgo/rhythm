/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Contains information about the current game state; nothing aside from the class variables
 * should ever be modified here.
 */

package engine.model;

import engine.helper.Enum;
import engine.helper.FEN;

public class GameInfo {
    public static final int WK = 1;
    public static final int WQ = 2;
    public static final int BK = 4;
    public static final int BQ = 8;
    public static final int[] castleType = {
        0, 0, 0, 0, 
        0, WQ, WK, 0, 
        0, 0, 0, 0, 
        0, BQ, BK, 0};

    private static int halfmoves = 0;
    private static int fullmoves = 1; 
    private static int ep = 0;
    private static int turn = Enum.WHITE;
    private static int castleRights = WK | WQ | BK | BQ;

    public static final int getHalfmoves() {
        return halfmoves;
    }

    public static final int getFullmoves() {
        return fullmoves;
    }

    public static final int getTurn() {
        return turn;
    }

    public static final int getOpTurn() {
        return Enum.BLACK - turn;
    }

    public static final String getTurnString() {
        return turn == 0 ? "white" : "black";
    }

    public static final int getCastleRights() {
        return castleRights;
    }

    public static final String getCastleRightsString() {
        if(castleRights == 0) {
            return "-";
        }
        
        StringBuilder castlingString = new StringBuilder();
        char[] castlingChar = { 'K', 'Q', 'k', 'q' };

        for (int i = 0; i < 4; i++) {
            if (((castleRights >> i) & 1) == 1) {
                castlingString.append(castlingChar[i]);
            }
        }

        return castlingString.toString();
    }

    public static final int getEP() {
        return ep;
    }

    public static final String getEPString() {
        return ep == 0 ? "-" : Enum.indexToString(ep);
    }

    public static final void setWithFEN(FEN fen) {
        halfmoves = fen.getHalfmoves();
        fullmoves = fen.getFullmoves();
        castleRights = parseCastleRights(fen.getCastleRights());
        ep = Move.getIndex(fen.getEp());
        turn = fen.getTurn().equals("w") ? Enum.WHITE : Enum.BLACK;
    }

    public static final void make(int from, int to) {
        int piece = StateStack.getPiece();

        castleRights &= Bitboard.CASTLE_MASK[from];
        castleRights &= Bitboard.CASTLE_MASK[to];

        if (StateStack.getTarget() != Enum.EMPTY || piece == Enum.WHITE + Enum.PAWN 
                || piece == Enum.BLACK + Enum.PAWN) {
            halfmoves = 0;
        } else {
            halfmoves++;
        }

        if (turn == Enum.BLACK) {
            fullmoves++;
        }
        
        turn = Enum.BLACK - turn;
    }

    public static final void unmake() {
        int piece = StateStack.getPiece();

        castleRights = StateStack.getCastleRights();

        if (StateStack.getTarget() != Enum.EMPTY || piece == Enum.WHITE + Enum.PAWN
                || piece == Enum.BLACK + Enum.PAWN) {
            halfmoves = StateStack.getHalfmoves();
        } else {
            halfmoves--;
        }
        
        if (turn == Enum.BLACK) {
            fullmoves--;
        }

        turn = Enum.BLACK - turn;
    }

    private static final int parseCastleRights(String castleRightsStr) {
        if (castleRightsStr.equals("-")) {
            return 0;
        }

        int mask = 0;

        if (castleRightsStr.indexOf('K') >= 0)
            mask |= WK;
        if (castleRightsStr.indexOf('Q') >= 0)
            mask |= WQ;
        if (castleRightsStr.indexOf('k') >= 0)
            mask |= BK;
        if (castleRightsStr.indexOf('q') >= 0)
            mask |= BQ;

        return mask;
    }
}
