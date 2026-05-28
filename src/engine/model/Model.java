/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Provide high-level methods for manipulating the engine
 */

package engine.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import engine.helper.FEN;
import engine.model.magics.MagicBitboard;

public class Model {
    private static volatile boolean isRunning = true;
    private static boolean isReady = false;
    private static final Object lock = new Object();

    public static final void init() {
        FEN start = new FEN(FEN.START_FEN);

        Bitboard.setWithFEN(start);
        Board.setWithFEN(start);
        MagicBitboard.init();

        setReady(true);
    }

    public static final void setReady(boolean isReady) {
        Model.isReady = isReady;

        synchronized (lock) {
            if (isReady) {
                lock.notifyAll();
            }
        }
    }

    public static final boolean getReady() {
        return isReady;
    }

    public static final void setRunning(boolean value) {
        isRunning = value;
    }

    public static final boolean isRunning() {
        return isRunning;
    }

    public static final Object getLock() {
        return lock;
    }

    public static final void setPosition(String fenStr) {
        FEN fenObj = new FEN(fenStr);

        Bitboard.setWithFEN(fenObj);
        Board.setWithFEN(fenObj);
        GameInfo.setWithFEN(fenObj);
    }

    public static final void setPosition(String fenStr, List<String> moves) {
        Model.setPosition(fenStr);

        for(String moveStr : moves) {
            short move = Move.create(moveStr);

            Move.make(move);
        }
    }
    
    public static final Map<String, Long> getPerftMap(int depth, String fenStr, List<String> movesMade) {
        Map<String, Long> map = new HashMap<>();
        MoveList legalMoves = new MoveList();
        short move;

        Model.setPosition(fenStr, movesMade);

        MoveGeneration.genLegal(legalMoves);

        for(int i = 0; i < legalMoves.size(); i++) {
            move = legalMoves.get(i);

            if (depth < 2) {
                map.put(Move.getAlgebraic(move), 1L);
            } else {
                Move.make(move);
                map.put(Move.getAlgebraic(move), perft(depth - 1, false, true));
                Move.unmake(move);
            }
        }

        return map;
    }

    public static final long perft(int depth, boolean print) {
        long start = System.nanoTime();
        long nodes = perft(depth, true, print);
        long taken = System.nanoTime() - start;

        if (nodes > 0 && print) {
            int nps = (int) (nodes / (++taken / 1_000_000_000.0));

            System.out.println("\nNodes: " + nodes);
            System.out.println("Time: " + taken / 1_000_000 + "ms");
            System.out.println("NPS: " + nps + "\n");
        }

        return nodes;
    }

    private static final long perft(int depth, boolean print, boolean root) {
        long totalNodes = 0;
        long branchNodes = 0;

        MoveList legalMoves = new MoveList();
        MoveGeneration.genLegal(legalMoves);

        short move;
        for(int i = 0; i < legalMoves.size(); i++) {
            move = legalMoves.get(i);

            if(depth < 2) {
                branchNodes = 1;
            } else {
                branchNodes = 0;

                Move.make(move);
                branchNodes += perft(depth - 1, print, false);
                Move.unmake(move);
            }

            totalNodes += branchNodes;

            if(root && print) {
                System.out.println(Move.getAlgebraic(move) + ": " + branchNodes);
            }
        }

        return totalNodes;
    }
}
