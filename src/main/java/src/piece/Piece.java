package src.piece;
import java.awt.*;
import java.util.*;
import src.Game;
import src.Location;
import src.Move;
import src.board.Board;

public abstract class Piece implements Cloneable
{
	//the board this piece is on
	public Board board;

	//the location of this piece on the board
	private Location location;

	//the color of the piece
	private Color color;

	//the file used to display this piece
	private String imageFileName;

	//the approximate value of this piece in a game of chess
	private int value;

    /**
     * Default constructor for the Piece class
     * @param col the color of the piece pertaining to which side the piece belongs to
     * @param fileName the fileName for which the disply must display the piece as
     * @param val the point value of the piece
     */
	public Piece(Color col, String fileName, int val)
	{
		color = col;
		imageFileName = fileName;
		value = val;
	}

    /**
     * Gets the board that the peice is on
     * @return type Board the board that the piece is on
     */
	public Board getBoard()
	{
		return board;
	}

    /**
     * Gets the location on the board that the piece is on
     * @return type Location the location on the board that the piece is on
     */
	public Location getLocation()
	{
		return location;
	}

    /**
     * Gets the color of the piece pertaining to which side of the board the piece is on
     * @return type Color the color of the piece
     */
	public Color getColor()
	{
		return color;
	}

    /**
     * Returns the image file name of the piece so that the display can display the piece
     * @return type String the fileName of the piece
     */
	public String getImageFileName()
	{
		return imageFileName;
	}

    /**
     * Gets the point value of the piece
     * @return type int the point value of the piece
     */
	public int getValue()
	{
		return value;
	}

    /**
     * Puts this piece into a board. If there is another piece at the given
     * location, it is removed. <br />
     * Precondition: (1) This piece is not contained in a grid (2)
     * <code>loc</code> is valid in <code>gr</code>
     * @param brd the board into which this piece should be placed
     * @param loc the location into which the piece should be placed
     */
    public void putSelfInGrid(Board brd, Location loc)
    {
        if (board != null)
        {
            throw new IllegalStateException(
                    "This piece is already contained in a board.");
        }
        Piece piece = brd.get(loc);
        if (piece != null)
        {
            piece.removeSelfFromGrid();
        }
        brd.put(loc, this);
        board = brd;
        location = loc;
    }

    /**
     * Removes this piece from its board. <br />
     * Precondition: This piece is contained in a board
     */
    public void removeSelfFromGrid()
    {
        if (board == null)
        {
            throw new IllegalStateException(
                    "This piece is not contained in a board.");
        }
        if (board.get(location) != this)
        {
            throw new IllegalStateException(
                    "The board contains a different piece at location "
                            + location + ".");
        }
        board.remove(location);
        board = null;
        location = null;
    }

    /**
     * Moves this piece to a new location. If there is another piece at the
     * given location, it is removed. <br />
     * Precondition: (1) This piece is contained in a grid (2)
     * <code>newLocation</code> is valid in the grid of this piece
     * @param newLocation the new location
     */
    public void moveTo(Location newLocation)
    {
        if (board == null)
        {
            throw new IllegalStateException("This piece is not on a board.");
        }
        if (board.get(location) != this)
        {
            throw new IllegalStateException(
                    "The board contains a different piece at location "
                            + location + ".");
        }
        if (!board.isValid(newLocation))
        {
            throw new IllegalArgumentException("Location " + newLocation
                    + " is not valid.");
        }
        if (newLocation.equals(location))
        {
            return;
        }
        board.remove(location);
        Piece other = board.get(newLocation);
        if (other != null)
        {
            other.removeSelfFromGrid();
        }
        location = newLocation;
        board.put(location, this);
    }

    /**
     * Checks if a location is a valid location on a board
     * @param dest the Location to check its validity
     * @return tpye Boolean, true if the location is valid, false otherwise
     */
    public boolean isValidDestination(Location dest)
    {
        return board.isValid(dest) &&
            (board.get(dest) == null || !board.get(dest).getColor().equals(color));
    }

