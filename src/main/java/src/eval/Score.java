package src.eval;

import org.deeplearning4j.nn.graph.ComputationGraph;
import src.Move;
import src.board.*;
import src.piece.Bishop;
import src.piece.Knight;
import src.piece.Pawn;
import src.piece.Piece;
import src.piece.Queen;
import src.piece.Rook;

public class Score
{
    public static ComputationGraph network;

    private static final int DOUBLED_PAWN_PENALTY = -15;
    private static final int ISOLATED_PAWN_PENALTY = -25;
    private static final int PASSED_PAWN_BONUS = 40;

    private static final int PAWN_VALUE = 100;
    private static final int KNIGHT_VALUE = 320;
    private static final int BISHOP_VALUE = 330;
    private static final int ROOK_VALUE = 500;
    private static final int QUEEN_VALUE = 1200;
    private static final int KING_VALUE = 5000;
    private static final int BISHOP_MOBILITY_VALUE = 3;
    private static final int KNIGHT_MOBILITY_VALUE = 3;
    private static final int ROOK_MOBILITY_VALUE = 5;
    private static final int QUEEN_MOBILITY_VALUE = 5;
    private static final int BISHOP_PAIR_VALUE = 60;
    private static final int KNIGHT_OUTPOST_VALUE = 40;

    private static final int DEVELOPMENT_KNIGHT_VALUE = 40;
    private static final int DEVELOPMENT_BISHOP_VALUE = 40;
    private static final int KNIGHT_EDGE_DEDUCT = -10;
    private static final int CENTER_CONTROL_VALUE = 20;
    private static final int EXTENDED_CONTROL_VALUE = 10;
    private static final int PAWN_SHIELD_SCORE = 20;
    private static final int OPEN_SURROUND_PENALTY = -10;
    private static final int ROOK_OPEN_FILE = 30;

    private static final long FILE_A = 0x0101010101010101L;
    private static final long FILE_B = 0x0202020202020202L;
    private static final long FILE_C = 0x0404040404040404L;
    private static final long FILE_D = 0x0808080808080808L;
    private static final long FILE_E = 0x1010101010101010L;
    private static final long FILE_F = 0x2020202020202020L;
    private static final long FILE_G = 0x4040404040404040L;
    private static final long FILE_H = 0x8080808080808080L;
    private static final long RANK_2 = 0x000000000000FF00L;
    private static final long RANK_3 = 0x0000000000FF0000L;
    private static final long RANK_4 = 0x00000000FF000000L;
    private static final long RANK_5 = 0x000000FF00000000L;
    private static final long RANK_6 = 0x0000FF0000000000L;
    private static final long RANK_7 = 0x00FF000000000000L;
    private static final long CENTER_SQUARES_MASK = 0x0000001818000000L; // e4, d4, e5, d5
    private static final long OUTER_CENTER_MASK = 0x600060000000L; // e3, d3, e6, d6
    private static final long WHITE_PAWN_SHIELD_MASK = 0x000000000000E600L; // e2, f2, g2
    private static final long BLACK_PAWN_SHIELD_MASK = 0x00E6000000000000L; // e7, f7, g7
    private static final long WHITE_KING_SURROUND_MASK = 0x0000000000001C00L; // e1, f1, g1
    private static final long BLACK_KING_SURROUND_MASK = 0x001C000000000000L; // e8, f8, g8
    private static final long[] FILES = {FILE_A, FILE_B, FILE_C, FILE_D, FILE_E, FILE_F, FILE_G, FILE_H};

    public static int networkScore(double[][][] bitboards)
    {
        return (int) (src.eval.Network.score(bitboards, network) * 100);
    }

    public static int bitboardScore(Bitboard board) {
        Bitboard moves = board.allMoves();
        int material = scoreMaterial(board);
        int position = scorePawnStructure(board) + scorePieceDevelopment(board) + scoreCenterControl(board, moves) + scoreRookOpenFiles(board) + scoreMobility(board) + scoreEdgeControl(board) + scoreBishopPairs(board) + scoreOutpostKnights(board);
        int king = scoreKing(board) + scoreKingSafety(board);
        return (material * 80 / 100) + position + king;
    }

