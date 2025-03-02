/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Provide high-level methods for manipulating the engine
 */

package engine.model;

import engine.helper.FEN;
import engine.model.magics.MagicBitboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public static int perft(int depth, boolean print) {
        return perft(depth, true, print);
    }

    public static int perft(int depth, boolean root, boolean print) {
        List<Short> moves = MoveGeneration.gen();
        List<String> output = new ArrayList<>();

        int nodes = 0;
        for(short move : moves) {
            if(true) {
                int state = GameState.create(move);

                Move.make(move, depth);

                int branchNodes = depth <= 1 ? 1 : perft(depth - 1, false, print);
                nodes += branchNodes;
                
                if(root) {
                    output.add(Move.getAlgebraic(move) + ": " + branchNodes);
                }

                Move.unmake(move, state);
            }
        }

        if(print) {
            Collections.sort(output);
            for(String s : output) {
                System.out.println(s);
            }
        }

        return nodes;
    }
}
