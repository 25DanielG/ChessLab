package src;
import java.awt.Color;
import java.util.Arrays;

public class Score
{
    /**
     * The scoring function to score a boards position, the scoring function relies on a more
     *      efficient piece evaluation point system as well as the mobility of each piece.
     * @return int the score, the higher the better for the player
     */
    public static int score(Board board, Color color)
    {
        int score = 0;
        int pawnValue = 100, knightValue = 320, bishopValue = 330, rookValue = 500;
        int queenValue = 900, kingValue = 20000;
        int centerBonus = 20, centerSquare = board.getNumRows() / 2;
        int[] centerRows = { centerSquare - 1, centerSquare, centerSquare + 1 };
        Color opposite;
        if (color.equals(Color.WHITE))
        {
            opposite = Color.BLACK;
        }
        else
        {
            opposite = Color.WHITE;
        }
        int botPawnStructureScore = evaluatePawnStructure(board, color);
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
                    }
                    else if (piece instanceof Knight)
                    {
                        value = knightValue;
                    }
                    else if (piece instanceof Bishop)
                    {
                        value = bishopValue;
                    }
                    else if (piece instanceof Rook)
                    {
                        value = rookValue;
                    }
                    else if (piece instanceof Queen)
                    {
                        value = queenValue;
                    }
                    else if (piece instanceof King)
                    {
                        value = kingValue;
                    }
                    int mobility = piece.destinations().size();
                    if (piece instanceof Knight || piece instanceof Bishop)
                    {
                        value += mobility * 10;
                    }
                    else if (piece instanceof Rook || piece instanceof Queen)
                    {
                        value += mobility * 5;
                    }
                    if (Arrays.binarySearch(centerRows, row) >= 0 
                        && Arrays.binarySearch(centerRows, col) >= 0)
                    {
                        value += centerBonus;
                    }
                    if (piece.getColor().equals(color))
                    {
                        score += value;
                    }
                    else
                    {
                        score -= value;
                    }
                }
            }
        }
        score += ((botPawnStructureScore - oppPawnStructureScore) / 5);
        return score;
    }

    /**
     * Evaluats the pawn structure of a specific color and returns a score based on that pawn
     *      structure.
     * @param board the board to evaluate the pawn structure of
     * @param color the color pieces to evaluate the pawn structure of
     * @return int the score of the pawn structure
     */    
    private static int evaluatePawnStructure(Board board, Color color)
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
                        pawnStructureScore += 50;
                    }
                    else if (isIsolatedPawn((Pawn) piece, board))
                    {
                        pawnStructureScore -= 10;
                    }
                    else if (isDoubledPawn((Pawn) piece, board))
                    {
                        pawnStructureScore -= 10;
                    }
                    else if (isPawnChain((Pawn) piece, board))
                    {
                        pawnStructureScore += 15;
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
    private static boolean isPassedPawn(Pawn pawn, Board board)
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
                }
                else
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
    private static boolean isIsolatedPawn(Pawn pawn, Board board)
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
    private static boolean isDoubledPawn(Pawn pawn, Board board)
    {
        Location loc = pawn.getLocation();
        int pawnCol = loc.getCol();
        Color pawnColor = pawn.getColor();
        for (int r = 0; r < board.getNumRows(); r++)
        {
            if (r == loc.getRow())
            {
                continue;
            }
            Piece piece = board.get(new Location(r, pawnCol));
            if (piece instanceof Pawn && piece.getColor().equals(pawnColor))
            {
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
    private static boolean isPawnChain(Pawn pawn, Board board)
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
}
