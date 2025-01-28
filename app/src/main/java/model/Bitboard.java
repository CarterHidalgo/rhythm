/*
 * Name: Bitboard.java
 * Author: Pigpen
 * 
 * Purpose: Contains dynamic and static bitboards as well as the methods for getting and setting them
 */

package model;

import helper.Bit;
import helper.FEN;

public class Bitboard {
    private static long[] bitboards = new long[20];

    public static long get(int key) {
        return bitboards[key];
    }

    public static long getSelf(int key) {
        return bitboards[(GameInfo.getTurn() ? Piece.WHITE : Piece.BLACK) + key];
    }

    public static long getSelf() {
        return bitboards[GameInfo.getTurn() ? Piece.WHITE : Piece.BLACK];
    }

    public static long getOp(int key) {
        return bitboards[(GameInfo.getTurn() ? Piece.BLACK : Piece.WHITE) + key];
    }

    public static long getOp() {
        return bitboards[GameInfo.getTurn() ? Piece.BLACK : Piece.WHITE];
    }

    public static void set(int key, long value) {
        bitboards[key] = value;
    }

    public static void setWithFEN(FEN fen) {
        int rank = 7;
        int file = 0;
        long bitboard = 0;

        clearAll();

        for(char c : fen.getFEN().toCharArray()) {
            if(c == '/') {
                rank--;
                file = 0;
            } else if(Character.isDigit(c)) {
                file += Character.getNumericValue(c);
            } else {
                int key = Piece.get(Character.toString(c));
                set(key, Bit.set(get(key), rank * 8 + file++));
            }
        }

        bitboard = 0L;
        for(int i = Piece.WHITE + Piece.PAWN; i <= Piece.WHITE + Piece.KING; i++) {
            bitboard |= get(i);
        }
        set(Piece.WHITE, bitboard);

        bitboard = 0L;
        for(int i = Piece.BLACK + Piece.PAWN; i <= Piece.BLACK + Piece.KING; i++) {
            bitboard |= get(i);
        }
        set(Piece.BLACK, bitboard);

        set(Piece.OCCUPIED, get(Piece.WHITE | Piece.BLACK));
        set(Piece.EMPTY, ~get(Piece.OCCUPIED));

        if(!fen.getEp().equals("-")) {
            System.out.println("Bitboard.java -> setWithFEN; setup ep bitboard by move");
        }
    }

    public static void create() {
        for(int i = 0; i < bitboards.length; i++) {
            bitboards[i] = 0L;
        }

        bitboards[Piece.WHITE + Piece.HOME] = 0xFF00L;
        bitboards[Piece.BLACK + Piece.HOME] = 0xFF000000000000L;
        bitboards[Piece.WHITE + Piece.PROMO] = 0xFF00000000000000L;
        bitboards[Piece.BLACK + Piece.PROMO] = 0xFFL;
    }

    public static void clear(int key) {
        bitboards[key] = 0L;
    }

    public static void clearAll() {
        for(int i = 0; i < bitboards.length; i++) {
            bitboards[i] = 0L;
        }
    }
}
