package src.board;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import src.Location;
import src.Move;
import src.piece.Bishop;
import src.piece.King;
import src.piece.Knight;
import src.piece.Pawn;
import src.piece.Piece;
import src.piece.Queen;
import src.piece.Rook;
import java.awt.Color;

public class Bitboard {
    public int moveNumber;
    public boolean moveWhite;

    public final long wPawns;
    public final long wKnights;
    public final long wBishops;
    public final long wRooks;
    public final long wQueens;
    public final long wKings;
    public final long bPawns;
    public final long bKnights;
    public final long bBishops;
    public final long bRooks;
    public final long bQueens;
    public final long bKings;

    public final long whites;
    public final long blacks;

    // public final long INIT_WPAWNS = 0x000000000000FF00L;
    // public final long INIT_WKNIGHTS = 0x0000000000000042L;
    // public final long INIT_WBISHOPS = 0x0000000000000024L;
    // public final long INIT_WROOKS = 0x0000000000000081L;
    // public final long INIT_WQUEENS = 0x0000000000000010L;
    // public final long INIT_WKINGS = 0x0000000000000008L;

    // public final long INIT_BPAWNS = 0x00FF000000000000L;
    // public final long INIT_BKNIGHTS = 0x4200000000000000L;
    // public final long INIT_BBISHOPS = 0x2400000000000000L;
    // public final long INIT_BROOKS = 0x8100000000000000L;
    // public final long INIT_BQUEENS = 0x1000000000000000L;
    // public final long INIT_BKINGS = 0x0800000000000000L;

    public final long INIT_BPAWNS = 0x000000000000FF00L;
    public final long INIT_BKNIGHTS = 0x0000000000000042L;
    public final long INIT_BBISHOPS = 0x0000000000000024L;
    public final long INIT_BROOKS = 0x0000000000000081L;
    public final long INIT_BQUEENS = 0x0000000000000010L;
    public final long INIT_BKINGS = 0x0000000000000008L;

    public final long INIT_WPAWNS = 0x00FF000000000000L;
    public final long INIT_WKNIGHTS = 0x4200000000000000L;
    public final long INIT_WBISHOPS = 0x2400000000000000L;
    public final long INIT_WROOKS = 0x8100000000000000L;
    public final long INIT_WQUEENS = 0x1000000000000000L;
    public final long INIT_WKINGS = 0x0800000000000000L;

    private static final long FILE_A = 0x0101010101010101L;
    private static final long FILE_H = 0x8080808080808080L;

    private final List<Long> JUMPS = initJumps();
    private final byte[] DIAGNOLS = {
        -9, -7, 7, 9
    };

    public Bitboard() {
        this.wPawns = INIT_WPAWNS;
        this.wKnights = INIT_WKNIGHTS;
        this.wBishops = INIT_WBISHOPS;
        this.wRooks = INIT_WROOKS;
        this.wQueens = INIT_WQUEENS;
        this.wKings = INIT_WKINGS;
        this.bPawns = INIT_BPAWNS;
        this.bKnights = INIT_BKNIGHTS;
        this.bBishops = INIT_BBISHOPS;
        this.bRooks = INIT_BROOKS;
        this.bQueens = INIT_BQUEENS;
        this.bKings = INIT_BKINGS;

        this.whites = whiteMoves();
        this.blacks = blackMoves();
    }

    private Bitboard(long wPawns, long wKnights, long wBishops, long wRooks, long wQueens, long wKings, long bPawns, long bKnights, long bBishops, long bRooks, long bQueens, long bKings) {
        this.wPawns = wPawns;
        this.wKnights = wKnights;
        this.wBishops = wBishops;
        this.wRooks = wRooks;
        this.wQueens = wQueens;
        this.wKings = wKings;
        this.bPawns = bPawns;
        this.bKnights = bKnights;
        this.bBishops = bBishops;
        this.bRooks = bRooks;
        this.bQueens = bQueens;
        this.bKings = bKings;

        this.whites = whiteMoves();
        this.blacks = blackMoves();
    }

    public Bitboard allMoves() {
        return new Bitboard(wPawnMoves(), wKnightMoves(), wBishopMoves(), wRookMoves(), wQueenMoves(), wKingMoves(), bPawnMoves(), bKnightMoves(), bBishopMoves(), bRookMoves(), bQueenMoves(), bKingMoves());
    }

    public long wPawnMoves() {
        final long notFileA = ~FILE_A;
        final long notFileH = ~FILE_H;
        
        final long emptySquares = ~(this.whites | this.blacks);
    
        // single moves
        long singleMoves = (this.wPawns >> 8) & emptySquares;
    
        // double moves
        long doubleMoves = ((this.wPawns >> 16) & emptySquares & (emptySquares >> 8));
    
        // diagnol captures
        long leftCaptures = (this.wPawns << 7) & this.blacks & notFileA;
        long rightCaptures = (this.wPawns << 9) & this.blacks & notFileH;
    
        return singleMoves | doubleMoves | leftCaptures | rightCaptures;
    }
    
