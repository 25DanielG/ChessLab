package src;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A class that describes the Knight piece of a chess set. It overrides a method
 *      destinations that returns all the places that it can move to.
 * @author Daniel Gergov
 * @version 3/31/23
 */
public class Knight extends Piece
{
    private static final int[][] offset = 
    {
        {-2, 1}, {-1, -2}, {2, 1}, {1, -2}, {2, -1}, {1, 2}, {-2, -1}, {-1, 2}
    };

    /**
     * Default constructor for the Knight class
     * @param col the color either black or white, describing which side the knight belongs to
     * @param fileName the fileName to display the picture of the piece with
     */
    public Knight(Color col, String fileName)
    {
        super(col, fileName, 3);
    }

    public String toString()
    {
        return "N";
    }

    /**
     * An overrided method from the Piece abstract class which returns all the places in which a
     *      Knight can move on the current chess board.
     * @return type ArrayList<Location> the locations in which a knight can move in a chess board
     */
    @Override
    public ArrayList<Location> destinations()
    {
        ArrayList<Location> destinations = new ArrayList<Location>();
        for (int i = 0; i < offset.length; i++)
        {
            Location loc = new Location(getLocation().getRow() + offset[i][0], getLocation().getCol() + offset[i][1]);
            if (isValidDestination(loc))
            {
                destinations.add(loc);
            }
        }
        return destinations;
    }
}
