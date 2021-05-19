import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * JBoardTest is a unit test application for tetris -- its sole purpose is
 *      exercising the tetris classes.
 *  Its interface only make sense if you think of it as a direct connection to
 *      the Board and Piece interfaces. JBoardTest is provided in finished form
 *      so the students can use it to test their Piece and Board code.
 *      
 *  Use the buttons on screen or the keys 4, 5, 6, 7, 0 for control.
 *  Add a piece, move it around.
 *  JBoardTest uses place() and undo() to move the piece around the board.
 *  Use the app to set up and test out specific cases for your tetris board
 *      and piece.
 *      
 *  Board has all sorts of hidden state -- heights, widths etc.. The hidden state
 *      is what makes Board hard to debug.
 *  The unit test tries to make the state of the Board transparent, so you can see
 *      what's going on as you exercise your code.
 *      
 * @author Nick Parlante
 * @version    1.0, Mar 1, 2001
 */
class JBoardTest extends JComponent
{
    // Board data structures
    private Board board;
    private Piece[] pieces;

    private JLabel label;

    // The currently played piece
    // null if there is no current piece
    private Piece piece;
    private int pieceX;
    private int pieceY;

    public JBoardTest(int width, int height)
    {
        super();

        setPreferredSize(new Dimension(width, height));

        this.pieces = Piece.getPieces();

        reset();
    }

    private void reset()
    {
        board = new Board(6, 10);
        piece = null;
    }

    /**
     * Whenever the user clicks a button, this runs to make the change. The
     *      strategy is: compute what the new piece position should be, undo to
     *      remove the old piece position, put in the new piece position.
     */

    public final int ADD = 1;
    public final int ADDRANDOM = 2;
    public final int LEFT = 3;
    public final int RIGHT = 4;
    public final int ROTATE = 5;
    public final int DOWN = 6;
    public final int DROP = 7;
    public final int CLEAR = 8;
    public final int RESET = 9;
    public final int UNDO = 10;

    public void action(int verb)
    {
        if (verb == RESET)
        {
            this.reset();
        }
        else if (verb == UNDO)
        {
            this.board.undo();
        }
        else if (verb == CLEAR)
        {
            if (this.board.clearRows())
            {
                this.board.commit(); // finalize the last position
                this.piece = null;
            }
        }
        else
        {
            // compute a new piece based on the old piece and the verb
            Piece newPiece = this.piece;
            int newX = this.pieceX;
            int newY = this.pieceY;

            switch (verb)
            {
                case ADD:
                case ADDRANDOM:
                    // finalize the last position
                    this.board.commit();
                    this.piece = null;

                    if (verb == ADD)
                    {
                        newPiece = pieces[6];
                    }
                    else
                    {
                        newPiece = pieces[(int)(Math.random()*pieces.length)];
                    }

                    newX = 0;
                    newY = this.board.getHeight() - newPiece.getHeight() - 1;
                    break;

                case LEFT:
                    newX--;
                    break;

                case RIGHT:
                    newX++;
                    break;

                case ROTATE:
                    if (this.piece != null)
                    {
                        newPiece = this.piece.nextRotation();
                    }
                    break;

                case DOWN:
                    newY--;
                    break;

                case DROP:
                    // remove the piece before the drop computation so
                    // it doesn't hit itself on the way down!
                    if (this.piece != null)
                    {
                        this.board.undo();
                        newY = this.board.dropHeight(newPiece, newX);
                    }
                    break;

                default:
                throw new RuntimeException("Bad verb");
            }

            // now we have a newPiece

            if (newPiece!=null)
            {
                this.board.undo();   // remove the old state

                // try putting in the new piece 
                int result = this.board.place(newPiece, newX, newY);

                // see if it worked
                if (result <= Board.PLACE_ROW_FILLED)
                {
                    this.label.setText("");
                    this.piece = newPiece;
                    this.pieceX = newX;
                    this.pieceY = newY;
                }
                else
                {
                    this.label.setText("bad:" + result);

                    // put it back the way it was
                    this.board.undo();
                    if (this.piece != null)
                    {
                        board.place(this.piece, this.pieceX, this.pieceY);
                    }
                }
            }
        }

        // redraw the whole board since we've changed it
        this.repaint();
    }

    /**
     * Pixel helpers -- these centralize how we map from block coords (x,y) to
     *      pixel coordinates on screen. By centralizing here, at least it's all
     *      consistent.
     *  These return the distance in pixels from the left or botton edge to the
     *      upper left corner of the rect for each tetris block.
     */
    private int xPixelDist(int x, float dx)
    {
        return(MARGIN + Math.round(x * dx));
    }

    private int yPixelDist(int y, float dy)
    {
        return(Math.round((y + 1) * dy));
    }

    // pixel space on the left and bottom
    public final int MARGIN = 20;

