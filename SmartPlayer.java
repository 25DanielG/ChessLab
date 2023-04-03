import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

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
        return (Move) (valueOfBestMove(5, Integer.MIN_VALUE, Integer.MAX_VALUE)[1]);
    }

    /**
     * The scoring function to score a boards position, the scoring function relies on a more
     *      efficient piece evaluation point system as well as the mobility of each piece.
     * @return int the score, the higher the better for the player
     */
    private int score()
    {
        Board board = getBoard();
        int score = 0;
        int pawnValue = 100, knightValue = 320, bishopValue = 330, rookValue = 500;
        int queenValue = 900, kingValue = 20000;
        int centerBonus = 20, centerSquare = board.getNumRows() / 2;
        int[] centerRows = { centerSquare - 1, centerSquare, centerSquare + 1 };
        Color opposite;
        if (getColor().equals(Color.WHITE))
        {
            opposite = Color.BLACK;
        }
        else
        {
            opposite = Color.WHITE;
        }
        int botPawnStructureScore = evaluatePawnStructure(board, getColor());
        int oppPawnStructureScore = evaluatePawnStructure(board, opposite);
        for (int row = 0; row < board.getNumRows(); row++)
        {
            for (int col = 0; col < board.getNumCols(); col++)
            {
                Location loc = new Location(row, col);
                Piece piece = board.get(loc);
                if (piece != null)
                {
                    int value = 0;
                    if (piece instanceof Pawn)
                    {
                        value = pawnValue;
                    } else if (piece instanceof Knight)
                    {
                        value = knightValue;
                    } else if (piece instanceof Bishop)
                    {
                        value = bishopValue;
                    } else if (piece instanceof Rook)
                    {
                        value = rookValue;
                    } else if (piece instanceof Queen)
                    {
                        value = queenValue;
                    } else if (piece instanceof King)
                    {
                        value = kingValue;
                    }
                    int mobility = piece.destinations().size();
                    if (piece instanceof Knight || piece instanceof Bishop)
                    {
                        value += mobility * 10;
                    } else if (piece instanceof Rook || piece instanceof Queen)
                    {
                        value += mobility * 5;
                    }
                    if (Arrays.binarySearch(centerRows, row) >= 0 
                        && Arrays.binarySearch(centerRows, col) >= 0)
                    {
                        value += centerBonus;
                    }
                    if (piece.getColor().equals(getColor()))
                    {
                        score += value;
                    } else
                    {
                        score -= value;
                    }
                }
            }
        }
        score += botPawnStructureScore;
        score -= oppPawnStructureScore;
        return score;
    }

    /**
     * Evaluats the pawn structure of a specific color and returns a score based on that pawn
     *      structure.
     * @param board the board to evaluate the pawn structure of
     * @param color the color pieces to evaluate the pawn structure of
     * @return int the score of the pawn structure
     */    
    private int evaluatePawnStructure(Board board, Color color)
    {
        int pawnStructureScore = 0;
        for (int row = 0; row < board.getNumRows(); row++)
        {
            for (int col = 0; col < board.getNumCols(); col++)
            {
                Location loc = new Location(row, col);
                Piece piece = board.get(loc);
    
                if (piece instanceof Pawn && piece.getColor().equals(color))
                {
                    if (isPassedPawn((Pawn) piece, board))
                    {
                        pawnStructureScore += 20;
                    } else if (isIsolatedPawn((Pawn) piece, board))
                    {
                        pawnStructureScore -= 10;
                    } else if (isDoubledPawn((Pawn) piece, board))
                    {
                        pawnStructureScore -= 10;
                    } else if (isPawnChain((Pawn) piece, board))
                    {
                        pawnStructureScore += 5;
                    }
                }
            }
        }
        return pawnStructureScore;
    }

    /**
     * Checks if a given pawn is a passed pawn meaning that if the pawn
     *      has a non-interupted path to the other side of the board to
     *      promote
     * @param pawn the piece to check if it is a passed pawn
     * @param board the board to check the piece on
     * @return boolean, true if the pawn is a passed pawn, false otherwise
     */
    private boolean isPassedPawn(Pawn pawn, Board board)
    {
        Location loc = pawn.getLocation();
        int pawnRow = loc.getRow();
        int pawnCol = loc.getCol();
        Color pawnColor = pawn.getColor();
        int startingRow = pawnColor == Color.WHITE ? 1 : board.getNumRows() - 2;
        if (pawnRow <= startingRow || pawnRow >= board.getNumRows() - 1 - startingRow)
        {
            return false;
        }
        int rowStep;
        if (pawnColor.equals(Color.WHITE))
        {
            rowStep = 1;
        }
        else
        {
            rowStep = -1;
        }
        for (int r = pawnRow + rowStep; r >= 0 && r < board.getNumRows(); r += rowStep)
        {
            for (int c = Math.max(0, pawnCol - 1); c <= Math.min(board.getNumCols() - 1, pawnCol + 1); c++)
            {
                Piece piece = board.get(new Location(r, c));
                if (piece instanceof Pawn && piece.getColor() != pawnColor)
                {
                    return false;
                }
            }
        }
        for (int r = pawnRow + rowStep; r >= 0 && r < board.getNumRows(); r += rowStep)
        {
            Piece piece = board.get(new Location(r, pawnCol));
            if (piece != null) {
                if (piece.getColor() == pawnColor && piece instanceof Pawn)
                {
                    return false;
                } else
                {
                    break;
                }
            }
        }
        return true;
    }

    /**
     * Checks if a given pawn is an isolated pawn meaning that there are no pawns in the
     *      adjacent files.
     * @param pawn the pawn to check if it is a isolated pawn
     * @param board the board to check the pawn on
     * @return boolean, true if the pawn is isolated, false otherwise
     */
    private boolean isIsolatedPawn(Pawn pawn, Board board)
    {
        Location loc = pawn.getLocation();
        int pawnCol = loc.getCol();
        Color pawnColor = pawn.getColor();
        int[] adjacentCols = {pawnCol - 1, pawnCol + 1};
        for (int c : adjacentCols)
        {
            if (c < 0 || c >= board.getNumCols())
            {
                continue;
            }
            Piece piece = board.get(new Location(loc.getRow(), c));
            if (piece instanceof Pawn && piece.getColor().equals(pawnColor))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a pawn is a doubled pawn meaning that there are 2 pawns vertically next
     *      to each other (which is a bad position)
     * @param pawn the pawn to check if it is a doubled pawn
     * @param board the board to check the pawn on
     * @return boolean, true if the pawn is doubled, false otherwise
     */
    private boolean isDoubledPawn(Pawn pawn, Board board) {
        Location loc = pawn.getLocation();
        int pawnCol = loc.getCol();
        Color pawnColor = pawn.getColor();
        for (int r = 0; r < board.getNumRows(); r++) {
            if (r == loc.getRow()) {
                continue;
            }
            Piece piece = board.get(new Location(r, pawnCol));
            if (piece instanceof Pawn && piece.getColor().equals(pawnColor)) {
                return true;
            }
        }
    
        return false;
    }

    /**
     * Checks if the given pawn forms a pawn chain which would put the pawn structure
     *      in a very strong position.
     * @param pawn the pawn to check if it forms a pawn chain
     * @param board the board to check the pawn on
     * @return boolean, true if the pawn forms a pawn chain, false otherwise
     */
    private boolean isPawnChain(Pawn pawn, Board board)
    {
        Location loc = pawn.getLocation();
        int pawnCol = loc.getCol();
        Color pawnColor = pawn.getColor();
        int pawnRow = loc.getRow();
        int[] adjacentCols = {pawnCol - 1, pawnCol + 1};
        for (int c : adjacentCols) {
            if (c < 0 || c >= board.getNumCols())
            {
                continue;
            }
            for (int r = pawnRow - 1; r <= pawnRow + 1; r++)
            {
                if (r < 0 || r >= board.getNumRows() || (r == pawnRow && c == pawnCol))
                {
                    continue;
                }
                Piece piece = board.get(new Location(r, c));
                if (piece instanceof Pawn && piece.getColor().equals(pawnColor))
                {
                    return true;
                }
            }
        }
    
        return false;
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
            return new Object[]{score(), null};
        }
        depth--;
        Color color = getColor();
        if (color.equals(Color.BLACK))
        {
            color = Color.WHITE;
        }
        ArrayList<Move> moves = getBoard().allMoves(color);
        int min = Integer.MAX_VALUE;
        Move best = null;
        for (Move move : moves)
        {
            getBoard().executeMove(move);
            Object[] p = valueOfBestMove(depth, alpha, beta);
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
        return new Object[]{min, best};
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
            return new Object[]{score(), null};
        }
        depth--;
        ArrayList<Move> moves = getBoard().allMoves(getColor());
        moves.sort((m1, m2) ->
        {
            Piece p1 = getBoard().get(m1.getDestination());
            Piece p2 = getBoard().get(m2.getDestination());
            int val1, val2;
            if (p1 == null)
            {
                val1 = 0;
            }
            else {
                val1 = p1.getValue();
            }
            if (p2 == null)
            {
                val2 = 0;
            }
            else {
                val2 = p2.getValue();
            }
            return Integer.compare(val2, val1);
        });
        int max = Integer.MIN_VALUE;
        Move best = null;
        for (Move move : moves)
        {
            getBoard().executeMove(move);
            Object[] p = valueOfMeanestResponse(depth, alpha, beta);
            if ((int) (p[0]) > max) 
            {
                max = (int ) (p[0]);
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
        return new Object[]{max, best};
    }
}