    public long wKnightMoves() {
        long legals = 0L;
        for(final byte position : bitPositions(this.wKnights)) {
            legals |= JUMPS.get(position) & ~(this.whites);
        }
        return legals;
    }

    public long wBishopMoves() {
        long legals = 0L;
        for(final byte position : bitPositions(this.wBishops)) {
            byte candidate = position;
            for(final byte diagonal : DIAGNOLS) {
                candidate += diagonal;
                while(candidate >= 0 && candidate < 64 && (candidate | this.wPawns) == 0L) {
                    legals |= candidate;
                    if((candidate | this.blacks) != 0L) {
                        break;
                    }
                    candidate += diagonal;
                }
            }
        }
        return legals;
    }

    public long wRookMoves() {
        long legals = 0L;
        for (final byte position : bitPositions(this.wRooks)) {
            int[] directions = {-8, 8, -1, 1}; 
            for (int direction : directions) {
                int candidate = position;
                while (true) {
                    candidate += direction;
                    if (candidate < 0 || candidate >= 64 || 
                        (candidate % 8 == 0 && direction == -1) || 
                        (candidate % 8 == 7 && direction == 1) || 
                        ((1L << candidate) & this.whites) != 0) {
                        break;
                    }
                    legals |= 1L << candidate;
                    if (((1L << candidate) & this.blacks) != 0) {
                        break;
                    }
                }
            }
        }
        return legals;
    }    

    public long wQueenMoves() {
        long legals = 0L;
        for (final byte position : bitPositions(this.wQueens)) {
            int[] directions = {-9, -8, -7, -1, 1, 7, 8, 9};
    
            for (int direction : directions) {
                int candidate = position;
                while (true) {
                    candidate += direction;
                    
                    boolean isLeftEdgeCase = (direction == -1 || direction == -9 || direction == 7) && (candidate % 8 == 7);
                    boolean isRightEdgeCase = (direction == 1 || direction == -7 || direction == 9) && (candidate % 8 == 0);
                    if (candidate < 0 || candidate >= 64 || isLeftEdgeCase || isRightEdgeCase) {
                        break;
                    }
                    legals |= 1L << candidate;
    
                    if (((1L << candidate) & (this.whites | this.blacks)) != 0) {
                        break;
                    }
                }
            }
        }
        return legals;
    }

    public long wKingMoves() {
        long legals = 0L;
        for (final byte position : bitPositions(this.wKings)) {
            int[] directions = {-9, -8, -7, -1, 1, 7, 8, 9};
            for (int direction : directions) {
                int candidate = position + direction;
                if (candidate >= 0 && candidate < 64 &&
                    Math.abs(candidate % 8 - position % 8) <= 1 &&
                    ((1L << candidate) & this.whites) == 0) {
                    legals |= 1L << candidate;
                }
            }
        }
        return legals;
    }

    public long whiteMoves() {
        return this.wPawns | this.wKnights | this.wBishops | this.wRooks | this.wQueens | this.wKings;
    }

    public long bPawnMoves() {
        final long seventhRankMask = INIT_BPAWNS >> 8;
        final long emptySquares = ~(this.whites | this.blacks);
    
        // single move forward
        long singleMoves = (this.bPawns >> 8) & emptySquares;
    
        // double move forward
        long doubleMoves = ((singleMoves & seventhRankMask) >> 8) & emptySquares;
    
        // diagonal captures
        long leftCaptures = (this.bPawns >> 9) & this.whites & ~FILE_A;
        long rightCaptures = (this.bPawns >> 7) & this.whites & ~FILE_H;
    
        return singleMoves | doubleMoves | leftCaptures | rightCaptures;
    }    

    public long bKnightMoves() {
        long legals = 0L;
        for(final byte position : bitPositions(this.bKnights)) {
            legals |= JUMPS.get(position) & ~(this.blacks);
        }
        return legals;
    }

    public long bBishopMoves() {
        long legals = 0L;
        for(final byte position : bitPositions(this.bBishops)) {
            byte candidate = position;
            for(final byte diagonal : DIAGNOLS) {
                candidate += diagonal;
                while(candidate >= 0 && candidate < 64 && (candidate | this.blacks) == 0L) {
                    legals |= candidate;
                    if((candidate | this.whites) != 0L) {
                        break;
                    }
                    candidate += diagonal;
                }
            }
        }
        return legals;
    }
    
    public long bRookMoves() {
        long legals = 0L;
        for (final byte position : bitPositions(this.bRooks)) {
            int[] directions = {-8, 8, -1, 1}; 
            for (int direction : directions) {
                int candidate = position;
                while (true) {
                    candidate += direction;
                    if (candidate < 0 || candidate >= 64 || 
                        (candidate % 8 == 0 && direction == -1) || 
                        (candidate % 8 == 7 && direction == 1) || 
                        ((1L << candidate) & this.blacks) != 0) {
                        break;
                    }
                    legals |= 1L << candidate;
                    if (((1L << candidate) & this.whites) != 0) {
                        break;
                    }
                }
            }
        }
        return legals;
    }

