/*
 * Name: Bit.java
 * Author: Pigpen
 * 
 * Purpose: Provide access to some important binary codes and some common or complex binary opperations
 */

package helper;

public class Bit {
    public static final byte EMPTY = 0b0000;
    public static final byte FULL = 0b1111;

    public static final byte WHITE_PAWN = 0b0001; 
    public static final byte WHITE_KNIGHT = 0b0010;
    public static final byte WHITE_BISHOP = 0b0011;
    public static final byte WHITE_ROOK = 0b0100;
    public static final byte WHITE_QUEEN = 0b0101;
    public static final byte WHITE_KING = 0b0110;
    public static final byte BLACK_PAWN = 0b1001;
    public static final byte BLACK_KNIGHT = 0b1010;
    public static final byte BLACK_BISHOP = 0b1011;
    public static final byte BLACK_ROOK = 0b1100;
    public static final byte BLACK_QUEEN = 0b1101;
    public static final byte BLACK_KING = 0b1110;

    public static long set(long value, int index) {
        long mask = 1L << index;
        
        return value | mask;
    }

    public static boolean isSet(byte value, int index) {
        return ((value >> index) & 1) == 1;
    }

    public static boolean isSet(long value, int index) {
        return ((value >> index) & 1) == 1;
    }

    public static long clear(long value, int index) {
        long mask = 1L << index;

        return value & ~mask;
    }
}
