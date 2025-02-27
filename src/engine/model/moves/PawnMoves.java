/*
 * Name: PawnAttacks.java
 * Author: Pigpen
 * 
 * Purpose: Constant time lookup for all possible pawn attacks on any square
 */


package engine.model.moves;

import engine.helper.Bit;

public class PawnMoves {
    private static final Long[] PAWN_MOVES = {
        0x1030000L, // 8
        0x2070000L, // 9
        0x40e0000L, // 10
        0x81c0000L, // 11
        0x10380000L, // 12
        0x20700000L, // 13
        0x40e00000L, // 14
        0x80c00000L, // 15
        
        0x3000000L, // 16
        0x7000000L, // 17
        0xe000000L, // 18
        0x1c000000L, // 19
        0x38000000L, // 20
        0x70000000L, // 21
        0xe0000000L, // 22
        0xc0000000L, // 23
        
        0x300000000L, // 24
        0x700000000L, // 25
        0xe00000000L, // 26
        0x1c00000000L, // 27
        0x3800000000L, // 28
        0x7000000000L, // 29
        0xe000000000L, // 30
        0xc000000000L, // 31
        
        0x30000000000L, // 32
        0x70000000000L, // 33
        0xe0000000000L, // 34
        0x1c0000000000L, // 35
        0x380000000000L, // 36
        0x700000000000L, // 37
        0xe00000000000L, // 38
        0xc00000000000L, // 39
        
        0x3000000000000L, // 40
        0x7000000000000L, // 41
        0xe000000000000L, // 42
        0x1c000000000000L, // 43
        0x38000000000000L, // 44
        0x70000000000000L, // 45
        0xe0000000000000L, // 46
        0xc0000000000000L, // 47
        
        0x300000000000000L, // 48
        0x700000000000000L, // 49
        0xe00000000000000L, // 50
        0x1c00000000000000L, // 51
        0x3800000000000000L, // 52
        0x7000000000000000L, // 53
        0xe000000000000000L, // 54
        0xc000000000000000L, // 55
    };

    public static Long get(int square) {
        if(square < 0 || square > 63) {
            square %= 64;
            System.out.println("WARNING: square has been modulated in getKnightAttacks() -> KnightAttacks.java");
        }

        return PAWN_MOVES[square];
    }

    public static void gen() {
        long bitboard = 0;
        int index;

        for(int rank = 1; rank < 7; rank++) {
            for(int file = 0; file < 8; file++) {
                index = rank * 8 + file;

                if(file == 0) {
                    bitboard = Bit.set(bitboard, index + 9);
                } else if(file == 7) {
                    bitboard = Bit.set(bitboard, index + 7);
                } else {
                    bitboard = Bit.set(bitboard, index + 7);
                    bitboard = Bit.set(bitboard, index + 9);
                }

                if(rank == 1) {
                    bitboard = Bit.set(bitboard, index + 16);
                }

                bitboard = Bit.set(bitboard, index + 8);

                System.out.println("0x" + Long.toHexString(bitboard) + "L, // " + index);
                bitboard = 0;
            }
            System.out.println();
        }
    }
}
