/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Lookups for attack patterns for all non-sliding pieces and squareIsAttacked check
 */

package engine.model;

import engine.helper.Enum;
import engine.model.magics.MagicBitboard;

public class AttackTable {
    public static final long[] KNIGHT_ATTACKS = {
        0x20400L, 0x50800L, 0xa1100L, 0x142200L, 
        0x284400L, 0x508800L, 0xa01000L, 0x402000L,
        0x2040004L, 0x5080008L, 0xa110011L, 0x14220022L, 
        0x28440044L, 0x50880088L, 0xa0100010L, 0x40200020L,     
        0x204000402L, 0x508000805L, 0xa1100110aL, 0x1422002214L, 
        0x2844004428L, 0x5088008850L, 0xa0100010a0L, 0x4020002040L,     
        0x20400040200L, 0x50800080500L, 0xa1100110a00L, 0x142200221400L, 
        0x284400442800L, 0x508800885000L, 0xa0100010a000L, 0x402000204000L,     
        0x2040004020000L, 0x5080008050000L, 0xa1100110a0000L, 0x14220022140000L, 
        0x28440044280000L, 0x50880088500000L, 0xa0100010a00000L, 0x40200020400000L,     
        0x204000402000000L, 0x508000805000000L, 0xa1100110a000000L, 0x1422002214000000L, 
        0x2844004428000000L, 0x5088008850000000L, 0xa0100010a0000000L, 0x4020002040000000L,     
        0x400040200000000L, 0x800080500000000L, 0x1100110a00000000L, 0x2200221400000000L, 
        0x4400442800000000L, 0x8800885000000000L, 0x100010a000000000L, 0x2000204000000000L,     
        0x4020000000000L, 0x8050000000000L, 0x110a0000000000L, 0x22140000000000L, 
        0x44280000000000L, 0x88500000000000L, 0x10a00000000000L, 0x20400000000000L
    };
    public static final long[] KING_ATTACKS = {
        0x302L, 0x705L, 0xe0aL, 0x1c14L, 
        0x3828L, 0x7050L, 0xe0a0L, 0xc040L,
        0x30203L, 0x70507L, 0xe0a0eL, 0x1c141cL, 
        0x382838L, 0x705070L, 0xe0a0e0L, 0xc040c0L,
        0x3020300L, 0x7050700L, 0xe0a0e00L, 0x1c141c00L, 
        0x38283800L, 0x70507000L, 0xe0a0e000L, 0xc040c000L,
        0x302030000L, 0x705070000L, 0xe0a0e0000L, 0x1c141c0000L, 
        0x3828380000L, 0x7050700000L, 0xe0a0e00000L, 0xc040c00000L,
        0x30203000000L, 0x70507000000L, 0xe0a0e000000L, 0x1c141c000000L, 
        0x382838000000L, 0x705070000000L, 0xe0a0e0000000L, 0xc040c0000000L,
        0x3020300000000L, 0x7050700000000L, 0xe0a0e00000000L, 0x1c141c00000000L, 
        0x38283800000000L, 0x70507000000000L, 0xe0a0e000000000L, 0xc040c000000000L,
        0x302030000000000L, 0x705070000000000L, 0xe0a0e0000000000L, 0x1c141c0000000000L, 
        0x3828380000000000L, 0x7050700000000000L, 0xe0a0e00000000000L, 0xc040c00000000000L,
        0x203000000000000L, 0x507000000000000L, 0xa0e000000000000L, 0x141c000000000000L, 
        0x2838000000000000L, 0x5070000000000000L, 0xa0e0000000000000L, 0x40c0000000000000L,
    };
    private static final long[] PAWN_ATTACKS = {
        0x0L, 0x0L, 0x0L, 0x0L,
        0x0L, 0x0L, 0x0L, 0x0L,
        0x2L, 0x5L, 0xaL, 0x14L,
        0x28L, 0x50L, 0xa0L, 0x40L,
        0x200L, 0x500L, 0xa00L, 0x1400L,
        0x2800L, 0x5000L, 0xa000L, 0x4000L,
        0x20000L, 0x50000L, 0xa0000L, 0x140000L,
        0x280000L, 0x500000L, 0xa00000L, 0x400000L,
        0x2000000L, 0x5000000L, 0xa000000L, 0x14000000L,
        0x28000000L, 0x50000000L, 0xa0000000L, 0x40000000L,
        0x200000000L, 0x500000000L, 0xa00000000L, 0x1400000000L,
        0x2800000000L, 0x5000000000L, 0xa000000000L, 0x4000000000L,
        0x20000000000L, 0x50000000000L, 0xa0000000000L, 0x140000000000L,
        0x280000000000L, 0x500000000000L, 0xa00000000000L, 0x400000000000L,
        0x2000000000000L, 0x5000000000000L, 0xa000000000000L, 0x14000000000000L,
        0x28000000000000L, 0x50000000000000L, 0xa0000000000000L, 0x40000000000000L,
        0x200000000000000L, 0x500000000000000L, 0xa00000000000000L, 0x1400000000000000L,
        0x2800000000000000L, 0x5000000000000000L, 0xa000000000000000L, 0x4000000000000000L,
        0x0L, 0x0L, 0x0L, 0x0L,
        0x0L, 0x0L, 0x0L, 0x0L,
    };

    public static final boolean squareIsAttacked(int index, int op) {
        long pawn = PAWN_ATTACKS[index + ((op << 1))] & Bitboard.get(op + Enum.PAWN);
        if(pawn != 0) {
            return true;
        }
        
        long knight = KNIGHT_ATTACKS[index] & Bitboard.get(op + Enum.KNIGHT);
        if(knight != 0) {
            return true;
        }
        
        long slider = MagicBitboard.getQueenMoves(index);
        long bishop = slider & Bitboard.BISHOP_MASK[index] & Bitboard.get(op + Enum.BISHOP);
        if(bishop != 0) {
            return true;
        }

        long rook = slider & Bitboard.ROOK_MASK[index] & Bitboard.get(op + Enum.ROOK);
        if(rook != 0) {
            return true;
        }

        long queen = slider & Bitboard.get(op + Enum.QUEEN);
        if(queen != 0) {
            return true;
        }

        long king = KING_ATTACKS[index] & Bitboard.get(op + Enum.KING);
        if(king != 0) {
            return true;
        }

        return false;
    }

    public static final void genPawnAttacks(int turn) {
        for(int i = 0; i < 64; i++) {
            long index = 1L << i;
            long bb = 0;

            if((index & Bitboard.FILE_H) == 0) {
                bb |= 1L << (i + 1);
            } 
            
            if((index & Bitboard.FILE_A) == 0) {
                bb |= 1L << (i - 1);
            }

            System.out.print("0x" + Long.toHexString(bb) + "L, ");

            if(i % 4 == 3) {
                System.out.println();
            }
        }
    }
}
