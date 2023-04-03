package src;
import java.awt.Color;
import java.util.ArrayList;

/**
 * A class that describes the Rook piece of a chess set. It overrides a method
 *      destinations that returns all the places that it can move to.
 * @author Daniel Gergov
 * @version 3/31/23
 */
public class Rook extends Piece
{
    private boolean moved;

    /**
     * Default constructor for the Rook class
     * @param col the color either black or white, describing which side the rook belongs to
     * @param fileName the fileName to display the picture of the piece with
     */
    public Rook(Color col, String fileName)
    {
        super(col, fileName, 5);
        moved = false;
    }

    /**
     * An overrided method from the Piece abstract class which returns all the places in which a
     *      Rook can move on the current chess board.
     * @return type ArrayList<Location> the locations in which a rook can move in a chess board
     */
    @Override
    public ArrayList<Location> destinations()
    {
        ArrayList<Location> destinations = new ArrayList<Location>();
        sweep(destinations, Location.NORTH);
        sweep(destinations, Location.SOUTH);
        sweep(destinations, Location.WEST);
        sweep(destinations, Location.EAST);
        return destinations;
    }

    /**
     * Gets the moved instance variable which dictates if the piece has moved
     * @return boolean the moved variable
     */
    public boolean getMoved()
    {
        return moved;
    }

    /**
     * Sets the moved instance variable to the given boolean
     */
    public void setMoved(boolean m)
    {
        moved = m;
    }
}
