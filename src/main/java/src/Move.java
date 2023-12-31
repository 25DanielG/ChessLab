package src;

import src.piece.*;

// Represents a single move, in which a piece moves to a destination location.
// Since a move can be undone, also keeps track of the source location and any captured victim.
public class Move
{
	private Piece piece;          // the piece being moved
	private Location source;      // the location being moved from
	private Location destination; // the location being moved to
	private Piece victim;         // any captured piece at the destination
	private int score;			  // score instance variable used to compare strategic moves

	// Constructs a new move for moving the given piece to the given destination.
	public Move(Piece piece, Location destination)
	{
		this.piece = piece;
		this.source = piece.getLocation();
		this.destination = destination;
		this.victim = piece.getBoard().get(destination);

		if (source.equals(destination))
			throw new IllegalArgumentException("Both source and dest are " + source);
	}

	public Move(Piece piece, Location source, Location destination)
	{
		this.piece = piece;
		this.source = source;
		this.destination = destination;

		if (source.equals(destination))
			throw new IllegalArgumentException("Both source and dest are " + source);
	}

	// Constructs a new move for moving the given piece to the given destination.
	public Move(Piece piece, Location destination, int score)
	{
		this.piece = piece;
		this.source = piece.getLocation();
		this.destination = destination;
		this.victim = piece.getBoard().get(destination);
		this.score = score;

		if (source.equals(destination))
			throw new IllegalArgumentException("Both source and dest are " + source);
	}


	// Returns the piece being moved
	public Piece getPiece()
	{
		return piece;
	}

	// Returns the location being moved from
	public Location getSource()
	{
		return source;
	}

	// Returns the location being moved to
	public Location getDestination()
	{
		return destination;
	}

	// Returns the piece being captured at the destination, if any
	public Piece getVictim()
	{
		return victim;
	}

	// Returns the score of the move
	public int getScore()
	{
		return score;
	}

	// Sets the score of the move
	public void setScore(int score)
	{
		this.score = score;
	}

	// Returns a string description of the move
	public String toString()
	{
		String capture = "";
		if (victim != null)
			capture = "x";
		String notation = "" + piece + capture + ((char) (97 + destination.getCol())) + (8 - destination.getRow());
		if (capture != "" && piece instanceof Pawn)
			notation = "" + ((char) (97 + source.getCol())) + "x" + ((char) (97 + destination.getCol())) + (8 - destination.getRow());
		return notation;
	}

	// Returns true if this move is equivalent to the given one.
	public boolean equals(Object x)
	{
		Move other = (Move)x;
		return piece == other.getPiece() && source.equals(other.getSource()) &&
			destination.equals(other.getDestination()) && victim == other.getVictim();
	}

	// Returns a hash code for this move, such that equivalent moves have the same hash code.
	public int hashCode()
	{
		return piece.hashCode() + source.hashCode() + destination.hashCode();
	}
}