    public long bQueenMoves() {
        long legals = 0L;
        for (final byte position : bitPositions(this.bQueens)) {
            int[] directions = {-9, -8, -7, -1, 1, 7, 8, 9};
    
            for (int direction : directions) {
                int candidate = position;
                while (true) {
                    candidate += direction;
                    
                    boolean isLeftEdgeCase = (direction == -1 || direction == -9 || direction == 7) && (candidate % 8 == 7);
                    boolean isRightEdgeCase = (direction == 1 || direction == -7 || direction == 9) && (candidate % 8 == 0);
                    if (candidate < 0 || candidate >= 64 || isLeftEdgeCase || isRightEdgeCase) {
                        break;
                    }
                    legals |= 1L << candidate;
    
                    if (((1L << candidate) & (this.whites | this.blacks)) != 0) {
                        break;
                    }
                }
            }
        }
        return legals;
    }

    public long bKingMoves() {
        long legals = 0L;
        for (final byte position : bitPositions(this.bKings)) {
            int[] directions = {-9, -8, -7, -1, 1, 7, 8, 9};
            for (int direction : directions) {
                int candidate = position + direction;
                if (candidate >= 0 && candidate < 64 &&
                    Math.abs(candidate % 8 - position % 8) <= 1 &&
                    ((1L << candidate) & this.blacks) == 0) {
                    legals |= 1L << candidate;
                }
            }
        }
        return legals;
    }    

    public long blackMoves() {
        return this.bPawns | this.bKnights | this.bBishops | this.bRooks | this.bQueens | this.bKings;
    }

    @Override
    public String toString() {
        String representation = new String("");
        String[] pieceTypes = {"BLACK KING", "BLACK QUEEN", "BLACK ROOKS", "BLACK BISHOPS", "BLACK KNIGHTS", "BLACK PAWNS", "WHITE KING", "WHITE QUEEN", "WHITE ROOKS", "WHITE BISHOPS", "WHITE KNIGHTS", "WHITE PAWNS"};
        long[] pieceValues = {this.bKings, this.bQueens, this.bRooks, this.bBishops, this.bKnights, this.bPawns, this.wKings, this.wQueens, this.wRooks, this.wBishops, this.wKnights, this.wPawns};

        for (int i = 0; i < pieceTypes.length; i++) {
            representation += binaryString(pieceTypes[i], pieceValues[i]);
        }
        return representation;
    }

    private String binaryString(String name, long bits) {
        String binString = Long.toBinaryString(bits);
        String padded = String.format("%64s", binString).replace(' ', '0');
        
        // split every 8 characters
        return Arrays.stream(padded.split("(?<=\\G.{8})")).collect(Collectors.joining(" ", name + "\n", "\n\n"));
    }

    private byte[] bitPositions(long n) {
        final byte[] result = new byte[Long.bitCount(n)];
        int i = 0;
        for (byte bit = 0; n != 0L; bit++) {
            if ((n & 1L) != 0) result[i++] = bit;
            n >>>= 1;
        }
        return result;
    }

