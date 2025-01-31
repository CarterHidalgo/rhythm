/*
 * Name: Move.java
 * Author: Pigpen
 * 
 * Purpose: Contains move type flag bitcodes and methods for creating, making, or printing moves (shorts)
 */

package model;

import helper.Bit;
import helper.Enum;

public class Move {
    public static final byte QUIET = 0b0000;
    public static final byte DOUBLE_PAWN = 0b0001;
    public static final byte KING_CASTLE = 0b0010;
    public static final byte QUEEN_CASTLE = 0b0011;
    public static final byte CAPTURE = 0b0100;
    public static final byte EP_CAPTURE = 0b0101;
    public static final byte KNIGHT_PROMO = 0b1000;
    public static final byte BISHOP_PROMO = 0b1001;
    public static final byte ROOK_PROMO = 0b1010;
    public static final byte QUEEN_PROMO = 0b1011;
    public static final byte KNIGHT_PROMO_CAPTURE = 0b1100;
    public static final byte BISHOP_PROMO_CAPTURE = 0b1101;
    public static final byte ROOK_PROMO_CAPTURE = 0b1110;
    public static final byte QUEEN_PROMO_CAPTURE = 0b1111;

    public static short create(int from, int to, int flags) {
        return (short) ((from & 0x3F) | ((to & 0x3F) << 6) | ((flags & 0xF) << 12));
    }

    public static int getTo(short move) {
        return ((move >> 6) & 0x3F);
    }

    public static int getFrom(short move) {
        return ((move & 0x3F));
    }

    public static void make(short move) {
        // push current state onto stack

        // apply move
        System.out.println("applying " + Move.getAlgebraic(move));
        
        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int piece = Board.get(from);
        int color = Piece.color(piece);
        long fromBitboard = 1 << from;
        long toBitboard = 1 << to;
        long fromToBitboard = fromBitboard ^ toBitboard;


        // quiet move
        Bitboard.toggle(piece, fromToBitboard);
        Bitboard.toggle(color, fromToBitboard);
        Bitboard.toggle(Enum.OCCUPIED, fromToBitboard);
        Bitboard.toggle(Enum.EMPTY, fromToBitboard);
    }

    public static void unmake() {
        // restore state from stack
    }

    public static boolean isLegal(short move) {
        return true;
    }

    public static boolean isPromotion(short move) {
        return Bit.isSet(move, 15);
    }

    public static String getIndexed(short move) {
        byte fromIndex = (byte) (move & 0b111111); 
        byte toIndex = (byte) ((move >> 6) & 0b111111);
        byte flags = (byte) ((move >> 12) & 0b1111);

        return fromIndex + " | " + toIndex + " | " + flags;
    }

    public static String getAlgebraic(short move) {
        byte fromIndex = (byte) (move & 0b111111); 
        byte toIndex = (byte) ((move >> 6) & 0b111111);
        byte flags = (byte) ((move >> 12) & 0b1111);

        return indexToAlgebraic(fromIndex) + indexToAlgebraic(toIndex) + flagsToAlgebraic(flags);
    }

    private static String indexToAlgebraic(byte index) {
        char file = (char) ('a' + (index % 8));
        int rank = 1 + (index / 8);
        return "" + file + rank;
    }

    private static String flagsToAlgebraic(byte flags) {
        if(Bit.isSet(flags, 3)) {
            switch(flags) {
                case KNIGHT_PROMO:
                case KNIGHT_PROMO_CAPTURE:
                    return "n";
                case BISHOP_PROMO:
                case BISHOP_PROMO_CAPTURE:
                    return "b";
                case ROOK_PROMO:
                case ROOK_PROMO_CAPTURE:
                    return "r";
                case QUEEN_PROMO:
                case QUEEN_PROMO_CAPTURE:
                    return "q";
                default:
                    return "";
            }
        } else {
            return "";
        }
    }
}