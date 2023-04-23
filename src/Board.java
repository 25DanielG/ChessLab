package src;
import java.awt.*;
import java.util.*;

/**
 * A class that represesents a rectangular game board, containing Piece objects.
 * @author Daniel Gergov
 * @version 3/31/23
 */
public class Board extends BoundedGrid<Piece>
{
	// Constructs a new Board with the given dimensions
	public Board()
	{
		super(8, 8);
	}

	/**
	 * @precondition:  move has already been made on the board
	 * @postcondition: piece has moved back to its source,
	 * 		and any captured piece is returned to its location
	 * @param move the move to undo
	 */
	public void undoMove(Move move)
	{
		Piece piece = move.getPiece();
		Location source = move.getSource();
		Location dest = move.getDestination();
		Piece victim = move.getVictim();
		if (piece instanceof King && dest.getCol() - source.getCol() == 2)
		{
			Location rookSource = new Location(dest.getRow(), dest.getCol() + 1);
			Location rookDest = new Location(source.getRow(), source.getCol() + 1);
			Piece p = this.get(rookDest);
			if (p instanceof Rook)
			{
				p.moveTo(rookSource);
			}
		}
		else if (piece instanceof King && dest.getCol() - source.getCol() == -2)
		{
			Location rookSource = new Location(dest.getRow(), dest.getCol() - 2);
			Location rookDest = new Location(source.getRow(), source.getCol() - 1);
			Piece p = this.get(rookDest);
			if (p instanceof Rook)
			{
				p.moveTo(rookSource);
			}
		}
		piece.moveTo(source);
		if (victim != null)
		{
			victim.putSelfInGrid(piece.getBoard(), dest);
		}
	}

	/**
	 * Returns all possible moves in a chess board by looping through all pieces and their possible moves.
	 * @param color the color of the pieces to look for all possible moves
	 * @return type ArrayList<Move> the list of all possible moves
	 */
	public ArrayList<Move> allMoves(Color color)
    {
        ArrayList<Location> occupied = getOccupiedLocations();
        ArrayList<Move> moves = new ArrayList<Move>();
        for (Location l : occupied)
        {
            Piece cur = get(l);
            if (cur.getColor().equals(color))
            {
                for (Location to : cur.destinations())
				{
                    moves.add(new Move(cur, to));
				}
            }
        }
        return moves;
    }

	/**
	 * Returns all strategic possible moves in a chess board by looping through all pieces and
	 * 		their possible moves while scoring the move based on calculateMoveScore method.
	 * @param color the color of the pieces to look for all possible moves
	 * @return type ArrayList<Move> the list of all strategic moves
	 */
	public ArrayList<Move> strategicMoves(Color color)
	{
		ArrayList<Location> occupied = getOccupiedLocations();
		ArrayList<Move> moves = new ArrayList<>();
		for (Location l : occupied)
		{
			Piece cur = get(l);
			if (cur.getColor().equals(color))
			{
				for (Location to : cur.destinations())
				{
					int score = calculateMoveScore(new Move(cur, to));
					if (score > 0)
					{
						moves.add(new Move(cur, to, score));
					}
				}
			}
		}
		moves.sort((m1, m2) -> Integer.compare(m2.getScore(), m1.getScore()));
		return moves;
	}

	/**
	 * Calculates the score of a move based on value of piece being moved, captured piece (if any)
	 * 		, center squares, and edge squares.
	 * @param move the Move to score
	 * @return an int representing score
	 */
	private int calculateMoveScore(Move move)
	{
		Piece piece = move.getPiece();
		Location destination = move.getDestination();
		int score = 0;
		// value of piece moved
		score += piece.getValue();
		// value of captured piece
		Piece victim = move.getVictim();
		if (victim != null)
		{
			score += victim.getValue();
		}
		// value of destination square
		int row = destination.getRow();
		int col = destination.getCol();
		int center = this.getNumCols() / 2;
		if (row == center && col == center)
		{
			// center square
			score += 2;
		}
		else if (row == center || col == center)
		{
			// - edge square
			score += 1;
		}
		return score;
	}	

	/**
	 * Executes a move by checking if it is valid or not and then moving the piece as well as
	 * 		checking if the given move is a castle or not.
	 * @param move the move to execute
	 */
	public void executeMove(Move move)
	{
		if (move.getPiece().isValidDestination(move.getDestination()))
        {
            if (move.getVictim() != null)
			{
                move.getVictim().removeSelfFromGrid();
			}
			if (move.getPiece() instanceof King)
			{
				if (move.getPiece().getLocation().getCol() - move.getDestination().getCol() == -2)
				{
					Location loc = move.getPiece().getLocation();
					Location nLoc = new Location(loc.getRow(), loc.getCol() + 3);
					Location mLoc = new Location(loc.getRow(), loc.getCol() + 1);
					if (isValid(nLoc) && isValid(mLoc))
					{
						Piece p = this.get(nLoc);
						if (p instanceof Rook && !((Rook) p).getMoved())
						{
							p.moveTo(mLoc);
						}
					}
				}
				else if (move.getPiece().getLocation().getCol() - move.getDestination().getCol() == 2)
				{
					Location loc = move.getPiece().getLocation();
					Location nLoc = new Location(loc.getRow(), loc.getCol() - 4);
					Location mLoc = new Location(loc.getRow(), loc.getCol() - 1);
					if (isValid(nLoc) && isValid(mLoc))
					{
						Piece p = this.get(nLoc);
						if (p instanceof Rook && !((Rook) p).getMoved())
						{
							p.moveTo(mLoc);
						}
					}
				}
			}
            move.getPiece().moveTo(move.getDestination());
        }
	}
}