package dev.ultreon.quantum.switchgdx;

import com.badlogic.gdx.Gdx;
import com.thelogicmaster.switchgdx.SwitchApplication;
import dev.ultreon.quantum.GamePlatform;
import dev.ultreon.quantum.async.AsyncExecutor;
import dev.ultreon.quantum.network.BaseSocket;
import dev.ultreon.quantum.resource.ResourceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SwitchPlatform implements GamePlatform {
    @Override
    public void loadResources(@NotNull ResourceManager resourceManager) {
        resourceManager.loadFromAssetsTxt(Gdx.files.internal("assets.txt"));
    }

    @Override
    public void dispose() {

    }

    @Override
    public boolean isMobile() {
        return false;
    }

    @Override
    public void nextFrame() {

    }

    @Override
    public boolean isSwitchGDX() {
        return true;
    }

    @Override
    public boolean isSwitch() {
        return SwitchApplication.isSwitch;
    }

    @Override
    public boolean isUWP() {
        return SwitchApplication.isUWP;
    }

    @Override
    public boolean isGLES3() {
        return false;
    }

    @Override
    public boolean isGLES2() {
        return true;
    }

    @Override
    public boolean isGL46() {
        return false;
    }

    @Override
    public boolean isGL45() {
        return false;
    }

    @Override
    public boolean isGL44() {
        return false;
    }

    @Override
    public boolean isGL43() {
        return false;
    }

    @Override
    public boolean isGL42() {
        return false;
    }

    @Override
    public boolean isGL41() {
        return false;
    }

    @Override
    public boolean isGL40() {
        return false;
    }

    @Override
    public boolean isGL33() {
        return false;
    }

    @Override
    public boolean isGL32() {
        return false;
    }

    @Override
    public boolean isGL31() {
        return false;
    }

    @Override
    public boolean isGL30() {
        return false;
    }

    @Override
    public boolean isGL20() {
        return false;
    }

    @Override
    public boolean isWebGL3() {
        return false;
    }

    @Override
    public boolean isWebGL2() {
        return false;
    }

    @Override
    public boolean isClient() {
        return true;
    }

    @Override
    public boolean isServer() {
        return false;
    }

    @Override
    public boolean isDebug() {
        return false;
    }

    @Override
    public boolean isWeb() {
        return false;
    }

    @Override
    public int cpuCores() {
        return 4;
    }

    @Override
    public void halt(int i) {
        System.exit(i);
    }

    @Override
    public @Nullable BaseSocket createClientSocket(@NotNull String asString) {
        return null;
    }

    @Override
    public boolean isDesktop() {
        return false;
    }

    @Override
    public boolean isMac() {
        return false;
    }

    @Override
    public boolean isWindows() {
        return false;
    }

    @Override
    public boolean isLinux() {
        return false;
    }

    @Override
    public boolean isAndroid() {
        return false;
    }

    @Override
    public boolean isIos() {
        return false;
    }

    @Override
    public void yield() {
        Thread.yield();
    }

    @Override
    public @NotNull AsyncExecutor createAsyncExecutor(int maxConcurrent, @NotNull String name) {
        return new SwitchAsyncExecutor(maxConcurrent, name);
    }

    @Override
    public void sleep(int i) {
        try {
            Thread.sleep(i);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
