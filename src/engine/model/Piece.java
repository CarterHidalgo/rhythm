/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Methods for understanding piece properties like color
 */

package engine.model;

import engine.helper.Enum;

public class Piece {
    public static final boolean isWhite(int piece) {
        return piece >= Enum.WHITE + Enum.PAWN && piece <= Enum.WHITE + Enum.KING;
    }

    public static final boolean isBlack(int piece) {
        return piece >= Enum.BLACK + Enum.PAWN && piece <= Enum.BLACK + Enum.KING;
    }

    public static final int color(int piece) {
        return (piece >= Enum.WHITE + Enum.PAWN && piece <= Enum.WHITE + Enum.KING) ? Enum.WHITE : Enum.BLACK;
    }
}
