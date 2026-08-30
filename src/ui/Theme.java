package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import javax.swing.UIManager;

/**
 *  Colours and typography for the interface, kept in one place so the
 *  window chrome stays consistent with the board artwork.
 *
 *  The palette is the board's own: a warm off-white ground, muted red and
 *  blue for the two players, and warm greys for text.
 *
 *  @author Pau
 *  @version 1.0
 */
final class Theme {

    /** Window ground; matches the margin baked into the board artwork */
    static final Color BACKGROUND   = new Color(0xF7, 0xF5, 0xF1);
    /** Slightly deeper tone for bars that need separating from the ground */
    static final Color SURFACE      = new Color(0xF1, 0xEE, 0xE8);
    /** Hairline separators */
    static final Color LINE         = new Color(0xE0, 0xDA, 0xD0);
    /** Primary text */
    static final Color TEXT         = new Color(0x4A, 0x44, 0x3C);
    /** Secondary text: timings, hints */
    static final Color TEXT_MUTED   = new Color(0x8A, 0x81, 0x75);

    /** Vertical player (matches the red stones and board edges) */
    static final Color RED          = new Color(0x8C, 0x3B, 0x2E);
    /** Horizontal player (matches the blue stones and board edges) */
    static final Color BLUE         = new Color(0x33, 0x54, 0x7A);

    /* Lighter members of the same hues — the board's border colours.
     * Used for washes over the board, where the stone colours are too
     * heavy and would compete with the pieces themselves. */
    static final Color RED_SOFT     = new Color(0xC0, 0x70, 0x5F);
    static final Color BLUE_SOFT    = new Color(0x5B, 0x7F, 0xA6);

    static final Font BODY;
    static final Font BODY_BOLD;
    static final Font SMALL;

    static {
        String family = pickFamily();
        BODY      = new Font(family, Font.PLAIN, 13);
        BODY_BOLD = new Font(family, Font.BOLD, 13);
        SMALL     = new Font(family, Font.PLAIN, 11);
    }

    /** Picks the nicest available UI face, falling back to the logical
     *  sans-serif family when none of the preferred ones is installed. */
    private static String pickFamily() {
        String[] preferred = {"SF Pro Text", "Helvetica Neue", "Segoe UI", "Inter", "Arial"};
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();
        for (String want : preferred)
            for (String have : available)
                if (have.equalsIgnoreCase(want)) return have;
        return Font.SANS_SERIF;
    }

    /** Applies the fonts and colours to the Swing defaults, so dialogs and
     *  menus pick them up without being styled individually. */
    static void apply() {
        String[] fontKeys = {
            "Label.font", "Button.font", "ComboBox.font", "CheckBox.font",
            "RadioButton.font", "Menu.font", "MenuItem.font",
            "CheckBoxMenuItem.font", "RadioButtonMenuItem.font",
            "TitledBorder.font", "ToolTip.font", "OptionPane.font"
        };
        for (String key : fontKeys) UIManager.put(key, BODY);
        UIManager.put("TitledBorder.font", BODY_BOLD);
        UIManager.put("TitledBorder.titleColor", TEXT_MUTED);

        UIManager.put("Label.foreground", TEXT);
        UIManager.put("Panel.background", BACKGROUND);
        UIManager.put("OptionPane.background", BACKGROUND);
        UIManager.put("MenuBar.background", BACKGROUND);
        UIManager.put("ToolTip.background", SURFACE);
        UIManager.put("ToolTip.foreground", TEXT);
    }

    private Theme() {
    }
}
