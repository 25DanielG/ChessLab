package src.piece;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import src.Location;

/**
 * A class that describes the Queen piece of a chess set. It overrides a method
 *      destinations that returns all the places that it can move to.
 * @author Daniel Gergov
 * @version 3/31/23
 */
public class Queen extends Piece
{
    /**
     * Default constructor for the Queen class
     * @param col the color either black or white, describing which side the rook belongs to
     * @param fileName the fileName to display the picture of the piece with
     */
    public Queen(Color col, String filename)
    {
        super(col, filename, 9);
    }

    public String toString()
    {
        return "Q";
    }

    /**
     * An overrided method from the Piece abstract class which returns all the places in which a
     *      Queen can move on the current chess board.
     * @return type ArrayList<Location> the locations in which a queen can move in a chess board
     */
    @Override
    public ArrayList<Location> destinations()
    {
        ArrayList<Location> destinations = illegalDestinations();
        removeIllegalMoves(destinations);
        return destinations;
    }

    public ArrayList<Location> illegalDestinations()
    {
        ArrayList<Location> destinations = new ArrayList<Location>();
        sweep(destinations, Location.NORTH);
        sweep(destinations, Location.SOUTH);
        sweep(destinations, Location.EAST);
        sweep(destinations, Location.WEST);
        sweep(destinations, Location.NORTHEAST);
        sweep(destinations, Location.NORTHWEST);
        sweep(destinations, Location.SOUTHEAST);
        sweep(destinations, Location.SOUTHWEST);
        return destinations;
    }
}
