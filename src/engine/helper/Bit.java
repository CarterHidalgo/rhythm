/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Provide access to common or complex binary opperations
 */

package engine.helper;

public class Bit {
    public static final long set(long value, int index) {
        long mask = 1L << index;

        return value | mask;
    }

    public static final boolean isSet(byte value, int index) {
        return ((value >> index) & 1) == 1;
    }

    public static final boolean isSet(long value, int index) {
        return ((value >> index) & 1) == 1;
    }
}
