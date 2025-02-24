/*
 * Name: ModelMain.java
 * Author: Pigpen
 * 
 * Purpose: Provide high-level methods for manipulating the engine
 */

package model;

import helper.FEN;
import model.magics.MagicBitboard;

import java.util.ArrayList;
import java.util.Collections;

public class Model {
    private static volatile boolean isRunning = true;
    private static boolean isReady = false;
    private static final Object lock = new Object();

    public static void init() {
        FEN start = new FEN(FEN.START_FEN);

        Bitboard.setWithFEN(start);
        Board.setWithFEN(start);
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
        Board.setWithFEN(fenObj);
        GameInfo.setWithFEN(fenObj);
    }

    public static int perft(int depth) {
        return perft(depth, true);
    }

    public static int perft(int depth, boolean root) {
        ArrayList<Short> moves = MoveGeneration.gen();
        ArrayList<String> output = new ArrayList<>();

        int nodes = 0;
        for(short move : moves) {
            if(Move.isLegal(move)) {
                int state = GameState.create(move);

                Move.make(move, depth);

                int branchNodes = depth <= 1 ? 1 : perft(depth - 1, false);
                nodes += branchNodes;
                
                if(root) {
                    output.add(Move.getAlgebraic(move) + ": " + branchNodes);
                }

                Move.unmake(move, state);
            }
        }

        Collections.sort(output);
        for(String s : output) {
            System.out.println(s);
        }

        return nodes;
    }
}