    private List<Long> initJumps() {
        long[] jumps = new long[64];

        jumps[0] = 1L << 10L | 1L << 17L;
        jumps[1] = 1L << 11L | 1L << 16L | 1L << 18L;
        jumps[2] = 1L << 8L | 1L << 12L | 1L << 17L | 1L << 19L;
        jumps[3] = 1L << 9L | 1L << 13L | 1L << 18L | 1L << 20L;
        jumps[4] = 1L << 10L | 1L << 14L | 1L << 19L | 1L << 21L;
        jumps[5] = 1L << 11L | 1L << 15L | 1L << 20L | 1L << 22L;
        jumps[6] = 1L << 12L | 1L << 21L | 1L << 23L;
        jumps[7] = 1L << 13L | 1L << 22L;

        jumps[8] = 1L << 2L | 1L << 18L | 1L << 25L;
        jumps[9] = 1L << 3L | 1L << 19L | 1L << 24L | 1L << 26L;
        jumps[10] = 1L | 1L << 4L | 1L << 16L | 1L << 20L | 1L << 25L | 1L << 27L;
        jumps[11] = 1L << 1L | 1L << 5L | 1L << 17L | 1L << 21L | 1L << 26L | 1L << 28L;
        jumps[12] = 1L << 2L | 1L << 6L | 1L << 18L | 1L << 22L | 1L << 27L | 1L << 29L;
        jumps[13] = 1L << 3L | 1L << 7L | 1L << 19L | 1L << 23L | 1L << 28L | 1L << 30L;
        jumps[14] = 1L << 4L | 1L << 20L | 1L << 29L | 1L << 31L;
        jumps[15] = 1L << 5L | 1L << 21L | 1L << 30L;

        jumps[16] = 1L << 1L | 1L << 10L | 1L << 26L | 1L << 33L;
        jumps[17] = 1L | 1L << 2L | 1L << 11L | 1L << 27L | 1L << 32L | 1L << 34L;
        jumps[18] = 1L << 1L | 1L << 3L | 1L << 8L | 1L << 12L | 1L << 24L | 1L << 28L | 1L << 33L | 1L << 35L;
        jumps[19] = 1L << 2L | 1L << 4L | 1L << 9L | 1L << 13L | 1L << 25L | 1L << 29L | 1L << 34L | 1L << 36L;
        jumps[20] = 1L << 3L | 1L << 5L | 1L << 10L | 1L << 14L | 1L << 26L | 1L << 30L | 1L << 35L | 1L << 37L;
        jumps[21] = 1L << 4L | 1L << 6L | 1L << 11L | 1L << 15L | 1L << 27L | 1L << 31L | 1L << 36L | 1L << 38L;
        jumps[22] = 1L << 5L | 1L << 7L | 1L << 12L | 1L << 28L | 1L << 37L | 1L << 39L;
        jumps[23] = 1L << 6L | 1L << 13L | 1L << 29L | 1L << 38L;

        jumps[24] = 1L << 9L | 1L << 18L | 1L << 34L | 1L << 41L;
        jumps[25] = 1L << 8L | 1L << 10L | 1L << 19L | 1L << 35L | 1L << 40L | 1L << 42L;
        jumps[26] = 1L << 9L | 1L << 11L | 1L << 16L | 1L << 20L | 1L << 32L | 1L << 36 | 1L << 41L | 1L << 43L;
        jumps[27] = 1L << 10L | 1L << 12L | 1L << 17L | 1L << 21L | 1L << 33L | 1L << 37L | 1L << 42L | 1L << 44L;
        jumps[28] = 1L << 11L | 1L << 13L | 1L << 18L | 1L << 22L | 1L << 34L | 1L << 38L | 1L << 43L | 1L << 45L;
        jumps[29] = 1L << 12L | 1L << 14L | 1L << 19L | 1L << 23L | 1L << 35L | 1L << 39L | 1L << 44L | 1L << 46L;
        jumps[30] = 1L << 13L | 1L << 15L | 1L << 20L | 1L << 36L | 1L << 45L | 1L << 47L;
        jumps[31] = 1L << 14L | 1L << 21L | 1L << 37L | 1L << 46L;

        jumps[32] = 1L << 17L | 1L << 26L | 1L << 42L | 1L << 49L;
        jumps[33] = 1L << 16L | 1L << 18L | 1L << 27L | 1L << 43L | 1L << 48L | 1L << 50L;
        jumps[34] = 1L << 17L | 1L << 19L | 1L << 24L | 1L << 28L | 1L << 40L | 1L << 44L | 1L << 49L | 1L << 51L;
        jumps[35] = 1L << 18L | 1L << 20L | 1L << 25L | 1L << 29L | 1L << 41L | 1L << 45L | 1L << 50L | 1L << 52L;
        jumps[36] = 1L << 19L | 1L << 21L | 1L << 26L | 1L << 30L | 1L << 42L | 1L << 46L | 1L << 51L | 1L << 53L;
        jumps[37] = 1L << 20L | 1L << 22L | 1L << 27L | 1L << 31L | 1L << 43L | 1L << 47L | 1L << 52L | 1L << 54L;
        jumps[38] = 1L << 21L | 1L << 23L | 1L << 28L | 1L << 44L | 1L << 53L | 1L << 55L;
        jumps[39] = 1L << 22L | 1L << 29L | 1L << 45L | 1L << 54L;

        jumps[40] = 1L << 25L | 1L << 34L | 1L << 50L | 1L << 57L;
        jumps[41] = 1L << 24L | 1L << 26L | 1L << 35L | 1L << 51L | 1L << 56L | 1L << 58L;
        jumps[42] = 1L << 25L | 1L << 27L | 1L << 32L | 1L << 36L | 1L << 48L | 1L << 52L | 1L << 57L | 1L << 59L;
        jumps[43] = 1L << 26L | 1L << 28L | 1L << 33L | 1L << 37L | 1L << 49L | 1L << 53L | 1L << 58L | 1L << 60L;
        jumps[44] = 1L << 27L | 1L << 29L | 1L << 34L | 1L << 38L | 1L << 50L | 1L << 54L | 1L << 59L | 1L << 61L;
        jumps[45] = 1L << 28L | 1L << 30L | 1L << 35L | 1L << 39L | 1L << 51L | 1L << 55L | 1L << 60L | 1L << 62L;
        jumps[46] = 1L << 29L | 1L << 31L | 1L << 36L | 1L << 52L | 1L << 61L | 1L << 63L;
        jumps[47] = 1L << 30L | 1L << 37L | 1L << 53L | 1L << 62L;

        jumps[48] = 1L << 33L | 1L << 42L | 1L << 58L;
        jumps[49] = 1L << 32L | 1L << 34L | 1L << 43L | 1L << 59L;
        jumps[50] = 1L << 33L | 1L << 35L | 1L << 40L | 1L << 44L | 1L << 56L | 1L << 60L;
        jumps[51] = 1L << 34L | 1L << 36L | 1L << 41L | 1L << 45L | 1L << 57L | 1L << 61L;
        jumps[52] = 1L << 35L | 1L << 37L | 1L << 42L | 1L << 46L | 1L << 58L | 1L << 62L;
        jumps[53] = 1L << 36L | 1L << 38L | 1L << 43L | 1L << 47L | 1L << 59L | 1L << 63L;
        jumps[54] = 1L << 37L | 1L << 39L | 1L << 44L | 1L << 60L;
        jumps[55] = 1L << 38L | 1L << 45L | 1L << 61L;

        jumps[56] = 1L << 41L | 1L << 50L;
        jumps[57] = 1L << 40L | 1L << 42L | 1L << 51L;
        jumps[58] = 1L << 41L | 1L << 43L | 1L << 48L | 1L << 52L;
        jumps[59] = 1L << 42L | 1L << 44L | 1L << 49L | 1L << 53L;
        jumps[60] = 1L << 43L | 1L << 45L | 1L << 50L | 1L << 54L;
        jumps[61] = 1L << 44L | 1L << 46L | 1L << 51L | 1L << 55L;
        jumps[62] = 1L << 45L | 1L << 47L | 1L << 52L;
        jumps[63] = 1L << 46L | 1L << 53L;

        List<Long> result = Arrays.stream(jumps).boxed().collect(Collectors.toList());
        return result;
    }

