/*
 * Name: Printer.java
 * Author: Pigpen
 * 
 * Purpose: To visually represent in the terminal data structures for debugging purposes
 */

package helper;

import model.Bitboard;
import model.BoardLookup;
import model.Piece;

public class Printer {
    private static final String WHITE = "\u001B[37m";
    private static final String BLUE = "\u001B[34m";
    private static final String RED = "\u001B[31m";
    private static final String PURPLE = "\u001B[35m";

    public static void board() {
        int index = 0;

        System.out.println("\n   a b c d e f g h\n");

        for(int i = 8; i > 0; i--) {
            System.out.print(i + " ");

            for(int j = 0; j < 8; j++) {
                index = (i-1) * 8 + j;

                System.out.print(" ");

                if(Piece.isWhite(BoardLookup.getCode(index))) {
                    blue(BoardLookup.getChar(index));
                } else if(Piece.isBlack(BoardLookup.getCode(index))) {
                    red(BoardLookup.getChar(index));
                } else {
                    System.out.print(".");
                }
            }
            System.out.println("  " + i);
        }

        System.out.println("\n   a b c d e f g h\n");
    }

    public static void bitboard(String param) {
        String[] fields = param.split(" ");
        int key = 0;
        for(int i = 0; i < fields.length; i++) {
            if(!Piece.map.containsKey(fields[i])) {
                System.out.println("Unknown parameter(s) \"" + param + "\". Type help for more information.");
                return;
            } else {
                key += Piece.get(fields[i]);
            }
        }

        long bitboard = Bitboard.get(key);

        System.out.println("\n>> " + param);

        for(int i = 7; i >= 0; i--) {
            for(int j = 0; j < 8; j++) {
                int index = i * 8 + j;
                int currentBit = (int) (bitboard >> index & 1);

                System.out.print(" ");

                if(currentBit == 1) {
                    purple('1');
                } else {
                    System.out.print(".");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void bitboard(long bitboard, String label) {
        System.out.println("\n>> " + label);

        for(int i = 7; i >= 0; i--) {
            for(int j = 0; j < 8; j++) {
                int index = i * 8 + j;
                int currentBit = (int) (bitboard >> index & 1);

                System.out.print(" ");

                if(currentBit == 1) {
                    purple('1');
                } else {
                    System.out.print(".");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    private static void red(char str) {
        System.out.print(RED + str + WHITE);
    }

    private static void blue(char str) {
        System.out.print(BLUE + str + WHITE);
    }

    private static void purple(char str) {
        System.out.print(PURPLE + str + WHITE);
    }
}