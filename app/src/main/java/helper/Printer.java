/*
 * Name: Printer.java
 * Author: Pigpen
 * 
 * Purpose: To visually represent in the terminal data structures for debugging purposes
 */

package helper;

import model.Bitboard;
import model.Board;
import model.Piece;

public class Printer {
    private static final String WHITE = "\u001B[37m";
    private static final String BLUE = "\u001B[34m";
    private static final String RED = "\u001B[31m";
    private static final String PURPLE = "\u001B[35m";

    public static void board() {
        int index = 0;
        int code = 0;

        System.out.println("\n   a b c d e f g h\n");

        for(int i = 8; i > 0; i--) {
            System.out.print(i + " ");

            for(int j = 0; j < 8; j++) {
                index = (i-1) * 8 + j;

                System.out.print(" ");

                code = Board.get(index);

                // check that Board @ index (which holds a code) matches 
                // long bitboard = Bitboard.get(Board.get(index));
                // long other = (1L << index);

                // Board.get(index) -> code
                // Bitboard.get(Board.get(index)) & (1L << index)


                if((Bitboard.get(Board.get(index)) & (1L << index)) == 0) {
                    String expected = "";
                    for(int k = 0; k < Enum.LENGTH; k++) {
                        if((Bitboard.get(k) & (1L << index)) != 0) {
                            if(expected.equals("")) {
                                expected += Enum.codeToString(k);
                            } else {
                                expected += "/" + Enum.codeToString(k);
                            }
                        }
                    }

                    System.out.println("\n\nWARNING: At index " + index + ": Missmatched Board and Bitboard data structures. " +
                        "Expected \"" + expected + "\", but found \"" + Board.getString(index) + "\" instead.");

                    return;
                } else 
                if(Piece.isWhite(code)) {
                    blue(Board.getString(index));
                } else if(Piece.isBlack(code)) {
                    red(Board.getString(index));
                } else {
                    System.out.print(".");
                }
            }
            System.out.println("  " + i);
        }

        System.out.println("\n   a b c d e f g h\n");
    }

    public static void bitboard(String paramString) {
        String[] params = paramString.split(" ");

        for(String param : params) {
            if(param.equals("help")) {
                System.out.println("\nType \"print\" followed by named bitboards separated by spaces (e.g. \"print wpawn bking\")" +
                    "\nto print white pawn and black king bitboards. Use FEN piece notation (e.g. P or k) or\n" + 
                    "\"w\" or \"b\" followed by the piece name (e.g. \"wpawn\" or \"bking\"). Type \"print\" for full board.\n");

                return;
            }
            int key = Enum.stringToCode(param);

            if(key != -1) {
                long bitboard = Bitboard.get(key);

                System.out.println("\n>> " + param);

                for(int i = 7; i >= 0; i--) {
                    for(int j = 0; j < 8; j++) {
                        int index = i * 8 + j;
                        int currentBit = (int) (bitboard >> index & 1);

                        System.out.print(" ");

                        if(currentBit == 1) {
                            purple("1");
                        } else {
                            System.out.print(".");
                        }
                    }
                    System.out.println();
                }
                System.out.println();
            } else {
                System.out.println("Unknown bitboard \"" + param + "\". Type \"print help\" for more.");
            }
        }
    }

    public static void bitboard(long bitboard, String label) {
        System.out.println("\n>> " + label);

        for(int i = 7; i >= 0; i--) {
            for(int j = 0; j < 8; j++) {
                int index = i * 8 + j;
                int currentBit = (int) (bitboard >> index & 1);

                System.out.print(" ");

                if(currentBit == 1) {
                    purple("1");
                } else {
                    System.out.print(".");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    private static void red(String str) {
        System.out.print(RED + str + WHITE);
    }

    private static void blue(String str) {
        System.out.print(BLUE + str + WHITE);
    }

    private static void purple(String str) {
        System.out.print(PURPLE + str + WHITE);
    }
}