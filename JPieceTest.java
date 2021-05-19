import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * Debugging client for the Piece class.
 * 
 * The JPieceTest component draws all the rotations of a tetris piece.
 * JPieceTest.main() creates a frame  with one JPieceTest for each of the 7
 *      standard tetris pieces.
 *       
 * @author Nick Parlante
 * @version    1.0, Mar 1, 2001
 */
class JPieceTest extends JComponent
{
    public final int MAX_ROTATIONS = 4;

    private Piece root;   

    public JPieceTest(Piece piece, int width, int height)
    {
        super();

        setPreferredSize(new Dimension(width, height));

        this.root = piece;
    }

    /**
     * Draws the rotations from left to right.
     * Each piece goes in its own little box.
     */
    @Override
    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D)g;
        Piece piece = this.root;
        double rectWidth = this.getWidth() / MAX_ROTATIONS;
        Rectangle2D.Double rect = new Rectangle2D.Double(0, 0,
                rectWidth, this.getHeight());
        do
        {
            this.drawPiece(g2, piece, rect);
            piece = piece.nextRotation();
            // remove this for final test of the Piece class
            if(piece == null)
                break;
            rect.setRect(rect.getX() + rectWidth, rect.getY(),
                    rect.getWidth(), rect.getHeight());
        }
        while(piece != this.root);
    }

    /**
     * Draw the piece inside the given rectangle.
     */
    private void drawPiece(Graphics2D g2, Piece piece, Rectangle2D.Double r)
    {
        double pixelSize = Math.min(r.getWidth(), r.getHeight()) / MAX_ROTATIONS;

        for(Point point : piece.getBody())
        {
            /*
             * leave a 1 pixel border around the block
             * 
             * adjust the y value as (0,0) is in the upper-left but the piece
             *      is placed at the lower-left of the rectangle
             */
            Rectangle2D.Double pixel = new Rectangle2D.Double(
                    r.getX() + (point.x * pixelSize) + 1,
                    r.getY() + ((3 - point.y) * pixelSize) + 1,
                    pixelSize - 2, pixelSize - 2);

            // draw skirt pixels in yellow
            if(point.y == piece.getSkirt()[point.x])
            {
                g2.setColor(Color.YELLOW);
            }

            g2.fill(pixel);

            // draw a description of the piece along the bottom
            g2.setColor(Color.RED);
            String desc = "w:" + piece.getWidth() + " h:" + piece.getHeight();
            g2.drawString(desc, (float)(r.getX() + 1),
                    (float)(r.getY() + (4 * pixelSize) - 2));

            g2.setColor(Color.BLACK);
        }
    }   

    /**
     * Draws all the pieces by creating a JPieceTest for each piece, and putting
     *      them all in a frame.
     */
    static public void main(String[] args)

    {
        JFrame frame = new JFrame("Piece Tester");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JComponent container = (JComponent)frame.getContentPane();

        // put in a BoxLayout to make a vertical list
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        Piece[] pieces = Piece.getPieces();

        for (int i = 0; i < pieces.length; i++)
        {
            JPieceTest test = new JPieceTest(pieces[i], 375, 75);
            container.add(test);
        }

        // size the window and show it on screen
        frame.pack();
        frame.setVisible(true);
    }
}
