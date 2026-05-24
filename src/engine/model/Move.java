/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Contains move, flag bitcodes and methods for creating, making, unmaking, or printing
 * moves (shorts)
 */

package engine.model;

import engine.helper.Bit;
import engine.helper.Enum;

public class Move {
    /*
     * Move Flag Enumeration (see https://www.chessprogramming.org/Encoding_Moves)
     * 
     * ------------------------------------------------------------------------------------
     * | <--bit 12--> | <--bit 13--> | <--bit 14--> | <--bit 15--> |                      |
     * | promotion    | capture      | special 1    | special 2    | type of move         |
     * | ---------------------------------------------------------------------------------|
     * | 0              0              0              0              quiet moves          |
     * | 0              0              0              1              double pawn push     |
     * | 0              0              1              0              king castle          |
     * | 0              0              1              1              queen castle         |
     * | 0              1              0              0              captures             |
     * | 0              1              0              1              ep-capture           |
     * | 1              0              0              0              knight-promotion     |
     * | 1              0              0              1              bishop-promotion     |
     * | 1              0              1              0              rook-promotion       |
     * | 1              0              1              1              queen-promotion      |
     * | 1              1              0              0              knight-promo capture |
     * | 1              1              0              1              bishop-promo capture |
     * | 1              1              1              0              rook-promo capture   |
     * | 1              1              1              1              queen-promo capture  |
     * ------------------------------------------------------------------------------------
     */

    public static final byte QUIET = 0b0000;
    public static final byte DOUBLE_PAWN = 0b0001;
    public static final byte KING_CASTLE = 0b0010;
    public static final byte QUEEN_CASTLE = 0b0011;
    public static final byte CAPTURE = 0b0100;
    public static final byte EP_CAPTURE = 0b0101;
    public static final byte KNIGHT_PROMO = 0b1000;
    public static final byte BISHOP_PROMO = 0b1001;
    public static final byte ROOK_PROMO = 0b1010;
    public static final byte QUEEN_PROMO = 0b1011;
    public static final byte KNIGHT_PROMO_CAPTURE = 0b1100;
    public static final byte BISHOP_PROMO_CAPTURE = 0b1101;
    public static final byte ROOK_PROMO_CAPTURE = 0b1110;
    public static final byte QUEEN_PROMO_CAPTURE = 0b1111;
    
    public static final byte INDEX_MASK = 0x3F;
    public static final byte FLAG_MASK = 0xF;
    public static final byte PROMO_MASK = 0b1000;
    
