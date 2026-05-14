/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Contains information about the current game; nothing aside from the class variables
 * should ever be modified here.
 */

package engine.model;

import engine.helper.FEN;

public class GameInfo {
    private static int halfmoves = 0;
    private static int fullmoves = 0; 
    private static boolean turn = true;
    private static boolean side = true; 
    private static int castling = 0xF;

    public static int getHalfmoves() {
        return halfmoves;
    }

    public static int getFullmoves() {
        return fullmoves;
    }

    public static boolean getTurn() {
        return turn;
    }

    public static boolean getSide() {
        return side;
    }

    public static String getSideString() {
        return side ? "white" : "black";
    }

    public static int getCastling() {
        return castling;
    }

    public static String getCastlingString() {
        String castlingString = "";
        char[] castlingChar = { 'K', 'Q', 'k', 'q' };

        for (int i = 0; i < 4; i++) {
            if (((castling >> i) & 1) == 1) {
                castlingString += castlingChar[i];
            }
        }

        return castlingString;
    }

    public static void setHalfmoves(int value) {
        halfmoves = value;
    }

    public static void setFullmoves(int value) {
        fullmoves = value;
    }

    public static void setTurn(boolean value) {
        turn = value;
    }

    public static void setCastling(int value) {
        castling = value;
    }

    public static void setWithFEN(FEN fen) {
        turn = fen.getTurn().equals("w") ? true : false;
    }

    /**
     * Increments <code>halfmove</code>, <code>fullmove</code>, and toggles
     * <code>turn</code>
     */
    public static void makeMove() {
        halfmoves++;
        if (!turn) {
            fullmoves++;
        }
        turn = !turn;
    }

    /**
     * Decrements <code>halfmove</code>, <code>fullmove</code>, and toggles
     * <code>turn</code>
     */
    public static void unmakeMove() {
        halfmoves--;
        if (turn) {
            fullmoves--;
        }
        turn = !turn;
    }

    public static void setSide(boolean side) {
        GameInfo.side = side;
    }
}
