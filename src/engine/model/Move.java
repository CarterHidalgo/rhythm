/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Contains move, flag bitcodes and methods for creating, making, unmaking, or printing
 * moves (shorts)
 */

package engine.model;

import java.util.Stack;

import engine.helper.Bit;
import engine.helper.Enum;

public class Move {
    /* 
     * Move Flag Enumeration (see https://www.chessprogramming.org/Encoding_Moves)
     * -------------------------------------------------------------------------------
     * | code  promotion  capture	special 1	special 0	kind of move             |
     * |-----------------------------------------------------------------------------|
     * | 0	   0	      0	        0	        0	        quiet moves              |
     * | 1	   0	      0	        0	        1	        double pawn push         |
     * | 2	   0          0         1 	        0	        king castle              |
     * | 3	   0          0         1 	        1	        queen castle             |
     * | 4	   0          1         0 	        0	        captures                 |
     * | 5	   0          1         0 	        1	        ep-capture               |
     * | 8	   1          0         0 	        0	        knight-promotion         |
     * | 9	   1          0         0 	        1	        bishop-promotion         |
     * | 10	   1          0         1 	        0	        rook-promotion           |
     * | 11	   1          0         1 	        1	        queen-promotion          |
     * | 12	   1          1         0 	        0	        knight-promo capture     |
     * | 13	   1          1         0 	        1	        bishop-promo capture     |
     * | 14	   1          1         1 	        0	        rook-promo capture       |
     * | 15	   1          1         1 	        1	        queen-promo capture      |
     * -------------------------------------------------------------------------------
     */

    public static final byte QUIET = 0b0000;
    public static final byte DOUBLE_PAWN = 0b1000;
    public static final byte KING_CASTLE = 0b0100;
    public static final byte QUEEN_CASTLE = 0b1100;
    public static final byte CAPTURE = 0b0010;
    public static final byte EP_CAPTURE = 0b1010;
    public static final byte KNIGHT_PROMO = 0b0001;
    public static final byte BISHOP_PROMO = 0b1001;
    public static final byte ROOK_PROMO = 0b0101;
    public static final byte QUEEN_PROMO = 0b1101;
    public static final byte KNIGHT_PROMO_CAPTURE = 0b0011;
    public static final byte BISHOP_PROMO_CAPTURE = 0b1011;
    public static final byte ROOK_PROMO_CAPTURE = 0b0111;
    public static final byte QUEEN_PROMO_CAPTURE = 0b1111;

    private static Stack<GameState> stateStack = new Stack<>();

    // position startpos moves a2a4 b7b5 a4b5
        
    public static void make(short move) {
        GameState state = new GameState(move);
        stateStack.push(state);

        long fromToBitboard = (1L << state.getFrom()) ^ (1L << state.getTo());

        Bitboard.toggle(fromToBitboard, state.getPiece(), Piece.color(state.getPiece()), Enum.OCCUPIED, Enum.EMPTY);
        Board.set(state.getTo(), state.getPiece());
        Board.set(state.getFrom(), Enum.EMPTY);
        
        if(Move.isCapture(move)) {
            Bitboard.toggle(state.getTo(), state.getTarget(), Enum.BLACK - Piece.color(state.getPiece()), Enum.OCCUPIED, Enum.EMPTY);
        }

        if(Move.isPromotion(move)) {
            Bitboard.toggle(state.getTo(), state.getPiece(), Piece.color(state.getPiece()) + Move.getFlags(move) - 10);
            Board.set(state.getTo(), Piece.color(state.getPiece()) + Move.getFlags(move) - 10);
        }

        GameInfo.makeMove();
    }

    public static void unmake(short move) {
        GameState state = stateStack.pop();
        long fromToBitboard = (1L << state.getFrom()) ^ (1L << state.getTo());

        Bitboard.toggle(fromToBitboard, state.getPiece(), Piece.color(state.getPiece()), Enum.OCCUPIED, Enum.EMPTY);
        Board.set(state.getFrom(), state.getPiece());
        Board.set(state.getTo(), state.getTarget());
        
        if(Move.isCapture(move)) {
            Bitboard.toggle(state.getTo(), state.getTarget(), Enum.BLACK - Piece.color(state.getPiece()), Enum.OCCUPIED, Enum.EMPTY);
        }
        
        if(Move.isPromotion(move)) {
            Bitboard.clear(state.getTo(), Piece.color(state.getPiece()) + Move.getFlags(move) - 10, Piece.color(state.getPiece()));
        }

        GameInfo.unmakeMove();
    }

    public static short create(int from, int to, int flags) {
        return (short) ((from & 0x3F) | ((to & 0x3F) << 6) | ((flags & 0xF) << 12));
    }

    public static int getTo(short move) {
        return ((move >> 6) & 0x3F);
    }

    public static int getFrom(short move) {
        return ((move & 0x3F));
    }

    public static int getFlags(short move) {
        return ((move >> 12) & 0xF);
    }
    
    public static boolean isCapture(short move) {
        return ((move >> 13) & 1) == 1;
    }

    public static boolean isPromotion(short move) {
        return ((move >> 12) & 1) == 1;
    }

    public static boolean isEP(short move) {
        return false;
    }

    public static String getIndexed(short move) {
        byte fromIndex = (byte) (move & 0b111111);
        byte toIndex = (byte) ((move >> 6) & 0b111111);
        byte flags = (byte) ((move >> 12) & 0b1111);

        return fromIndex + " | " + toIndex + " | " + flags;
    }

    public static String getAlgebraic(short move) {
        byte fromIndex = (byte) (move & 0b111111);
        byte toIndex = (byte) ((move >> 6) & 0b111111);
        byte flags = (byte) ((move >> 12) & 0b1111);

        return indexToAlgebraic(fromIndex) + indexToAlgebraic(toIndex) + flagsToAlgebraic(flags);
    }

    private static String indexToAlgebraic(byte index) {
        char file = (char) ('a' + (index % 8));
        int rank = 1 + (index / 8);
        return "" + file + rank;
    }

    private static String flagsToAlgebraic(byte flags) {
        if (Bit.isSet(flags, 3)) {
            switch (flags) {
                case KNIGHT_PROMO:
                case KNIGHT_PROMO_CAPTURE:
                    return "n";
                case BISHOP_PROMO:
                case BISHOP_PROMO_CAPTURE:
                    return "b";
                case ROOK_PROMO:
                case ROOK_PROMO_CAPTURE:
                    return "r";
                case QUEEN_PROMO:
                case QUEEN_PROMO_CAPTURE:
                    return "q";
                default:
                    return "";
            }
        } else {
            return "";
        }
    }
}
