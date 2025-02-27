/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Provide constant time attack lookups for sliding pieces - see below for implementation and compacting scheme details
 */

package engine.model.magics;

import java.util.ArrayList;
import engine.helper.Bit;
import engine.helper.Coord;
import engine.helper.Enum;
import engine.model.Bitboard;

public class MagicBitboard {
    /*
    COMPACTING SCHEME 
    (scheme credit Robert Houdart from Houdini see 
    https://www.talkchess.com/forum/viewtopic.php?topic_view=threads&p=368026&t=35858 for original post)

    How I understood and implemented it:

    Number of unique rook attacks by square
        A   B   C   D   E   F   G   H
    -----------------------------------
    8 | 49  42  70  84  84  70  42  49
    7 | 42  36  60  72  72  60  36  42
    6 | 70  60  100 120 120 100 60  70 
    5 | 84  72  120 144 144 120 72  84 
    4 | 84  72  120 144 144 120 72  84
    3 | 70  60  100 120 120 100 60  70
    2 | 42  36  60  72  72  60  36  42
    1 | 49  42  70  84  84  70  42  49
    Total: 4900 distinct attacks

    Number of unique bisohp attacks by square
        A   B   C   D   E   F   G   H
    -----------------------------------
    8 | 7   6   10  12  12  10  6   7
    7 | 6   6   10  12  12  10  6   6
    6 | 10  10  40  48  48  40  10  10 
    5 | 12  12  48  108 108 48  12  12 
    4 | 12  12  48  108 108 48  12  12 
    3 | 10  10  40  48  48  40  10  10 
    2 | 6   6   10  12  12  10  6   6
    1 | 7   6   10  12  12  10  6   7
    Total: 1428 distinct attacks

    Total: 6328 unique rook/bishop attacks

    Conclusions: 
     - There is a maximum of 144 attacks for a rook on any square
     - There is a maximum of 108 attacks for a bishop on any square
     - There is a maximum of 252 attacks for any square

    Instead of storing the attack in the table, we store a 1 byte index into the attack table.
    The attack table will contain the 6328 unique rook/bishop attack sets as shown below. 
    
    ---------------------------------------------------------------------------------------
    | 49 rook | 42 rook | 70 rook | 84 rook | ... | 7 bishop | 6 bishop | 10 bishop | ... |
    ---------------------------------------------------------------------------------------

    Starting from a1 and going to h8 (from [0, 63]) first all rook attacks (the first 4900 entries)
    followed by all bishop attacks (the next and last 1428 entries)

    The attack table size will be (6328 unique attacks * 8 bytes) = 50,624 bytes or ~51 KB
    The lookup table size will be (107,648 combined lookups * 1 byte) = 107,648 bytes or ~108 KB
    The total memory footprint for magic bitboards will be (51 + 108) ~160 KB 
    (Remember we still have to account for array headers and overhead so 160 KB is just an estimate.)

    When indexing into the attack table, the 1 byte can only distinguish 256 different values,
    but fortunately the maximum number of attacks (rook + bishop) for any square is 252, so
    we simply store an offset for each square to determine where the "0" index for that square
    is and then use the 1 byte value from the lookup table to find the correct attack. 

    Ex: Offset for square 2 is 56 (49 + 7) so 56 = starting index for all attacks on square 2 (B1)
    An index of 15 (arbitrarily chosen for this example) would yield an attack table index of 
    71 (56 + 15). The attack table would have the appropriate attack stored at index 71. 
    
    IMPORTANT: We are NOT using the magic numbers or the lookup index to index into the 
    attack table. 
    Magic number + config + shift = lookup index
    lookup index -> 1 byte index
    attack square offset + 1 byte index -> attack
    
    The 107,648 lookups is the sum of all relevant occupancies (i.e. every config for every square
    for both rooks and bishops). Since some squares can be represented with fewer bits than the 
    number of bits in the mask (see https://www.chessprogramming.org/Best_Magics_so_far) this number
    can be reduced, further shrinking the resulting memory footprint. In particular, most squares in
    the top two ranks for rooks can be successfully represented with 1 less bit per square than bits in their
    respective masks while bishops have several groupings spread throughout the board with the same 
    reduction feature. This further compacting scheme has not been implemented since it would only reduce
    the table size by a few kilobytes and the table is already small enough to fit entirely inside my
    1.1 MB L1 cache (translation: I couldn't figure it out)

    Best estimate for total memory footprint: 159.26 KB
    
    */

    private static byte[] lookups = new byte[(102400 + 5248)];
    private static int[] lookupsOffsets = new int[128];
    private static long[] attacks = new long[6328];
    private static short[] attacksOffsets = new short[128];
    private static final byte[] rookShift =  {
        52, 53, 53, 53, 53, 53, 53, 52,
        53, 54, 54, 54, 54, 54, 54, 53,
        53, 54, 54, 54, 54, 54, 54, 53,
        53, 54, 54, 54, 54, 54, 54, 53,
        53, 54, 54, 54, 54, 54, 54, 53,
        53, 54, 54, 54, 54, 54, 54, 53,
        53, 54, 54, 54, 54, 54, 54, 53,
        52, 53, 53, 53, 53, 53, 53, 52
    };
    private static final byte[] bishopShift = {
        58, 59, 59, 59, 59, 59, 59, 58,
        59, 59, 59, 59, 59, 59, 59, 59,
        59, 59, 57, 57, 57, 57, 59, 59,
        59, 59, 57, 55, 55, 57, 59, 59,
        59, 59, 57, 55, 55, 57, 59, 59,
        59, 59, 57, 57, 57, 57, 59, 59,
        59, 59, 59, 59, 59, 59, 59, 59,
        58, 59, 59, 59, 59, 59, 59, 58
    };
    
