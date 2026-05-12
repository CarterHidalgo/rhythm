/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Stores the minimum amount of game state information for make/unmake
 */

package engine.model;

import java.util.Stack;

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
     * | Captured Piece:  | 5 bits  |  0-4    |
     * |--------------------------------------|
     * | Total:           | -  bits | -  bits |
     * ----------------------------------------
     * 
     * 
     */

    private static Stack<Integer> stateStack = new Stack<>();

    public static void push(short move) {
        stateStack.push( 
            (Board.get(Move.getTo(move)) & 0x1F) 
        );
    }

    public static int pop() {
        return stateStack.pop();
    }

    public static int getCaptured() {
        return (stateStack.peek() & 0x1F);
    }
}
