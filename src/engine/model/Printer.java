/*
 * Author: Carter Hidalgo
 * 
 * Purpose: To visually represent in the terminal data structures for debugging purposes
 */

package engine.model;

import java.util.ArrayList;
import java.util.List;

import engine.helper.Enum;

public class Printer {
    private static final String WHITE = "\u001B[37m";
    private static final String BLUE = "\u001B[34m";
    private static final String RED = "\u001B[31m";
    private static final String PURPLE = "\u001B[35m";
    private static final String GREEN = "\u001B[32m";

    public static final void board() {
        int index = 0;
        int code = 0;

        System.out.println("\n   a b c d e f g h\n");

        for(int i = 8; i > 0; i--) {
            System.out.print(i + " ");

            for(int j = 0; j < 8; j++) {
                index = (i-1) * 8 + j;

                System.out.print(" ");

                code = Board.get(index);

                if((Bitboard.get(Board.get(index)) & (1L << index)) == 0) {
                    List<Integer> bitboardHits = new ArrayList<>();
                    for(int k = 0; k < Enum.BITBOARD_LENGTH; k++) {
                        if(k == Enum.WHITE + Enum.PROMO || k == Enum.BLACK + Enum.PROMO || k == Enum.EP) {
                            continue;
                        } else if((Bitboard.get(k) & (1L << index)) != 0) {
                            bitboardHits.add(k);
                        }
                    }
                    
                    int boardValue = Board.get(index);
                    String boardValueStr = Enum.codeToString(boardValue);

                    System.out.println("\n\nBoard has " + boardValueStr + " at index " + index + ", but the " + boardValueStr + " bitboard is ");
                    bitboard(boardValue, boardValueStr);
                    System.out.println("These bitboards are set instead: ");
                    for(int bb : bitboardHits) {
                        bitboard(bb);
                    }
                    System.exit(1);
                } else if(Piece.isWhite(code)) {
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

    public static final void bitboard(String paramString) {
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
                        int currentBit = (int) (bitboard >> index & 1L);

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

    public static final void bitboard(int... codes) {
        for(int code : codes) {
            bitboard(Enum.codeToString(code));
        }
    }

    public static final void bitboard(long bitboard, String label) {
        System.out.println("\n>> " + label);

        for(int i = 7; i >= 0; i--) {
            for(int j = 0; j < 8; j++) {
                int index = i * 8 + j;
                int currentBit = (int) (bitboard >> index & 1L);

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

    public static final void bitboard(long bitboard, int label) {
        bitboard(bitboard, String.valueOf(label));
    }

    public static final void all() {
        for(int i = 0; i < Enum.BITBOARD_LENGTH; i++) {
            Printer.bitboard(i);
        }
        Printer.board();
    }

    public static final void move(short move) {
        System.out.println(Move.getAlgebraic(move));
    }

    public static final void white(String str) {
        System.out.print(WHITE + str + WHITE);
    }

    public static final void red(String str) {
        System.out.print(RED + str + WHITE);
    }

    public static final void red(long num) {
        System.out.print(RED + num + WHITE);
    }

    public static final void blue(String str) {
        System.out.print(BLUE + str + WHITE);
    }

    public static final void purple(String str) {
        System.out.print(PURPLE + str + WHITE);
    }

    public static final void green(String str) {
        System.out.print(GREEN + str + WHITE);
    }

    public static final void green(long num) {
        System.out.print(GREEN + num + WHITE);
    }

    public static String formatUnderscores(long value) {
        String str = Long.toString(value);
        StringBuilder result = new StringBuilder();

        int count = 0;

        for (int i = str.length() - 1; i >= 0; i--) {
            result.append(str.charAt(i));
            count++;

            if (count % 3 == 0 && i != 0) {
                result.append('_');
            }
        }

        return result.reverse().toString();
    }

    public static String formatTimeDuration(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        if (minutes == 0) {
            return seconds + (seconds == 1 ? " second" : " seconds");
        }

        String minutePart = minutes + (minutes == 1 ? " minute" : " minutes");
        String secondPart = seconds + (seconds == 1 ? " second" : " seconds");

        return minutePart + ", " + secondPart;
    }
}