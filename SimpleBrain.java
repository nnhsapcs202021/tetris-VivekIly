/**
 * A simple Brain implementation.
 *  bestMove() iterates through all the possible x values and rotations to play a
 *      particular piece (there are only around 10-30 ways to play a piece).
 *      
 *  For each play, it uses the rateBoard() message to rate how good the resulting
 *      board is and it just remembers the play with the lowest score. Undo() is
 *      used to back-out each play before trying the next. To experiment with
 *      writing your own brain -- just subclass off SimpleBrain and override
 *      rateBoard().
 * 
 * @author Nick Parlante
 * @version    1.0, Mar 1, 2001
 */

public class SimpleBrain implements Brain
{
    /**
     * Given a piece and a board, returns a move object that represents the best
     *      play for that piece, or returns null if no play is possible.
     *  See the Brain interface for details.
     *  
     *  @param board        the board in which to calculate the best move
     *  @param piece        the piece to place in the optimal location on the board
     *  @param limitHeight  the piece must be placed below this height
     *  @return             the best move for the specified piece
     */
    public Move bestMove(Board board, Piece piece, int limitHeight)
    {
        double bestScore = 1e20;
        int bestX = 0;
        int bestY = 0;
        Piece bestPiece = null;
        Piece current = piece;

        // loop through all the rotations
        while (true)
        {
            final int yBound = limitHeight - current.getHeight() + 1;
            final int xBound = board.getWidth() - current.getWidth() + 1;

            // for current rotation, try all the possible columns
            for (int x = 0; x < xBound; x++)
            {
                int y = board.dropHeight(current, x);
                if (y < yBound) // piece does not stick up too far
                {
                    int result = board.place(current, x, y);
                    if (result <= Board.PLACE_ROW_FILLED)
                    {
                        if (result == Board.PLACE_ROW_FILLED)
                        {
                            board.clearRows();
                        }

                        double score = rateBoard(board);

                        if (score < bestScore)
                        {
                            bestScore = score;
                            bestX = x;
                            bestY = y;
                            bestPiece = current;
                        }
                    }

                    // back out that play, loop around for the next
                    board.undo();
                }
            }

            current = current.nextRotation();
            if (current == piece)
            {
                break;    // break if back to original rotation
            }
        }

        if (bestPiece == null)
        {
            return null;    // could not find a play at all!
        }
        else
        {
            return new Move(bestX, bestY, bestPiece, bestScore);
        }
    }

    /**
     * A simple brain function.
     *  Given a board, produce a number that rates that board position -- larger
     *      numbers for worse boards.
     *  This version just counts the height and the number of "holes" in the board.
     *  See Tetris-Architecture.html for brain ideas.
     *  
     *  @param board    the specified board to rate
     *  @return         the rating for the specified board (the lower the number
     *                      the better the board)
     */
    public double rateBoard(Board board)
    {
        final int width = board.getWidth();
        final int maxHeight = board.getMaxHeight();

        int sumHeight = 0;
        int holes = 0;

        // count the holes, and sum up the heights
        for (int x=0; x<width; x++)
        {
            final int colHeight = board.getColumnHeight(x);
            sumHeight += colHeight;

            int y = colHeight - 2;  // addr of first possible hole

            while (y >= 0)
            {
                if (!board.getGrid(x, y))
                {
                    holes++;
                }
                
                y--;
            }
        }

        double avgHeight = ((double)sumHeight) / width;

        // Add up the counts to make an overall score
        // The weights, 8, 40, etc., are just made up numbers that appear to work
        return ((8 * maxHeight) + (40 * avgHeight) + (1.25 * holes));
    }
    
    public String toString()
    {
        return "Simple Brain";
    }

}
