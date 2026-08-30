package ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.*;
import game.*;


/** Class to represent the About dialog box */
class AboutDialog extends JDialog{
    public AboutDialog(GameWindow principal){
        super(principal, "About Hexodus", true);
        setSize(300, 350);
        setResizable(false);
        
        Container panel = getContentPane();
        panel.setLayout(null);
        
        JPanel options = new JPanel(); 
        options.setBounds(10, 10, 280, 280);
        panel.add(options);
        options.setLayout(new FlowLayout());
        JLabel title = new JLabel("<html><br><br><center><font size=+4>" + 
                "<b>Hexodus</b></font><br>versin 1.0</center>");

        JLabel text = new JLabel("<html><br><center>Copyright © 2006 - 2008 " + 
                "Pablo Torrecilla<br>GNU General Public License." + "<br><br>" +
                "pau@nosololinux.com</center>");

        options.add(title);
        options.add(text);
        
        JButton Ok = new JButton();        
        Ok.addActionListener(new AcceptHandler());
        Ok.setBounds(190, 290, 100, 30);
        Ok.setText("Close");
        panel.add(Ok);
    }
    
    /** Listens for the "close" button event to close the window */
    class AcceptHandler implements ActionListener{
        public void actionPerformed(ActionEvent e){    
            dispose();
        }
    }
}

/** Class that represents a match and the window used to provide
 *  support for the new game, maintaining interaction with the user */
