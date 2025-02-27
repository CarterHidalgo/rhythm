/*
 * Name: Board.java Author: Pigpen
 * 
 * Purpose: Provide constant time lookup for what piece is on a square given a square index in the
 * range [0-63]
 */

package engine.model;

import engine.helper.Enum;
import engine.helper.FEN;

public class Board {
    private static int[] board = new int[64];

    public static int get(int index) {
        return board[index];
    }

    public static void set(int index, int code) {
        board[index] = code;
    }

    public static String getString(int index) {
        return Enum.codeToString(board[index]);
    }

    public static void setWithFEN(FEN fen) {
        byte rank = 7;
        byte file = 0;

        for (char c : fen.getFEN().toCharArray()) {
            if (c == ' ') {
                break;
            } else if (c == '/') {
                rank--;
                file = 0;
            } else if (Character.isDigit(c)) {
                int numEmpty = Character.getNumericValue(c);

                for (int i = 0; i < numEmpty; i++) {
                    board[rank * 8 + file] = Enum.EMPTY;
                    file++;
                }
            } else {
                board[rank * 8 + file] = Enum.stringToCode(Character.toString(c));
                file++;
            }
        }
    }

    public static void clear() {
        for (int i = 0; i < board.length; i++) {
            board[i] = 0;
        }
    }
}
