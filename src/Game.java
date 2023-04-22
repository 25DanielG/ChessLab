package src;
import java.awt.*;

public class Game
{
    private static King wKing;
    private static King bKing;
    /**
     * The main method of the chess game that sets up the chess board and the two players.
     * @param args optional arguments that can be passed via the terminal
     */
    public static void main(String[] args)
    {
        Board board = new Board();

        Piece blackKing = new King(Color.BLACK, "./img/black_king.gif");
        blackKing.putSelfInGrid(board, new Location(0, 4));
        Piece blackQueen = new Queen(Color.BLACK, "./img/black_queen.gif");
        blackQueen.putSelfInGrid(board, new Location(0, 3));
        Piece blackRookOne = new Rook(Color.BLACK, "./img/black_rook.gif");
        blackRookOne.putSelfInGrid(board, new Location(0, 0));
        Piece blackRookTwo = new Rook(Color.BLACK, "./img/black_rook.gif");
        blackRookTwo.putSelfInGrid(board, new Location(0, 7));
        Piece blackKnightOne = new Knight(Color.BLACK, "./img/black_knight.gif");
        blackKnightOne.putSelfInGrid(board, new Location(0, 1));
        Piece blackKnightTwo = new Knight(Color.BLACK, "./img/black_knight.gif");
        blackKnightTwo.putSelfInGrid(board, new Location(0, 6));
        Piece blackBishopOne = new Bishop(Color.BLACK, "./img/black_bishop.gif");
        blackBishopOne.putSelfInGrid(board, new Location(0, 2));
        Piece blackBishopTwo = new Bishop(Color.BLACK, "./img/black_bishop.gif");
        blackBishopTwo.putSelfInGrid(board, new Location(0, 5));
        Pawn[] blackPawns = new Pawn[8];
        for (int c = 0; c < 8; c++)
        {
            blackPawns[c] = new Pawn(Color.BLACK, "./img/black_pawn.gif");
            blackPawns[c].putSelfInGrid(board, new Location(1, c));
        }
        Piece whiteKing = new King(Color.WHITE, "./img/white_king.gif");
        whiteKing.putSelfInGrid(board, new Location(7, 4));
        Piece whiteQueen = new Queen(Color.WHITE, "./img/white_queen.gif");
        whiteQueen.putSelfInGrid(board, new Location(7, 3));
        Piece whiteRookOne = new Rook(Color.WHITE, "./img/white_rook.gif");
        whiteRookOne.putSelfInGrid(board, new Location(7, 0));
        Piece whiteRookTwo = new Rook(Color.WHITE, "./img/white_rook.gif");
        whiteRookTwo.putSelfInGrid(board, new Location(7, 7));
        Piece whiteKnightOne = new Knight(Color.WHITE, "./img/white_knight.gif");
        whiteKnightOne.putSelfInGrid(board, new Location(7, 1));
        Piece whiteKnightTwo = new Knight(Color.WHITE, "./img/white_knight.gif");
        whiteKnightTwo.putSelfInGrid(board, new Location(7, 6));
        Piece whiteBishopOne = new Bishop(Color.WHITE, "./img/white_bishop.gif");
        whiteBishopOne.putSelfInGrid(board, new Location(7, 2));
        Piece whiteBishopTwo = new Bishop(Color.WHITE, "./img/white_bishop.gif");
        whiteBishopTwo.putSelfInGrid(board, new Location(7, 5));
        Pawn[] whitePawns = new Pawn[8];
        for (int c = 0; c < 8; c++)
        {
            whitePawns[c] = new Pawn(Color.WHITE, "./img/white_pawn.gif");
            whitePawns[c].putSelfInGrid(board, new Location(6, c));
        }
        BoardDisplay display = new BoardDisplay(board);
        wKing = (King) whiteKing;
        bKing = (King) blackKing;
        play(board, display, new HumanPlayer(board, display, Color.WHITE, "Human"), new SmartPlayer(board, Color.BLACK, "SmartPlayer"));
        // play(board, display, new SmartPlayer(board, Color.WHITE, "SmartPlayer"), new HumanPlayer(board, display, Color.BLACK, "Human"));
        // play(board, display, new SmartPlayer(board, Color.WHITE, "SmartPlayer"), new SmartPlayer(board, Color.BLACK, "SmartPlayer"));
    }

    /**
     * Gets the next turn of the player, changes the title to reflect whose turn it is
     *      and executs the move.
     * @param board the board to get the next turn on
     * @param display the display to display the board
     * @param player the player of who to get the turn from
     */
    private static void nextTurn(Board board, BoardDisplay display, Player player)
    {
        display.setTitle(player.getName());
        Move next = player.nextMove();
        board.executeMove(next);
        if (next.getPiece() instanceof King)
		{
			((King) next.getPiece()).setMoved(true);
		}
        else if (next.getPiece() instanceof Rook)
        {
            ((Rook) next.getPiece()).setMoved(true);
        }
        display.clearColors();
        display.setColor(next.getSource(), Color.YELLOW);
        display.setColor(next.getDestination(), Color.YELLOW);
        try
        {
            Thread.sleep(500);
        }
        catch(InterruptedException e) {}
    }

    /**
     * The main play method that repeatedly takes moves from each player until the king either king
     *      is captured.
     * @param board the board to take turns on
     * @param display the display to display the board
     * @param white the white player
     * @param black the black player
     */
    public static void play(Board board, BoardDisplay display, Player white, Player black)
    {
        while (wKing.getLocation() != null && bKing.getLocation() != null)
        {
            nextTurn(board, display, white);
            display.showBoard();
            nextTurn(board, display, black);
            display.showBoard();
        }
        if (wKing.getLocation() == null)
        {
            System.out.println(black.getName() + " wins!");
        }
        else
        {
            System.out.println(white.getName() + " wins!");
        }
    }
}
