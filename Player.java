import java.awt.Color;

/**
 * A player abstract class that describes how a player class should look like, provides
 *      methods such as getters and a nextMove method which all players should implement.
 * @author Daniel Gergov
 * @version 3/31/23
 */
public abstract class Player
{
    private Board board;
    private String name;
    private Color color;

    /**
     * Default constructor for a player
     * @param board the board for which the player belongs to
     * @param name the name of the player for which the game to call it by
     * @param color the color of the player, black or white, pertaining to which side
     *      they play for
     */
    public Player(Board board, String name, Color color)
    {
        this.board = board;
        this.name = name;
        this.color = color;
    }

    /**
     * Returns the board for which the player is playing on
     * @return type Board the board the player is playing on
     */
    public Board getBoard()
    {
        return board;
    }

    /**
     * Returns the name of the player
     * @return type String the name of the player
     */
    public String getName()
    {
        return name;
    }

    /**
     * Returns the color of the player pertaining to which side they are playing on
     * @postcondition either Color.BLACK or Color.WHITE is returned
     * @return type Color the color of the player
     */
    public Color getColor()
    {
        return color;
    }

    /**
     * An abstract method to get the nextMove for a player
     * @return type Move the move that a player wants to make
     */
    public abstract Move nextMove();
}
