import java.awt.Point;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * An immutable representation of a tetris piece in a particular rotation.
 * Each piece is defined by the blocks that make up its body.
 * See the lab document for an overview.
 * 
 * @author Nick Parlante
 * @version    1.0, Mar 1, 2001
 */
public final class Piece {
    /*
     * Implementation hints:
     *  -The starter code does out a few simple things for you
     *      -Store the body as a Point[] array
     *  -The attributes of the Point class are x and y
     *  -Do not assume there are 4 points in the body -- use array.length
     *          to keep the code general
     */

    private Point[] body;
    private int[] skirt;
    private int width;
    private int height;
    private Piece next; // "next" rotation

    static private Piece[] pieces;  // singleton array of first rotations

    /**
     * Defines a new piece given the Points that make up its body.
     * Makes its own copy of the array and the Points inside it.
     * Does not set up the rotations.
     * 
     * This constructor is PRIVATE -- if a client
     * wants a piece object, they must use Piece.getPieces().
     * 
     * @param points    array of points that make up this Piece's body
     */
    private Piece(Point[] points)
    {
        // TODO: implement constructor
    }   

    /**
     * Returns the width of the piece measured in blocks.
     * 
     * @return the width of the piece measured in blocks
     */
    public int getWidth()
    {
        return this.width;
    }

    /**
     * Returns the height of the piece measured in blocks.
     * 
     * @return the height of the piece measured in blocks
     */
    public int getHeight()
    {
        return this.height;
    }

    /**
     * Returns a reference to this piece's body.
     * The caller should not modify this array.
     * 
     * @return a reference to this piece's body
     */
    public Point[] getBody()
    {
        return this.body;
    }

    /**
     * Returns a reference to this piece's skirt.
     * For each x value across the piece, the skirt gives the lowest y value
     *      in the body.
     * This useful for computing where the piece will land.
     * The caller should not modify this array.
     * 
     * @return a reference to this piece's skirt
     */
    public int[] getSkirt()
    {
        return this.skirt;
    }

    /**
     * Returns a reference to a Piece that is 90 degrees counter-clockwise
     *      rotated from this Piece.
     *      
     *  Implementation:
     *      The Piece class pre-computes all the rotations once.
     *      This method just hops from one pre-computed rotation to the next in
     *          constant time.
     *          
     *  @return a reference to a Piece that is 90 degrees counter-clockwise
     *      rotated from this Piece
     */ 
    public Piece nextRotation()
    {
        return this.next;
    }

    /**
     * Returns true if two pieces are the same -- their bodies contain the same
     *      points.
     *  Interestingly, this is not the same as having exactly the same body
     *      arrays, since the points may not be in the same order in the bodies.
     *  Used internally to detect if two rotations are effectively the same.
     *  
     *  @param other    the object with which to test equality 
     *  @return true if two pieces are the same
     */
    @Override
    public boolean equals(Object other)
    {
        // self check
        if(this == other)
        {
            return true;
        }

        // null check
        if(other == null)
        {
            return false;
        }

        // type check and cast
        if(this.getClass() != other.getClass())
        {
            return false;
        }

        Piece otherPiece = (Piece)other;

        // field comparison
        Set<Point> thisSet = new HashSet<Point>(Arrays.asList(this.body));
        Set<Point> otherSet = new HashSet<Point>(Arrays.asList(otherPiece.body));

        return thisSet.equals(otherSet);
    }

    /**
     * Returns the attributes of the Piece as a String
     * 
     * @return the attributes of the Piece as a String
     */
    @Override
    public String toString()
    {
        String str = "";
        
        // TODO: build a string that contains all of the attributes of this Piece
        
        return str;
    }
    
    /**
     * Returns an array containing the first rotation of each of the 7 standard
     *      tetris pieces. The next (counterclockwise) rotation can be obtained
     *      from each piece with the {@link #nextRotation()} method. In this way,
     *      the client can iterate through all the rotations until eventually
     *      getting back to the first rotation.
     *      
     *  @return an array containing the first rotation of each of the 7 standard
     *      tetris pieces
     */
    public static Piece[] getPieces() {
        if(Piece.pieces != null)
        {
            return Piece.pieces;
        }

        Piece.pieces = new Piece[]
        {
            Piece.pieceRow(new Piece(Piece.parsePoints("0 0 0 1 0 2 0 3"))),    // 0
            Piece.pieceRow(new Piece(Piece.parsePoints("0 0 0 1 0 2 1 0"))),    // 1
            Piece.pieceRow(new Piece(Piece.parsePoints("0 0 1 0 1 1 1 2"))),    // 2
            Piece.pieceRow(new Piece(Piece.parsePoints("0 0 1 0 1 1 2 1"))),    // 3
            Piece.pieceRow(new Piece(Piece.parsePoints("0 1 1 1 1 0 2 0"))),    // 4
            Piece.pieceRow(new Piece(Piece.parsePoints("0 0 0 1 1 0 1 1"))),    // 5
            Piece.pieceRow(new Piece(Piece.parsePoints("0 0 1 0 1 1 2 0"))),    // 6
        };

        return Piece.pieces;
    }

    /**
     * Computes all the rotations of the specified Piece and connects them by
     *      their next attributes
     *      
     *  @param piece    the specified piece from which to compute all the
     *                  rotations
     *  @return a reference to the specified piece
     */
    private static Piece pieceRow(Piece firstPiece)
    {
        Piece piece = firstPiece;
        
        System.out.println("\nfirst piece: " + piece);

        // maximum of 4 rotations until we are back at the first piece (we may break earlier)
        for( int j = 0; j < 4; j++)
        {
            // copy the points from the specified piece before transforming
            Point[] rotatedPoints = new Point[piece.getBody().length];
            for(int i = 0; i < rotatedPoints.length; i++)
            {
                rotatedPoints[i] = new Point(piece.getBody()[i]);
            }

            // TODO: step 1: reflect across the line y = x
            
            // TODO: step 2: reflect across y axis
            
            // TODO: step 3: translate right
            
            // create the rotated piece, update next, prepare for nextIteration
            Piece rotatedPiece = new Piece(rotatedPoints);
            
            System.out.println(rotatedPiece);

            // check if we are back to the original piece
            if(rotatedPiece.equals(firstPiece))
            {
                // the previous piece links back to the original piece
                piece.next = firstPiece;
                break;
            }
            else
            {
                // update the next attribute and prepare for the next rotation
                piece.next = rotatedPiece;
                piece = rotatedPiece;
            }
        }

        return firstPiece;
    }

    /**
     * Given a string of x,y pairs ("0 0   0 1 0 2 1 0"), parses the points into
     *      a Point[] array.
     */
    private static Point[] parsePoints(String string)
    {
        ArrayList<Point> points = new ArrayList();
        StringTokenizer tok = new StringTokenizer(string);
        try
        {
            while(tok.hasMoreTokens())
            {
                int x = Integer.parseInt(tok.nextToken());
                int y = Integer.parseInt(tok.nextToken());

                points.add(new Point(x, y));
            }
        }
        catch (NumberFormatException e)
        {
            throw new RuntimeException("Could not parse x,y string:" + string);
        }

        // Make an array out of the Vector
        Point[] array = new Point[points.size()];
        array = points.toArray(array);
        return(array);
    }
}
