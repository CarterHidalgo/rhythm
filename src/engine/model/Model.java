/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Provide high-level methods for manipulating the engine
 */

package engine.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import engine.helper.FEN;
import engine.helper.Pair;
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

    public static void setPosition(String fenStr, Queue<String> moves) {
        FEN fenObj = new FEN(fenStr);

        Bitboard.setWithFEN(fenObj);
        Board.setWithFEN(fenObj);
        GameInfo.setWithFEN(fenObj);

        while (moves != null && !moves.isEmpty()) {
            List<Short> moveList = MoveGeneration.gen();

            for (Short move : moveList) {
                if (Move.getAlgebraic(move).equals(moves.peek())) {
                    Move.make(move);

                    if (moves.size() > 1) {
                        moves.poll();
                        break;
                    } else {
                        return;
                    }
                }
            }
        }

    }

    public static Pair perft(int depth, boolean print) {
        return perft(depth, true, print);
    }

    public static Pair perft(int depth, boolean root, boolean print) {
        List<Short> moves = MoveGeneration.gen();
        List<String> output = new ArrayList<>();

        int nodes = 0;
        for (short move : moves) {
            Move.make(move);

            int branchNodes = depth <= 1 ? 1 : perft(depth - 1, false, print).getNodes();
            nodes += branchNodes;

            if (root) {
                output.add(Move.getAlgebraic(move) + ": " + branchNodes);
            }

            Move.unmake(move);
        }

        if (print) {
            Collections.sort(output);
            for (String s : output) {
                System.out.println(s);
            }
        }

        return new Pair(output, nodes);
    }
}
