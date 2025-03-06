package dev.ultreon.gameprovider.quantum;

/**
 * A class that provides information about the current platform.
 *
 * @since 0.1.0
 */
public class PlatformOS {
    public static final boolean isWindows = System.getProperty("os.name").contains("Windows");
    public static final boolean isLinux = System.getProperty("os.name").contains("Linux") || System.getProperty("os.name").contains("FreeBSD");
    public static final boolean isMac = System.getProperty("os.name").contains("Mac");

    private PlatformOS() {
        // Prevent instantiation
    }
}
