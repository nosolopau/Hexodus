package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 *  Status bar across the top of the window: the current message on the
 *  left, and on the right a turn indicator showing whose move it is as a
 *  coloured dot plus a label.
 *
 *  @author Pau
 *  @version 1.0
 */
class StatusBar extends JPanel {

    private final JLabel message = new JLabel(" ");
    private final TurnDot dot = new TurnDot();
    private final JLabel turnText = new JLabel("");
    private final JPanel controls = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));

    StatusBar(int width) {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);
        setBorder(new EmptyBorder(6, 16, 6, 16));

        message.setFont(Theme.BODY);
        message.setForeground(Theme.TEXT_MUTED);
        add(message, BorderLayout.WEST);

        controls.setOpaque(false);
        add(controls, BorderLayout.CENTER);

        JPanel turn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        turn.setOpaque(false);
        turnText.setFont(Theme.BODY_BOLD);
        turnText.setForeground(Theme.TEXT);
        turn.add(dot);
        turn.add(turnText);
        add(turn, BorderLayout.EAST);
    }

    /** Sets the left-hand message (empty string clears it) */
    void setMessage(String text) {
        message.setText((text == null || text.isEmpty()) ? " " : text);
    }

    /** Updates the turn indicator.
     *  @param vertical True when it is the vertical (red) player's turn
     *  @param label Text to show, e.g. "Your turn" or "Thinking" */
    void setTurn(boolean vertical, String label) {
        dot.setColor(vertical ? Theme.RED : Theme.BLUE);
        turnText.setText(label);
        repaint();
    }

    /** Lays out and repaints synchronously.
     *
     *  Used while the event thread is busy searching: a queued repaint
     *  would not be serviced until the search returned, and without the
     *  explicit layout the message label would keep its previous width and
     *  clip any longer text. */
    void refreshNow() {
        doLayout();
        paintImmediately(0, 0, getWidth(), getHeight());
    }

    /** Adds a transient control (such as the swap button) between the
     *  message and the turn indicator */
    void addControl(Component c) {
        controls.add(c);
        controls.revalidate();
        controls.repaint();
    }

    /** Hides the turn indicator (game over) */
    void clearTurn(String label) {
        dot.setColor(null);
        turnText.setText(label == null ? "" : label);
        repaint();
    }

    /** Small filled circle showing the colour to move */
    private static class TurnDot extends JComponent {
        private Color color;

        TurnDot() {
            setPreferredSize(new Dimension(12, 12));
        }

        void setColor(Color c) {
            color = c;
            repaint();
        }

        protected void paintComponent(Graphics g) {
            if (color == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0, 1, 11, 11);
            g2.dispose();
        }
    }
}
