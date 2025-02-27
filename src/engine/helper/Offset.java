package engine.helper;

import engine.model.GameInfo;

public class Offset {
    public static int forward(int index) {
        int multiplier = GameInfo.getTurn() ? 1 : -1;
        return index + (8 * multiplier);
    }

    public static int dbForward(int index) {
        int multiplier = GameInfo.getTurn() ? 1 : -1;
        return index + (16 * multiplier);
    }

    public static int behind(int index) {
        int multiplier = GameInfo.getTurn() ? 1 : -1;
        return index - (8 * multiplier);
    }

    public static int lForward(int index) {
        int multiplier = GameInfo.getTurn() ? 1 : -1;
        if(Math.abs((index % 8) - ((index + (7 * multiplier)) % 8)) == 1) {
            return index + (7 * multiplier);
        } else {
            return -1;
        }
    }

    public static int rForward(int index) {
        int multiplier = GameInfo.getTurn() ? 1 : -1;
        if(Math.abs((index % 8) - ((index + (9 * multiplier)) % 8)) == 1) {
            return index + (9 * multiplier);
        } else {
            return -1;
        }
    }
}
