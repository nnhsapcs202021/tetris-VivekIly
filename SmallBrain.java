/**
 * A joke implementation based on SimpleBrain -- very badly.
 * 
 * @author Nick Parlante
 * @version    1.0, Mar 1, 2001
 */
public class SmallBrain extends SimpleBrain 
{
    public double rateBoard(Board board)
    {
        double score = super.rateBoard(board);
        return (10000 - score);
    }
    
    public String toString()
    {
        return "Small Brain";
    }
}
