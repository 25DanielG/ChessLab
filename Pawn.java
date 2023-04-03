import java.awt.Color;
import java.util.ArrayList;

/**
 * A class that describes the Pawn piece of a chess set. It overrides a method
 *      destinations that returns all the places that it can move to.
 * @author Daniel Gergov
 * @version 3/31/23
 */
public class Pawn extends Piece 
{
    /**
     * Default constructor for the Pawn class
     * @param col the color either black or white, describing which side the pawn belongs to
     * @param fileName the fileName to display the picture of the piece with
     */
    public Pawn(Color col, String fileName)
    {
        super(col, fileName, 1);
    }

    /**
     * An overrided method from the Piece abstract class which returns all the places in which a
     *      Pawn can move on the current chess board.
     * @return type ArrayList<Location> the locations in which a pawn can move in a chess board
     */
    @Override
    public ArrayList<Location> destinations()
    {
        ArrayList<Location> destinations = new ArrayList<Location>();
        Location up, twoUp, upLeft, upRight;
        if (getColor().equals(Color.BLACK))
        {
            up = getLocation().getAdjacentLocation(Location.SOUTH);
            twoUp = up.getAdjacentLocation(Location.SOUTH);
            upLeft = getLocation().getAdjacentLocation(Location.SOUTHWEST);
            upRight = getLocation().getAdjacentLocation(Location.SOUTHEAST);
        }
        else
        {
            up = getLocation().getAdjacentLocation(Location.NORTH);
            twoUp = up.getAdjacentLocation(Location.NORTH);
            upLeft = getLocation().getAdjacentLocation(Location.NORTHWEST);
            upRight = getLocation().getAdjacentLocation(Location.NORTHEAST);
        }
        if (isValidDestination(up) && getBoard().get(up) == null)
        {
            destinations.add(up);
        }
        if (((getColor().equals(Color.BLACK) && getLocation().getRow() == 1) 
            || (getColor().equals(Color.WHITE) && getLocation().getRow() == 6))
            && isValidDestination(twoUp) && getBoard().get(twoUp) == null)
        {
            destinations.add(twoUp);
        }
        if (isValidDestination(upLeft) && getBoard().get(upLeft) != null)
        {
            destinations.add(upLeft);
        }
        if (isValidDestination(upRight) && getBoard().get(upRight) != null)
        {
            destinations.add(upRight);
        }
        return destinations;
    }
}
