import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;

public class JBrainTetris extends JTetris {

    private ArrayList<Brain> brains;
    private Brain currentBrain;
    private boolean brainEnabled;

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
        brains.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                Brain selectedBrain = (Brain)cb.getSelectedItem();
                updateBrain(selectedBrain);
            }
        });

        panel.add(brains);

        JButton enable = new JButton("Enable Brain");
        enable.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateEnabled(true);

            }
        });

        panel.add(enable);

        return panel;
    }

    public void updateBrain(Brain newBrain) {
        this.currentBrain = newBrain;
    }
    public void updateEnabled(boolean newValue) {
        this.brainEnabled = newValue;
    }
}