    public static void init() {
        initAttacks();
        initLookups();
    }

    public static long getRookMoves(int square) {
        return attacks[attacksOffsets[square] + (lookups[transform(Bitboard.get(Enum.OCCUPIED) & MagicBitboardMask.getRookMask(square), MagicNumbers.ROOK_MAGICS[square], rookShift[square]) + lookupsOffsets[square]] & 0xFF)];
    }

    public static long getBishopMoves(int square) {
        return attacks[attacksOffsets[square + 64] + (lookups[transform(Bitboard.get(Enum.OCCUPIED) & MagicBitboardMask.getBishopMask(square), MagicNumbers.BISHOP_MAGICS[square], bishopShift[square]) + lookupsOffsets[square + 64]] & 0xFF)];
    }

    public static long getQueenMoves(int square) {
        return getRookMoves(square) | getBishopMoves(square);
    }

    private static void initAttacks() {
        short attacksIndex = 0;
        long[] blockerSet;
        long attack;
        ArrayList<Long> usedRookAttacks = new ArrayList<>();
        ArrayList<Long> usedBishopAttacks = new ArrayList<>();

        for(byte square = 0; square < 64; square++) {
            blockerSet = createBlockerSet(MagicBitboardMask.getRookMask(square));
            attacksOffsets[square] = attacksIndex;

            for(long config : blockerSet) {
                attack = createAttack(square, config, true);

                if(!usedRookAttacks.contains(attack)) {
                    attacks[attacksIndex++] = attack;
                    usedRookAttacks.add(attack);
                }
            }

            usedRookAttacks.clear();
        }

        for(byte square = 0; square < 64; square++) {
            blockerSet = createBlockerSet(MagicBitboardMask.getBishopMask(square));
            attacksOffsets[square + 64] = attacksIndex;

            for(long config : blockerSet) {
                attack = createAttack(square, config, false);

                if(!usedBishopAttacks.contains(attack)) {
                    attacks[attacksIndex++] = attack;
                    usedBishopAttacks.add(attack);
                }
            }

            usedBishopAttacks.clear(); 
        }
    }

    private static void initLookups() {
        short attacksIndex;
        int lookupsIndex, shift;
        long attack;
        long magic;
        long[] blockerSet;
        int lookupsOffsetCounter = 0;
        
        for(byte square = 0; square < 64; square++) {
            blockerSet = createBlockerSet(MagicBitboardMask.getRookMask(square));
            magic = MagicNumbers.ROOK_MAGICS[square];
            shift = rookShift[square];

            lookupsOffsets[square] = lookupsOffsetCounter;

            for(long config : blockerSet) {
                lookupsIndex = transform(config, magic, shift) + lookupsOffsetCounter;
                attack = createAttack(square, config, true);
                attacksIndex = indexOfAttack(square, attack, true);

                if(attack != attacks[attacksIndex]) {
                    System.out.println("Incorrect attack index returned for rooks; shutting down.");
                    System.exit(1);
                }

                lookups[lookupsIndex] = (byte) (attacksIndex - attacksOffsets[square]);
            }

            lookupsOffsetCounter += blockerSet.length;
        }

        for(byte square = 0; square < 64; square++) {
            blockerSet = createBlockerSet(MagicBitboardMask.getBishopMask(square));
            magic = MagicNumbers.BISHOP_MAGICS[square];
            shift = bishopShift[square];

            lookupsOffsets[square + 64] = lookupsOffsetCounter;

            for(long config : blockerSet) {
                lookupsIndex = transform(config, magic, shift) + lookupsOffsetCounter;
                attack = createAttack(square, config, false);
                attacksIndex = indexOfAttack(square, attack, false);

                if(attacksIndex < 0 || attack != attacks[attacksIndex]) {
                    System.out.println("Incorrect attack index returned for bishops; shutting down.");
                    System.exit(1);
                }

                lookups[lookupsIndex] = (byte) (attacksIndex - attacksOffsets[square + 64]);
            }

            lookupsOffsetCounter += blockerSet.length;
        }
    }

    private static long[] createBlockerSet(long mask) {
        int maxNumConfigs = (int) Math.pow(2, Long.bitCount(mask));
        long[] blockerBitboards = new long[maxNumConfigs];
        long config = 0L;
        short configIndex = 0;

        do {
            blockerBitboards[configIndex++] = config;
            config = (config - mask) & mask;
        } while(config != 0);

        return blockerBitboards;
    }

    private static long createAttack(int square, long config, boolean major) {
        long attack = 0;

        Coord[] directions = (major) ? Coord.rookDirections : Coord.bishopDirections;
        Coord startSquare = new Coord(square);
        
        for(Coord dir : directions) {
            for(byte dist = 1; dist < 8; dist++) {
                Coord attackCoord = startSquare.add(dir.mul(dist));
                
                if(attackCoord.isValid()) {
                    attack |= (1L << attackCoord.getIndex());
                    
                    if(Bit.isSet(config, attackCoord.getIndex())) {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        return attack;
    }

    private static int transform(long config, long magic, int shift) {
        return (int) (((config * magic) >>> (shift)) & 0xFFFFFFFFL);
    }

    private static short indexOfAttack(byte square, long attack, boolean major) {
        int majorOffset = major ? 0 : 64;
        
        for(short i = attacksOffsets[square + majorOffset]; i < attacksOffsets[square + majorOffset] + 144; i++) {
            if(attacks[i] == attack) {
                return i;
            }
        }

        return -1;
    }
}
