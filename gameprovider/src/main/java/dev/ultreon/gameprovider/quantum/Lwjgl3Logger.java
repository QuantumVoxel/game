package dev.ultreon.gameprovider.quantum;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Lwjgl3Logger {
    private final String name;
    private final String nerdName;
    private final RandomAccessFile file;
    private final String newline = System.lineSeparator();

    private String time() {
        return " " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " ";
    }

    public Lwjgl3Logger(String inName) throws IOException {
        name = inName.length() > 15 ? inName.substring(0, 12) + "..." : inName;
        nerdName = String.format(" %-" + Math.max(name.length(), 15) + "s ", name);
        file = new RandomAccessFile("logs/" + inName + ".txt", "rw");
        file.setLength(0);
    }

    public void info(String message) {
        synchronized (this) {
            try {
                for (String msg : message.split("\n")) {
                    msg = " " + msg;
                    System.out.println(ANSI.BOLD + ANSI.BG_BRIGHT_BLACK + ANSI.FG_BRIGHT_WHITE + time() + ANSI.RESET + ANSI.BG_BLUE + ANSI.FG_BRIGHT_WHITE + " INFO  " + ANSI.RESET + ANSI.FG_BLUE + ANSI.BG_BRIGHT_BLACK + ANSI.FG_BLUE + nerdName + ANSI.RESET + ANSI.FG_WHITE + msg + ANSI.RESET);
                    file.write((time() + " | ℹ️ INFO: " + msg + newline).getBytes(StandardCharsets.UTF_8));
                }
                file.getFD().sync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void error(String message) {
        synchronized (this) {
            try {
                for (String msg : message.split("\n")) {
                    msg = " " + msg;
                    System.out.println(ANSI.BOLD + ANSI.BG_BRIGHT_BLACK + ANSI.FG_BRIGHT_WHITE + time() + ANSI.RESET + ANSI.BG_RED + ANSI.FG_BRIGHT_WHITE + " ERROR " + ANSI.RESET + ANSI.FG_RED + ANSI.BG_BRIGHT_BLACK + ANSI.FG_RED + nerdName + ANSI.RESET + ANSI.FG_WHITE + msg + ANSI.RESET);
                    file.write((time() + " | ❌ ERROR: " + msg + newline).getBytes(StandardCharsets.UTF_8));
                }
                file.getFD().sync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void warn(String message) {
        synchronized (this) {
            try {
                for (String msg : message.split("\n")) {
                    msg = " " + msg;
                    System.out.println(ANSI.BOLD + ANSI.BG_BRIGHT_BLACK + ANSI.FG_BRIGHT_WHITE + time() + ANSI.RESET + ANSI.BG_YELLOW + ANSI.FG_BRIGHT_WHITE + " WARN  " + ANSI.RESET + ANSI.FG_YELLOW + ANSI.BG_BRIGHT_BLACK + ANSI.FG_YELLOW + nerdName + ANSI.RESET + ANSI.FG_WHITE + msg + ANSI.RESET);
                    file.write((time() + " | ⚠️ WARN: " + msg + newline).getBytes(StandardCharsets.UTF_8));
                }
                file.getFD().sync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void debug(String message) {
        synchronized (this) {
            try {
                for (String msg : message.split("\n")) {
                    msg = " " + msg;
                    System.out.println(ANSI.BOLD + ANSI.BG_BRIGHT_BLACK + ANSI.FG_BRIGHT_WHITE + time() + ANSI.RESET + ANSI.BG_CYAN + ANSI.FG_BRIGHT_BLACK + " DEBUG " + ANSI.RESET + ANSI.FG_CYAN + ANSI.BG_BRIGHT_BLACK + ANSI.FG_CYAN + nerdName + ANSI.RESET + ANSI.FG_WHITE + msg + ANSI.RESET);
                    file.write((time() + " | 🐞 DEBUG: " + msg + newline).getBytes(StandardCharsets.UTF_8));
                }
                file.getFD().sync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void trace(String message) {
        synchronized (this) {
            try {
                for (String msg : message.split("\n")) {
                    msg = " " + msg;
                    System.out.println(ANSI.BOLD + ANSI.BG_BRIGHT_BLACK + ANSI.FG_BRIGHT_WHITE + time() + ANSI.RESET + ANSI.BG_WHITE + ANSI.FG_BRIGHT_WHITE + " TRACE " + ANSI.RESET + ANSI.FG_WHITE + ANSI.BG_BRIGHT_BLACK + ANSI.FG_WHITE + nerdName + ANSI.RESET + ANSI.FG_WHITE + msg + ANSI.RESET);
                    file.write((time() + " | 🧬 TRACE: " + msg + newline).getBytes(StandardCharsets.UTF_8));
                }
                file.getFD().sync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void close() throws IOException {
        file.close();
    }
}
