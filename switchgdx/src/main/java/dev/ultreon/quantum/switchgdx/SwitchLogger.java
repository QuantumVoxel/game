package dev.ultreon.quantum.switchgdx;

import dev.ultreon.quantum.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class SwitchLogger implements Logger {
    private final @NotNull String name;

    public SwitchLogger(@NotNull String name) {
        this.name = name;
    }

    @Override
    public void trace(@NotNull String message, @Nullable Object obj) {
        log("[TRACE] ", message);
        sleep();
    }

    private void sleep() {
        // No-op
    }

    @Override
    public void debug(@NotNull String message, @Nullable Object obj) {
        log("[DEBUG] ", message);
        sleep();
    }

    @Override
    public void error(@NotNull String message, @Nullable Object obj) {
        log("[ERROR] ", message);
        sleep();
    }

    @Override
    public void warn(@NotNull String message, @Nullable Object obj) {
        log("[WARN] ", message);
        sleep();
    }

    @Override
    public void info(@NotNull String message, @Nullable Object obj) {
        log("[INFO] ", message);
        sleep();
    }

    @Override
    public void info(@NotNull String message) {
        log("[INFO] ", message);
        sleep();
    }

    @Override
    public void warn(@NotNull String message) {
        log("[WARN] ", message);
        sleep();
    }

    @Override
    public void error(@NotNull String message) {
        log("[ERROR] ", message);
        sleep();
    }

    @Override
    public void debug(@NotNull String message) {
        log("[DEBUG] ", message);
        sleep();
    }

    @Override
    public void trace(@NotNull String message) {
        log("[TRACE] ", message);
        sleep();
    }

    private void log(String prefix, String message) {
        for (String line : message.split("\r\n|\r|\n")) {
            System.out.println("[" + name + "/" + Thread.currentThread().getName() + "] " + prefix + line);
        }
    }
}
