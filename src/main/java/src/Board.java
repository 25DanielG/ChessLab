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

	public Board(Board b)
	{
		super(b.getNumRows(), b.getNumCols());
		for (int r = 0; r < b.getNumRows(); r++)
		{
			for (int c = 0; c < b.getNumCols(); c++)
			{
				Piece p = b.get(new Location(r, c));
				if (p != null)
				{
					Piece newPiece = p.clone();
					newPiece.putSelfInGrid(this, new Location(r, c));
				}
			}
		}
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
	public Vector<Move> allMoves(Color color)
	{
        ArrayList<Location> occupied = getOccupiedLocations();
        Vector<Move> moves = new Vector<Move>();
        for (Location l : occupied)
        {
            Piece cur = get(l);
            if (cur.getColor().equals(color))
            {
                for (Location to : cur.destinations())
				{
					Move m = new Move(cur, to);
					this.executeMove(m);
					int score = Score.quickScore(this, color);
					this.undoMove(m);
                    moves.add(new Move(cur, to, score));
				}
            }
        }
        return moves;
    }

	/**
	 * Returns all possible moves in a chess board by looping through all pieces and their possible moves.
	 * @param color the color of the pieces to look for all possible moves
	 * @return type ArrayList<Move> the list of all possible moves
	 */
	public Vector<Move> allIllegalMoves(Color color)
	{
        ArrayList<Location> occupied = getOccupiedLocations();
        Vector<Move> moves = new Vector<Move>();
        for (Location l : occupied)
        {
            Piece cur = get(l);
            if (cur.getColor().equals(color))
            {
                for (Location to : cur.illegalDestinations())
				{
                    Move m = new Move(cur, to);
					this.executeMove(m);
					int score = Score.quickScore(this, color);
					this.undoMove(m);
                    moves.add(new Move(cur, to, score));
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
	public Vector<Move> strategicMoves(Color color)
	{
		Vector<Move> moves = this.allMoves(color);
		Vector<Move> strategicMoves = new Vector<Move>();
		int previousScore = Score.score(this, color);
		moves.parallelStream().forEach(m ->
		{
			Board boardCopy = new Board(this);
			Move newMove = new Move(boardCopy.get(m.getSource()), m.getDestination(), Score.quickScore(this, color));
			boardCopy.executeMove(newMove);
			int score = Score.score(boardCopy, color);
			if (score - previousScore > 10)
			{
				m.setScore(score - previousScore);
				synchronized (strategicMoves)
				{
					strategicMoves.add(m);
				}
			}
		});
		strategicMoves.sort((m1, m2) -> Integer.compare(m2.getScore(), m1.getScore()));
		return strategicMoves;
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

	public String toFEN(Color activeColor)
	{
		String FEN = "";
		for (int i = 1; i <= 8; i++)
		{
			int empty = 0;
			for (int j = 1; j <= 8; j++)
			{
				if (this.get(new Location(i - 1, j - 1)) != null)
				{
					if (empty != 0)
					{
						FEN += empty;
						empty = 0;
					}
					Piece current = this.get(new Location(i - 1, j - 1));
					char name = 'z';
					if (current instanceof Pawn)
					{
						name = 'p';
					}
					else if (current instanceof Bishop)
					{
						name = 'b';
					}
					else if (current instanceof Knight)
					{
						name = 'n';
					}
					else if (current instanceof Rook)
					{
						name = 'r';
					}
					else if (current instanceof Queen)
					{
						name = 'q';
					}
					else if (current instanceof King)
					{
						name = 'k';
					}
					if (current.getColor().equals(Color.WHITE))
					{
						name = Character.toUpperCase(name);
						FEN += name;
					}
					else if (name != 'z')
					{
						FEN += name;
					}
				}
				else
				{
					empty++;
				}
			}
			if(empty != 0)
			{
				FEN += empty;
			}
			if(i != 8)
			{
				FEN += "/";
			}
		}
		FEN += " " + (activeColor.equals(Color.WHITE) ? "w" : "b");
		FEN += " " + this.getCastlingRights();
		FEN += " " + (this.getEnPassantTarget() == null ? "-" : this.getEnPassantTarget());
		FEN += " " + this.getHalfmoveClock();
		FEN += " " + this.getFullmoveNumber();
		return FEN;
	}

	private Piece getEnPassantTarget()
	{
		return null;
	}

	private int getHalfmoveClock()
	{
		return 0;
	}

	private int getFullmoveNumber()
	{
		return 1;
	}

	private String getCastlingRights()
	{
		King whiteKing = Game.wKing, blackKing = Game.bKing;
		Rook wRookKing = Game.wRookKing, wRookQueen = Game.wRookQueen, bRookKing = Game.bRookKing, bRookQueen = Game.bRookQueen;
		String rights = "";
		if (!whiteKing.getMoved())
		{
			if (!wRookKing.getMoved() && whiteKing.isValidCastle(wRookKing.getLocation(), wRookKing.getLocation()))
			{
				rights += "K";
			}
			if (!wRookQueen.getMoved() && whiteKing.isValidCastle(wRookQueen.getLocation(), wRookQueen.getLocation()))
			{
				rights += "Q";
			}
		}
		if (!blackKing.getMoved())
		{
			if (!bRookKing.getMoved() && blackKing.isValidCastle(bRookKing.getLocation(), bRookKing.getLocation()))
			{
				rights += "k";
			}
			if (!bRookQueen.getMoved() && blackKing.isValidCastle(bRookQueen.getLocation(), bRookQueen.getLocation()))
			{
				rights += "q";
			}
		}
		return rights.equals("") ? "-" : rights;
	}
}