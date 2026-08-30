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


/** Options dialog box for creating a new game */
class OptionsDialog extends JDialog{
    private JRadioButton v1, v2, h1, h2;    // References to the controls
    private JComboBox selDimension;
    private JComboBox selDifficulty;
    private JComboBox selAlgorithm;
    private JCheckBox enableSwap;
    private GameWindow game;

    /** Shows the dialog to create a new match
     *  @param principal    Reference to the main game window */
    public OptionsDialog(GameWindow principal){
        super(principal, "New Game", true);
        setResizable(false);

        game = principal;

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Theme.BACKGROUND);
        content.setBorder(new EmptyBorder(18, 22, 16, 22));
        setContentPane(content);

        /* ---- controls ---- */

        selDimension = new JComboBox();
        for(int i = 0; i < Main.DIMENSIONS.length; i++)
            selDimension.addItem(Main.DIMENSIONS[i] + " x " + Main.DIMENSIONS[i]);
        selDimension.setToolTipText("<html>Thinking time grows steeply with board size.<br>"
            + "8x8 and 9x9 are comfortable on Normal, but take<br>"
            + "minutes per move on Expert and Master.</html>");
        selDimension.setSelectedIndex(restoreIndex(GamePrefs.DIMENSION,
            Main.DEFAULT_DIMENSION_INDEX, selDimension.getItemCount()));

        selDifficulty = new JComboBox();
        selDifficulty.addItem("Normal  \u00b7  searches 1 move ahead");
        selDifficulty.addItem("Expert  \u00b7  searches 2 moves ahead");
        selDifficulty.addItem("Master  \u00b7  searches 3 moves ahead");
        selDifficulty.setToolTipText("Deeper search plays better but takes longer.");
        selDifficulty.setSelectedIndex(restoreIndex(GamePrefs.DIFFICULTY, 0,
            selDifficulty.getItemCount()));

        selAlgorithm = new JComboBox();
        selAlgorithm.addItem("Object-Oriented H-Search");
        selAlgorithm.addItem("Bitmask H-Search");
        selAlgorithm.setToolTipText("<html>Both play identical moves.<br>"
            + "Bitmask H-Search analyses about 5x faster.</html>");
        selAlgorithm.setSelectedIndex(restoreIndex(GamePrefs.ALGORITHM, 1,
            selAlgorithm.getItemCount()));

        enableSwap = new JCheckBox("Enable swap rule");
        enableSwap.setOpaque(false);
        enableSwap.setToolTipText("<html>Lets the second player take over the first<br>"
            + "player's opening move, offsetting the advantage<br>of moving first.</html>");
        enableSwap.setSelected(GamePrefs.get(GamePrefs.SWAP, true));

        ButtonGroup verticalGroup = new ButtonGroup();
        ButtonGroup horizontalGroup = new ButtonGroup();
        v1 = radio("Human");
        v2 = radio("Computer");
        h1 = radio("Human");
        h2 = radio("Computer");
        /* Always opens on human vs computer: computer-vs-computer is a
         * demo mode, chosen deliberately rather than inherited from the
         * previous game. */
        v1.setSelected(true);
        h2.setSelected(true);
        verticalGroup.add(v1);
        verticalGroup.add(v2);
        horizontalGroup.add(h1);
        horizontalGroup.add(h2);

        /* ---- layout: label column, field column ---- */

        GridBagConstraints label = new GridBagConstraints();
        label.gridx = 0;
        label.anchor = GridBagConstraints.LINE_END;
        label.insets = new Insets(4, 0, 4, 10);

        GridBagConstraints field = new GridBagConstraints();
        field.gridx = 1;
        field.fill = GridBagConstraints.HORIZONTAL;
        field.weightx = 1.0;
        field.insets = new Insets(4, 0, 4, 0);

        GridBagConstraints wide = new GridBagConstraints();
        wide.gridx = 0;
        wide.gridwidth = 2;
        wide.fill = GridBagConstraints.HORIZONTAL;
        wide.anchor = GridBagConstraints.LINE_START;

        int row = 0;

        row = section(content, "Board", row, wide, true);
        label.gridy = field.gridy = row++;
        content.add(new JLabel("Size"), label);
        content.add(selDimension, field);
        label.gridy = field.gridy = row++;
        content.add(new JLabel("Difficulty"), label);
        content.add(selDifficulty, field);

        row = section(content, "Engine", row, wide, false);
        label.gridy = field.gridy = row++;
        content.add(new JLabel("Algorithm"), label);
        content.add(selAlgorithm, field);

