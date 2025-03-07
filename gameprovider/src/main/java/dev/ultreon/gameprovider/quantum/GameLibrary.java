/*
 * Copyright 2016 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ultreon.gameprovider.quantum;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.game.LibClassifier.LibraryType;

public enum GameLibrary implements LibraryType {
  QUANTUM_VXL_LWJGL3(EnvType.CLIENT, "dev/ultreon/quantum/lwjgl3/Lwjgl3Launcher.class"),
  QUANTUM_VXL_CLIENT(EnvType.CLIENT, "dev/ultreon/quantum/client/QuantumVoxel.class"),
  QUANTUM_VXL_COMMOn("dev/ultreon/quantum/server/QuantumVoxelServer.class"),
  QUANTUM_VXL_DEDICATED_SERVER(EnvType.SERVER, "dev/ultreon/quantum/dedicated/HeadlessLauncher.class"),
  LIBGDX("com/badlogic/gdx/Gdx.class"),
  KOTLIN("kotlin/jvm/internal/DefaultConstructorMarker.class");

  private final EnvType env;
  private final String[] paths;

  GameLibrary(String... paths) {
    this(null, paths);
  }

  GameLibrary(EnvType env, String... paths) {
    this.env = env;
    this.paths = paths;
  }

  @Override
  public boolean isApplicable(EnvType env) {
    return this.env == null || this.env == env;
  }

  @Override
  public String[] getPaths() {
    return paths;
  }

  public static final GameLibrary[] GAME = new GameLibrary[] { QUANTUM_VXL_LWJGL3, QUANTUM_VXL_DEDICATED_SERVER };
  public static final GameLibrary[] LOGGING = new GameLibrary[] {  };
}
