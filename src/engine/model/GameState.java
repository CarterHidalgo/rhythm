/*
 * Name: GameState.java
 * Author: Pigpen
 * 
 * Purpose: Stores the minimum amount of game state information for make/unmake
 */

package engine.model;

import engine.helper.Enum;

public class GameState {
    /*
     * Context-Sensitive State Representation
     * 
     * Idea: When we want to store the game state, it is always in the context of a move. Therefore,
     * we should never need to store all bitboards (1088 bits + additional castling/halfmove info).
     * Instead, we simply store changes, namely the move itself (16 bits) and the additional castling,
     * EP, and Half-move info. This allows for a much smaller game state size at the cost of some
     * additional computation at unmake. 
     * 
     * ----------------------------------------
     * | From square:     | 6 bits  |  0-5    |
     * | To square:       | 6 bits  |  6-11   |
     * | Flags:           | 4 bits  |  12-15  |
     * | Castling rights: | 4 bits  |  16-19  |
     * | EP square:       | 5 bits  |  20-24  |
     * | Half-move clock: | 7 bits  |  25-31  |
     * |--------------------------------------|
     * | Total:           | 32 bits | 32 bits |
     * ----------------------------------------
     */

    public static int create(short move) {
        return ((Move.getFrom(move) & 0x3F) | ((Move.getTo(move) & 0x3F) << 6) | ((Move.getFlags(move) & 0xF) << 12) | ((GameInfo.getCastling() & 0xF) << 16) | ((int) ((Bitboard.get(Enum.EP) << 16) & 0x1F) << 20) | ((GameInfo.getHalfmoves() & 0x7F) << 25));
    }

    public static int getFrom(int state) {
        return ((state & 0x3F));
    }

    public static int getTo(int state) {
        return ((state >> 6) & 0x3F);
    }

    public static int getFlags(int state) {
        return ((state >> 12) & 0xF);
    }

    public static int getCastling(int state) {
        return ((state >> 16) & 0xF);
    }

    public static int getEP(int state) {
        return ((state >> 20) & 0x1F);
    }

    public static int getHalfmoves(int state) {
        return ((state >> 25) & 0x7F);
    }
}
