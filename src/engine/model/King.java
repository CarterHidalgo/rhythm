/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Return useful information about the king such as check state
 */

package engine.model;

import engine.helper.Enum;

public class King {
    public static final boolean inCheck(int turn) {
        int kingSquare = Long.numberOfTrailingZeros(Bitboard.get(turn + Enum.KING));

        return AttackTable.squareIsAttacked(kingSquare, Enum.BLACK - turn);
    }
}
