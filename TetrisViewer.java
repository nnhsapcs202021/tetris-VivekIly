import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * Viewer class that creates and displays the window for the Tetris game
 *
 * @author gcschmit
 * @version 24 January 2020
 */
public class TetrisViewer
{
    /**
     * Creates a window, installs the JTetris or JBrainTetris, checks the
     *      testMode state, install the controls in the EAST.
    */
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("Tetris 2020");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JComponent container = (JComponent)frame.getContentPane();
        container.setLayout(new BorderLayout());
            
        final int PIXELS = 16;
        JTetris tetris = new JTetris((JTetris.WIDTH * PIXELS) + 2,
                (JTetris.HEIGHT + JTetris.TOP_SPACE) * (PIXELS + 2));
        
        
        container.add(tetris, BorderLayout.CENTER);


        if (args.length != 0 && args[0].equals("test"))
        {
            tetris.testMode = true;
        }
        
        Container panel = tetris.createControlPanel();
        
        // Add the quit button last so it's at the bottom
        panel.add(Box.createVerticalStrut(12));
        JButton quit = new JButton("Quit");
        panel.add(quit);
        quit.addActionListener( new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                System.exit(0);
            }
        });
        
        
        container.add(panel, BorderLayout.EAST);
        frame.pack();
        frame.setVisible(true);
    }
}
