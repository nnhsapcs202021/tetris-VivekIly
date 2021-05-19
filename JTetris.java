import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * JTetris presents a tetris game in a window.
 *  It handles the GUI and the animation.
 *  The Piece and Board classes handle the lower-level computations.
 *  This code is provided in finished form for the students.
 *  See Tetris-Architecture.html for an overview.
 *  
 *  @author    Nick Parlante
 *  @version   1.0, March 1, 2001
*/

/*
 * Implementation notes:
 *      The "currentPiece" points to a piece that is currently falling, or is
 *          null when there is no piece.
 *      tick() moves the current piece
 *      a timer object calls tick(DOWN) periodically
 *      keystrokes call tick with LEFT, RIGHT, etc.
 *      Board.undo() is used to remove the piece from its old position and then
 *          Board.place() is used to install the piece in its new position.
*/

public class JTetris extends JComponent
{
    // size of the board in blocks
    public static final int WIDTH = 10;
    public static final int HEIGHT = 20;
    
    // extra blocks at the top for pieces to start.
    // if a piece is sticking up into this area when it has landed -- game over!
    public static final int TOP_SPACE = 4;
    
    
    // when this is true, plays a fixed sequence of 100 pieces
    protected boolean testMode = false;
    public final int TEST_LIMIT = 100;
    
    
    // is drawing optimized
    protected boolean DRAW_OPTIMIZE = false;
    
    // Board data structures
    protected Board board;
    protected Piece[] pieces;
    
    
    // the current piece in play or null
    protected Piece currentPiece;
    protected int currentX;
    protected int currentY;
    protected boolean moved;    // did the player move the piece
    
    
    // the piece we're thinking about playing -- set by computeNewPosition
    protected Piece newPiece;
    protected int newX;
    protected int newY;
    
    // state of the game
    protected boolean gameOn;   // true if we are playing
    protected int count;        // how many pieces played so far
    protected long startTime;   // used to measure elapsed time
    protected Random random;    // the random generator for new pieces
    
    
    // controls
    protected JLabel countLabel;
    protected JLabel timeLabel;
    protected JButton startButton;
    protected JButton stopButton;
    protected javax.swing.Timer timer;
    protected JSlider speed;
    
    public final int DELAY = 400;   // milliseconds per tick
    

