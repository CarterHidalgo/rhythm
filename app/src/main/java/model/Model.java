/*
 * Name: ModelMain.java
 * Author: Pigpen
 * 
 * Purpose: To provide high-level methods for manipulating the engine model
 */

package model;

import helper.FEN;
import model.magics.MagicBitboard;

import java.util.ArrayList;

public class Model {
    private static volatile boolean isRunning = true;
    private static boolean isReady = false;
    private static final Object lock = new Object();

    public static void init() {
        FEN start = new FEN();

        Bitboard.create();
        Bitboard.setWithFEN(start);
        BoardLookup.setWithFEN(start);
        MagicBitboard.init();

        setReady(true);
    }

    public static void setReady(boolean isReady) {
        Model.isReady = isReady;
        
        synchronized(lock) {
            if(isReady) {
                lock.notifyAll();
            }
        }
    }

    public static boolean getReady() {
        return isReady;
    }

    public static void setRunning(boolean value) {
        isRunning = value;
    }

    public static boolean isRunning() {
        return isRunning;
    }

    public static Object getLock() {
        return lock;
    }

    public static void setPosition(String fenStr) {
        FEN fenObj = new FEN(fenStr);

        Bitboard.setWithFEN(fenObj);
        BoardLookup.setWithFEN(fenObj);
    }

    public static void makeMoves(String[] moves) {
        // make every move in moves 
    }

    public static int perft(int depth) {
        return perft(depth, true);
    }

    public static int perft(int depth, boolean root) {
        ArrayList<Short> moves = MoveGeneration.gen();

        int nodes = 0;
        if(depth <= 1) {
            for(short move : moves) {
                if(Move.isLegal(move)) {
                    nodes++;

                    // if(root) {
                    //     UciProtocol.write(Move.getAlgebraic(move) + ": 1");
                    // }
                }
            }

            return nodes;
        }

        int branchNodes;
        for(short move : moves) {
            if(Move.isLegal(move)) {
                // add something to make the move so structures are updated

                branchNodes = perft(depth - 1, false);

                // if(root) {
                //     UciProtocol.write(Move.getAlgebraic(move) + ": " + branchNodes);
                // }

                nodes += branchNodes;
            }
        }

        return nodes;
    }
}
