import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;

public class JBrainTetris extends JComponent {
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

    JBrainTetris(int width, int height) {
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
                new JBrainTetris.LeftActionListener(), "left", KeyStroke.getKeyStroke('4'), WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(
                new JBrainTetris.LeftActionListener(), "left", KeyStroke.getKeyStroke('j'), WHEN_IN_FOCUSED_WINDOW);


        // RIGHT
        registerKeyboardAction(
                new JBrainTetris.RightActionListener(), "right", KeyStroke.getKeyStroke('6'), WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(
                new JBrainTetris.RightActionListener(), "right", KeyStroke.getKeyStroke('l'), WHEN_IN_FOCUSED_WINDOW);


        // ROTATE
        registerKeyboardAction(
                new JBrainTetris.RotateActionListener(), "rotate", KeyStroke.getKeyStroke('5'), WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(
                new JBrainTetris.RotateActionListener(), "rotate", KeyStroke.getKeyStroke('k'), WHEN_IN_FOCUSED_WINDOW);


        // DROP
        registerKeyboardAction(
                new JBrainTetris.DropActionListener(), "drop", KeyStroke.getKeyStroke('0'), WHEN_IN_FOCUSED_WINDOW);
        registerKeyboardAction(
                new JBrainTetris.DropActionListener(), "drop", KeyStroke.getKeyStroke('n'), WHEN_IN_FOCUSED_WINDOW);


        // Create the Timer object and have it send tick(DOWN) periodically
        this.timer = new javax.swing.Timer(DELAY, new JBrainTetris.DownActionListener());
    }

    /**
     * Called to change the position of the current piece.
     * Each key press call this once with the verbs LEFT RIGHT ROTATE DROP for
     * the user moves, and the timer calls it with the verb DOWN to move
     * the piece down one square.
     * <p>
     * Before this is called, the piece is at some location in the board.
     * This advances the piece to be at its next location.
     * <p>
     * Overriden by the brain when it plays.
     */
    public void tick(int verb) {
        if (!this.gameOn) {
            return;
        }

        if (this.currentPiece != null) {
            this.board.undo();   // remove the piece from its old position
        }

        // Sets the newXXX attributes
        this.computeNewPosition(verb);

        // try out the new position (rolls back if it doesn't work)
        int status = this.setCurrent(this.newPiece, this.newX, this.newY);

        // if row clearing is going to happen, draw the whole board so the green
        //      row shows up
        if (status == Board.PLACE_ROW_FILLED) {
            this.repaint();
        }


        boolean failed = (status >= Board.PLACE_OUT_BOUNDS);

        // if it didn't work, put it back the way it was
        if (failed) {
            if (this.currentPiece != null) {
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
        if (failed && verb == DOWN && !this.moved)   // it's landed
        {
            if (this.board.clearRows()) {
                this.repaint();  // repaint to show the result of the row clearing
            }

            // if the board is too tall, we've lost
            if (this.board.getMaxHeight() > this.board.getHeight() - TOP_SPACE) {
                this.stopGame();
            }
            // Otherwise add a new piece and keep playing
            else {
                this.addNewPiece();
            }
        }

        // Note if the player made a successful non-DOWN move --
        //      used to detect if the piece has landed on the next tick()
        this.moved = (!failed && verb != DOWN);
    }

    private class LeftActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tick(LEFT);
        }
    }

    private class RightActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tick(RIGHT);
        }
    }

    private class DownActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tick(DOWN);
        }
    }

    private class DropActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tick(DROP);
        }
    }

    private class RotateActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            tick(ROTATE);
        }
    }

    private class StartActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            startGame();
        }
    }

    private class StopActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            stopGame();
        }
    }

    private class UpdateTimerChangeListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            updateTimer();
        }
    }
}
