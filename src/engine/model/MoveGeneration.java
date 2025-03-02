/*
 * Author: Carter Hidalgo
 * 
 * Purpose: Create a list of all pseudo-legal moves given the current position
 */

package engine.model;

import java.util.ArrayList;
import java.util.List;
import engine.helper.Bit;
import engine.helper.Enum;
import engine.helper.Offset;
import engine.model.magics.MagicBitboard;
import engine.model.moves.KingMoves;
import engine.model.moves.KnightMoves;

public class MoveGeneration {
    public static List<Short> gen() {
        List<Short> moves = new ArrayList<>();

        addPawnMoves(moves);
        addKnightMoves(moves);
        addBishopMoves(moves);
        addRookMoves(moves);
        addQueenMoves(moves);
        addKingMoves(moves);

        return moves;
    }

    private static void addPawnMoves(List<Short> moves) {
        long self = Bitboard.getSelf(Enum.PAWN);
        int index;

        while (self != 0) {
            index = Long.numberOfTrailingZeros(self);

            if (Bit.isSet(Bitboard.get(Enum.EMPTY), Offset.forward(index))) {
                if (Bit.isSet(Bitboard.getSelf(Enum.PROMO), Offset.forward(index))) {
                    moves.add(Move.create(index, Offset.forward(index), Move.QUEEN_PROMO));
                    moves.add(Move.create(index, Offset.forward(index), Move.ROOK_PROMO));
                    moves.add(Move.create(index, Offset.forward(index), Move.BISHOP_PROMO));
                    moves.add(Move.create(index, Offset.forward(index), Move.KNIGHT_PROMO));
                } else {
                    moves.add(Move.create(index, Offset.forward(index), Move.QUIET));
                }

                if (Bit.isSet(Bitboard.get(Enum.EMPTY), Offset.dbForward(index))
                        && Bit.isSet(Bitboard.getSelf(Enum.HOME), index)) {
                    moves.add(Move.create(index, Offset.dbForward(index), Move.DOUBLE_PAWN));
                }
            }

            if (Offset.lForward(index) != -1
                    && Bit.isSet(Bitboard.getOp(), Offset.lForward(index))) {
                if (Bit.isSet(Bitboard.getSelf(Enum.PROMO), Offset.lForward(index))) {
                    moves.add(Move.create(index, Offset.lForward(index), Move.QUEEN_PROMO_CAPTURE));
                    moves.add(Move.create(index, Offset.lForward(index), Move.ROOK_PROMO_CAPTURE));
                    moves.add(
                            Move.create(index, Offset.lForward(index), Move.BISHOP_PROMO_CAPTURE));
                    moves.add(
                            Move.create(index, Offset.lForward(index), Move.KNIGHT_PROMO_CAPTURE));
                } else {
                    moves.add(Move.create(index, Offset.lForward(index), Move.CAPTURE));
                }
            }

            if (Offset.rForward(index) != -1
                    && Bit.isSet(Bitboard.getOp(), Offset.rForward(index))) {
                if (Bit.isSet(Bitboard.getSelf(Enum.PROMO), Offset.rForward(index))) {
                    moves.add(Move.create(index, Offset.rForward(index), Move.QUEEN_PROMO_CAPTURE));
                    moves.add(Move.create(index, Offset.rForward(index), Move.ROOK_PROMO_CAPTURE));
                    moves.add(
                            Move.create(index, Offset.rForward(index), Move.BISHOP_PROMO_CAPTURE));
                    moves.add(
                            Move.create(index, Offset.rForward(index), Move.KNIGHT_PROMO_CAPTURE));
                } else {
                    moves.add(Move.create(index, Offset.rForward(index), Move.CAPTURE));
                }
            }

            self = Bit.clear(self, index);
        }
    }

    private static void addKnightMoves(List<Short> moves) {
        long to;
        long self = Bitboard.getSelf(Enum.KNIGHT);
        int fromIndex, toIndex;

        while (self != 0) {
            fromIndex = Long.numberOfTrailingZeros(self);
            to = KnightMoves.get(fromIndex) & ~Bitboard.getSelf();

            while (to != 0) {
                toIndex = Long.numberOfTrailingZeros(to);

                if (Bit.isSet(Bitboard.getOp(), toIndex)) {
                    moves.add(Move.create(fromIndex, toIndex, Move.CAPTURE));
                } else {
                    moves.add(Move.create(fromIndex, toIndex, Move.QUIET));
                }

                to = Bit.clear(to, toIndex);
            }

            self = Bit.clear(self, fromIndex);
        }
    }

