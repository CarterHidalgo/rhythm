/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Contains dynamic and static bitboards as well as the methods for getting and setting
 * them
 */

package engine.model;

import java.util.Arrays;

import engine.helper.Coord;
import engine.helper.Enum;
import engine.helper.FEN;

public class Bitboard {
    public static final long FILE_A = 0x101010101010101L;
    public static final long FILE_B = 0x202020202020202L;
    public static final long FILE_C = 0x404040404040404L;
    public static final long FILE_D = 0x808080808080808L;
    public static final long FILE_E = 0x1010101010101010L;
    public static final long FILE_F = 0x2020202020202020L;
    public static final long FILE_G = 0x4040404040404040L;
    public static final long FILE_H = 0x8080808080808080L;

    public static final long RANK_1 = 0xffL;
    public static final long RANK_2 = 0xff00L;
    public static final long RANK_3 = 0xff0000L;
    public static final long RANK_4 = 0xff000000L;
    public static final long RANK_5 = 0xff00000000L;
    public static final long RANK_6 = 0xff0000000000L;
    public static final long RANK_7 = 0xff000000000000L;
    public static final long RANK_8 = 0xff00000000000000L;

    public static final long[] ROOK_MASK = {
        0x1010101010101feL, 0x2020202020202fdL, 0x4040404040404fbL, 0x8080808080808f7L, 
        0x10101010101010efL, 0x20202020202020dfL, 0x40404040404040bfL, 0x808080808080807fL, 
        0x10101010101fe01L, 0x20202020202fd02L, 0x40404040404fb04L, 0x80808080808f708L, 
        0x101010101010ef10L, 0x202020202020df20L, 0x404040404040bf40L, 0x8080808080807f80L, 
        0x101010101fe0101L, 0x202020202fd0202L, 0x404040404fb0404L, 0x808080808f70808L, 
        0x1010101010ef1010L, 0x2020202020df2020L, 0x4040404040bf4040L, 0x80808080807f8080L, 
        0x1010101fe010101L, 0x2020202fd020202L, 0x4040404fb040404L, 0x8080808f7080808L, 
        0x10101010ef101010L, 0x20202020df202020L, 0x40404040bf404040L, 0x808080807f808080L, 
        0x10101fe01010101L, 0x20202fd02020202L, 0x40404fb04040404L, 0x80808f708080808L, 
        0x101010ef10101010L, 0x202020df20202020L, 0x404040bf40404040L, 0x8080807f80808080L, 
        0x101fe0101010101L, 0x202fd0202020202L, 0x404fb0404040404L, 0x808f70808080808L, 
        0x1010ef1010101010L, 0x2020df2020202020L, 0x4040bf4040404040L, 0x80807f8080808080L, 
        0x1fe010101010101L, 0x2fd020202020202L, 0x4fb040404040404L, 0x8f7080808080808L, 
        0x10ef101010101010L, 0x20df202020202020L, 0x40bf404040404040L, 0x807f808080808080L, 
        0xfe01010101010101L, 0xfd02020202020202L, 0xfb04040404040404L, 0xf708080808080808L, 
        0xef10101010101010L, 0xdf20202020202020L, 0xbf40404040404040L, 0x7f80808080808080L,
    };
    public static final long[] BISHOP_MASK = {
        0x8040201008040200L, 0x80402010080500L, 0x804020110a00L, 0x8041221400L, 
        0x182442800L, 0x10204885000L, 0x102040810a000L, 0x102040810204000L, 
        0x4020100804020002L, 0x8040201008050005L, 0x804020110a000aL, 0x804122140014L, 
        0x18244280028L, 0x1020488500050L, 0x102040810a000a0L, 0x204081020400040L, 
        0x2010080402000204L, 0x4020100805000508L, 0x804020110a000a11L, 0x80412214001422L, 
        0x1824428002844L, 0x102048850005088L, 0x2040810a000a010L, 0x408102040004020L, 
        0x1008040200020408L, 0x2010080500050810L, 0x4020110a000a1120L, 0x8041221400142241L, 
        0x182442800284482L, 0x204885000508804L, 0x40810a000a01008L, 0x810204000402010L, 
        0x804020002040810L, 0x1008050005081020L, 0x20110a000a112040L, 0x4122140014224180L, 
        0x8244280028448201L, 0x488500050880402L, 0x810a000a0100804L, 0x1020400040201008L, 
        0x402000204081020L, 0x805000508102040L, 0x110a000a11204080L, 0x2214001422418000L, 
        0x4428002844820100L, 0x8850005088040201L, 0x10a000a010080402L, 0x2040004020100804L, 
        0x200020408102040L, 0x500050810204080L, 0xa000a1120408000L, 0x1400142241800000L, 
        0x2800284482010000L, 0x5000508804020100L, 0xa000a01008040201L, 0x4000402010080402L, 
        0x2040810204080L, 0x5081020408000L, 0xa112040800000L, 0x14224180000000L, 
        0x28448201000000L, 0x50880402010000L, 0xa0100804020100L, 0x40201008040201L, 
    };
    public static int[] CASTLE_MASK = new int[64];
    
