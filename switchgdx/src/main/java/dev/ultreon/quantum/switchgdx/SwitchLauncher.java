package dev.ultreon.quantum.switchgdx;

import com.thelogicmaster.switchgdx.SwitchApplication;

/**
 * Launches the switch (SwitchGDX) application.
 * This class is the main class for the SwitchGDX application.
 */
public class SwitchLauncher {
    public static void main(String[] args) {
        new SwitchApplication(new SafeLoadWrapper());
    }
}
