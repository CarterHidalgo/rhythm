/*
 * Name: BoardLookup.java
 * Author: Pigpen
 * 
 * Purpose: Provide constant time lookup for what piece is on a square given a square index in the range [0-63]
 */

package model;

import helper.Bit;
import helper.FEN;

public class BoardLookup {
    private static byte[] board = new byte[64];

    public static byte getCode(int index) {
        return board[index];
    }

    public static char getChar(int index) {
        return toChar(board[index]);
    }

    public static void setWithFEN(FEN fen) {
        byte rank = 7;
        byte file = 0;

        for(char c : fen.getFEN().toCharArray()) {
            if(c == ' ') {
                break;
            } else if(c == '/') {
                rank--;
                file = 0;
            } else if(Character.isDigit(c)) {
                int numEmpty = Character.getNumericValue(c);

                for(int i = 0; i < numEmpty; i++) {
                    board[rank * 8 + file] = Bit.EMPTY;
                    file++;
                }
            } else {
                board[rank * 8 + file] = toCode(c);
                file++;
            }
        }
    }

    public static void clear() {
        for(int i = 0; i < board.length; i++) {
            board[i] = 0;
        }
    }

    private static byte toCode(char piece) {
        switch(piece) {
            case 'P': return Bit.WHITE_PAWN; 
            case 'N': return Bit.WHITE_KNIGHT;
            case 'B': return Bit.WHITE_BISHOP;
            case 'R': return Bit.WHITE_ROOK;
            case 'Q': return Bit.WHITE_QUEEN;
            case 'K': return Bit.WHITE_KING;
            case 'p': return Bit.BLACK_PAWN; 
            case 'n': return Bit.BLACK_KNIGHT;
            case 'b': return Bit.BLACK_BISHOP;
            case 'r': return Bit.BLACK_ROOK;
            case 'q': return Bit.BLACK_QUEEN;
            case 'k': return Bit.BLACK_KING;
            default: return Bit.EMPTY;
        }
    }

    private static char toChar(byte code) {
        switch(code) {
            case Bit.WHITE_PAWN: return 'P';
            case Bit.WHITE_KNIGHT: return 'N';
            case Bit.WHITE_BISHOP: return 'B';
            case Bit.WHITE_ROOK: return 'R';
            case Bit.WHITE_QUEEN: return 'Q';
            case Bit.WHITE_KING: return 'K';
            case Bit.BLACK_PAWN: return 'p';
            case Bit.BLACK_KNIGHT: return 'n';
            case Bit.BLACK_BISHOP: return 'b';
            case Bit.BLACK_ROOK: return 'r';
            case Bit.BLACK_QUEEN: return 'q';
            case Bit.BLACK_KING: return 'k';
            default: return ' ';
        }
    }
}
