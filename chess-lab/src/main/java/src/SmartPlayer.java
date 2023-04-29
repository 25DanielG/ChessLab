package src;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Vector;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

/**
 * A smart player utilizing minimax to play chess very well. The player has dynamic depth and uses
 *      alpha-beta pruning with move ordering to become efficient.
 * @author Daniel Gergov
 * @version 3/31/23
 */
public class SmartPlayer extends Player 
{
    private MultiLayerNetwork MLP;

    /**
     * Default constructor for Smart Player
     * @param board the board that player belongs
     * @param color the color of pieces that the player plays with
     * @param name the name of the player
     */
    public SmartPlayer(Board board, Color color, String name, MultiLayerNetwork MLP)
    {
        super(board, "SmartPlayer", color);
        this.MLP = MLP;
    }

    /**
     * Returns the nextMove that the player finds and wants to play
     * @return type Move, the move returned to play
     */
    public Move nextMove() 
    {
        Object[] best = (findBestMove(6, 15000));
        System.out.println("---------------------------------");
        System.out.println("Score: " + best[0] + ", Line: " + best[2] + ", Depth: " + best[3]);
        String FEN = getBoard().toFEN(getColor().equals(Color.WHITE) ? Color.BLACK : Color.WHITE);
        System.out.println("Board: " + FEN);
        System.out.println("MLP Score: " + src.MLP.score(FEN, MLP));
        System.out.println("---------------------------------");
        return (Move) best[1];
    }

    /**
     * Finds the best move in a current chess position by using iterative deepening by 
     *      progressively searching the minimax tree in deeper and deeper depths to 
     *      increase efficiency. Once the timeout is reached, the method automatically
     *      returns from all the recursive calls.
     * @param maxDepth the maxDepth to search the minimax tree
     * @param timeout the timeout in milliseconds to stop the search
     * @return Object[] index 0 containing the max score and index 1 containing the best move
     */
    public Object[] findBestMove(int maxDepth, long timeout)
    {
        Move bestMove = null;
        Move meanest = null;
        int bestScore = Integer.MIN_VALUE;
        Object[] result = null;
        int maxDepthReached = 0;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;
        Thread currentThread = Thread.currentThread();
        Thread timerThread = new Thread(() -> {
            try
            {
                Thread.sleep(timeout);
                System.out.println("Time limit reached!");
                currentThread.interrupt();
            }
            catch (InterruptedException e)
            {
                currentThread.interrupt();
            }
        });
        timerThread.start();
        for (int depth = 2; depth <= maxDepth; depth++)
        {
            long time = System.currentTimeMillis();
            result = valueOfBestMove(depth, alpha, beta);
            if (!Thread.currentThread().isInterrupted())
            {
                System.out.println("Ran depth " + depth + " in " + (System.currentTimeMillis() - time) + "ms");
            }
            if (result != null && depth > maxDepthReached)
            {
                maxDepthReached = depth;
                bestMove = (Move) result[1];
                bestScore = (int) result[0];
                meanest = (Move) result[2];
            }
        }
        timerThread.interrupt();
        return new Object[] {bestScore, bestMove, meanest, maxDepthReached};
    }    
    
