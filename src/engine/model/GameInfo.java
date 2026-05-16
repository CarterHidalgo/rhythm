/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Contains information about the current game; nothing aside from the class variables
 * should ever be modified here.
 */

package engine.model;

import engine.helper.Enum;
import engine.helper.FEN;

public class GameInfo {
    private static int halfmoves = 0;
    private static int fullmoves = 1; 
    private static int castling = 0xF;
    private static int ep = 0;
    private static int turn = Enum.WHITE;

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

    public static final int getCastling() {
        return castling;
    }

    public static final String getCastlingString() {
        StringBuilder castlingString = new StringBuilder();
        char[] castlingChar = { 'K', 'Q', 'k', 'q' };

        for (int i = 0; i < 4; i++) {
            if (((castling >> i) & 1) == 1) {
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

    public static final void setHalfmoves(int value) {
        halfmoves = value;
    }

    public static final void setFullmoves(int value) {
        fullmoves = value;
    }

    public static final void setTurn(int value) {
        turn = value;
    }

    public static final void setCastling(int value) {
        castling = value;
    }

    public static final void setEP(int value) {
        ep = value;
    }

    public static final void setWithFEN(FEN fen) {
        turn = fen.getTurn().equals("w") ? Enum.WHITE : Enum.BLACK;
        halfmoves = fen.getHalfmoves();
        fullmoves = fen.getFullmoves();
    }

    public static final void makeMove(GameState state) {
        if (state.getPiece() == Enum.WHITE + Enum.PAWN || state.getPiece() == Enum.BLACK + Enum.PAWN
                || Move.isCapture(state.getMove())) {
            halfmoves = 0;
        } else {
            halfmoves++;
        }

        if (turn == Enum.BLACK) {
            fullmoves++;
        }
        
        turn = Enum.BLACK - turn;
    }

    public static final void unmakeMove(GameState state) {
        if (state.getPiece() == Enum.WHITE + Enum.PAWN || state.getPiece() == Enum.BLACK + Enum.PAWN
                || Move.isCapture(state.getMove())) {
            halfmoves = state.getHalfmoves();
        } else {
            halfmoves--;
        }
        
        if (turn == Enum.BLACK) {
            fullmoves--;
        }

        turn = Enum.BLACK - turn;
    }
}
