/*
 * Name: GameState.java
 * Author: Pigpen
 * 
 * Purpose: Stores the minimum amount of game state information for make/unmake
 */

package model;

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
     * ------------------------------
     * | From square:     | 6 bits  |
     * | To square:       | 6 bits  |
     * | Flags:           | 4 bits  |
     * | Castling rights: | 4 bits  |
     * | EP square:       | 5 bits  |
     * | Half-move clock: | 7 bits  |
     * |----------------------------|
     * | Total:           | 32 bits |
     * ------------------------------
     */

    public static int get(short move) {
        int state = move;

        return state;
    }
}
