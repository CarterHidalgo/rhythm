/*
 * Name: GameInfo.java
 * Author: Pigpen
 * 
 * Purpose: Contains information about the current game; nothing aside from the class variables should ever be modified here. 
 */

package model;

public class GameInfo {
    private static int halfmoves = 0; // how many moves both players have made since the last pawn advance or piece capture
    private static int fullmoves = 0; // the number of completed turns in the game - incremented by one every time Black moves
    private static boolean turn = true; // indicates whose turn it is to play - true for White false for Black
    private static boolean side = true; // indicates which color the engine is playing as - true for White false for Black
    private static boolean[][] castleRights = {{true, true}, {true, true}};

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

    public static boolean getCastleRights(boolean turn, int dir) {
        if(turn) {
            return castleRights[0][dir];
        } else {
            return castleRights[1][dir];
        }
    }

    public static void incHalfmoves() {
        halfmoves++;
    }

    public static void incFullmoves() {
        fullmoves++;
    }

    public static void nextTurn() {
        turn = !turn;
    }

    public static void setSide(boolean side) {
        GameInfo.side = side;
    }
}