    public static int scoreMaterial(Bitboard board) {
        int whiteScore = Long.bitCount(board.wPawns) * PAWN_VALUE + Long.bitCount(board.wKnights) * KNIGHT_VALUE + Long.bitCount(board.wBishops) * BISHOP_VALUE + Long.bitCount(board.wRooks) * ROOK_VALUE + Long.bitCount(board.wQueens) * QUEEN_VALUE;
        int blackScore = Long.bitCount(board.bPawns) * PAWN_VALUE + Long.bitCount(board.bKnights) * KNIGHT_VALUE + Long.bitCount(board.bBishops) * BISHOP_VALUE + Long.bitCount(board.bRooks) * ROOK_VALUE + Long.bitCount(board.bQueens) * QUEEN_VALUE;
        return whiteScore - blackScore;
    }

    public static int scoreKing(Bitboard board) {
        int score = 0;
        if (Long.bitCount(board.wKings) == 0) score -= KING_VALUE;
        if (Long.bitCount(board.bKings) == 0) score += KING_VALUE;
        return score;
    }

    public static int scorePawnStructure(Bitboard board) {
        return sidedPawnStructure(board.wPawns, board.bPawns) - sidedPawnStructure(board.bPawns, board.wPawns);
    }

    public static int scoreEdgeControl(Bitboard board) {
        // count knights on A and H file
        int white = (Long.bitCount(board.wKnights & (FILE_A | FILE_H))) * KNIGHT_EDGE_DEDUCT;
        int black = Long.bitCount(board.bKnights & (FILE_A | FILE_H)) * KNIGHT_EDGE_DEDUCT;
        return white - black;
    }

    private static int sidedPawnStructure(long pawns, long opp) {
        int score = 0;
        long isolatedMask = 0L;
        long passedPawns = pawns;

        for (long fileMask : FILES) {
            long pawnsInFile = pawns & fileMask;
            if (Long.bitCount(pawnsInFile) > 1) {
                score += DOUBLED_PAWN_PENALTY * (Long.bitCount(pawnsInFile) - 1);
            }
            isolatedMask |= pawnsInFile;
            passedPawns &= ~(opp & fileMask);

            // isolated pawns
            if ((pawns & fileMask) != 0 && (isolatedMask & (fileMask << 1 | fileMask >> 1)) == 0) {
                score += ISOLATED_PAWN_PENALTY;
            }
        }

        // passed pawns
        score += PASSED_PAWN_BONUS * Long.bitCount(passedPawns);

        return score;
    }

    public static int scorePieceDevelopment(Bitboard board) {    
        long movedWKnights = board.INIT_WKNIGHTS & ~board.wKnights;
        long movedWBishops = board.INIT_WBISHOPS & ~board.wBishops;
    
        long movedBKnights = board.INIT_BKNIGHTS & ~board.bKnights;
        long movedBBishops = board.INIT_BBISHOPS & ~board.bBishops;
    
        int whiteDevelopmentScore = Long.bitCount(movedWKnights) * DEVELOPMENT_KNIGHT_VALUE + Long.bitCount(movedWBishops) * DEVELOPMENT_BISHOP_VALUE;
    
        int blackDevelopmentScore = Long.bitCount(movedBKnights) * DEVELOPMENT_KNIGHT_VALUE + Long.bitCount(movedBBishops) * DEVELOPMENT_BISHOP_VALUE;
    
        return whiteDevelopmentScore - blackDevelopmentScore;
    }

    public static int scoreCenterControl(Bitboard board, Bitboard attacked) {
        int wCControl = Long.bitCount(CENTER_SQUARES_MASK & (attacked.wPawns | attacked.wKnights | attacked.wBishops | attacked.wQueens));
        int bCControl = Long.bitCount(CENTER_SQUARES_MASK & (attacked.bPawns | attacked.bKnights | attacked.bBishops | attacked.bQueens));
        int wEControl = Long.bitCount(OUTER_CENTER_MASK & (attacked.wPawns | attacked.wKnights | attacked.wBishops | attacked.wQueens));
        int bEControl = Long.bitCount(OUTER_CENTER_MASK & (attacked.bPawns | attacked.bKnights | attacked.bBishops | attacked.bQueens));
        return ((wCControl - bCControl) * CENTER_CONTROL_VALUE) + ((wEControl - bEControl) * EXTENDED_CONTROL_VALUE);
    }

