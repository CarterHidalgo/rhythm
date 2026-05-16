/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Create a list of all pseudolegal/legal moves given the current position
 */

package engine.model;

import engine.helper.Enum;
import engine.model.magics.MagicBitboard;

public class MoveGeneration {
    public static final MoveList genLegal(MoveList legalMoves) {
        legalMoves.clear();
        legalMoves = genPseudo(legalMoves);

        return legalMoves;
    }

    public static final MoveList genPseudo(MoveList pseudoMoves) {
        pseudoMoves.clear();

        addPawnMoves(pseudoMoves);
        addKnightMoves(pseudoMoves);
        addBishopMoves(pseudoMoves);
        addRookMoves(pseudoMoves);
        addQueenMoves(pseudoMoves);
        addKingMoves(pseudoMoves);
        
        return pseudoMoves;
    }

    public static final void addPawnMoves(MoveList moves) {
        boolean white = GameInfo.getTurn() == Enum.WHITE;

        if(white) {
            addWhitePawns(moves);
        } else {
            addBlackPawns(moves);
        }
    }

    private static final void addWhitePawns(MoveList moves) {
        long pawns = Bitboard.getSelf(Enum.PAWN);
        long op = Bitboard.getOp();
        long empty = Bitboard.get(Enum.EMPTY);
        long promo = Bitboard.getSelf(Enum.PROMO);

        long leftCaptures, rightCaptures, quiets, doubles, promos, leftCapturePromos, rightCapturePromos;

        quiets = (pawns << 8) & empty;
        doubles = (quiets & Bitboard.RANK_3 << 8) & empty;
        leftCaptures = (pawns << 7) & op & ~Bitboard.FILE_A;
        rightCaptures = (pawns << 9) & op & ~Bitboard.FILE_H;
        promos = quiets & promo;
        leftCapturePromos = leftCaptures & op & promo;
        rightCapturePromos = rightCaptures & op & promo;

        while (pawns != 0) {
            while (leftCaptures != 0) {
                int to = Long.numberOfTrailingZeros(leftCaptures);
                moves.add(Move.create(to - 7, to, Move.CAPTURE));
                leftCaptures &= leftCaptures - 1;
            }

            while (rightCaptures != 0) {
                int to = Long.numberOfTrailingZeros(rightCaptures);
                moves.add(Move.create(to - 9, to, Move.CAPTURE));
                rightCaptures &= rightCaptures - 1;
            }

            while (quiets != 0) {
                int to = Long.numberOfTrailingZeros(quiets);
                moves.add(Move.create(to - 8, to, Move.QUIET));
                quiets &= quiets - 1;
            }

            while (doubles != 0) {
                int to = Long.numberOfTrailingZeros(doubles);
                moves.add(Move.create(to - 16, to, Move.DOUBLE_PAWN));
                doubles &= doubles - 1;
            }

            while (promos != 0) {
                int to = Long.numberOfTrailingZeros(promos);
                moves.add(Move.create(to - 8, to, Move.KNIGHT_PROMO));
                moves.add(Move.create(to - 8, to, Move.BISHOP_PROMO));
                moves.add(Move.create(to - 8, to, Move.ROOK_PROMO));
                moves.add(Move.create(to - 8, to, Move.QUEEN_PROMO));
                promos &= promos - 1;
            }

            while (leftCapturePromos != 0) {
                int to = Long.numberOfTrailingZeros(leftCapturePromos);
                moves.add(Move.create(to - 7, to, Move.KNIGHT_PROMO_CAPTURE));
                moves.add(Move.create(to - 7, to, Move.BISHOP_PROMO_CAPTURE));
                moves.add(Move.create(to - 7, to, Move.ROOK_PROMO_CAPTURE));
                moves.add(Move.create(to - 7, to, Move.QUEEN_PROMO_CAPTURE));
                leftCapturePromos &= leftCapturePromos - 1;
            }

            while (rightCapturePromos != 0) {
                int to = Long.numberOfTrailingZeros(rightCapturePromos);
                moves.add(Move.create(to - 9, to, Move.KNIGHT_PROMO_CAPTURE));
                moves.add(Move.create(to - 9, to, Move.BISHOP_PROMO_CAPTURE));
                moves.add(Move.create(to - 9, to, Move.ROOK_PROMO_CAPTURE));
                moves.add(Move.create(to - 9, to, Move.QUEEN_PROMO_CAPTURE));
                rightCapturePromos &= rightCapturePromos - 1;
            }

            pawns &= pawns - 1;
        }
    }