    public static final void make(short move) {
        StateStack.push(move);
        
        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int piece = Board.get(from);
        int target = Board.get(to);
        int self = Piece.color(piece);
        
        long fromToBitboard = (1L << from) ^ (1L << to);
        
        Bitboard.clear(Enum.EP);
        
        Bitboard.toggle(fromToBitboard, piece);
        Bitboard.toggle(fromToBitboard, self);
        Bitboard.toggle(fromToBitboard, Enum.OCCUPIED);
        Bitboard.toggle(fromToBitboard, Enum.EMPTY);
        
        Board.set(to, piece);
        Board.set(from, Enum.EMPTY);
        
        if(Move.isCapture(move) && target != Enum.EMPTY) {
            Bitboard.toggle(to, target);
            Bitboard.toggle(to, Enum.BLACK - self);
            Bitboard.toggle(to, Enum.OCCUPIED);
            Bitboard.toggle(to, Enum.EMPTY);
        }
        
        if(Move.isPromotion(move)) {
            Bitboard.toggle(to, piece);
            Bitboard.toggle(to, self + (Move.getFlags(move) & 0x3) + 2);
            
            Board.set(to, self + (Move.getFlags(move) & 0x3) + 2);
        }
        
        if(Move.isDouble(move)) {
            Bitboard.set(GameInfo.getTurn() == Enum.WHITE ? from + 8 : from - 8, Enum.EP);
        }
        
        if(Move.isEP(move)) {
            int capturedIndex = GameInfo.getTurn() == Enum.WHITE ? to - 8 : to + 8;
            
            Bitboard.toggle(capturedIndex, Enum.BLACK - self + Enum.PAWN);
            Bitboard.toggle(capturedIndex, Enum.BLACK - self);
            Bitboard.toggle(capturedIndex, Enum.OCCUPIED);
            Bitboard.toggle(capturedIndex, Enum.EMPTY);
            
            Board.set(capturedIndex, Enum.EMPTY);
        }
        
        if(Move.isKingCastle(move)) {
            long fromToCastleRook = (1L << (to - 1)) | (1L << (to + 1));
            
            Bitboard.toggle(fromToCastleRook, self + Enum.ROOK);
            Bitboard.toggle(fromToCastleRook, self);
            Bitboard.toggle(fromToCastleRook, Enum.OCCUPIED);
            Bitboard.toggle(fromToCastleRook, Enum.EMPTY);
            
            Board.set(to - 1, self + Enum.ROOK);
            Board.set(to + 1, Enum.EMPTY);
        }
        
        if(Move.isQueenCastle(move)) {
            long fromToCastleRook = (1L << (to - 2)) | (1L << (to + 1));
            
            Bitboard.toggle(fromToCastleRook, self + Enum.ROOK);
            Bitboard.toggle(fromToCastleRook, self);
            Bitboard.toggle(fromToCastleRook, Enum.OCCUPIED);
            Bitboard.toggle(fromToCastleRook, Enum.EMPTY);
            
            Board.set(to + 1, self + Enum.ROOK);
            Board.set(to - 2, Enum.EMPTY);
        }

        GameInfo.make(from, to);
    }
    
    public static final void unmake(short move) {
        GameInfo.unmake();
        
        int from = Move.getFrom(move);
        int to = Move.getTo(move);
        int piece = StateStack.getPiece();
        int target = StateStack.getTarget();
        int self = Piece.color(piece);
        
        long fromToBitboard = (1L << from) ^ (1L << to);
        
        Bitboard.set(StateStack.getEP(), Enum.EP);
        
        Bitboard.toggle(fromToBitboard, piece);
        Bitboard.toggle(fromToBitboard, self);
        Bitboard.toggle(fromToBitboard, Enum.OCCUPIED);
        Bitboard.toggle(fromToBitboard, Enum.EMPTY);

        Board.set(from, piece);
        Board.set(to, target);
        
        if(Move.isCapture(move) && target != Enum.EMPTY) {
            Bitboard.toggle(to, target);
            Bitboard.toggle(to, Enum.BLACK - self);
            Bitboard.toggle(to, Enum.OCCUPIED);
            Bitboard.toggle(to, Enum.EMPTY);
        }
        
        if(Move.isPromotion(move)) {
            Bitboard.clear(to, self + (Move.getFlags(move) & 0x3) + 2);
            Bitboard.clear(to, self);

            Board.set(from, self + Enum.PAWN);
        }

        if(Move.isDouble(move)) {
            Bitboard.clear(Enum.EP);
        }

        if(Move.isEP(move)) {
            int capturedIndex = GameInfo.getTurn() == Enum.WHITE ? to - 8 : to + 8;

            Bitboard.toggle(capturedIndex, Enum.BLACK - self + Enum.PAWN);            
            Bitboard.toggle(capturedIndex, Enum.BLACK - self);
            Bitboard.toggle(capturedIndex, Enum.OCCUPIED);
            Bitboard.toggle(capturedIndex, Enum.EMPTY);

            Board.set(capturedIndex, Enum.BLACK - self + Enum.PAWN);

            Bitboard.set(GameInfo.getTurn() == Enum.WHITE ? to - 8 : to + 8, Enum.EP);
        }

        if(Move.isKingCastle(move)) {
            long fromToCastleRook = (1L << (to - 1)) | (1L << (to + 1));

            Bitboard.toggle(fromToCastleRook, self + Enum.ROOK);
            Bitboard.toggle(fromToCastleRook, self);
            Bitboard.toggle(fromToCastleRook, Enum.OCCUPIED);
            Bitboard.toggle(fromToCastleRook, Enum.EMPTY);

            Board.set(to + 1, self + Enum.ROOK);
            Board.set(to - 1, Enum.EMPTY);
        }

        if(Move.isQueenCastle(move)) {
            long fromToCastleRook = (1L << (to - 2)) | (1L << (to + 1));

            Bitboard.toggle(fromToCastleRook, self + Enum.ROOK);
            Bitboard.toggle(fromToCastleRook, self);
            Bitboard.toggle(fromToCastleRook, Enum.OCCUPIED);
            Bitboard.toggle(fromToCastleRook, Enum.EMPTY);

            Board.set(to - 2, self + Enum.ROOK);
            Board.set(to + 1, Enum.EMPTY);
        }
        
        StateStack.pop();
    }

