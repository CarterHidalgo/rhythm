/*
 * Name: RhythmUCI.java
 * Author: FrequentlyMissedDeadlines (https://github.com/FrequentlyMissedDeadlines/Chess-UCI) Thanks! =]
 * 
 * Purpose: Implement the UciListner interface with the minimum number of methods to get UCI working; All UCI-related engine operations must be done from here
 */

package uci;

import helper.Printer;
import model.Model;
import model.Piece;

public class RhythmUci implements UciListener {
    @Override
    public String getEngineName() {
        return "rhythm 2.0";
    }

    @Override
    public String getAuthorName() {
        return "Pigpen";
    }

    @Override 
    public void getReady() {
        synchronized(Model.getLock()) {
            while(!Model.getReady()) {
                try {
                    Model.getLock().wait();
                } catch(InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void setPosition(String initPos, String[] moves) {
        Model.setPosition(initPos);
        Model.makeMoves(moves);
    }

    @Override
    public String go(GoParameters parameters) {
        return "[bestmove goes here]";
    }

    @Override 
    public int perft(int depth) {
        int nodes = Model.perft(depth);

        return nodes;
    }

    @Override
    public void print(String param) {
        if(param.isEmpty()) {
            Printer.board();
        } else {
            Printer.bitboard(param);
        }
    }
}
