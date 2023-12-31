package src.board;

import java.awt.*;
import java.util.*;
import src.Game;
import src.Location;
import src.Move;
import src.piece.Bishop;
import src.piece.King;
import src.piece.Knight;
import src.piece.Pawn;
import src.piece.Piece;
import src.piece.Queen;
import src.piece.Rook;

/**
 * A class that represesents a rectangular game board, containing Piece objects.
 * @author Daniel Gergov
 * @version 3/31/23
 */
public class Board extends BoundedGrid<Piece>
{
	public Color active;
	public int fullMove;
	public ArrayList<String> sequence;

	// Constructs a new Board with the given dimensions
	public Board()
	{
		super(8, 8);
		active = null;
		this.sequence = new ArrayList<String>();
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
					this.undoMove(m);
                    moves.add(new Move(cur, to));
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
					this.undoMove(m);
                    moves.add(new Move(cur, to));
				}
            }
        }
        return moves;
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
			Location loc = new Location(whiteKing.getLocation().getRow(), whiteKing.getLocation().getCol() + 2);
			if (!wRookKing.getMoved() && whiteKing.isValidCastle(whiteKing.getLocation(), loc))
			{
				rights += "K";
			}
			loc = new Location(whiteKing.getLocation().getRow(), whiteKing.getLocation().getCol() - 2);
			if (!wRookQueen.getMoved() && whiteKing.isValidCastle(whiteKing.getLocation(), loc))
			{
				rights += "Q";
			}
		}
		if (!blackKing.getMoved())
		{
			Location loc = new Location(blackKing.getLocation().getRow(), blackKing.getLocation().getCol() + 2);
			if (!bRookKing.getMoved() && blackKing.isValidCastle(blackKing.getLocation(), loc))
			{
				rights += "k";
			}
			loc = new Location(blackKing.getLocation().getRow(), blackKing.getLocation().getCol() - 2);
			if (!bRookQueen.getMoved() && blackKing.isValidCastle(blackKing.getLocation(), loc))
			{
				rights += "q";
			}
		}
		return rights.equals("") ? "-" : rights;
	}

	public double[][][] boardToBitboards() {
		double[][][] bitboards = new double[19][8][8]; // 19 layers, 8x8
		String castlingRights = this.getCastlingRights();
		int activeColorLayer = this.active.equals(Color.WHITE) ? 0 : 1;
		int fullMoveNumber = this.fullMove;

		for (int r = 0; r < 8; r++) {
			for (int c = 0; c < 8; c++) {
				Location loc = new Location(r, c);
				Piece piece = this.get(loc);
	
				if (piece != null) {
					int layerIndex = getPieceLayerIndex(piece);
					if (layerIndex != -1) {
						bitboards[layerIndex][r][c] = 1;
					}
				}

				// castling
				if (castlingRights.contains("K")) bitboards[12][r][c] = 1;
				if (castlingRights.contains("Q")) bitboards[13][r][c] = 1;
				if (castlingRights.contains("k")) bitboards[14][r][c] = 1;
				if (castlingRights.contains("q")) bitboards[15][r][c] = 1;

				// active color
				bitboards[17][r][c] = activeColorLayer;

				// full move counter
				bitboards[18][r][c] = fullMoveNumber;

				// en passant not completed
			}
		}
	
		return bitboards;
	}
	
	private static int getPieceLayerIndex(Piece piece) {
		if (piece instanceof Pawn) {
			return piece.getColor().equals(Color.WHITE) ? 0 : 6;
		} else if (piece instanceof Knight) {
			return piece.getColor().equals(Color.WHITE) ? 1 : 7;
		} else if (piece instanceof Bishop) {
			return piece.getColor().equals(Color.WHITE) ? 2 : 8;
		} else if (piece instanceof Rook) {
			return piece.getColor().equals(Color.WHITE) ? 3 : 9;
		} else if (piece instanceof Queen) {
			return piece.getColor().equals(Color.WHITE) ? 4 : 10;
		} else if (piece instanceof King) {
			return piece.getColor().equals(Color.WHITE) ? 5 : 11;
		}
		return -1;
	}
}