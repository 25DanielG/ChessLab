package src;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Vector;

/**
 * A RandomPlayer that plays chess by playing random moves.
 * @author Daniel Gergov
 * @version 3/31/23
 */
public class RandomPlayer extends Player
{
    /**
     * Default constructor for the RandomPlayer class.
     * @param board the board in which the player belongs to
     * @param color the color of the player either white or black pertaining
     *      to which side the player plays for
     * @param name the name of the player
     */
    public RandomPlayer(Board board, Color color, String name)
    {
        super(board, name, color);
    }

    /**
     * Gets the nextMove of the random player by randomly choosing a move out of
     *      all possible moves.
     * @return type Move, the move that the random player wants to play
     */
    public Move nextMove()
    {
        Vector<Move> moves = getBoard().allMoves(getColor());
        return moves.get((int) (Math.random() * moves.size()));
    }
}