    static {
        Arrays.fill(CASTLE_MASK, 0xF);
        CASTLE_MASK[Enum.A1] = ~GameInfo.WQ;
        CASTLE_MASK[Enum.H1] = ~GameInfo.WK;
        CASTLE_MASK[Enum.E1] = ~(GameInfo.WQ | GameInfo.WK);

        CASTLE_MASK[Enum.A8] = ~GameInfo.BQ;
        CASTLE_MASK[Enum.H8] = ~GameInfo.BK;
        CASTLE_MASK[Enum.E8] = ~(GameInfo.BQ | GameInfo.BK);
    }

    private static long[] bitboards = new long[Enum.BITBOARD_LENGTH];

    public static final long get(int code) {
        return bitboards[code];
    }

    public static final long getSelf(int code) {
        return bitboards[GameInfo.getTurn() + code];
    }

    public static final long getSelf() {
        return bitboards[GameInfo.getTurn()];
    }

    public static final long getOp(int code) {
        return bitboards[GameInfo.getTurn() + code];
    }

    public static final long getOp() {
        return bitboards[Enum.BLACK - GameInfo.getTurn()];
    }

    public static final long getOcc() {
        return bitboards[Enum.OCCUPIED];
    }

    public static final long getEmpty() {
        return bitboards[Enum.EMPTY];
    }
    
    public static final void set(int index, int code) {
        bitboards[code] |= 1L << index;
    }
    
    public static final void clear(int index, int code) {
        bitboards[code] &= ~(1L << index);
    }

    public static final void clear(int code) {
        bitboards[code] = 0L;
    }

    public static final void toggle(int index, int code) {
        bitboards[code] ^= (1L << index);
    }

    public static final void toggle(long bitboard, int code) {
        bitboards[code] ^= bitboard;
    }

    public static final void setWithFEN(FEN fen) {
        int rank = 7;
        int file = 0;

        clearAll();

        for (char c : fen.getFEN().toCharArray()) {
            if (c == '/') {
                rank--;
                file = 0;
            } else if (Character.isDigit(c)) {
                file += Character.getNumericValue(c);
            } else {
                int code = Enum.stringToCode(Character.toString(c));

                bitboards[code] |= 1L << (rank * 8 + file++);
            }
        }

        for (int i = Enum.WHITE + Enum.PAWN; i <= Enum.WHITE + Enum.KING; i++) {
            bitboards[Enum.WHITE] |= bitboards[i];
        }

        for (int i = Enum.BLACK + Enum.PAWN; i <= Enum.BLACK + Enum.KING; i++) {
            bitboards[Enum.BLACK] |= bitboards[i];
        }

        bitboards[Enum.OCCUPIED] = bitboards[Enum.WHITE] | bitboards[Enum.BLACK];
        bitboards[Enum.EMPTY] = ~bitboards[Enum.OCCUPIED];

        bitboards[Enum.WHITE + Enum.PROMO] = 0xFF00000000000000L;
        bitboards[Enum.BLACK + Enum.PROMO] = 0xFFL;

        if (!fen.getEp().equals("-")) {
            bitboards[Enum.EP] = 1L << Move.getIndex(fen.getEp());
        }
    }

    public static final void clearAll() {
        for (int i = 0; i < bitboards.length; i++) {
            bitboards[i] = 0L;
        }
    }

    public static final void genSliderMasks() {
        System.out.println("\nRook Masks");
        for(int i = 0; i < 64; i++) {
            long mask = genSliderMasks(true, i);

            System.out.print("0x" + Long.toHexString(mask) + "L, " + (((i+1) % 4 == 0) ? "\n" : ""));
        }

        System.out.println("\nBishop Masks");
        for (int i = 0; i < 64; i++) {
            long mask = genSliderMasks(false, i);

            System.out.print("0x" + Long.toHexString(mask) + "L, " + (((i + 1) % 4 == 0) ? "\n" : ""));
        }
    }

    private static final long genSliderMasks(boolean major, int square) {
        long attack = 0;

        Coord[] directions = (major) ? Coord.rookDirections : Coord.bishopDirections;
        Coord startSquare = new Coord(square);
        
        for(Coord dir : directions) {
            for(byte dist = 1; dist < 8; dist++) {
                Coord attackCoord = startSquare.add(dir.mul(dist));
                
                if(attackCoord.isValid()) {
                    attack |= (1L << attackCoord.getIndex());
                } else {
                    break;
                }
            }
        }

        return attack;
    }
}