    private static final void addBlackPawns(MoveList moves) {
        long pawns = Bitboard.getSelf(Enum.PAWN);
        long op = Bitboard.getOp();
        long empty = Bitboard.get(Enum.EMPTY);
        long promo = Bitboard.getSelf(Enum.PROMO);

        long leftCaptures, rightCaptures, quiets, doubles, promos, leftCapturePromos, rightCapturePromos;

        quiets = (pawns >>> 8) & empty;
        doubles = (quiets & Bitboard.RANK_6 >>> 8) & empty;
        leftCaptures = (pawns >>> 7) & op & ~Bitboard.FILE_H;
        rightCaptures = (pawns >>> 9) & op & ~Bitboard.FILE_A;
        promos = quiets & promo;
        leftCapturePromos = leftCaptures & op & promo;
        rightCapturePromos = rightCaptures & op & promo;

        while (pawns != 0) {
            while (leftCaptures != 0) {
                int to = Long.numberOfTrailingZeros(leftCaptures);
                moves.add(Move.create(to + 7, to, Move.CAPTURE));
                leftCaptures &= leftCaptures - 1;
            }

            while (rightCaptures != 0) {
                int to = Long.numberOfTrailingZeros(rightCaptures);
                moves.add(Move.create(to + 9, to, Move.CAPTURE));
                rightCaptures &= rightCaptures - 1;
            }

            while (quiets != 0) {
                int to = Long.numberOfTrailingZeros(quiets);
                moves.add(Move.create(to + 8, to, Move.QUIET));
                quiets &= quiets - 1;
            }

            while (doubles != 0) {
                int to = Long.numberOfTrailingZeros(doubles);
                moves.add(Move.create(to + 16, to, Move.DOUBLE_PAWN));
                doubles &= doubles - 1;
            }

            while (promos != 0) {
                int to = Long.numberOfTrailingZeros(promos);
                moves.add(Move.create(to + 8, to, Move.KNIGHT_PROMO));
                moves.add(Move.create(to + 8, to, Move.BISHOP_PROMO));
                moves.add(Move.create(to + 8, to, Move.ROOK_PROMO));
                moves.add(Move.create(to + 8, to, Move.QUEEN_PROMO));
                promos &= promos - 1;
            }

            while (leftCapturePromos != 0) {
                int to = Long.numberOfTrailingZeros(leftCapturePromos);
                moves.add(Move.create(to + 7, to, Move.KNIGHT_PROMO_CAPTURE));
                moves.add(Move.create(to + 7, to, Move.BISHOP_PROMO_CAPTURE));
                moves.add(Move.create(to + 7, to, Move.ROOK_PROMO_CAPTURE));
                moves.add(Move.create(to + 7, to, Move.QUEEN_PROMO_CAPTURE));
                leftCapturePromos &= leftCapturePromos - 1;
            }

            while (rightCapturePromos != 0) {
                int to = Long.numberOfTrailingZeros(rightCapturePromos);
                moves.add(Move.create(to + 9, to, Move.KNIGHT_PROMO_CAPTURE));
                moves.add(Move.create(to + 9, to, Move.BISHOP_PROMO_CAPTURE));
                moves.add(Move.create(to + 9, to, Move.ROOK_PROMO_CAPTURE));
                moves.add(Move.create(to + 9, to, Move.QUEEN_PROMO_CAPTURE));
                rightCapturePromos &= rightCapturePromos - 1;
            }

            pawns &= pawns - 1;
        }
    }

    private static final void addKnightMoves(MoveList moves) {
        long to, captures, quiets;
        long self = Bitboard.getSelf();
        long knights = Bitboard.getSelf(Enum.KNIGHT);
        long op = Bitboard.getOp();
        int from;

        while (knights != 0) {
            from = Long.numberOfTrailingZeros(knights);
            to = AttackTable.getKnightAttacks(from) & ~self;

            captures = to & op;
            quiets = to & ~op;

            while(captures != 0) {
                moves.add(Move.create(from, Long.numberOfTrailingZeros(captures), Move.CAPTURE));
                captures &= captures - 1;
            }

            while(quiets != 0) {
                moves.add(Move.create(from, Long.numberOfTrailingZeros(quiets), Move.QUIET));
                quiets &= quiets - 1;
            }

            knights &= knights - 1;
        }
    }

