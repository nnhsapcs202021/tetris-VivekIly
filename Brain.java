/**
 *  The interface for Tetris brains.
 *  See Tetris-Architecture.html for an overview.
 *  
 *  @author    Nick Parlante
 *  @version   1.0, Mar 1, 2001
 */

public interface Brain
{
    /**
     * Given a piece and a board, returns a move object that represents the best
     *      play for that piece, or returns null if no play is possible.
     *  The board should be in the committed state when this is called.
     *  "limitHeight" is the bottom section of the board that where pieces must
     *      come to rest -- typically 20.
     *  If the passed in move is non-null, it is used to hold the result
     *      (just to save the memory allocation).
     *      
     *  @param board        the board in which to calculate the best move
     *  @param piece        the piece to place in the optimal location on the board
     *  @param limitHeight  the piece must be placed below this height
     *  @return             the best move for the specified piece
    */
    public Move bestMove(Board board, Piece piece, int limitHeight);
}