    /**
     * Checks if a location is a valid castle location on a board
     * @param source the Location of the source king
     * @param dest the Location to check its validity
     * @return tpye Boolean, true if the location is valid, false otherwise
     */
    public boolean isValidCastle(Location source, Location dest)
    {
        if (!board.isValid(source) || !board.isValid(dest))
        {
            return false;
        }
        int side = source.getCol() - dest.getCol();
        if (side == 2)
        {
            for (int i = 1; i <= 3; i++)
            {
                Location test = new Location(source.getRow(), source.getCol() - i);
                if (!board.isValid(test) || board.get(test) != null)
                {
                    return false;
                }
            }
            Location test = new Location(source.getRow(), source.getCol() - 4);
            if (!board.isValid(test) || !(board.get(test) instanceof Rook))
            {
                return false;
            }
        }
        else if (side == -2)
        {
            for (int i = 1; i <= 2; i++)
            {
                Location test = new Location(source.getRow(), source.getCol() + i);
                if (!board.isValid(test) || (board.get(test) != null))
                {
                    return false;
                }
            }
            Location test = new Location(source.getRow(), source.getCol() + 3);
            if (!board.isValid(test) || !(board.get(test) instanceof Rook))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * The destinations that a piece can move to for which every piece class must implement
     * @return type ArrayList<Locaion> representing each Location that the piece can move to
     */
    public abstract ArrayList<Location> destinations();

    public abstract ArrayList<Location> illegalDestinations();

    /**
     * Sweeps the locations in a given direction and modifies a given ArrayList by adding
     *      all valid locations in the direction given
     * @param dests the ArrayList to modify and add all the locations to
     * @param direction the direction in which to check all valid locations
     */
    public void sweep(ArrayList<Location> dests, int direction)
    {
        Location loc = location.getAdjacentLocation(direction);
        while (loc != null && isValidDestination(loc))
        {
            dests.add(loc);
            if (board.get(loc) == null || board.get(loc).getColor() == this.color)
            {
                loc = loc.getAdjacentLocation(direction);
            }
            else
            {
                loc = null;
            }
        }
    }

    @Override
    public Piece clone()
    {
        try
        {
            Piece newPiece = (Piece) super.clone();
            newPiece.color = color;
            newPiece.location = null;
            newPiece.board = null;
            newPiece.imageFileName = imageFileName;
            newPiece.value = value;
            return newPiece;
        }
        catch (CloneNotSupportedException e)
        {
            throw new InternalError(e);
        }
    }

    public boolean isKingInCheck(Move move, Color color)
    {
        Board board = getBoard();
        board.executeMove(move);
        King king = color.equals(Color.WHITE) ? Game.wKing : Game.bKing;
        Location kingLocation = king.getLocation();
        Vector<Move> oppMoves = board.allIllegalMoves(color.equals(Color.WHITE) ? Color.BLACK : Color.WHITE);
        for(Move m : oppMoves)
        {
            if(m.getDestination().equals(kingLocation))
            {
                board.undoMove(move);
                return true;
            }
        }
        board.undoMove(move);
        return false;
    }

    public void removeIllegalMoves(ArrayList<Location> moves)
    {
        for(int i = 0; i < moves.size(); i++)
        {
            Location loc = moves.get(i);
            Move m = new Move(this, loc);
            if(isKingInCheck(m, getColor()))
            {
                moves.remove(i);
                i--;
            }
        }
    }

    public String toFEN()
    {
        if (this instanceof Pawn)
        {
            return this.getColor().equals(Color.WHITE) ? "P" : "p";
        }
        else if (this instanceof Knight)
        {
            return this.getColor().equals(Color.WHITE) ? "N" : "n";
        }
        else if (this instanceof Bishop)
        {
            return this.getColor().equals(Color.WHITE) ? "B" : "b";
        }
        else if (this instanceof Rook)
        {
            return this.getColor().equals(Color.WHITE) ? "R" : "r";
        }
        else if (this instanceof Queen)
        {
            return this.getColor().equals(Color.WHITE) ? "Q" : "q";
        }
        else if (this instanceof King)
        {
            return this.getColor().equals(Color.WHITE) ? "K" : "k";
        }
        else
        {
            throw new IllegalStateException("Invalid piece type");
        }
    }

}