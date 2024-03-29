package src.piece;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import src.Location;

/**
 * A class that describes the Bishop piece of a chess set. It overrides a method
 *      destinations that returns all the places that it can move to.
 * @author Daniel Gergov
 * @version 3/31/23
 */
public class Bishop extends Piece
{
    /**
     * Default constructor for the Bishop class
     * @param col the color either black or white, describing which side the bishop belongs to
     * @param fileName the fileName to display the picture of the piece with
     */
    public Bishop(Color col, String filename)
    {
        super(col, filename, 3);
    }

    public String toString()
    {
        return "B";
    }

    /**
     * An overrided method from the Piece abstract class which returns all the places in which a
     *      Bishop can move on the current chess board.
     * @return type ArrayList<Location> the locations in which a bishop can move in a chess board
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
        sweep(destinations, Location.NORTHEAST);
        sweep(destinations, Location.NORTHWEST);
        sweep(destinations, Location.SOUTHEAST);
        sweep(destinations, Location.SOUTHWEST);
        return destinations;
    }
}