        row = section(content, "Players", row, wide, false);
        wide.gridy = row++;
        wide.insets = new Insets(1, 0, 1, 0);
        content.add(playerRow(Theme.RED, "Vertical", v1, v2), wide);
        wide.gridy = row++;
        content.add(playerRow(Theme.BLUE, "Horizontal", h1, h2), wide);

        row = section(content, "Rules", row, wide, false);
        wide.gridy = row++;
        wide.insets = new Insets(2, 0, 2, 0);
        content.add(enableSwap, wide);

        /* ---- actions ---- */

        JButton start = new JButton("Start Game");
        start.addActionListener(new AcceptHandler());
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){ dispose(); }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(start);

        wide.gridy = row++;
        wide.insets = new Insets(18, 0, 0, 0);
        wide.anchor = GridBagConstraints.LINE_END;
        content.add(buttons, wide);

        getRootPane().setDefaultButton(start);          // Enter starts the game
        getRootPane().registerKeyboardAction(new ActionListener(){   // Escape closes
                public void actionPerformed(ActionEvent e){ dispose(); }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        pack();
        setLocationRelativeTo(principal);
    }

    /** A radio button that lets the dialog background show through */
    private static JRadioButton radio(String text){
        JRadioButton b = new JRadioButton(text);
        b.setOpaque(false);
        return b;
    }

    /** Adds a small section heading with a hairline rule under it.
     *  @return The next free grid row */
    private static int section(JPanel panel, String title, int row,
            GridBagConstraints wide, boolean first){
        JLabel heading = new JLabel(title.toUpperCase());
        heading.setFont(Theme.SMALL.deriveFont(Font.BOLD));
        heading.setForeground(Theme.TEXT_MUTED);

        wide.gridy = row++;
        wide.insets = new Insets(first ? 0 : 16, 0, 2, 0);
        panel.add(heading, wide);

        JSeparator rule = new JSeparator();
        rule.setForeground(Theme.LINE);
        wide.gridy = row++;
        wide.insets = new Insets(0, 0, 8, 0);
        panel.add(rule, wide);

        wide.insets = new Insets(0, 0, 0, 0);
        return row;
    }

    /** One player's row: a dot in that player's colour, the side they are
     *  connecting, and the human/computer choice. */
    private static JPanel playerRow(final Color colour, String side,
            JRadioButton human, JRadioButton computer){
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        rowPanel.setOpaque(false);

        JComponent dot = new JComponent(){
            public Dimension getPreferredSize(){ return new Dimension(10, 12); }
            protected void paintComponent(Graphics g){
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(colour);
                g2.fillOval(0, 2, 9, 9);
                g2.dispose();
            }
        };
        rowPanel.add(dot);

        JLabel name = new JLabel(side);
        name.setPreferredSize(new Dimension(72, name.getPreferredSize().height));
        rowPanel.add(name);
        rowPanel.add(human);
        rowPanel.add(computer);
        return rowPanel;
    }

    /** Returns a stored combo index, falling back to the default when the
     *  stored value does not fit the current item count
     *  @param key Preference key
     *  @param def Default index
     *  @param itemCount Number of items in the combo */
    private static int restoreIndex(String key, int def, int itemCount){
        int index = GamePrefs.get(key, def);
        if(index < 0 || index >= itemCount) return def;
        return index;
    }

    /** Class to listen for the accept button press */
    class AcceptHandler implements ActionListener{
        public void actionPerformed(ActionEvent e){
            int v, h;
            int dim;
            int difficulty;

            if(v1.isSelected()) v = 0;
            else v = 1;
            if(h1.isSelected()) h = 0;
            else h = 1;

            dim = Main.DIMENSIONS[selDimension.getSelectedIndex()];

            // Get difficulty level (1, 2, or 3)
            difficulty = selDifficulty.getSelectedIndex() + 1;

            /* Must be set before the new Match (and its Simulations/Paths)
             * is created; safe to toggle here because newGame builds a
             * fresh engine and board. */
            heuristics.OptConfig.USE_BITPATH = (selAlgorithm.getSelectedIndex() == 1);

            // Remember the selections for the next game and the next session
            GamePrefs.put(GamePrefs.DIMENSION, selDimension.getSelectedIndex());
            GamePrefs.put(GamePrefs.DIFFICULTY, selDifficulty.getSelectedIndex());
            GamePrefs.put(GamePrefs.ALGORITHM, selAlgorithm.getSelectedIndex());
            GamePrefs.put(GamePrefs.SWAP, enableSwap.isSelected());

            dispose();
            game.newGame(dim, v, h, enableSwap.isSelected(), difficulty);
        }
    }
}

/** Class to represent the About dialog box */