    /**
     * Finds the value of the worst response that can be returned to the smartest player by looking
     *      at all possible moves for the opponent and finding the best move for the opponent so
     *      that the minimax can minimize the score.
     * @param depth the depth at which the function is at, base case stops at 0
     * @param alpha the alpha value for alpha-beta pruning
     * @param beta the beta value for alpha-beta pruning
     * @postcondition Object[] has length two, 0 index score, 1 index type Move (best move)
     * @return Object[] the 0 index containing the min score and the 1 index containing the best
     *      move
     */
    private Object[] valueOfMeanestResponse(int depth, int alpha, int beta)
    {
        if (depth <= 0)
        {
            return new Object[] {Score.mainScore(getBoard(), getColor()), null};
        }
        Color oppositeColor = getColor().equals(Color.BLACK) ? Color.WHITE : Color.BLACK;
        Vector<Move> moves = getBoard().allMoves(oppositeColor);
        moves.sort((m1, m2) -> Integer.compare(m1.getScore(), m2.getScore()));
        int min = Integer.MAX_VALUE;
        Move best = null;
        int previousScore = Score.quickScore(getBoard(), oppositeColor);
        for (Move move : moves)
        {
            getBoard().executeMove(move);
            int quickScored = Score.quickScore(getBoard(), oppositeColor);
            if (previousScore - quickScored > 500)
            {
                getBoard().undoMove(move);
                continue;
            }
            Object[] p = valueOfBestMove(depth - 1, alpha, beta);
            if (p == null)
            {
                getBoard().undoMove(move);
                return null;
            }
            if ((int) (p[0]) < min)
            {
                min = (int) (p[0]);
                best = move;
            }
            getBoard().undoMove(move);
            if (min < beta)
            {
                beta = min;
            }
            if (beta <= alpha)
            {
                break;
            }
        }
        return new Object[] {min, best};
    }

    /**
     * Finds the value of the best response of the current player so that the minimax
     *      can maximize the score.
     * @param depth the depth at which the function is at, base case stops at 0
     * @param alpha the alpha value for alpha-beta pruning
     * @param beta the beta value for alpha-beta pruning
     * @postcondition Object[] has length two, 0 index score, 1 index type Move (best move)
     * @return Object[] the 0 index containing the max score and the 1 indexx containing the best
     *      move
     */
    private Object[] valueOfBestMove(int depth, int alpha, int beta)
    {
        if (depth <= 0)
        {
            return new Object[] {Score.mainScore(getBoard(), getColor()), null};
        }
        if (Thread.currentThread().isInterrupted())
        {
            return null;
        }
        Vector<Move> moves = getBoard().allMoves(getColor());
        moves.sort((m1, m2) -> -Integer.compare(m1.getScore(), m2.getScore()));
        int max = Integer.MIN_VALUE;
        Move best = null;
        Move meanest = null;
        int previousScore = Score.quickScore(getBoard(), getColor());
        for (Move move : moves)
        {
            getBoard().executeMove(move);
            int quickScored = Score.quickScore(getBoard(), getColor());
            if (previousScore - quickScored > 500)
            {
                getBoard().undoMove(move);
                continue;
            }
            Object[] p = valueOfMeanestResponse(depth - 1, alpha, beta);
            if (p == null)
            {
                getBoard().undoMove(move);
                return null;
            }
            int score = (int) p[0];
            if (score > max)
            {
                max = score;
                best = move;
                meanest = (Move) p[1];
            }
            getBoard().undoMove(move);
            if (max > alpha)
            {
                alpha = max;
            }
            if (beta <= alpha)
            {
                break;
            }
        }
        // Quiescence search
        if (depth == 1)
        {
            Object[] q = quiescenceSearch(4, alpha, beta);
            if (q != null)
            {
                int score = (int) q[0];
                Move move = (Move) q[1];
                if (score >= beta)
                {
                    return new Object[] {score, null};
                }
                if (score > alpha)
                {
                    alpha = score;
                    best = move;
                }
            }
        }
        return new Object[] {max, best, meanest};
    }
    
    private Object[] quiescenceSearch(int depth, int alpha, int beta)
    {
        if (depth <= 0)
        {
            return new Object[] {Score.mainScore(getBoard(), getColor()), null};
        }
        int stand_pat = Score.mainScore(getBoard(), getColor());
        if (stand_pat >= beta)
        {
            return new Object[] {stand_pat, null};
        }
        if (alpha < stand_pat)
        {
            alpha = stand_pat;
        }
        Vector<Move> allMoves = getBoard().strategicMoves(getColor());
        Move best = null;
        int max = alpha;
        for (Move move : allMoves.parallelStream().toArray(Move[]::new))
        {
            getBoard().executeMove(move);
            int score = -Score.mainScore(getBoard(), getColor());
            getBoard().undoMove(move);
            if (score >= beta)
            {
                return new Object[] {score, null};
            }
            if (score > max)
            {
                max = score;
                best = move;
            }
        }
        return new Object[] {max, best};
    }
}
