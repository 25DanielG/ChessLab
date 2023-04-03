package src;
import java.awt.Color;
import java.util.ArrayList;

/**
 * A HumanPlayer that plays chess by accepting inputed moves from the user.
 * @author Daniel Gergov
 * @version 3/31/23
 */
public class HumanPlayer extends Player
{
    private BoardDisplay display;

    /**
     * Default constructor for the HumanPlayer class.
     * @param board the board in which the player belongs to
     * @param display the board display to update
     * @param color the color of the player either white or black pertaining
     *      to which side the player plays for
     * @param name the name of the human player
     */
    public HumanPlayer(Board board, BoardDisplay display, Color color, String name)
    {
        super(board, name, color);
        this.display = display;
    }

    /**
     * Gets the nextMove of the human player by continually accepting input until it is
     *      valid
     * @return type Move, the move that the random player wants to play
     */
    public Move nextMove()
    {
        ArrayList<Move> moves = getBoard().allMoves(getColor());
        Move move = null;
        while (!moves.contains(move))
        {
            move = display.selectMove();
        }
        return move;
    }
}
