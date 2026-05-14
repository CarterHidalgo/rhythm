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

    public static void init() {
        FEN start = new FEN(FEN.START_FEN);

        Bitboard.setWithFEN(start);
        Board.setWithFEN(start);
        MagicBitboard.init();

        setReady(true);
    }

    public static void setReady(boolean isReady) {
        Model.isReady = isReady;

        synchronized (lock) {
            if (isReady) {
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

    public static void setPosition(String fenStr, List<String> moves) {
        FEN fenObj = new FEN(fenStr);

        Bitboard.setWithFEN(fenObj);
        Board.setWithFEN(fenObj);
        GameInfo.setWithFEN(fenObj);

        while (moves != null && !moves.isEmpty()) {
            List<Short> moveList = MoveGeneration.gen();

            for (Short move : moveList) {
                if (Move.getAlgebraic(move).equals(moves.getFirst())) {
                    Move.make(move);
                    if (moves.size() > 1) {
                        moves.removeFirst();
                        break;
                    } else {
                        return;
                    }
                }
            }
        }

    }
    
    public static Map<String, Long> getPerftMap(int depth, String fenStr, List<String> movesMade) {
        Model.setPosition(fenStr, movesMade);
        
        Map<String, Long> map = new HashMap<>();
        List<Short> movesGen = MoveGeneration.gen();
        
        for (short move : movesGen) {
            Move.make(move);
            
            long nodes = perft(depth - 1, false);
            
            Move.unmake(move);
            
            map.put(Move.getAlgebraic(move), nodes);
        }
        
        return map;
    }

    public static long perft(int depth, boolean print) {
        long start = System.nanoTime();
        long nodes = perft(depth, true, print);
        long taken = System.nanoTime() - start;

        if (nodes > 0 && print) {
            System.out.println("\nnodes: " + nodes);
            System.out.println("time: " + taken / 1_000_000 + "ms");
            System.out.println("nps: " + (int) (nodes / (++taken / 1_000_000_000.0)) + "\n");
        }

        return nodes;
    }
    
    private static long perft(int depth, boolean root, boolean print) {
        if(depth == 0) {
            return 1;
        }

        long totalNodes = 0;
        long branchNodes = 0;
        List<Short> moves = MoveGeneration.gen();

        for (short move : moves) {
            branchNodes = 0;
            
            Move.make(move);
            branchNodes += perft(depth - 1, false, print);
            Move.unmake(move);

            totalNodes += branchNodes;

            if(root && print) {
                System.out.println(Move.getAlgebraic(move) + ": " + branchNodes);
            }
        }

        return totalNodes;
    }
}