    public void paintComponent(Graphics g)
    {
        // area where tetris blocks are drawn
        int pixelWidth = this.getWidth() - MARGIN;
        int pixelHeight = this.getHeight() - MARGIN;

        // block size of the board
        int bWidth = this.board.getWidth();
        int bHeight = this.board.getHeight();
        int bMaxHeight = this.board.getMaxHeight();

        // pixels across of each block
        float dx = ((float)pixelWidth) / bWidth;
        float dy = ((float)pixelHeight) / bHeight;

        g.drawRect(MARGIN, 0, pixelWidth - 1, pixelHeight - 1);

        int x, y;

        // draw the board blocks and the bits of text...
        for (y = 0; y < bMaxHeight; y++)
        {
            // draw row width on the left
            g.drawString(Integer.toString(this.board.getRowWidth(y)),
                4, pixelHeight - this.yPixelDist(y - 1, dy) - 4);

            for (x = 0; x < bWidth; x++)
            {
                if (this.board.getGrid(x, y))
                {
                    // emphasize the col height in green
                    if (this.board.getColumnHeight(x) == y + 1)
                    {
                        g.setColor(Color.GREEN);
                    }

                    // draw the block, inset by one pixel all around
                    g.fillRect(this.xPixelDist(x, dx) + 1,
                        pixelHeight - this.yPixelDist(y, dy) + 1,
                        (int)dx - 2, (int)dy - 2);

                    g.setColor(Color.BLACK);
                }
            }
        }

        // draw the height numbers along the bottom
        for (x = 0; x < bWidth; x++)
        {
            g.drawString(Integer.toString(this.board.getColumnHeight(x)),
                this.xPixelDist(x, dx) + 6, pixelHeight + MARGIN - 4);
        }

    }

    /**
     * Creates all the buttons in a panel, and wire them to the action() message.
     * 
     * Installs keyboard listeners and wire them to the action() message as well.
     */
    public Container createControlPanel()
    {
        Container panel = new Box(BoxLayout.Y_AXIS);

        JButton button;

        button = new JButton("Add");
        panel.add(button);
        button.addActionListener( new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(ADD);
                }
            });

        button = new JButton("Add Random");
        panel.add(button);
        button.addActionListener( new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(ADDRANDOM);
                }
            });

        button = new JButton("Left");
        panel.add(button);
        button.addActionListener( new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(LEFT);
                }
            });

        button = new JButton("Right");
        panel.add(button);
        button.addActionListener( new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(RIGHT);
                }
            });

        button = new JButton("Rotate");
        panel.add(button);
        button.addActionListener( new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(ROTATE);
                }
            });

        button = new JButton("Down");
        panel.add(button);
        button.addActionListener( new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(DOWN);
                }
            });

        button = new JButton("Drop");
        panel.add(button);
        button.addActionListener( new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(DROP);
                }
            });

        button = new JButton("Clear Rows");
        panel.add(button);
        button.addActionListener( new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(CLEAR);
                }
            });

        label = new JLabel();
        panel.add(label);

        panel.add(Box.createVerticalStrut(12));

        button = new JButton("Undo");
        panel.add(button);
        button.addActionListener( new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(UNDO);
                }
            });

        button = new JButton("Reset");
        panel.add(button);
        button.addActionListener( new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(RESET);
                }
            });

        button = new JButton("Quit");
        panel.add(button);
        button.addActionListener( new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    System.exit(0);
                }
            });

        // Register the 4 keystroke listeners
        // to call action() with the appropriate constant.
        // 4=left 5=rotate 6=right 0=drop 7=add
        registerKeyboardAction(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(ADD);
                }
            },
            "add",
            KeyStroke.getKeyStroke('7'),
            WHEN_IN_FOCUSED_WINDOW
        );

        registerKeyboardAction(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(LEFT);
                }
            },
            "left",
            KeyStroke.getKeyStroke('4'),
            WHEN_IN_FOCUSED_WINDOW
        );

        registerKeyboardAction(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(RIGHT);
                }
            },
            "right",
            KeyStroke.getKeyStroke('6'),
            WHEN_IN_FOCUSED_WINDOW
        );

        registerKeyboardAction(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(ROTATE);
                }
            },
            "rotate",
            KeyStroke.getKeyStroke('5'),
            WHEN_IN_FOCUSED_WINDOW
        );

        registerKeyboardAction(
            new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    action(DROP);
                }
            },
            "drop",
            KeyStroke.getKeyStroke('0'),
            WHEN_IN_FOCUSED_WINDOW
        );

        return(panel);
    }

    /*
     * Creates a frame for the board tester and its controls.
     */
    static public void main(String[] args)
    {
        JFrame frame = new JFrame("JBoardTest");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JComponent container = (JComponent)frame.getContentPane();
        container.setLayout(new BorderLayout());

        JBoardTest bt = new JBoardTest(200, 300);

        container.add(bt, BorderLayout.CENTER);

        Container panel = bt.createControlPanel();
        container.add(panel, BorderLayout.EAST);

        frame.pack();
        frame.setVisible(true);
    }
}