    JTetris(int width, int height)
    {
        super();

        setPreferredSize(new Dimension(width, height));
        this.gameOn = false;
        
        this.pieces = Piece.getPieces();
        this.board = new Board(WIDTH, HEIGHT + TOP_SPACE);


        /*
         * Register key handlers that call tick with the appropriate constant.
         *      e.g. 'j' and '4'  call tick(LEFT)
        */
        
        // LEFT
        registerKeyboardAction(
            new LeftActionListener(), "left", KeyStroke.getKeyStroke('4'), WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(
            new LeftActionListener(), "left", KeyStroke.getKeyStroke('j'), WHEN_IN_FOCUSED_WINDOW);
        
        
        // RIGHT
        registerKeyboardAction(
            new RightActionListener(), "right", KeyStroke.getKeyStroke('6'), WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(
            new RightActionListener(), "right", KeyStroke.getKeyStroke('l'), WHEN_IN_FOCUSED_WINDOW);
        
        
        // ROTATE   
        registerKeyboardAction(
            new RotateActionListener(), "rotate", KeyStroke.getKeyStroke('5'), WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(
            new RotateActionListener(), "rotate", KeyStroke.getKeyStroke('k'), WHEN_IN_FOCUSED_WINDOW);
        
        
        // DROP
        registerKeyboardAction(
            new DropActionListener(), "drop", KeyStroke.getKeyStroke('0'), WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(
            new DropActionListener(), "drop", KeyStroke.getKeyStroke('n'), WHEN_IN_FOCUSED_WINDOW);      
        
        
        // Create the Timer object and have it send tick(DOWN) periodically
        this.timer = new javax.swing.Timer(DELAY, new DownActionListener());
    }

    /**
     * Sets the internal state and starts the timer so the game is happening.
    */
    public void startGame()
    {
        // cheap way to reset the board state
        this.board = new Board(WIDTH, HEIGHT + TOP_SPACE);
        
        // draw the new board state once
        this.repaint();
        
        this.count = 0;
        this.gameOn = true;
        
        if (this.testMode)
        {
            this.random = new Random(0);   // same seq every time
        }
        else
        {
            this.random = new Random(); // diff seq each game
        }
        
        this.enableButtons();
        this.timeLabel.setText(" ");
        this.addNewPiece();
        this.timer.start();
        this.startTime = System.currentTimeMillis();
    }
    
    
    /**
     * Sets the enabling of the start/stop buttons based on the gameOn state.
    */
    private void enableButtons()
    {
        this.startButton.setEnabled(!this.gameOn);
        this.stopButton.setEnabled(this.gameOn);
    }
    
    /**
     * Stops the game.
    */
    public void stopGame()
    {
        this.gameOn = false;
        this.enableButtons();
        this.timer.stop();
        
        long delta = (System.currentTimeMillis() - this.startTime) / 10;
        this.timeLabel.setText(Double.toString(delta / 100.0) + " seconds");

    }
    
    /**
     * Given a piece, tries to install that piece into the board and set it to be
     *      the current piece.
     *  Does the necessary repaints.
     *  If the placement is not possible, then the placement is undone, and the
     *      board is not changed. The board should be in the committed state when
     *      this is called.
     *  Returns the same status code as Board.place().
    */
    public int setCurrent(Piece piece, int x, int y)
    {
        int status = this.board.place(piece, x, y);
        
        if (status <= Board.PLACE_ROW_FILLED)  // SUCESS
        {
            // repaint the rect where it used to be
            if (this.currentPiece != null)
            {
                this.repaintPiece(this.currentPiece, this.currentX, this.currentY);
            }
            
            this.currentPiece = piece;
            this.currentX = x;
            this.currentY = y;
            
            // repaint the rect where it is now
            this.repaintPiece(this.currentPiece, this.currentX, this.currentY);
        }
        else
        {
            this.board.undo();
        }
        
        return status;
    }


    /**
     * Selects the next piece to use using the random generator set in startGame().
    */
    public Piece pickNextPiece()
    {
        int pieceNum = (int)(this.pieces.length * this.random.nextDouble());
        return this.pieces[pieceNum];
    }
    
            
    /**
     * Tries to add a new random at the top of the board.
     * Ends the game if it's not possible.
    */
    public void addNewPiece()
    {
        this.count++;
        
        if (this.testMode && this.count == TEST_LIMIT+1)
        {
             this.stopGame();
             return;
        }

        // commit the board before invoking pickNextPiece as the board must be in the
        //  committed state when a brain is enabled
        this.board.commit();
        this.currentPiece = null;
        
        Piece piece = this.pickNextPiece();
        
        // Center it up at the top
        int px = (this.board.getWidth() - piece.getWidth()) / 2;
        int py = this.board.getHeight() - piece.getHeight();

        // add the new piece to be in play
        int status = this.setCurrent(piece, px, py);
        
        // This probably never happens, since the blocks at the top allow space
        //  for new pieces to at least be added.
        if (status > Board.PLACE_ROW_FILLED)
        {
            this.stopGame();
        }

        this.countLabel.setText(Integer.toString(this.count));
    }
    
    
    /**
     * Figures a new position for the current piece based on the given verb
     *      (LEFT, RIGHT, ...).
     *  The board should be in the committed state -- i.e. the piece should not
     *      be in the board at the moment.
     *  This is necessary so dropHeight() may be called without the piece
     *      "hitting itself" on the way down.
     *      
     *  Sets the attributes newX, newY, and newPiece to hold what it thinks the
     *      new piece position should be. (Storing an intermediate result like
     *      that in attributes is a little tacky.)
    */
    public void computeNewPosition(int verb)
    {
        // as a starting point, the new position is the same as the old
        this.newPiece = this.currentPiece;
        this.newX = this.currentX;
        this.newY = this.currentY;
        
        // make changes based on the verb
        switch (verb)
        {
            case LEFT:
                this.newX--;
                break;
            
            case RIGHT:
                this.newX++;
                break;
            
            case ROTATE:
                this.newPiece = this.newPiece.nextRotation();
                
                // tricky: make the piece appear to rotate about its center
                // can't just leave it at the same lower-left origin as the
                // previous piece.
                this.newX = this.newX + (this.currentPiece.getWidth() -
                        this.newPiece.getWidth()) / 2;
                this.newY = this.newY + (this.currentPiece.getHeight() -
                        this.newPiece.getHeight()) / 2;
                break;
                
            case DOWN:
                this.newY--;
                break;
            
            case DROP:
                // note: if the piece were in the board, it would interfere here
                this.newY = this.board.dropHeight(this.newPiece, this.newX);
                break;
             
            default:
                throw new RuntimeException("Bad verb");
        }
    
    }

    public static final int ROTATE = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int DROP = 3;
    public static final int DOWN = 4;
    /**
     * Called to change the position of the current piece.
     *  Each key press call this once with the verbs LEFT RIGHT ROTATE DROP for
     *      the user moves, and the timer calls it with the verb DOWN to move
     *      the piece down one square.
     *      
     *  Before this is called, the piece is at some location in the board.
     *  This advances the piece to be at its next location.    
     *  
     *  Overriden by the brain when it plays.
    */
    public void tick(int verb)
    {
        if (!this.gameOn)
        {
            return;
        }
        
        if (this.currentPiece != null)
        {
            this.board.undo();   // remove the piece from its old position
        }
        
        // Sets the newXXX attributes
        this.computeNewPosition(verb);
        
        // try out the new position (rolls back if it doesn't work)
        int status = this.setCurrent(this.newPiece, this.newX, this.newY);
        
        // if row clearing is going to happen, draw the whole board so the green
        //      row shows up
        if (status ==  Board.PLACE_ROW_FILLED)
        {
            this.repaint();
        }
        

        boolean failed = (status >= Board.PLACE_OUT_BOUNDS);
        
        // if it didn't work, put it back the way it was
        if (failed)
        {
            if (this.currentPiece != null)
            {
                this.board.place(this.currentPiece, this.currentX, this.currentY);
            }
        }
        
        /*
         * How to detect when a piece has landed:
         *      if this move hits something on its DOWN verb, and the previous
         *          verb was also DOWN (i.e. the player was not still moving it),
         *          then the previous position must be the correct "landed"
         *          position, so we're done with the falling of this piece.
        */
        if (failed && verb==DOWN && !this.moved)   // it's landed
        {
            if (this.board.clearRows())
            {
                this.repaint();  // repaint to show the result of the row clearing
            }
            
            // if the board is too tall, we've lost
            if (this.board.getMaxHeight() > this.board.getHeight() - TOP_SPACE)
            {
                this.stopGame();
            }
            // Otherwise add a new piece and keep playing
            else
            {
                this.addNewPiece();
            }
        }
        
        // Note if the player made a successful non-DOWN move --
        //      used to detect if the piece has landed on the next tick()
        this.moved = (!failed && verb!=DOWN);
    }

    /**
     * Given a piece and a position for the piece, generates a repaint for the
     *      rectangle that just encloses the piece.
    */
    public void repaintPiece(Piece piece, int x, int y)
    {
        if (DRAW_OPTIMIZE)
        {
            int px = this.xPixel(x);
            int py = this.yPixel(y + piece.getHeight() - 1);
            int pwidth = this.xPixel(x + piece.getWidth()) - px;
            int pheight = this.yPixel(y - 1) - py;
            
            this.repaint(px, py, pwidth, pheight);
        }
        else
        {
            this.repaint();
        }
    }
    
    
    /*
     * Pixel helpers.
     * These centralize the translation of (x,y) coords that refer to blocks in
     *      the board to (x,y) coords that count pixels. Centralizing these
     *      computations here is the only prayer that repaintPiece() and
     *      paintComponent() will be consistent.
     *      
     *  The +1's and -2's are to account for the 1 pixel rect around the
     *      perimeter.
    */
    
    // width in pixels of a block
    private final float dX()
    {
        return(((float)(this.getWidth()-2)) / this.board.getWidth() );
    }

    // height in pixels of a block
    private final float dY()
    {
        return(((float)(this.getHeight()-2)) / this.board.getHeight() );
    }
    
    // the x pixel coord of the left side of a block
    private final int xPixel(int x)
    {
        return(Math.round(1 + (x * this.dX())));
    }
    
    // the y pixel coord of the top of a block
    private final int yPixel(int y)
    {
        return(Math.round(this.getHeight() -1 - (y + 1) * this.dY()));
    }


    /**
     * Draws the current board with a 1 pixel border around the whole thing.
     *  Uses the pixel helpers above to map board coords to pixel coords.
     *  Draws rows that are filled all the way across in green.
    */
    public void paintComponent(Graphics g)
    {
        // draw a rect around the whole thing
        g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
        
        // draw the line separating the top
        int spacerY = this.yPixel(this.board.getHeight() - TOP_SPACE - 1);
        g.drawLine(0, spacerY, this.getWidth() - 1, spacerY);


        // check if we are drawing with clipping
        Rectangle clip = null;
        if (DRAW_OPTIMIZE)
        {
            clip = g.getClipBounds();
        }
        
        
        // Factor a few things out to help the optimizer
        final int dx = Math.round(this.dX()-2);
        final int dy = Math.round(this.dY()-2);
        final int bWidth = this.board.getWidth();
        final int bHeight = this.board.getHeight();

        int x, y;
        // Loop through and draw all the blocks left-right, bottom-top
        for (x = 0; x < bWidth; x++)
        {
            int left = this.xPixel(x);   // the left pixel
            
            // right pixel (useful for clip optimization)
            int right = this.xPixel(x + 1) -1;
            
            // skip this x if it is outside the clip rect
            if (DRAW_OPTIMIZE && clip != null)
            {
                if ((right < clip.x) || (left >= (clip.x + clip.width)))
                {
                    continue;
                }
            }
            
            // draw from 0 up to the col height
            final int yHeight = this.board.getColumnHeight(x);
            for (y = 0; y < yHeight; y++)
            {
                if (this.board.getGrid(x, y))
                {
                    final boolean filled = (board.getRowWidth(y) == bWidth);
                    if (filled)
                    {
                        g.setColor(Color.GREEN);
                    }
                    
                    // +1 to leave a white border
                    g.fillRect(left + 1, this.yPixel(y) + 1, dx, dy);    
                    
                    if (filled)
                    {
                        g.setColor(Color.BLACK);
                    }
                }
            }
        }
    }
    
    
    /**
     * Updates the timer to reflect the current setting of the  speed slider.
    */
    public void updateTimer()
    {
        double value = ((double)this.speed.getValue()) / this.speed.getMaximum();
        this.timer.setDelay((int)(DELAY - (value * DELAY)));
    }
    
    
    /**
     * Creates the panel of UI controls.
     */
    public Container createControlPanel()
    {
        Container panel = Box.createVerticalBox();

        // COUNT
        countLabel = new JLabel("0");
        panel.add(countLabel);
        
        // TIME 
        timeLabel = new JLabel(" ");
        panel.add(timeLabel);

        panel.add(Box.createVerticalStrut(12));
        
        // START button
        this.startButton = new JButton("Start");
        panel.add(this.startButton);
        this.startButton.addActionListener( new StartActionListener());
        
        // STOP button
        this.stopButton = new JButton("Stop");
        panel.add(this.stopButton);
        this.stopButton.addActionListener( new StopActionListener());
        
        this.enableButtons();
        
        JPanel row = new JPanel();
        
        // SPEED slider
        panel.add(Box.createVerticalStrut(12));
        row.add(new JLabel("Speed:"));
        this.speed = new JSlider(0, 200, 75);    // min, max, current
        this.speed.setPreferredSize(new Dimension(100,15));
        if (this.testMode)
        {
            this.speed.setValue(200);  // max for test mode
        }
        
        this.updateTimer();
        row.add(this.speed);
        
        panel.add(row);
        this.speed.addChangeListener( new UpdateTimerChangeListener());
        
        
        return panel;
    }
    
    private class LeftActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            tick(LEFT);
        }
    }
    
    private class RightActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            tick(RIGHT);
        }
    }
    
    private class DownActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            tick(DOWN);
        }
    }
    
    private class DropActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            tick(DROP);
        }
    }
    
    private class RotateActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            tick(ROTATE);
        }
    }
    
    private class StartActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            startGame();
        }
    }
    
    private class StopActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            stopGame();
        }
    }
    
    private class UpdateTimerChangeListener implements ChangeListener
    {
        public void stateChanged(ChangeEvent e)
        {
            updateTimer();
        }
    }
}
