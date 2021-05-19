
/**
 * Move is used as a structure to store a single Move
 *
 * @author gcschmit
 * @version 24 January 2020
 */
public class Move
{
    private int x;
    private int y;
    private Piece piece;
    private double score;
    
    /**
     * Constructs a new Move object
     * 
     * @param initialX the desired x coordinate of the bottom-left corner of the piece
     * @param initialY the desired y coordinate of the bottom-left corner of the piece
     * @param initialPiece the desired orientation (rotation) of the piece
     * @param initialScore the score of this move (lower scores are better)
     */
    public Move(int initialX, int initialY, Piece initialPiece, double initialScore)
    {
        this.x = initialX;
        this.y = initialY;
        this.piece = initialPiece;
        this.score = initialScore;
    }
    
    /**
     * Returns the desired x coordinate of the bottom-left corner of the piece for this move
     * 
     * @return the desired x coordinate of the bottom-left corner of the piece for this move
     */
    public int getX()
    {
        return this.x;
    }
    
    /**
     * Returns the desired y coordinate of the bottom-left corner of the piece for this move
     * 
     * @return the desired y coordinate of the bottom-left corner of the piece for this move
     */
    public int getY()
    {
        return this.y;
    }
    
    /**
     * Returns the desired orientation (rotation) of the piece for this move
     * 
     * @return the desired orientation (rotation) of the piece for this move
     */
    public Piece getPiece()
    {
        return this.piece;
    }
    
    /**
     * Returns the score of this move (lower scores are better)
     * 
     * @return the score of this move
     */
    public double getScore()
    {
        return this.score;
    }
}
