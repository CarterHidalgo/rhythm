/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Create a list of all pseudolegal/legal moves given the current position
 */

package engine.model;

import engine.helper.Enum;
import engine.model.magics.MagicBitboard;

public class MoveGeneration {
    private static MoveList temp = new MoveList();

    public static final MoveList genLegal(MoveList legalMoves) {
        int turn = GameInfo.getTurn();

        // if(King.inCheck(turn)) {
            // special move generation when king is in check
        // }

        temp.clear();
        temp = genPseudo(temp);

        short move;
        for(int i = 0; i < temp.size(); i++) {
            move = temp.get(i);
            
            if(Move.isLegal(move, turn)) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }

    public static final MoveList genPseudo(MoveList pseudoMoves) {
        pseudoMoves.clear();

        long self = Bitboard.getSelf();
        long op = Bitboard.getOp();
        long empty = Bitboard.getEmpty();
        long promo = Bitboard.getSelf(Enum.PROMO);
        long ep = Bitboard.get(Enum.EP);

        addPawnMoves(pseudoMoves, self, op, empty, promo, ep);
        addKnightMoves(pseudoMoves, self, op, empty, promo);
        addBishopMoves(pseudoMoves, self, op, empty, promo);
        addRookMoves(pseudoMoves, self, op, empty, promo);
        addQueenMoves(pseudoMoves, self, op, empty, promo);
        addKingMoves(pseudoMoves, self, op, empty, promo);
        
        return pseudoMoves;
    }

    private static final void addPawnMoves(MoveList moves, long self, long op, long empty, long promo, long ep) {
        boolean white = GameInfo.getTurn() == Enum.WHITE;

        if(white) {
            addWhitePawns(moves, self, op, empty, promo, ep);
        } else {
            addBlackPawns(moves, self, op, empty, promo, ep);
        }
    }

    private static final void addWhitePawns(MoveList moves, long self, long op, long empty, long promo, long ep) {
        long pawns = Bitboard.getSelf(Enum.PAWN);        
        long pushes = (pawns << 8) & empty;
        long quiets = pushes & ~promo;
        long doubles = ((quiets & Bitboard.RANK_3) << 8) & empty;
        long leftCaptures = ((pawns & ~Bitboard.FILE_A) << 7) & op;
        long rightCaptures = ((pawns & ~Bitboard.FILE_H) << 9) & op;
        long promos = pushes & promo;
        long leftCapturePromos = leftCaptures & op & promo;
        long rightCapturePromos = rightCaptures & op & promo;
        long leftEPs = ((pawns & ~Bitboard.FILE_A) << 7) & ep & Bitboard.RANK_6;
        long rightEPs = ((pawns & ~Bitboard.FILE_H) << 9) & ep & Bitboard.RANK_6;
        int to;

        leftCaptures ^= leftCapturePromos;
        rightCaptures ^= rightCapturePromos;

        while (pawns != 0) {
            while (leftCaptures != 0) {
                to = Long.numberOfTrailingZeros(leftCaptures);
                moves.add(Move.create(to - 7, to, Move.CAPTURE));
                leftCaptures &= leftCaptures - 1;
            }

            while (rightCaptures != 0) {
                to = Long.numberOfTrailingZeros(rightCaptures);
                moves.add(Move.create(to - 9, to, Move.CAPTURE));
                rightCaptures &= rightCaptures - 1;
            }

            while (quiets != 0) {
                to = Long.numberOfTrailingZeros(quiets);
                moves.add(Move.create(to - 8, to, Move.QUIET));
                quiets &= quiets - 1;
            }

            while (doubles != 0) {
                to = Long.numberOfTrailingZeros(doubles);
                moves.add(Move.create(to - 16, to, Move.DOUBLE_PAWN));
                doubles &= doubles - 1;
            }

            while (promos != 0) {
                to = Long.numberOfTrailingZeros(promos);
                moves.add(Move.create(to - 8, to, Move.KNIGHT_PROMO));
                moves.add(Move.create(to - 8, to, Move.BISHOP_PROMO));
                moves.add(Move.create(to - 8, to, Move.ROOK_PROMO));
                moves.add(Move.create(to - 8, to, Move.QUEEN_PROMO));
                promos &= promos - 1;
            }

            while (leftCapturePromos != 0) {
                to = Long.numberOfTrailingZeros(leftCapturePromos);
                moves.add(Move.create(to - 7, to, Move.KNIGHT_PROMO_CAPTURE));
                moves.add(Move.create(to - 7, to, Move.BISHOP_PROMO_CAPTURE));
                moves.add(Move.create(to - 7, to, Move.ROOK_PROMO_CAPTURE));
                moves.add(Move.create(to - 7, to, Move.QUEEN_PROMO_CAPTURE));
                leftCapturePromos &= leftCapturePromos - 1;
            }

            while (rightCapturePromos != 0) {
                to = Long.numberOfTrailingZeros(rightCapturePromos);
                moves.add(Move.create(to - 9, to, Move.KNIGHT_PROMO_CAPTURE));
                moves.add(Move.create(to - 9, to, Move.BISHOP_PROMO_CAPTURE));
                moves.add(Move.create(to - 9, to, Move.ROOK_PROMO_CAPTURE));
                moves.add(Move.create(to - 9, to, Move.QUEEN_PROMO_CAPTURE));
                rightCapturePromos &= rightCapturePromos - 1;
            }

            while (leftEPs != 0) {
                to = Long.numberOfTrailingZeros(leftEPs);
                moves.add(Move.create(to - 7, to, Move.EP_CAPTURE));
                leftEPs &= leftEPs - 1;
            }

            while (rightEPs != 0) {
                to = Long.numberOfTrailingZeros(rightEPs);
                moves.add(Move.create(to - 9, to, Move.EP_CAPTURE));
                rightEPs &= rightEPs - 1;
            }

            pawns &= pawns - 1;
        }
    }

    private static final void addBlackPawns(MoveList moves, long self, long op, long empty, long promo, long ep) {
        long pawns = Bitboard.getSelf(Enum.PAWN);
        long pushes = (pawns >>> 8) & empty;
        long quiets = pushes & ~promo;
        long doubles = ((quiets & Bitboard.RANK_6) >>> 8) & empty;
        long leftCaptures = ((pawns & ~Bitboard.FILE_H) >>> 7) & op;
        long rightCaptures = ((pawns & ~Bitboard.FILE_A) >>> 9) & op;
        long promos = pushes & promo;
        long leftCapturePromos = leftCaptures & op & promo;
        long rightCapturePromos = rightCaptures & op & promo;
        long leftEPs = ((pawns & ~Bitboard.FILE_H) >>> 7) & ep & Bitboard.RANK_3;
        long rightEPs = ((pawns & ~Bitboard.FILE_A) >>> 9) & ep & Bitboard.RANK_3;
        int to;

        leftCaptures ^= leftCapturePromos;
        rightCaptures ^= rightCapturePromos;

        while (pawns != 0) {
            while (leftCaptures != 0) {
                to = Long.numberOfTrailingZeros(leftCaptures);
                moves.add(Move.create(to + 7, to, Move.CAPTURE));
                leftCaptures &= leftCaptures - 1;
            }

            while (rightCaptures != 0) {
                to = Long.numberOfTrailingZeros(rightCaptures);
                moves.add(Move.create(to + 9, to, Move.CAPTURE));
                rightCaptures &= rightCaptures - 1;
            }

            while (quiets != 0) {
                to = Long.numberOfTrailingZeros(quiets);
                moves.add(Move.create(to + 8, to, Move.QUIET));
                quiets &= quiets - 1;
            }

            while (doubles != 0) {
                to = Long.numberOfTrailingZeros(doubles);
                moves.add(Move.create(to + 16, to, Move.DOUBLE_PAWN));
                doubles &= doubles - 1;
            }

            while (promos != 0) {
                to = Long.numberOfTrailingZeros(promos);
                moves.add(Move.create(to + 8, to, Move.KNIGHT_PROMO));
                moves.add(Move.create(to + 8, to, Move.BISHOP_PROMO));
                moves.add(Move.create(to + 8, to, Move.ROOK_PROMO));
                moves.add(Move.create(to + 8, to, Move.QUEEN_PROMO));
                promos &= promos - 1;
            }

            while (leftCapturePromos != 0) {
                to = Long.numberOfTrailingZeros(leftCapturePromos);
                moves.add(Move.create(to + 7, to, Move.KNIGHT_PROMO_CAPTURE));
                moves.add(Move.create(to + 7, to, Move.BISHOP_PROMO_CAPTURE));
                moves.add(Move.create(to + 7, to, Move.ROOK_PROMO_CAPTURE));
                moves.add(Move.create(to + 7, to, Move.QUEEN_PROMO_CAPTURE));
                leftCapturePromos &= leftCapturePromos - 1;
            }

            while (rightCapturePromos != 0) {
                to = Long.numberOfTrailingZeros(rightCapturePromos);
                moves.add(Move.create(to + 9, to, Move.KNIGHT_PROMO_CAPTURE));
                moves.add(Move.create(to + 9, to, Move.BISHOP_PROMO_CAPTURE));
                moves.add(Move.create(to + 9, to, Move.ROOK_PROMO_CAPTURE));
                moves.add(Move.create(to + 9, to, Move.QUEEN_PROMO_CAPTURE));
                rightCapturePromos &= rightCapturePromos - 1;
            }

            while (leftEPs != 0) {
                to = Long.numberOfTrailingZeros(leftEPs);
                moves.add(Move.create(to + 7, to, Move.EP_CAPTURE));
                leftEPs &= leftEPs - 1;
            }

            while (rightEPs != 0) {
                to = Long.numberOfTrailingZeros(rightEPs);
                moves.add(Move.create(to + 9, to, Move.EP_CAPTURE));
                rightEPs &= rightEPs - 1;
            }

            pawns &= pawns - 1;
        }
    }

    private static final void addKnightMoves(MoveList moves, long self, long op, long empty, long promo) {
        long knights = Bitboard.getSelf(Enum.KNIGHT);
        long to, captures, quiets;
        int from;

        while (knights != 0) {
            from = Long.numberOfTrailingZeros(knights);
            to = AttackTable.KNIGHT_ATTACKS[from] & ~self;

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

    private static final void addBishopMoves(MoveList moves, long self, long op, long empty, long promo) {
        long bishops = Bitboard.getSelf(Enum.BISHOP);
        long to, captures, quiets;
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

    private static final void addRookMoves(MoveList moves, long self, long op, long empty, long promo) {
        long to, captures, quiets;
        long rooks = Bitboard.getSelf(Enum.ROOK);
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

    private static final void addQueenMoves(MoveList moves, long self, long op, long empty, long promo) {
        long to, captures, quiets;
        long queens = Bitboard.getSelf(Enum.QUEEN);
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

    private static final void addKingMoves(MoveList moves, long self, long op, long empty, long promo) {
        long kings = Bitboard.getSelf(Enum.KING);
        int from = Long.numberOfTrailingZeros(kings);
        long to = AttackTable.KING_ATTACKS[from] & ~self;
        long captures = to & op;
        long quiets = to & ~op;
        int castleRights = GameInfo.getCastleRights();
        int turn = GameInfo.getTurn();

        while (captures != 0) {
            moves.add(Move.create(from, Long.numberOfTrailingZeros(captures), Move.CAPTURE));
            captures &= captures - 1;
        }

        while (quiets != 0) {
            moves.add(Move.create(from, Long.numberOfTrailingZeros(quiets), Move.QUIET));
            quiets &= quiets - 1;
        }


        if((castleRights & GameInfo.castleType[turn + Enum.QUEEN]) != 0)  {
            if((~empty & (0xeL << (turn * 7))) == 0) {
                moves.add(Move.create(from, from - 2, Move.QUEEN_CASTLE));
            }
        }

        if((castleRights & GameInfo.castleType[turn + Enum.KING]) != 0) {
            if((~empty & (0x60L << (turn * 7))) == 0) {
                moves.add(Move.create(from, from + 2, Move.KING_CASTLE));
            }
        }

    }
}
