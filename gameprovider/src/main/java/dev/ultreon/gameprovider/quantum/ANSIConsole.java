package dev.ultreon.gameprovider.quantum;

public class ANSIConsole {

    // Define input stream
    private static final java.io.InputStream stdin = System.in;

    public static void print(String message) {
        System.out.print(message);
    }

    public static void println(String message) {
        System.out.println(message);
    }

    public static void moveCursor(int x, int y) {
        println("\u001B[" + (y + 1) + ";" + (x + 1) + "H");
    }

    public static void moveCursorUp(int count) {
        println("\u001B[" + count + "A");
    }

    public static void moveCursorDown(int count) {
        println("\u001B[" + count + "B");
    }

    public static void moveCursorRight(int count) {
        println("\u001B[" + count + "C");
    }

    public static void moveCursorLeft(int count) {
        println("\u001B[" + count + "D");
    }

    public static void clear() {
        println(ANSI.CLEAR_SCREEN);
    }

    public static void clearLine() {
        println(ANSI.CLEAR_LINE);
    }

    public static void hideCursor() {
        println("\u001B[?25l");
    }

    public static void showCursor() {
        println("\u001B[?25h");
    }

    public static void reset() {
        println(ANSI.RESET);
    }

    public static void bold() {
        println(ANSI.BOLD);
    }

    public static void dim() {
        println(ANSI.DIM);
    }

    public static void italic() {
        println(ANSI.ITALIC);
    }

    public static void underline() {
        println(ANSI.UNDERLINE);
    }

    public static void blink() {
        println(ANSI.BLINK);
    }

    public static void reverse() {
        println(ANSI.REVERSE);
    }

    public static void hidden() {
        println(ANSI.HIDDEN);
    }

    public static void color(int r, int g, int b) {
        println("\u001B[38;2;" + r + ";" + g + ";" + b + "m");
    }

    public static void resetFormat() {
        println("\u001B[0m");
    }

    public static void resetColor() {
        println("\u001B[39m");
    }

    public static void resetBackground() {
        println("\u001B[49m");
    }

    public static void background(int r, int g, int b) {
        println("\u001B[48;2;" + r + ";" + g + ";" + b + "m");
    }
}
