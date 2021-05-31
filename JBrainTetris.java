import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;

public class JBrainTetris extends JTetris {

    private ArrayList<Brain> brains;
    private Brain currentBrain;
    public boolean brainEnabled;

    private Move bestMove;

    JBrainTetris(int width, int height) {
        super(width, height);
        this.brainEnabled = false;
    }

    /**
     * Creates the panel of UI controls.
     */
    @Override
    public Container createControlPanel() {
        Container panel = super.createControlPanel();

        JComboBox brains = new JComboBox(BrainFactory.createBrains().toArray(new Brain[0]));
        this.currentBrain = (Brain) brains.getSelectedItem();
        brains.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                currentBrain = (Brain) cb.getSelectedItem();
                System.out.println("Current Brain: " + currentBrain);
            }
        });

        panel.add(brains);

        JButton enable = new JButton("Enable Brain");
        enable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                random = new Random();
                brainEnabled = !brainEnabled;

                if (gameOn) tick(DROP);
                pickNextPiece();

                if (brainEnabled) enable.setText("Disable Brain");
                else enable.setText("Enable Brain");
            }
        });

        panel.add(enable);

        return panel;
    }

    /**
     * Selects the next piece to use using the random generator set in startGame().
     */
    @Override
    public Piece pickNextPiece() {
        int pieceNum = (int) (this.pieces.length * this.random.nextDouble());

        if (this.brainEnabled) {
            this.bestMove = this.currentBrain.bestMove(this.board, this.pieces[pieceNum], 16);
            System.out.println("Best Move: (" + this.bestMove.getX() + ", " + this.bestMove.getY() + ")");
        }

        return this.pieces[pieceNum];
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
    @Override
    public void tick(int verb) {
        if (verb == DOWN && this.brainEnabled && this.bestMove != null) {
            if (this.bestMove.getPiece().nextRotation() != this.currentPiece.nextRotation()) tick(ROTATE);
            if (this.bestMove.getX() < this.currentX) tick(LEFT);
            else if (this.bestMove.getX() > this.currentX) tick(RIGHT);
            //else tick(DROP);
        }

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
            System.out.println("Landed");
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
                System.out.println("New piece added.");
                this.addNewPiece();
            }
        }

        // Note if the player made a successful non-DOWN move --
        //      used to detect if the piece has landed on the next tick()
        this.moved = (!failed && verb!=DOWN);
    }
}
