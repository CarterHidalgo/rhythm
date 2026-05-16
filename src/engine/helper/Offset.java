/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Give integer indices into the 0-63 board for the different directions and amounts a piece can move
 */

package engine.helper;

import engine.model.GameInfo;

public class Offset {
    public static final int forward(int index) {
        int multiplier = GameInfo.getTurn() == Enum.WHITE ? 1 : -1;
        return index + (8 * multiplier);
    }

    public static final int dbForward(int index) {
        int multiplier = GameInfo.getTurn() == Enum.WHITE ? 1 : -1;
        return index + (16 * multiplier);
    }

    public static final int behind(int index) {
        int multiplier = GameInfo.getTurn() == Enum.WHITE ? 1 : -1;
        return index - (8 * multiplier);
    }

    public static final int lForward(int index) {
        int multiplier = GameInfo.getTurn() == Enum.WHITE ? 1 : -1;
        if(Math.abs((index % 8) - ((index + (7 * multiplier)) % 8)) == 1) {
            return index + (7 * multiplier);
        } else {
            return -1;
        }
    }

    public static final int rForward(int index) {
        int multiplier = GameInfo.getTurn() == Enum.WHITE ? 1 : -1;
        if(Math.abs((index % 8) - ((index + (9 * multiplier)) % 8)) == 1) {
            return index + (9 * multiplier);
        } else {
            return -1;
        }
    }
}
