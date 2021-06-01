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

        super.tick(verb);
    }
}