    public void printBitboard(long bitboard) {
        for (int row = 7; row >= 0; --row) {
            for (int col = 0; col < 8; ++col) {
                long mask = 1L << (row * 8 + col);
                if ((bitboard & mask) != 0) {
                    System.out.print("1 ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }

    public Bitboard movePiece(int fromIndex, int toIndex, Class<?> pieceType, boolean isWhite) {
        long fromMask = 1L << fromIndex;
        long toMask = 1L << toIndex;
        long clearFromMask = ~fromMask;

        // new bitboard values
        long newWPawns = this.wPawns, newWKnights = this.wKnights, newWBishops = this.wBishops,
             newWRooks = this.wRooks, newWQueens = this.wQueens, newWKings = this.wKings,
             newBPawns = this.bPawns, newBKnights = this.bKnights, newBBishops = this.bBishops,
             newBRooks = this.bRooks, newBQueens = this.bQueens, newBKings = this.bKings;

        if (isWhite) {
            if (Pawn.class.equals(pieceType))
                newWPawns = (newWPawns & clearFromMask) | toMask;
            else if (Knight.class.equals(pieceType))
                newWKnights = (newWKnights & clearFromMask) | toMask;
            else if (Bishop.class.equals(pieceType))
                newWBishops = (newWBishops & clearFromMask) | toMask;
            else if (Rook.class.equals(pieceType))
                newWRooks = (newWRooks & clearFromMask) | toMask;
            else if (Queen.class.equals(pieceType))
                newWQueens = (newWQueens & clearFromMask) | toMask;
            else if (King.class.equals(pieceType))
                newWKings = (newWKings & clearFromMask) | toMask;
        } else {
            if (Pawn.class.equals(pieceType))
                newBPawns = (newBPawns & clearFromMask) | toMask;
            else if (Knight.class.equals(pieceType))
                newBKnights = (newBKnights & clearFromMask) | toMask;
            else if (Bishop.class.equals(pieceType))
                newBBishops = (newBBishops & clearFromMask) | toMask;
            else if (Rook.class.equals(pieceType))
                newBRooks = (newBRooks & clearFromMask) | toMask;
            else if (Queen.class.equals(pieceType))
                newBQueens = (newBQueens & clearFromMask) | toMask;
            else if (King.class.equals(pieceType))
                newBKings = (newBKings & clearFromMask) | toMask;
        }

        // clear target square if capture
        if (isWhite) {
            if ((toMask & this.blacks) != 0) {
                newBPawns &= ~toMask;
                newBKnights &= ~toMask;
                newBBishops &= ~toMask;
                newBRooks &= ~toMask;
                newBQueens &= ~toMask;
                newBKings &= ~toMask;
            }
        } else {
            if ((toMask & this.whites) != 0) {
                newWPawns &= ~toMask;
                newWKnights &= ~toMask;
                newWBishops &= ~toMask;
                newWRooks &= ~toMask;
                newWQueens &= ~toMask;
                newWKings &= ~toMask;
            }
        }

        return new Bitboard(newWPawns, newWKnights, newWBishops, newWRooks, newWQueens, newWKings, newBPawns, newBKnights, newBBishops, newBRooks, newBQueens, newBKings);
    }

    public Bitboard fromBoard(Board board) {
        long wPawns = 0L, wKnights = 0L, wBishops = 0L, wRooks = 0L, wQueens = 0L, wKings = 0L;
        long bPawns = 0L, bKnights = 0L, bBishops = 0L, bRooks = 0L, bQueens = 0L, bKings = 0L;

        for (int row = 0; row < 8; ++row) {
            for (int col = 0; col < 8; ++col) {
                Piece piece = board.get(new Location(row, col));
                if (piece == null)
                    continue;

                int bitIndex = row * 8 + col;
                long bitValue = 1L << bitIndex;

                if (piece instanceof Pawn) {
                    if (piece.getColor().equals(Color.WHITE))
                        wPawns |= bitValue;
                    else
                        bPawns |= bitValue;
                } else if (piece instanceof Knight) {
                    if (piece.getColor().equals(Color.WHITE))
                        wKnights |= bitValue;
                    else
                        bKnights |= bitValue;
                } else if (piece instanceof Bishop) {
                    if (piece.getColor().equals(Color.WHITE))
                        wBishops |= bitValue;
                    else
                        bBishops |= bitValue;
                } else if (piece instanceof Rook) {
                    if (piece.getColor().equals(Color.WHITE))
                        wRooks |= bitValue;
                    else
                        bRooks |= bitValue;
                } else if (piece instanceof Queen) {
                    if (piece.getColor().equals(Color.WHITE))
                        wQueens |= bitValue;
                    else
                        bQueens |= bitValue;
                } else if (piece instanceof King) {
                    if (piece.getColor().equals(Color.WHITE))
                        wKings |= bitValue;
                    else
                        bKings |= bitValue;
                }
            }
        }

        return new Bitboard(wPawns, wKnights, wBishops, wRooks, wQueens, wKings, bPawns, bKnights, bBishops, bRooks, bQueens, bKings);
    }

    public List<Move> generateAllMoves(Color color) {
        List<Move> moves = new ArrayList<>();
        boolean white = color.equals(Color.WHITE);
        long pieces = white ? this.whites : this.blacks;

        for (int i = 0; i < 64; ++i) {
            long mask = 1L << i;
            if ((mask & pieces) == 0)
                continue;
            if ((mask & (white ? this.wPawns : this.bPawns)) != 0)
                moves.addAll(movesForPiece(mask, Pawn.class, color));
            else if ((mask & (white ? this.wKnights : this.bKnights)) != 0)
                moves.addAll(movesForPiece(mask, Knight.class, color));
            else if ((mask & (white ? this.wBishops : this.bBishops)) != 0)
                moves.addAll(movesForPiece(mask, Bishop.class, color));
            else if ((mask & (white ? this.wRooks : this.bRooks)) != 0)
                moves.addAll(movesForPiece(mask, Rook.class, color));
            else if ((mask & (white ? this.wQueens : this.bQueens)) != 0)
                moves.addAll(movesForPiece(mask, Queen.class, color));
            else if ((mask & (white ? this.wKings : this.bKings)) != 0)
                moves.addAll(movesForPiece(mask, King.class, color));
        }
        
        return moves;
    }

    private List<Move> movesForPiece(long pieceMask, Class<?> pieceType, Color color) {
        List<Move> moves = new ArrayList<>();
        int position = Long.numberOfTrailingZeros(pieceMask);
    
        if (Pawn.class.equals(pieceType)) {
            int direction = (color == Color.WHITE) ? -8 : 8;
            int startRank = (color == Color.WHITE) ? 6 : 1;
            int leftCapture = (color == Color.WHITE) ? -9 : 7;
            int rightCapture = (color == Color.WHITE) ? -7 : 9;
        
            long allPieces = this.whites | this.blacks;
            long enemyPieces = (color == Color.WHITE) ? this.blacks : this.whites;
        
            int forwardOneIndex = position + direction;
            if (forwardOneIndex >= 0 && forwardOneIndex < 64 && ((1L << forwardOneIndex) & allPieces) == 0) {
                moves.add(new Move(new Pawn(color, null), new Location(position / 8, position % 8), new Location(forwardOneIndex / 8, forwardOneIndex % 8)));
            }
        
            if (position / 8 == startRank) {
                int forwardTwoIndex = position + 2 * direction;
                if (forwardTwoIndex >= 0 && forwardTwoIndex < 64 && ((1L << forwardTwoIndex) & allPieces) == 0 && ((1L << (position + direction)) & allPieces) == 0) {
                    moves.add(new Move(new Pawn(color, null), new Location(position / 8, position % 8), new Location(forwardTwoIndex / 8, forwardTwoIndex % 8)));
                }
            }
        
            int[] captures = {leftCapture, rightCapture};
            for (int capture : captures) {
                int captureIndex = position + capture;
                // wrap around
                boolean isValidCapture = true;
                if (capture == leftCapture && position % 8 == 0) {
                    isValidCapture = false;
                }
                if (capture == rightCapture && position % 8 == 7) {
                    isValidCapture = false;
                }
                if (isValidCapture && captureIndex >= 0 && captureIndex < 64 && ((1L << captureIndex) & enemyPieces) != 0) {
                    moves.add(new Move(new Pawn(color, null), new Location(position / 8, position % 8), new Location(captureIndex / 8, captureIndex % 8)));
                }
            }
        } else if (Knight.class.equals(pieceType)) {
            long possibleMoves = JUMPS.get(position);
            long allyPieces = (color == Color.WHITE) ? this.whites : this.blacks;
        
            for (int i = 0; i < 64; i++) {
                if ((possibleMoves & (1L << i)) != 0) {
                    if (((1L << i) & allyPieces) == 0) {
                        moves.add(new Move(new Knight(color, null), new Location(position / 8, position % 8), new Location(i / 8, i % 8)));
                    }
                }
            }
        } else if (Bishop.class.equals(pieceType)) {
            int[] directions = {-9, -7, 7, 9}; // All four diagonals
            for (int direction : directions) {
                int nextPosition = position + direction;
                while (nextPosition >= 0 && nextPosition < 64) {
                    // prevent wrapping
                    if ((direction == -9 || direction == 7) && nextPosition % 8 == 7) break;
                    if ((direction == -7 || direction == 9) && nextPosition % 8 == 0) break;
        
                    long nextPositionMask = 1L << nextPosition;
                    if ((nextPositionMask & (this.whites | this.blacks)) != 0) {
                        if ((nextPositionMask & (color == Color.WHITE ? this.blacks : this.whites)) != 0) {
                            moves.add(new Move(new Bishop(color, null), new Location(position / 8, position % 8), new Location(nextPosition / 8, nextPosition % 8)));
                        }
                        break;
                    }
        
                    moves.add(new Move(new Bishop(color, null), new Location(position / 8, position % 8), new Location(nextPosition / 8, nextPosition % 8)));
                    nextPosition += direction;
                }
            }
        } else if (Rook.class.equals(pieceType)) {
            int[] directions = {-8, 8, -1, 1};
            for (int direction : directions) {
                int nextPosition = position;
                while (true) {
                    nextPosition += direction;
                    
                    if (direction == -1 && nextPosition % 8 == 7) break;
                    if (direction == 1 && nextPosition % 8 == 0) break;
                    if (nextPosition < 0 || nextPosition >= 64) break;
        
                    long nextPositionMask = 1L << nextPosition;
                    if ((nextPositionMask & (this.whites | this.blacks)) != 0) {
                        if ((nextPositionMask & (color == Color.WHITE ? this.blacks : this.whites)) != 0) {
                            moves.add(new Move(new Rook(color, null), new Location(position / 8, position % 8), new Location(nextPosition / 8, nextPosition % 8)));
                        }
                        break;
                    }
        
                    moves.add(new Move(new Rook(color, null), new Location(position / 8, position % 8), new Location(nextPosition / 8, nextPosition % 8)));
                }
            }
        } else if (Queen.class.equals(pieceType)) {
            int[] directions = {-9, -8, -7, -1, 1, 7, 8, 9};
            for (int direction : directions) {
                int nextPosition = position;
                while (true) {
                    nextPosition += direction;
        
                    if (nextPosition < 0 || nextPosition >= 64) break;
                    if (direction == -1 && nextPosition % 8 == 7) break;
                    if (direction == 1 && nextPosition % 8 == 0) break;
                    if ((direction == -9 || direction == 7) && nextPosition % 8 == 7) break;
                    if ((direction == -7 || direction == 9) && nextPosition % 8 == 0) break;
        
                    long nextPositionMask = 1L << nextPosition;
        
                    if ((nextPositionMask & (this.whites | this.blacks)) != 0) {
                        if ((nextPositionMask & (color == Color.WHITE ? this.blacks : this.whites)) != 0) {
                            moves.add(new Move(new Queen(color, null), new Location(position / 8, position % 8), new Location(nextPosition / 8, nextPosition % 8)));
                        }
                        break;
                    }
                    moves.add(new Move(new Queen(color, null), new Location(position / 8, position % 8), new Location(nextPosition / 8, nextPosition % 8)));
                }
            }
        }   else if (King.class.equals(pieceType)) {
            int[] directions = {-9, -8, -7, -1, 1, 7, 8, 9};
        
            for (int direction : directions) {
                int nextPosition = position + direction;
        
                if (nextPosition >= 0 && nextPosition < 64 && Math.abs(nextPosition % 8 - position % 8) <= 1) {
                    long nextPositionMask = 1L << nextPosition;
                    long occupied = this.whites | this.blacks;
        
                    if ((nextPositionMask & occupied) == 0 || (nextPositionMask & (color == Color.WHITE ? this.blacks : this.whites)) != 0) {
                        moves.add(new Move(new King(color, null), new Location(position / 8, position % 8), new Location(nextPosition / 8, nextPosition % 8)));
                    }
                }
            }
        }
    
        return moves;
    }

    public int index(Location location) {
        return location.getRow() * 8 + location.getCol();
    }

    public Bitboard processMove(Move move) {
        int fromIndex = index(move.getSource());
        int toIndex = index(move.getDestination());
        Class<?> pieceType = move.getPiece().getClass();
        boolean isWhite = move.getPiece().getColor().equals(Color.WHITE);
    
        return this.movePiece(fromIndex, toIndex, pieceType, isWhite);
    }

    public void printWhitePieces() {
        this.printBitboard(this.whites);
    }

    public void printBlackPieces() {
        this.printBitboard(this.blacks);
    }

    public double[][][] to3dBitboard() {
        double[][][] bitboards = new double[19][8][8];

        fillLayer(bitboards, this.wPawns, 0);
        fillLayer(bitboards, this.wKnights, 1);
        fillLayer(bitboards, this.wBishops, 2);
        fillLayer(bitboards, this.wRooks, 3);
        fillLayer(bitboards, this.wQueens, 4);
        fillLayer(bitboards, this.wKings, 5);
        fillLayer(bitboards, this.bPawns, 6);
        fillLayer(bitboards, this.bKnights, 7);
        fillLayer(bitboards, this.bBishops, 8);
        fillLayer(bitboards, this.bRooks, 9);
        fillLayer(bitboards, this.bQueens, 10);
        fillLayer(bitboards, this.bKings, 11);

        // 12, 13, 14, 15 = KQkq castling
        // 17 = active color
        // bitboards[17][r][c] = this.moveWhite ? "w" : "b";
        // bitboards[18][r][c] = this.moveNumber;

        return bitboards;
    }

    private void fillLayer(double[][][] bitboards, long pieceLayer, int layerIndex) {
        for (int i = 0; i < 64; ++i) {
            if ((pieceLayer & (1L << i)) != 0) {
                int row = i / 8;
                int col = i % 8;
                bitboards[layerIndex][row][col] = 1;
            }
        }
    }

    public Bitboard copy() {
        return new Bitboard(this.wPawns, this.wKnights, this.wBishops, this.wRooks, this.wQueens, this.wKings, this.bPawns, this.bKnights, this.bBishops, this.bRooks, this.bQueens, this.bKings);
    }

    public boolean endgame() {
        return (Long.bitCount(this.whites) + Long.bitCount(this.blacks)) <= 20;
    }

    public boolean midgame() {
        return (Long.bitCount(this.whites) + Long.bitCount(this.blacks)) <= 28;
    }
    
    public boolean tactical(Color color) {
        boolean w = color.equals(Color.WHITE);
        long m = w ? this.blackMoves() : this.whiteMoves();
        if (Long.bitCount(m & (w ? this.wKings : this.bKings)) >= 1) return true;
        if (Long.bitCount(m & (w ? this.wQueens : this.bQueens)) >= 1) return true;
        int attacked = 2;
        attacked += Long.bitCount(m & (w ? this.wKnights : this.bKnights));
        if (attacked >= 3) return true;
        attacked += Long.bitCount(m & (w ? this.wBishops : this.bBishops));
        if (attacked >= 3) return true;
        attacked += Long.bitCount(m & (w ? this.wRooks : this.bRooks));
        return attacked >= 3;
    }

    public List<Move> captureMoves(Color color) {
        List<Move> allMoves = generateAllMoves(color);
        long opponentPieces = color.equals(Color.WHITE) ? this.blacks : this.whites;
    
        return allMoves.stream().filter(move -> {
            Location destination = move.getDestination();
            long destinationMask = 1L << index(destination);
            return (destinationMask & opponentPieces) != 0;
        }).collect(Collectors.toList());
    }

    public Piece get(Location l) {
        int index = this.index(l);
        long mask = 1L << index;
        if ((mask & this.wPawns) != 0) {
            return new Pawn(Color.WHITE, null);
        } else if ((mask & this.bPawns) != 0) {
            return new Pawn(Color.BLACK, null);
        } else if ((mask & this.wKnights) != 0) {
            return new Knight(Color.WHITE, null);
        } else if ((mask & this.bKnights) != 0) {
            return new Knight(Color.BLACK, null);
        } else if ((mask & this.wBishops) != 0) {
            return new Bishop(Color.WHITE, null);
        } else if ((mask & this.bBishops) != 0) {
            return new Bishop(Color.BLACK, null);
        } else if ((mask & this.wRooks) != 0) {
            return new Rook(Color.WHITE, null);
        } else if ((mask & this.bRooks) != 0) {
            return new Rook(Color.BLACK, null);
        } else if ((mask & this.wQueens) != 0) {
            return new Queen(Color.WHITE, null);
        } else if ((mask & this.bQueens) != 0) {
            return new Queen(Color.BLACK, null);
        } else if ((mask & this.wKings) != 0) {
            return new King(Color.WHITE, null);
        } else if ((mask & this.bKings) != 0) {
            return new King(Color.BLACK, null);
        }
        return null; // king or empty
    }
}