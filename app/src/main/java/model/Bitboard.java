/*
 * Name: Bitboard.java
 * Author: Pigpen
 * 
 * Purpose: Contains dynamic and static bitboards as well as the methods for getting and setting them
 */

package model;

import helper.Enum;
import helper.FEN;

public class Bitboard {
    private static long[] bitboards = new long[21];

    public static long get(int code) {
        return bitboards[code];
    }

    public static long getSelf(int code) {
        return bitboards[(GameInfo.getTurn() ? Enum.WHITE : Enum.BLACK) + code];
    }

    public static long getSelf() {
        return bitboards[GameInfo.getTurn() ? Enum.WHITE : Enum.BLACK];
    }

    public static long getOp(int code) {
        return bitboards[(GameInfo.getTurn() ? Enum.BLACK : Enum.WHITE) + code];
    }

    public static long getOp() {
        return bitboards[GameInfo.getTurn() ? Enum.BLACK : Enum.WHITE];
    }

    public static void set(int code, long value) {
        bitboards[code] = value;
    }

    public static void toggle(int code, long bitboard) {
        bitboards[code] ^= bitboard;
    }

    public static void setWithFEN(FEN fen) {
        int rank = 7;
        int file = 0;

        clearAll();

        for (char c : fen.getFEN().toCharArray()) {
            if (c == '/') {
                rank--;
                file = 0;
            } else if (Character.isDigit(c)) {
                file += Character.getNumericValue(c);
            } else {
                int code = Enum.stringToCode(Character.toString(c));

                bitboards[code] |= 1L << (rank * 8 + file++);
            }
        }

        for (int i = Enum.WHITE + Enum.PAWN; i <= Enum.WHITE + Enum.KING; i++) {
            bitboards[Enum.WHITE] |= bitboards[i];
        }

        for (int i = Enum.BLACK + Enum.PAWN; i <= Enum.BLACK + Enum.KING; i++) {
            bitboards[Enum.BLACK] |= bitboards[i];
        }

        bitboards[Enum.OCCUPIED] = bitboards[Enum.WHITE] | bitboards[Enum.BLACK];
        bitboards[Enum.EMPTY] = ~bitboards[Enum.OCCUPIED];

        bitboards[Enum.WHITE + Enum.HOME] = 0xFF00L;
        bitboards[Enum.BLACK + Enum.HOME] = 0xFF000000000000L;
        bitboards[Enum.WHITE + Enum.PROMO] = 0xFF00000000000000L;
        bitboards[Enum.BLACK + Enum.PROMO] = 0xFFL;

        if (!fen.getEp().equals("-")) {
            System.out.println("Bitboard.java -> setWithFEN; setup ep bitboard by move");
        }
    }

    public static void clear(int code) {
        bitboards[code] = 0L;
    }

    public static void clearAll() {
        for (int i = 0; i < bitboards.length; i++) {
            bitboards[i] = 0L;
        }
    }
}
