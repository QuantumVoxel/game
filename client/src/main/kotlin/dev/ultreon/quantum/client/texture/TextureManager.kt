package dev.ultreon.quantum.client.texture

import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Pixmap.Format
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.PixmapPacker
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Disposable
import dev.ultreon.quantum.client.QuantumVoxel
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.resource.*
import dev.ultreon.quantum.util.NamespaceID
import ktx.assets.disposeSafely

/**
 * Manages textures and texture atlases used in the application.
 *
 * The [TextureManager] is responsible for initializing, registering, and managing
 * texture atlases, as well as safely disposing of texture resources when no longer needed.
 * It provides fallback textures for missing resources, preventing application crashes due
 * to missing texture data.
 *
 * The fallback texture is used when a texture or atlas cannot be found, providing
 * a mechanism to prevent runtime errors caused by missing resources.
 *
 * @constructor
 * Creates a new instance of [TextureManager] with the given [ResourceManager].
 *
 * @property resourceManager The resource manager used to load and manage resources.
 */
class TextureManager(val resourceManager: ResourceManager) : Disposable {
  private val atlases: MutableMap<String, TextureAtlas> = HashMap()
  private lateinit var texturesDir: ResourceDirectory
  private val packers: MutableMap<String, PixmapPacker> = HashMap()
  private val warns: MutableSet<NamespaceID> = HashSet()
  private val atlasWarns: MutableSet<String> = HashSet()

  private var fallbackTexture: TextureRegion? = null

  /**
   * Initializes the textures category and sets up a fallback texture to be used when
   * a requested texture is not found in the atlases.
   *
   * This method retrieves the `textures` category from the `resourceManager` and assigns it to
   * the `texturesCategory` property. It also creates a default fallback texture region using
   * a temporary pixmap to draw a small 2x2 texture with a specific color.
   *
   * Ensures that the fallback texture can be utilized when handling missing textures
   * in the texture management system.
   */
  fun init() {
    texturesDir = resourceManager["textures"]?.asDirOrNull()?.asDirectoryOrNull() ?: run {
      logger.error("Textures directory not found")
      return
    }
    fallbackTexture = TextureRegion(Pixmap(2, 2, Format.RGB888).let { pixmap ->
      pixmap.setColor(1f, 0.5f, 0.0f, 1f)
      pixmap.drawPixel(0, 0)
      pixmap.drawPixel(1, 1)
      QuantumVoxel {
        Texture(pixmap).also { _ ->
          pixmap.disposeSafely()
        }
      }
    }, 0, 0, 2, 2)
  }

  /**
   * Registers a new texture atlas with the specified name.
   *
   * This method registers a new atlas in the texture category
   * system by associating it with a newly created `TextureCategory` instance.
   * This ensures that the atlas is properly tracked and available for resource management.
   *
   * @param name The unique identifier for the texture atlas to be registered.
   */
  fun registerAtlas(name: String) {
    val skylineStrategy = PixmapPacker.SkylineStrategy()
    packers[name] = PixmapPacker(2048, 2048, Format.RGBA8888, 0, false, skylineStrategy)
  }

  /**
   * Packs all registered texture atlases by processing their associated resources.
   *
   * This method ensures that all texture resources are properly packed into texture atlases
   * and made available for efficient rendering within the application.
   */
  fun pack() {
    packers.forEach { (name, packer) ->
      logger.debug("Packing atlas: $name")
      texturesDir[name]?.asDirOrNull()?.forEach { resources ->
        if (resources is ResourceLeaf) {
          if (resources.isEmpty()) {
            logger.warn("Texture group empty: $name")
            return
          }
          val resource = resources.last()
          val location = resource.location
          packer.pack("$location", Pixmap(resource.data, 0, resource.data.size))
        } else if (resources is ResourceDirectory) {
          pack(packer, resources)
        }
      } ?: run {
        logger.warn("Texture group not found: $name")
        return
      }

      QuantumVoxel {
        val generateTextureAtlas =
          packer.generateTextureAtlas(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest, false)

        atlases[name] = generateTextureAtlas
        packer.dispose()
      }
    }

    packers.clear()
  }

  private fun pack(packer: PixmapPacker, resources: ResourceDirectory) {
    for (resource in resources) {
      if (resource is ResourceLeaf) {
        val last = resource.last()
        packer.pack("${last.location}", Pixmap(last.data, 0, last.data.size))
      } else if (resource is ResourceDirectory) {
        pack(packer, resource)
      }
    }
  }

  /**
   * Retrieves a texture region associated with the specified `NamespaceID`.
   *
   * This operator function attempts to locate the texture within its relevant atlas
   * by extracting the atlas name from the texture's path. If the atlas is not found,
   * a fallback texture will be used, and a warning will be logged only once per missing atlas.
   *
   * If the atlas exists but the specific texture region cannot be located,
   * the fallback texture will also be returned, and a warning will be logged only once per missing texture.
   *
   * @param texture The `NamespaceID` representing the texture to retrieve.
   * @return The corresponding `TextureRegion` if found, or the fallback texture if the specified texture or atlas is missing.
   */
  operator fun get(texture: NamespaceID): TextureRegion {
    check(texture.path.startsWith("textures/")) { "Not starting with textures category!" }
    val location = texture.mapPath { it.substringAfter("textures/") }
    val atlas = atlases[location.path.split("/")[0]] ?: run {
      if (location.path.split("/")[0] !in atlasWarns) {
        logger.warn("Atlas not found: ${location.path.split("/")[0]}")
        atlasWarns += location.path.split("/")[0]
      }
      return fallbackTexture!!
    }
    return atlas.findRegion("$texture") ?: run {
      if (location !in warns) {
        logger.warn("Texture not found: $texture")
        warns += location
      }
      fallbackTexture!!
    }
  }

  override fun dispose() {
    atlases.forEach { (_, atlas) ->
      atlas.disposeSafely()
    }

    fallbackTexture?.texture.disposeSafely()

    packers.forEach { (_, packer) ->
      packer.disposeSafely()
    }
  }
}
