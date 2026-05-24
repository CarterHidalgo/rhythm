/*
 * Author: Carter Hidalgo
 * 
 * Purpose: A primitive game stack data structure optimized for performance
 */

package engine.model;

public class StateStack {
    /* 
     * StateStack bit breakdown
     * 
     * 0-4: raw target occupant enum (5 bits)
     * 5-11: halfmove clock (7 bits)
     * 12-15: castling rights (4 bits)
     * 16-21: ep state (6 bits)
     * 22-25: original piece moved enum (4 bits)
     */

    private static int[] data = new int[64];
    private static int top = -1;

    public static final void push(short move) {
        int state = 0;
        
        state |= Board.get(Move.getTo(move)) & 0x1F;
        state |= (GameInfo.getHalfmoves() & 0x7F) << 5;
        state |= (GameInfo.getCastleRights() & 0xF) << 12;
        state |= (GameInfo.getEP() & 0x3F) << 16;
        state |= (Board.get(Move.getFrom(move)) & 0xF) << 22;

        data[++top] = state;
    }

    public static final int pop() {
        return data[top--];
    }

    public static final int peek() {
        return data[top];
    }

    public static final boolean isEmpty() {
        return top == -1;
    }

    public static final void clear() {
        top = -1;
    }

    public static final int getTarget() {
        return data[top] & 0x1F;
    }

    public static final int getHalfmoves() {
        return (data[top] >>> 5) & 0x7f;
    }

    public static final int getCastleRights() {
        return (data[top] >>> 12) & 0xF;
    }

    public static final int getEP() {
        return (data[top] >>> 16) & 0x3F;
    }

    public static final int getPiece() {
        return (data[top] >>> 22) & 0xF;
    }
}