    private static void addBishopMoves(List<Short> moves) {
        long to;
        long self = Bitboard.getSelf(Enum.BISHOP);
        int fromIndex, toIndex;

        while (self != 0) {
            fromIndex = Long.numberOfTrailingZeros(self);
            to = MagicBitboard.getBishopMoves(fromIndex) & ~Bitboard.getSelf();

            while (to != 0) {
                toIndex = Long.numberOfTrailingZeros(to);

                if (Bit.isSet(Bitboard.getOp(), toIndex)) {
                    moves.add(Move.create(fromIndex, toIndex, Move.CAPTURE));
                } else {
                    moves.add(Move.create(fromIndex, toIndex, Move.QUIET));
                }

                to = Bit.clear(to, toIndex);
            }

            self = Bit.clear(self, fromIndex);
        }
    }

    private static void addRookMoves(List<Short> moves) {
        long to;
        long self = Bitboard.getSelf(Enum.ROOK);
        int fromIndex, toIndex;

        while (self != 0) {
            fromIndex = Long.numberOfTrailingZeros(self);
            to = MagicBitboard.getRookMoves(fromIndex) & ~Bitboard.getSelf();

            while (to != 0) {
                toIndex = Long.numberOfTrailingZeros(to);

                if (Bit.isSet(Bitboard.getOp(), toIndex)) {
                    moves.add(Move.create(fromIndex, toIndex, Move.CAPTURE));
                } else {
                    moves.add(Move.create(fromIndex, toIndex, Move.QUIET));
                }

                to = Bit.clear(to, toIndex);
            }

            self = Bit.clear(self, fromIndex);
        }
    }

    private static void addQueenMoves(List<Short> moves) {
        long to;
        long self = Bitboard.getSelf(Enum.QUEEN);
        int fromIndex, toIndex;

        while (self != 0) {
            fromIndex = Long.numberOfTrailingZeros(self);
            to = MagicBitboard.getQueenMoves(fromIndex) & ~Bitboard.getSelf();

            while (to != 0) {
                toIndex = Long.numberOfTrailingZeros(to);

                if (Bit.isSet(Bitboard.getOp(), toIndex)) {
                    moves.add(Move.create(fromIndex, toIndex, Move.CAPTURE));
                } else {
                    moves.add(Move.create(fromIndex, toIndex, Move.QUIET));
                }

                to = Bit.clear(to, toIndex);
            }

            self = Bit.clear(self, fromIndex);
        }
    }

    private static void addKingMoves(List<Short> moves) {
        long to;
        long self = Bitboard.getSelf(Enum.KING);
        int fromIndex, toIndex;

        while (self != 0) {
            fromIndex = Long.numberOfTrailingZeros(self);
            to = KingMoves.get(fromIndex) & ~Bitboard.getSelf();

            while (to != 0) {
                toIndex = Long.numberOfTrailingZeros(to);

                if (Bit.isSet(Bitboard.getOp(), toIndex)) {
                    moves.add(Move.create(fromIndex, toIndex, Move.CAPTURE));
                } else {
                    moves.add(Move.create(fromIndex, toIndex, Move.QUIET));
                }

                to = Bit.clear(to, toIndex);
            }

            self = Bit.clear(self, fromIndex);

            // if (GameInfo.getCastleRights(GameInfo.getTurn(), 0) &&
            // Bit.isSet(Bitboard.getSelf(Enum.ROOK), fromIndex + 3) &&
            // !Bit.isSet(Bitboard.get(Enum.OCCUPIED), fromIndex + 1) &&
            // !Bit.isSet(Bitboard.get(Enum.OCCUPIED), fromIndex + 2)) {
            // moves.add(Move.create(fromIndex, fromIndex + 2, Move.KING_CASTLE));
            // }

            // if (GameInfo.getCastleRights(GameInfo.getTurn(), 1) &&
            // Bit.isSet(Bitboard.getSelf(Enum.ROOK), fromIndex + 3) &&
            // !Bit.isSet(Bitboard.get(Enum.OCCUPIED), fromIndex - 1) &&
            // !Bit.isSet(Bitboard.get(Enum.OCCUPIED), fromIndex - 2) &&
            // !Bit.isSet(Bitboard.get(Enum.OCCUPIED), fromIndex - 3)) {
            // moves.add(Move.create(fromIndex, fromIndex - 2, Move.QUEEN_CASTLE));
            // }
        }
    }
}