    public static int scoreKingSafety(Bitboard board) {
        int whiteSafety = scoreKingSide(board.wPawns, WHITE_PAWN_SHIELD_MASK, WHITE_KING_SURROUND_MASK, board, true);
        int blackSafety = scoreKingSide(board.bPawns, BLACK_PAWN_SHIELD_MASK, BLACK_KING_SURROUND_MASK, board, false);
        return whiteSafety - blackSafety;
    }

    private static int scoreKingSide(long pawns, long shieldMask, long surroundMask, Bitboard board, boolean white) {
        int shieldScore = Long.bitCount(pawns & shieldMask) * PAWN_SHIELD_SCORE;
        long openSquares = ~(white ? board.whites : board.blacks) & surroundMask;
        int openSquarePenalty = Long.bitCount(openSquares) * OPEN_SURROUND_PENALTY;
        return shieldScore + openSquarePenalty;
    }

    public static int scoreRookOpenFiles(Bitboard board) {
        int whiteScore = 0;
        int blackScore = 0;
        long allPawns = board.wPawns | board.bPawns;
        for (long file : FILES) {
            if ((allPawns & file) == 0) { // file open
                whiteScore += Long.bitCount(board.wRooks & file) * ROOK_OPEN_FILE;
                blackScore += Long.bitCount(board.bRooks & file) * ROOK_OPEN_FILE;
            }
        }
        return whiteScore - blackScore;
    }

    public static int mvvlva(Move move, Bitboard board) {
        Piece aggressor = move.getPiece();
        Piece victim = board.get(move.getDestination());
        int aVal = pieceValue(aggressor);
        int vVal = pieceValue(victim);
        return vVal - aVal;
    }

    private static int pieceValue(Piece piece) {
        if (piece instanceof Pawn)
            return PAWN_VALUE;
        else if (piece instanceof Knight)
            return KNIGHT_VALUE;
        else if (piece instanceof Bishop)
            return BISHOP_VALUE;
        else if (piece instanceof Rook)
            return ROOK_VALUE;
        else if (piece instanceof Queen)
            return QUEEN_VALUE;
        return 0; // king or null
    }

    public static int scoreMobility(Bitboard board) {
        int wMobility = (Long.bitCount(board.wBishopMoves()) * BISHOP_MOBILITY_VALUE) + (Long.bitCount(board.wKnightMoves()) * KNIGHT_MOBILITY_VALUE);
        int bMobility = (Long.bitCount(board.bBishopMoves()) * BISHOP_MOBILITY_VALUE) + (Long.bitCount(board.bKnightMoves()) * KNIGHT_MOBILITY_VALUE);
        if (board.midgame()) {
            wMobility += (Long.bitCount(board.wRookMoves()) * ROOK_MOBILITY_VALUE) + (Long.bitCount(board.wQueenMoves()) * QUEEN_MOBILITY_VALUE);
            bMobility += (Long.bitCount(board.bRookMoves()) * ROOK_MOBILITY_VALUE) + (Long.bitCount(board.bQueenMoves()) * QUEEN_MOBILITY_VALUE);
        }
        return wMobility - bMobility;
    }

    public static int scoreBishopPairs(Bitboard board) {
        int score = 0;
        if (Long.bitCount(board.wBishops) >= 2) score += BISHOP_PAIR_VALUE;
        if (Long.bitCount(board.bBishops) >= 2) score -= BISHOP_PAIR_VALUE;
        return score;
    }

    public static int scoreOutpostKnights(Bitboard board) {
        if (board.endgame()) return 0;
        int score = 0;
        long wAttacks = board.bPawnMoves();
        long bAttacks = board.wPawnMoves();
        long wOutposts = (RANK_5 | RANK_6 | RANK_7) & ~wAttacks;
        long bOutposts = (RANK_2 | RANK_3 | RANK_4) & ~bAttacks;
    
        score += Long.bitCount(board.wKnights & wOutposts) * KNIGHT_OUTPOST_VALUE;
        score -= Long.bitCount(board.bKnights & bOutposts) * KNIGHT_OUTPOST_VALUE;
        return score;
    }
}