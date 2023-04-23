package src;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * A smart player utilizing minimax to play chess very well. The player has dynamic depth and uses
 *      alpha-beta pruning with move ordering to become efficient.
 * @author Daniel Gergov
 * @version 3/31/23
 */
public class SmartPlayer extends Player 
{
    /**
     * Default constructor for Smart Player
     * @param board the board that player belongs
     * @param color the color of pieces that the player plays with
     * @param name the name of the player
     */
    public SmartPlayer(Board board, Color color, String name)
    {
        super(board, "SmartPlayer", color);
    }

    /**
     * Returns the nextMove that the player finds and wants to play
     * @return type Move, the move returned to play
     */
    public Move nextMove() 
    {
        Object[] best = (findBestMove(10, 5000));
        System.out.println("----------------------");
        System.out.println("Score: " + best[0]);
        System.out.println("----------------------");
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
        for (int depth = 1; depth <= maxDepth; depth++)
        {
            if (result != null)
            {
                alpha = bestScore - 1000;
                beta = bestScore + 1000;
            }
            result = valueOfBestMove(depth, alpha, beta);
            if (result != null)
            {
                int score = (int) result[0];
                Move move = (Move) result[1];
                maxDepthReached = depth;
                bestMove = move;
                bestScore = score;
            }
        }
        timerThread.interrupt();
        System.out.println("Parsed tree with depth: " + maxDepthReached);
        return new Object[]{bestScore, bestMove};
    }    
    
    /**
     * Finds the value of the worst response that can be returned to the smartest player by looking
     *      at all possible moves for the opponent and finding the best move for the opponent so
     *      that the minimax can minimize the score.
     * @param depth the depth at which the function is at, base case stops at 0
     * @param alpha the alpha value for alpha-beta pruning
     * @param beta the beta value for alpha-beta pruning
     * @postcondition Object[] has length two, 0 index score, 1 index type Move (best move)
     * @return Object[] the 0 index containing the min score and the 1 indexx containing the best
     *      move
     */
    private Object[] valueOfMeanestResponse(int depth, int alpha, int beta)
    {
        if (depth <= 0)
        {
            return new Object[] {Score.score(getBoard(), getColor()), null};
        }
        Color color = getColor();
        if (color.equals(Color.BLACK))
        {
            color = Color.WHITE;
        }
        else
        {
            color = Color.BLACK;
        }
        ArrayList<Move> moves = getBoard().allMoves(color);
        int min = Integer.MAX_VALUE;
        Move best = null;
        for (Move move : moves)
        {
            getBoard().executeMove(move);
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
            if (min <= alpha)
            {
                break;
            }
            if (min < beta)
            {
                beta = min;
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
            return new Object[] {Score.score(getBoard(), getColor()), null};
        }
        if (Thread.currentThread().isInterrupted())
        {
            return null;
        }
    
        ArrayList<Move> moves = getBoard().strategicMoves(getColor());
        moves.sort((m1, m2) ->
        {
            Piece p1 = getBoard().get(m1.getDestination());
            Piece p2 = getBoard().get(m2.getDestination());
            int val1 = (p1 == null) ? 0 : p1.getValue();
            int val2 = (p2 == null) ? 0 : p2.getValue();
            return Integer.compare(val2, val1);
        });
    
        int max = Integer.MIN_VALUE;
        Move best = null;
        for (Move move : moves)
        {
            getBoard().executeMove(move);
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
            }
            getBoard().undoMove(move);
    
            if (max >= beta)
            {
                break;
            }
            if (max > alpha)
            {
                alpha = max;
            }
        }
    
        // Quiescence search
        if (depth == 1)
        {
            int stand_pat = Score.score(getBoard(), getColor());
            if (stand_pat >= beta)
            {
                return new Object[] {stand_pat, null};
            }
            if (alpha < stand_pat)
            {
                alpha = stand_pat;
            }
            ArrayList<Move> allMoves = getBoard().allMoves(getColor());
            for (Move move : allMoves)
            {
                if (move.getVictim() == null)
                {
                    continue;
                }
                getBoard().executeMove(move);
                int score = -quiescenceSearch(4, -beta, -alpha);
                getBoard().undoMove(move);
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
        return new Object[] {max, best};
    }
    
    private int quiescenceSearch(int depth, int alpha, int beta) {
        int stand_pat = Score.score(getBoard(), getColor());
        if (depth == 0)
        {
            return stand_pat;
        }
        if (stand_pat >= beta)
        {
            return beta;
        }
        if (alpha < stand_pat)
        {
            alpha = stand_pat;
        }
        ArrayList<Move> captureMoves = getBoard().strategicMoves(getColor());
        for (Move move : captureMoves)
        {
            if (move.getVictim() == null)
            {
                continue;
            }
            getBoard().executeMove(move);
            int score = -quiescenceSearch(depth - 1, -beta, -alpha);
            getBoard().undoMove(move);
            if (score >= beta)
            {
                return beta;
            }
            if (score > alpha)
            {
                alpha = score;
            }
        }
        return alpha;
    }
}