    public static final boolean isLegal(short move, int turn) {
        Move.make(move);
        
        if(King.inCheck(turn)) {
            Move.unmake(move);
            
            return false;
        }

        int op = Enum.BLACK - turn;

        if(Move.isQueenCastle(move)) {
            int to = Move.getTo(move);
            if(AttackTable.squareIsAttacked(to + 1, op) || AttackTable.squareIsAttacked(to + 2, op)) {
                Move.unmake(move);

                return false;
            }
        }

        if(Move.isKingCastle(move)) {
            int to = Move.getTo(move);
            if (AttackTable.squareIsAttacked(to - 1, op) || AttackTable.squareIsAttacked(to - 2, op)) {
                Move.unmake(move);

                return false;
            }
        }

        Move.unmake(move);

        return true;
    }

    public static final short create(int from, int to, int flags) {
        return (short) ((from & Move.INDEX_MASK) | ((to & Move.INDEX_MASK) << 6) | ((flags & Move.FLAG_MASK) << 12));
    }

    public static final int getTo(short move) {
        return ((move >>> 6) & Move.INDEX_MASK);
    }

    public static final int getFrom(short move) {
        return ((move & Move.INDEX_MASK));
    }

    public static final int getFlags(short move) {
        return ((move >>> 12) & Move.FLAG_MASK);
    }
    
    public static final boolean isCapture(short move) {
        return ((move >>> 12) & Move.CAPTURE) != 0;
    }

    public static final boolean isPromotion(short move) {
        return ((move >>> 12) & Move.PROMO_MASK) != 0;
    }

    public static final boolean isDouble(short move) {
        return ((move >>> 12) & Move.FLAG_MASK) == DOUBLE_PAWN;
    }

    public static final boolean isEP(short move) {
        return ((move >>> 12) & Move.FLAG_MASK) == EP_CAPTURE;
    }

    public static final boolean isQueenCastle(short move) {
        return ((move >>> 12) & Move.FLAG_MASK) == QUEEN_CASTLE;
    }

    public static final boolean isKingCastle(short move) {
        return ((move >>> 12) & Move.FLAG_MASK) == KING_CASTLE;
    }

    public static final int getIndex(String algebraic) {
        return algebraic.equals("-") ? 0 : (algebraic.charAt(1) - '1') * 8 + (algebraic.charAt(0) - 'a');
    }

    public static final String getAlgebraic(short move) {
        byte fromIndex = (byte) (move & Move.INDEX_MASK);
        byte toIndex = (byte) ((move >>> 6) & Move.INDEX_MASK);
        byte flags = (byte) ((move >>> 12) & Move.FLAG_MASK);

        return indexToAlgebraic(fromIndex) + indexToAlgebraic(toIndex) + flagsToAlgebraic(flags);
    }

    private static final String indexToAlgebraic(byte index) {
        char file = (char) ('a' + (index % 8));
        int rank = 1 + (index / 8);
        return "" + file + rank;
    }

    private static final String flagsToAlgebraic(byte flags) {
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
