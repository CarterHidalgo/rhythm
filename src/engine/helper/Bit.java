/*
 * Name: Bit.java
 * Author: Pigpen
 * 
 * Purpose: Provide access to common or complex binary opperations
 */

package engine.helper;

public class Bit {
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
