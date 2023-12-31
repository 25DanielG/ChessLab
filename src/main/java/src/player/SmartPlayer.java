package src.player;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import src.Location;
import src.Move;
import src.board.*;
import src.eval.*;
import src.piece.*;

/**
 * A smart player utilizing minimax to play chess very well. The player has dynamic depth and uses
 *      alpha-beta pruning with move ordering to become efficient.
 * @author Daniel Gergov
 * @version 3/31/23
 */
public class SmartPlayer extends Player
{
    private static final int DEPTH = 10;
    private static final int TIMEOUT = 10000; // ms
    private static final int WINDOW = 100;

    private static final Logger logger = LogManager.getLogger(src.player.SmartPlayer.class);
    private int[][] historyTable = new int[6][64];
    private ExecutorService executorService;
    public Board board;
    private Map<Integer, Move> moves;
    public int MAX_DEPTH = 0;
    private OpeningEngine openingEngine;
    private boolean opening = true;
    public String open;
	public String line;

    /**
     * Default constructor for Smart Player
     * @param board the board that player belongs
     * @param color the color of pieces that the player plays with
     * @param name the name of the player
     */
    public SmartPlayer(Board board, Color color, String name)
    {
        super(board, "SmartPlayer", color);
        this.board = board;
        moves = new HashMap<Integer, Move>();
        openingEngine = new OpeningEngine(this);
        int availableCores = Runtime.getRuntime().availableProcessors();
        this.executorService = Executors.newFixedThreadPool(availableCores);
    }

    /**
     * Returns the nextMove that the player finds and wants to play
     * @return type Move, the move returned to play
     */
    public Move nextMove() 
    {
        if (opening && openingEngine.search(board)) {
            String move = this.open;
            Location destination = new Location(8 - Character.getNumericValue(move.charAt(move.length() - 1)), move.charAt(move.length() - 2) - 'a');
            Location source = findSource((Character.isLowerCase(move.charAt(0))) ? 'P' : move.charAt(0), destination, board);
            if (source != null) {
                System.out.println("---------------------------------");
                System.out.println("Theory: " + this.line);
                System.out.println("Move: " + this.open);
                System.out.println("---------------------------------");
                return new Move(board.get(source), source, destination);
            }
        } else
            opening = false;
        Bitboard bitboard = new Bitboard().fromBoard(getBoard()); bitboard.moveNumber = board.fullMove; bitboard.moveWhite = getColor().equals(Color.WHITE);
        Object[] best = (findBestMove(bitboard, DEPTH, TIMEOUT));
        double[][][] bitboards = getBoard().boardToBitboards();
        System.out.println("---------------------------------");
        System.out.println("Move Score: " + best[0] + ", Depth: " + MAX_DEPTH);
        System.out.println("Move: " + (Move) best[1] + ", Network Score: " + src.eval.Score.networkScore(bitboards));
        System.out.println("Midgame: " + bitboard.midgame() + ", Endgame: " + bitboard.endgame() + ", Tactical: " + bitboard.tactical(getColor()));
        System.out.println("---------------------------------");
        return (Move) best[1];
    }

    /**
     * Finds the best move in a current chess position by using iterative deepening.
     * @param maxDepth the maxDepth to search the minimax tree
     * @param timeout the timeout in milliseconds to stop the search
     * @param board the current state of the game as a Bitboard object
     * @return Object[] index 0 containing the max score and index 1 containing the best move
     */
    public Object[] findBestMove(Bitboard board, int maxDepth, long timeout) {
        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        Thread currentThread = Thread.currentThread();
        Thread timerThread = new Thread(() -> {
            try {
                Thread.sleep(timeout);
                currentThread.interrupt();
            } catch (InterruptedException e) {
                currentThread.interrupt();
            }
        });
        timerThread.start();
    
        for (int depth = 2; depth <= maxDepth; depth++) {
            long time = System.currentTimeMillis();
            int alpha = bestScore - WINDOW;
            int beta = bestScore + WINDOW;
            Object[] result = minimax(board, depth, alpha, beta, true, null);
    
            // aspiration window
            if (result != null && ((int) result[0] <= alpha || (int) result[0] >= beta)) {
                alpha = Integer.MIN_VALUE; // re-search
                beta = Integer.MAX_VALUE;
                System.out.println("Re-searching...");
                result = minimax(board, depth, alpha, beta, true, null);
            }
    
            if (Thread.currentThread().isInterrupted() || result == null)
                break;
            System.out.println("Ran depth " + depth + " in " + (System.currentTimeMillis() - time) + "ms");
    
            if (result != null) {
                MAX_DEPTH = depth;
                Move m = (Move) result[1];
                bestMove = new Move(this.board.get(m.getSource()), m.getSource(), m.getDestination());
                bestScore = (int) result[0];
                moves.put(depth, bestMove);
            }
        }
    
        timerThread.interrupt();
        return new Object[] {bestScore, bestMove};
    }

