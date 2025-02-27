/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Provides enums for pieces/bitboards and methods for converting from char/String to int
 * values
 */

package engine.helper;

public class Enum {
    public static final int LENGTH = 21;

    public static final int WHITE = 0;
    public static final int BLACK = 9;

    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int ROOK = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;
    public static final int HOME = 7;
    public static final int PROMO = 8;

    public static final int EMPTY = 18;
    public static final int OCCUPIED = 19;
    public static final int EP = 20;

    public static int stringToCode(String piece) {
        switch (piece) {
            case "white":
                return WHITE;
            case "P":
            case "wpawn":
                return WHITE + PAWN;
            case "N":
            case "wknight":
                return WHITE + KNIGHT;
            case "B":
            case "wbishop":
                return WHITE + BISHOP;
            case "R":
            case "wrook":
                return WHITE + ROOK;
            case "Q":
            case "wqueen":
                return WHITE + QUEEN;
            case "K":
            case "wking":
                return WHITE + KING;
            case "black":
                return BLACK;
            case "p":
            case "bpawn":
                return BLACK + PAWN;
            case "n":
            case "bknight":
                return BLACK + KNIGHT;
            case "b":
            case "bbishop":
                return BLACK + BISHOP;
            case "r":
            case "brook":
                return BLACK + ROOK;
            case "q":
            case "bqueen":
                return BLACK + QUEEN;
            case "k":
            case "bking":
                return BLACK + KING;
            case "empty":
                return EMPTY;
            case "occupied":
                return OCCUPIED;
            case "ep":
                return EP;
            default:
                return -1;
        }
    }

    public static String codeToString(int code) {
        switch (code) {
            case WHITE:
                return "white";
            case WHITE + PAWN:
                return "P";
            case WHITE + KNIGHT:
                return "N";
            case WHITE + BISHOP:
                return "B";
            case WHITE + ROOK:
                return "R";
            case WHITE + QUEEN:
                return "Q";
            case WHITE + KING:
                return "K";
            case WHITE + HOME:
                return "whome";
            case WHITE + PROMO:
                return "wpromo";
            case BLACK:
                return "black";
            case BLACK + PAWN:
                return "p";
            case BLACK + KNIGHT:
                return "n";
            case BLACK + BISHOP:
                return "b";
            case BLACK + ROOK:
                return "r";
            case BLACK + QUEEN:
                return "q";
            case BLACK + KING:
                return "k";
            case EMPTY:
                return "empty";
            case OCCUPIED:
                return "occupied";
            case EP:
                return "ep";
            default:
                System.out.println("Unknown code: " + code);
                return ".";
        }
    }
}
