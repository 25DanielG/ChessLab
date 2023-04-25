package src;
import java.awt.*;
import java.util.*;

/**
 * A class that describes the King piece of a chess set. It overrides a method
 *      destinations that returns all the places that it can move to.
 * @author Daniel Gergov
 * @version 3/31/23
 */
public class King extends Piece
{
    private static final int[][] offset = 
    {
        {1, 0}, {-1, 0}, {1, -1}, {-1, -1}, {0, -1}, {1, 1}, {-1, 1}, {0, 1}
    };
    private boolean moved;

    /**
     * Default constructor for the King class
     * @param col the color either black or white, describing which side the king belongs to
     * @param fileName the fileName to display the picture of the piece with
     */
    public King(Color col, String fileName)
	{
		super(col, fileName, 1000);
        moved = false;
	}

    public String toString()
    {
        return "K";
    }

    /**
     * An overrided method from the Piece abstract class which returns all the places in which a
     *      King can move on the current chess board.
     * @return type ArrayList<Location> the locations in which a king can move in a chess board
     */
    @Override
    public ArrayList<Location> destinations()
    {
        return illegalDestinations();
    }

    public ArrayList<Location> illegalDestinations()
    {
        ArrayList<Location> destinations = new ArrayList<Location>();
        for (int i = 0; i < offset.length; i++)
        {
            Location dest = new Location(super.getLocation().getRow() + offset[i][0],
                    super.getLocation().getCol() + offset[i][1]);
            if (isValidDestination(dest))
            {
                destinations.add(dest);
            }
        }
        if (!moved)
        {
            Location lOne = new Location(super.getLocation().getRow(), super.getLocation().getCol() + 2);
            if (isValidCastle(super.getLocation(), lOne))
            {
                destinations.add(lOne);
            }
            Location lTwo = new Location(super.getLocation().getRow(), super.getLocation().getCol() - 2);
            if (isValidCastle(super.getLocation(), lTwo))
            {
                destinations.add(lTwo);
            }
        }
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
     * Sets the moved instance variable of the king to a given value
     * @param move the boolean to set the instance variable with
     */
    public void setMoved(boolean move)
    {
        moved = move;
    }
}
