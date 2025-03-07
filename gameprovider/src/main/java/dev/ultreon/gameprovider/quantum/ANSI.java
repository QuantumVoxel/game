package dev.ultreon.gameprovider.quantum;

/**
 * Provides ANSI escape code constants and utility functions for styling terminal output.
 *
 * This class includes constants for text styles, foreground and background colors,
 * cursor movement, and screen manipulation. It also provides methods for dynamically
 * constructing styled text and enabling ANSI support on Windows platforms.
 */
public class ANSI {
    // ANSI Escape Codes
    private static final String ESC = "\u001B";

    // Text styles
    public static final String RESET = ESC + "[0m";
    public static final String BOLD = ESC + "[1m";
    public static final String DIM = ESC + "[2m";
    public static final String ITALIC = ESC + "[3m";
    public static final String UNDERLINE = ESC + "[4m";
    public static final String BLINK = ESC + "[5m";
    public static final String REVERSE = ESC + "[7m";
    public static final String HIDDEN = ESC + "[8m";

    // Foreground colors
    public static final String FG_BLACK = ESC + "[30m";
    public static final String FG_RED = ESC + "[31m";
    public static final String FG_GREEN = ESC + "[32m";
    public static final String FG_YELLOW = ESC + "[33m";
    public static final String FG_BLUE = ESC + "[34m";
    public static final String FG_MAGENTA = ESC + "[35m";
    public static final String FG_CYAN = ESC + "[36m";
    public static final String FG_WHITE = ESC + "[37m";
    public static final String FG_DEFAULT = ESC + "[39m";

    // Bright foreground colors
    public static final String FG_BRIGHT_BLACK = ESC + "[90m";
    public static final String FG_BRIGHT_RED = ESC + "[91m";
    public static final String FG_BRIGHT_GREEN = ESC + "[92m";
    public static final String FG_BRIGHT_YELLOW = ESC + "[93m";
    public static final String FG_BRIGHT_BLUE = ESC + "[94m";
    public static final String FG_BRIGHT_MAGENTA = ESC + "[95m";
    public static final String FG_BRIGHT_CYAN = ESC + "[96m";
    public static final String FG_BRIGHT_WHITE = ESC + "[97m";

    // Background colors
    public static final String BG_BLACK = ESC + "[40m";
    public static final String BG_RED = ESC + "[41m";
    public static final String BG_GREEN = ESC + "[42m";
    public static final String BG_YELLOW = ESC + "[43m";
    public static final String BG_BLUE = ESC + "[44m";
    public static final String BG_MAGENTA = ESC + "[45m";
    public static final String BG_CYAN = ESC + "[46m";
    public static final String BG_WHITE = ESC + "[47m";
    public static final String BG_DEFAULT = ESC + "[49m";

    // Bright background colors
    public static final String BG_BRIGHT_BLACK = ESC + "[100m";
    public static final String BG_BRIGHT_RED = ESC + "[101m";
    public static final String BG_BRIGHT_GREEN = ESC + "[102m";
    public static final String BG_BRIGHT_YELLOW = ESC + "[103m";
    public static final String BG_BRIGHT_BLUE = ESC + "[104m";
    public static final String BG_BRIGHT_MAGENTA = ESC + "[105m";
    public static final String BG_BRIGHT_CYAN = ESC + "[106m";
    public static final String BG_BRIGHT_WHITE = ESC + "[107m";

    // Cursor control
    public static String moveCursorUp(int lines) {
        return ESC + "[" + lines + "A";
    }

    public static String moveCursorDown(int lines) {
        return ESC + "[" + lines + "B";
    }

    public static String moveCursorForward(int columns) {
        return ESC + "[" + columns + "C";
    }

    public static String moveCursorBack(int columns) {
        return ESC + "[" + columns + "D";
    }

    public static String setCursorPosition(int row, int col) {
        return ESC + "[" + row + ";" + col + "H";
    }

    public static final String CLEAR_SCREEN = ESC + "[2J";
    public static final String CLEAR_LINE = ESC + "[2K";

    /**
     * Styles the given text using ANSI escape codes for formatting.
     *
     * This method allows you to apply one or more text styles (e.g., bold, italic, colored text)
     * to the input string. It concatenates the provided styles, applies them to the text,
     * and appends a reset code to ensure subsequent text output is unaffected.
     *
     * @param text The text to be styled.
     * @param styles A variable number of style codes (e.g., colors, font effects) to apply.
     * @return The styled text as a string with ANSI escape codes applied.
     */
    public static String styleText(String text, String... styles) {
        return String.join("", styles) + text + RESET;
    }
}
