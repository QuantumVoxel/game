package dev.ultreon.gameprovider.quantum;

import net.fabricmc.loader.impl.util.log.LogCategory;
import net.fabricmc.loader.impl.util.log.LogHandler;
import net.fabricmc.loader.impl.util.log.LogLevel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A log handler for {@link QuantumVxlGameProvider} that uses the same logger as the one from the game itself.
 *
 * @author <a href="https://github.com/XyperCode">Qubilux</a>
 * @since 0.1.0
 */
@SuppressWarnings("unused")
public class QuantumVxlLogHandler implements LogHandler {

    // Logger instance for logging
    private static final Lwjgl3Logger LOGGER;

    static {
        try {
            Files.createDirectories(Path.of("logs"));
            LOGGER = new Lwjgl3Logger("FabricLoader");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Log a message with the specified level, category, message, exception, and other flags.
     *
     * @param time          The time of the log event
     * @param level         The log level
     * @param category      The category of the log
     * @param msg           The message to log
     * @param exc           The exception to log
     * @param fromReplay    Flag indicating if log is from a replay
     * @param wasSuppressed Flag indicating if log was suppressed
     */
    @Override
    public void log(long time, LogLevel level, LogCategory category, String msg, Throwable exc, boolean fromReplay, boolean wasSuppressed) {
        // Compute or get the marker for the log category
        switch (level) {
            case INFO:
                LOGGER.info(msg);
                break;
            case WARN:
                LOGGER.warn(msg);
                break;
            case DEBUG:
                LOGGER.debug(msg);
                break;
            case ERROR:
                LOGGER.error(msg);
                break;
            case TRACE:
                LOGGER.trace(msg);
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Check if logging is enabled for the specified level and category.
     *
     * @param level    The log level
     * @param category The log category
     * @return True if logging is enabled, false otherwise
     */
    @Override
    public boolean shouldLog(LogLevel level, LogCategory category) {
        return true;
    }

    /**
     * Close method for any necessary cleanup operations.
     */
    @Override
    public void close() {
        // No cleanup needed at the moment
    }
}
