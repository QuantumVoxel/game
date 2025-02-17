package dev.ultreon.quantum.switchgdx;

import dev.ultreon.quantum.Logger;
import dev.ultreon.quantum.LoggerFactory;
import org.jetbrains.annotations.NotNull;

class SwitchLoggerFactory implements LoggerFactory {
    @Override
    public @NotNull Logger getLogger(@NotNull String name) {
        return new SwitchLogger(name);
    }
}
