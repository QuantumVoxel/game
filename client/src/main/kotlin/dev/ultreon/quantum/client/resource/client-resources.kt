package dev.ultreon.quantum.client.resource

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.ExperimentalApi
import dev.ultreon.quantum.resource.Resource
import ktx.assets.disposeSafely
import java.nio.charset.Charset

private val jsonReader = JsonReader()

/**
 * Opens the resource as a [Texture].
 */
fun Resource.texture(): Texture {
  val pixmap = pixmap()
  val texture = Texture(pixmap)
  pixmap.disposeSafely()
  texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
  return texture
}

/**
 * Opens the resource as a [Pixmap].
 */
fun Resource.pixmap(): Pixmap = Pixmap(data, 0, data.size)

/**
 * Opens the resource as a [String].
 *
 * @param charset The character set to use when decoding the resource.
 */
fun Resource.text(charset: Charset = Charsets.UTF_8): String = String(data, charset)

/**
 * Opens the resource as a [JsonValue].
 */
fun Resource.json(): JsonValue = jsonReader.parse(text())

/**
 * Opens the resource as a [JsonValue].
 *
 * @param charset The character set to use when decoding the resource.
 */
fun Resource.json(charset: Charset): JsonValue = jsonReader.parse(text(charset))

/**
 * Opens the resource as a [Material].
 */
@ExperimentalApi
fun Resource.material(): Material {
  val json = json()

  return Material(
    location.toString()
  ).apply {
//    if ("diffuseColor" in json) diffuse(json["diffuseColor"].color)
  }
}

private fun JsonValue.color(out: Color): Color {
  if (isString) {
    out.set(this.asString())
  }
  if (isNumber) {
    out.set(this.asInt())
  }
  if (isArray) {
    val asFloatArray = this.asFloatArray()
    out.set(
      if (asFloatArray.isNotEmpty()) asFloatArray[0] else 0f,
      if (asFloatArray.size >= 2) asFloatArray[1] else 0f,
      if (asFloatArray.size >= 3) asFloatArray[2] else 0f,
      if (asFloatArray.size >= 4) asFloatArray[3] else 1f
    )
  }
  return out
}

private fun Color.set(hex: String): Color {
  if (!hex.startsWith("#")) throw GdxRuntimeException("Invalid color: $hex")
  val color = hex.substring(1)
  if (color.length == 4 || color.length == 3) {
    val r = color.substring(0, 1).repeat(2).toInt(16)
    val g = color.substring(1, 2).repeat(2).toInt(16)
    val b = color.substring(2, 3).repeat(2).toInt(16)
    val a = if (color.length == 4) color.substring(3, 4).repeat(2).toInt(16) else 255
    this.set(r / 255f, g / 255f, b / 255f, a / 255f)
    return this
  }
  if (color.length != 6 && color.length != 8) throw GdxRuntimeException("Invalid color: $hex")
  val r = color.substring(0, 2).toInt(16)
  val g = color.substring(2, 4).toInt(16)
  val b = color.substring(4, 6).toInt(16)
  val a =if (color.length == 8) color.substring(6, 8).toInt(16) else 255
  this.set(r / 255f, g / 255f, b / 255f, a / 255f)
  return this
}