    private Object[] minimax(Bitboard board, int depth, int alpha, int beta, boolean maximize, Move last) {
        if (depth <= 0)
            return quiescence(board, alpha, beta, maximize);
    
        if (Thread.currentThread().isInterrupted())
            return null;
        
        Color color = maximize ? getColor() : (getColor().equals(Color.BLACK) ? Color.WHITE : Color.BLACK);

        // verified null move heuristic
        if (depth >= 4 && !board.endgame() && !board.tactical(color)) {
            Bitboard nullMoveBoard = board.copy();
            nullMoveBoard.moveWhite = !board.moveWhite;
        
            // null move reduction
            Object[] nullMoveResult = minimax(nullMoveBoard, depth - 3 - 1, alpha, beta, !maximize, null);
            if (nullMoveResult == null || Thread.currentThread().isInterrupted())
                return null;
            int nullScore = (int) nullMoveResult[0];
            
            // null move verification
            if ((maximize && nullScore >= beta) || (!maximize && nullScore <= alpha)) {
                Object[] verificationResult = minimax(board, depth - 4, alpha, beta, maximize, null);
                if (verificationResult == null || Thread.currentThread().isInterrupted())
                    return null;
                int verification = (int) verificationResult[0];
        
                if ((maximize && verification >= beta) || (!maximize && verification <= alpha))
                    return nullMoveResult;
            }
        }
    
        List<Move> moves = board.generateAllMoves(color);
        moves.sort(advancedComparator(board)); // mvvlva ordering
        Move bestMove = null;
        int bestScore = maximize ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Move move : moves) {
            if (board.get(move.getDestination()) instanceof King)
                return null; // illegal move

            Bitboard newBoard = board.processMove(move);
            newBoard.moveNumber = board.moveNumber + 1;
            newBoard.moveWhite = !board.moveWhite;

            int extension = this.isCritical(move, newBoard) ? 1 : 0; // extend search in critical positions
            Object[] response = minimax(newBoard, depth - 1 + extension, alpha, beta, !maximize, move);

            if (response == null)
                continue;
            
            int score = (int) response[0];

            if ((maximize && score >= beta) || (!maximize && score <= alpha)) {
                updateHistory(move, depth); // update history table for cutoff
            }
    
            if (maximize && score > bestScore) {
                bestScore = score;
                bestMove = move;
            } else if (!maximize && score < bestScore) {
                bestScore = score;
                bestMove = move;
            }
    
            if (maximize)
                alpha = Math.max(alpha, score);
            else
                beta = Math.min(beta, score);
            if (beta <= alpha)
                break;
        }
        bestMove = (bestMove != null) ? new Move(this.board.get(bestMove.getSource()), bestMove.getSource(), bestMove.getDestination()) : null;
        if (Thread.currentThread().isInterrupted())
            return null;
        return new Object[] {bestScore, bestMove};
    }

    private Object[] quiescence(Bitboard board, int alpha, int beta, boolean maximize) {
        int standPat = Score.bitboardScore(board);
        if (maximize) {
            if (standPat >= beta) {
                return new Object[]{beta, null};
            }
            if (standPat > alpha) {
                alpha = standPat;
            }
        } else {
            if (standPat <= alpha) {
                return new Object[]{alpha, null};
            }
            if (standPat < beta) {
                beta = standPat;
            }
        }
    
        List<Move> captureMoves = board.captureMoves(maximize ? getColor() : (getColor().equals(Color.BLACK) ? Color.WHITE : Color.BLACK));
        captureMoves.sort(advancedComparator(board)); // mvvlva ordering
        for (Move move : captureMoves) {
            Bitboard newBoard = board.processMove(move);
            newBoard.moveNumber = board.moveNumber + 1;
            newBoard.moveWhite = !board.moveWhite;
    
            Object[] response = quiescence(newBoard, alpha, beta, !maximize);
            if (response == null) continue;
    
            int score = (int) response[0];
            if (maximize) {
                if (score >= beta) {
                    return new Object[]{beta, null};
                }
                if (score > alpha) {
                    alpha = score;
                }
            } else {
                if (score <= alpha) {
                    return new Object[]{alpha, null};
                }
                if (score < beta) {
                    beta = score;
                }
            }
        }
        return new Object[]{maximize ? alpha : beta, null};
    }

    private Location findSource(char pieceChar, Location destination, Board board) {
        Vector<Move> moves = board.allMoves(getColor());
        Piece p;
        for (Move m : moves) {
            p = m.getPiece();
            if (p instanceof Pawn && pieceChar == 'P' && m.getDestination().equals(destination)) {
                return m.getSource();
            } else if (p instanceof Knight && pieceChar == 'N' && m.getDestination().equals(destination)) {
                return m.getSource();
            } else if (p instanceof Bishop && pieceChar == 'B' && m.getDestination().equals(destination)) {
                return m.getSource();
            } else if (p instanceof Rook && pieceChar == 'R' && m.getDestination().equals(destination)) {
                return m.getSource();
            } else if (p instanceof Queen && pieceChar == 'Q' && m.getDestination().equals(destination)) {
                return m.getSource();
            } else if (p instanceof King && pieceChar == 'K' && m.getDestination().equals(destination)) {
                return m.getSource();
            }
        }
        throw new IllegalArgumentException("No piece found for " + pieceChar + " at " + destination);
    }

    private Comparator<Move> mvvLvaComparator(Bitboard board) {
        return (Move m1, Move m2) -> {
            boolean m1Capture = isCapture(m1, board);
            boolean m2Capture = isCapture(m2, board);
            if (m1Capture && m2Capture) {
                return Integer.compare(Score.mvvlva(m2, board), Score.mvvlva(m1, board));
            }
            return m1Capture ? -1 : (m2Capture ? 1 : 0);
        };
    }

    private Comparator<Move> advancedComparator(Bitboard board) {
        return (Move m1, Move m2) -> {
            boolean m1Capture = isCapture(m1, board);
            boolean m2Capture = isCapture(m2, board);
    
            if (m1Capture && m2Capture) {
                // both captures, use MVV-LVA
                return Integer.compare(Score.mvvlva(m2, board), Score.mvvlva(m1, board));
            } else if (!m1Capture && !m2Capture) {
                // neither a capture, use history scores
                int score1 = this.historyTable[indexPiece(m1.getPiece())][index(m1.getDestination())];
                int score2 = this.historyTable[indexPiece(m2.getPiece())][index(m2.getDestination())];
                return Integer.compare(score2, score1);
            } else {
                // prioritize capture
                return m1Capture ? -1 : 1;
            }
        };
    }
    

    private boolean isCapture(Move move, Bitboard board) {
        int d = board.index(move.getDestination());
        long mask = 1L << d;
        long opp = move.getPiece().getColor() == Color.WHITE ? board.blacks : board.whites;
        return (mask & opp) != 0;
    }

    public boolean isCritical(Move move, Bitboard board) {
        boolean w = move.getPiece().getColor().equals(Color.WHITE);
        int d = board.index(move.getDestination());
        long mask = 1L << d;
        return (mask & (w ? board.bKings : board.wKings)) != 0;
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            } 
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    private void updateHistory(Move move, int depth) {
        int pieceType = indexPiece(move.getPiece()); // Convert piece to an index, e.g., Pawn -> 0, Knight -> 1, etc.
        int destination = index(move.getDestination()); // Convert destination square to an index (0-63)
        historyTable[pieceType][destination] += depth * depth; // Increment history score based on depth
    }

    private int indexPiece(Piece p) {
        if (p instanceof Pawn)
            return 0;
        else if (p instanceof Knight)
            return 1;
        else if (p instanceof Bishop)
            return 2;
        else if (p instanceof Rook)
            return 3;
        else if (p instanceof Queen)
            return 4;
        else if (p instanceof King)
            return 5;
        throw new IllegalArgumentException("Invalid piece type given to indexPiece: " + p);
    }

    private int index(Location location) {
        return location.getRow() * 8 + location.getCol();
    }
}