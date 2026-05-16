/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Contains dynamic and static bitboards as well as the methods for getting and setting
 * them
 */

package engine.model;

import engine.helper.Enum;
import engine.helper.FEN;

public class Bitboard {
    public static final long FILE_A = 0x101010101010101L;
    public static final long FILE_H = 0x8080808080808080L;
    public static final long RANK_3 = 0xff0000L;
    public static final long RANK_6 = 0xff0000000000L;

    private static long[] bitboards = new long[Enum.LENGTH];

    public static final long get(int code) {
        return bitboards[code];
    }

    public static final long getSelf(int code) {
        return bitboards[GameInfo.getTurn() + code];
    }

    public static final long getSelf() {
        return bitboards[GameInfo.getTurn()];
    }

    public static final long getOp(int code) {
        return bitboards[GameInfo.getTurn() + code];
    }

    public static final long getOp() {
        return bitboards[Enum.BLACK - GameInfo.getTurn()];
    }
    
    public static final void set(int index, int... codes) {
        for(int code : codes) {
            bitboards[code] |= 1L << index;
        }
    }
    
    public static final void clear(int index, int... codes) {
        for(int code : codes) {
            bitboards[code] &= ~(1L << index);
        }
    }

    public static final void toggle(int index, int... codes) {
        for(int code : codes) {
            bitboards[code] ^= (1L << index);
        }
    }

    public static final void toggle(long bitboard, int... codes) {
        for(int code : codes) {
            bitboards[code] ^= bitboard;
        }
    }

    public static final void setWithFEN(FEN fen) {
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

    public static final void clearAll() {
        for (int i = 0; i < bitboards.length; i++) {
            bitboards[i] = 0L;
        }
    }
}