    private static final void addBishopMoves(MoveList moves) {
        long to, captures, quiets;
        long self = Bitboard.getSelf();
        long bishops = Bitboard.getSelf(Enum.BISHOP);
        long op = Bitboard.getOp();
        int from;

        while (bishops != 0) {
            from = Long.numberOfTrailingZeros(bishops);
            to = MagicBitboard.getBishopMoves(from) & ~self;

            captures = to & op;
            quiets = to & ~op;

            while(captures != 0) {
                moves.add(Move.create(from, Long.numberOfTrailingZeros(captures), Move.CAPTURE));
                captures &= captures - 1;
            }

            while(quiets != 0) {
                moves.add(Move.create(from, Long.numberOfTrailingZeros(quiets), Move.QUIET));
                quiets &= quiets - 1;
            }

            bishops &= bishops - 1;
        }
    }

    private static final void addRookMoves(MoveList moves) {
        long to, captures, quiets;
        long self = Bitboard.getSelf();
        long rooks = Bitboard.getSelf(Enum.ROOK);
        long op = Bitboard.getOp();
        int from;

        while (rooks != 0) {
            from = Long.numberOfTrailingZeros(rooks);
            to = MagicBitboard.getRookMoves(from) & ~self;

            captures = to & op;
            quiets = to & ~op;

            while (captures != 0) {
                moves.add(Move.create(from, Long.numberOfTrailingZeros(captures), Move.CAPTURE));
                captures &= captures - 1;
            }

            while (quiets != 0) {
                moves.add(Move.create(from, Long.numberOfTrailingZeros(quiets), Move.QUIET));
                quiets &= quiets - 1;
            }

            rooks &= rooks - 1;
        }
    }

    private static final void addQueenMoves(MoveList moves) {
        long to, captures, quiets;
        long self = Bitboard.getSelf();
        long queens = Bitboard.getSelf(Enum.QUEEN);
        long op = Bitboard.getOp();
        int from;

        while (queens != 0) {
            from = Long.numberOfTrailingZeros(queens);
            to = MagicBitboard.getQueenMoves(from) & ~self;

            captures = to & op;
            quiets = to & ~op;

            while (captures != 0) {
                moves.add(Move.create(from, Long.numberOfTrailingZeros(captures), Move.CAPTURE));
                captures &= captures - 1;
            }

            while (quiets != 0) {
                moves.add(Move.create(from, Long.numberOfTrailingZeros(quiets), Move.QUIET));
                quiets &= quiets - 1;
            }

            queens &= queens - 1;
        }
    }

    private static final void addKingMoves(MoveList moves) {
        long to, captures, quiets;
        long self = Bitboard.getSelf();
        long kings = Bitboard.getSelf(Enum.KING);
        long op = Bitboard.getOp();
        int from;

        while (kings != 0) {
            from = Long.numberOfTrailingZeros(kings);
            to = AttackTable.getKingAttacks(from) & ~self;

            captures = to & op;
            quiets = to & ~op;

            while (captures != 0) {
                moves.add(Move.create(from, Long.numberOfTrailingZeros(captures), Move.CAPTURE));
                captures &= captures - 1;
            }

            while (quiets != 0) {
                moves.add(Move.create(from, Long.numberOfTrailingZeros(quiets), Move.QUIET));
                quiets &= quiets - 1;
            }

            kings &= kings - 1;

            // if (GameInfo.getCastleRights(GameInfo.getTurn(), 0) &&
            // Bit.isSet(Bitboard.getSelf(Enum.ROOK), from + 3) &&
            // !Bit.isSet(Bitboard.get(Enum.OCCUPIED), from + 1) &&
            // !Bit.isSet(Bitboard.get(Enum.OCCUPIED), from + 2)) {
            // moves.add(Move.create(from, from + 2, Move.KING_CASTLE));
            // }

            // if (GameInfo.getCastleRights(GameInfo.getTurn(), 1) &&
            // Bit.isSet(Bitboard.getSelf(Enum.ROOK), from + 3) &&
            // !Bit.isSet(Bitboard.get(Enum.OCCUPIED), from - 1) &&
            // !Bit.isSet(Bitboard.get(Enum.OCCUPIED), from - 2) &&
            // !Bit.isSet(Bitboard.get(Enum.OCCUPIED), from - 3)) {
            // moves.add(Move.create(from, from - 2, Move.QUEEN_CASTLE));
            // }
        }
    }
}
