/*
 * Name: Piece.java
 * Author: Pigpen
 * 
 * Purpose: Methods for understanding piece properties like value, color, and enums
 */

package model;

import java.util.HashMap;

public class Piece {
    public static final int WHITE = 0;
    public static final int BLACK = 9;

    public static final int ALL = 0;
    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int ROOK = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;
    public static final int HOME = 7;
    public static final int PROMO = 8;

    public static final int OCCUPIED = 18;
    public static final int EMPTY = 19;
    public static final int EP = 20;
    
    public static HashMap<String, Integer> map = new HashMap<>();

    static {
        map.put("white", WHITE);
        map.put("black", BLACK);

        map.put("pawn", PAWN);
        map.put("knight", KNIGHT);
        map.put("bishop", BISHOP);
        map.put("rook", ROOK);
        map.put("queen", QUEEN);
        map.put("king", KING);

        map.put("occupied", OCCUPIED);
        map.put("empty", EMPTY);
        map.put("ep", EP);

        map.put("P", WHITE + PAWN);
        map.put("N", WHITE + KNIGHT);
        map.put("B", WHITE + BISHOP);
        map.put("R", WHITE + ROOK);
        map.put("Q", WHITE + QUEEN);
        map.put("K", WHITE + KING);

        map.put("p", BLACK + PAWN);
        map.put("n", BLACK + KNIGHT);
        map.put("b", BLACK + BISHOP);
        map.put("r", BLACK + ROOK);
        map.put("q", BLACK + QUEEN);
        map.put("k", BLACK + KING);
        
    }

    public static boolean isWhite(byte code) {
        return code > 0 && code < 7;
    }

    public static boolean isBlack(byte code) {
        return code > 8 && code < 15;
    }

    public static int get(String param) {
        return map.get(param);
    }
}
