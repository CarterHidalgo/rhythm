/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Provides "enums" for pieces/bitboards and methods for converting from char/String to int
 * values
 */

package engine.helper;

public class Enum {
    public static final int BITBOARD_LENGTH = 19;

    public static final int WHITE = 0;
    public static final int BLACK = 8;

    public static final int PAWN = 1;
    public static final int KNIGHT = 2;
    public static final int BISHOP = 3;
    public static final int ROOK = 4;
    public static final int QUEEN = 5;
    public static final int KING = 6;
    public static final int PROMO = 7;

    public static final int EMPTY = 16;
    public static final int OCCUPIED = 17;
    public static final int EP = 18;

    public static final int A1 = 0;
    public static final int E1 = 4;
    public static final int H1 = 7;
    public static final int A8 = 56;
    public static final int E8 = 60;
    public static final int H8 = 63;

    public static final int stringToCode(String piece) {
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
            case "wpromo":
                return WHITE + PROMO;
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
            case "bpromo":
                return BLACK + PROMO;
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

    public static final String codeToString(int code) {
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
            case BLACK + PROMO:
                return "bpromo";
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

    public static final String indexToString(int index) {
        if (index < 0 || index > 63) {
            throw new IllegalArgumentException("Index must be between 0 and 63");
        }

        char file = (char) ('a' + (index % 8));
        int rank = (index / 8) + 1;

        return "" + file + rank;
    }
}
