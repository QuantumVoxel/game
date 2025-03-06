package dev.ultreon.quantum.client.gui

import com.badlogic.gdx.graphics.g2d.Batch
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.util.NamespaceID

/**
 * Draws a texture with the specified [id] at the specified [x] and [y] coordinates.
 *
 * @param id The id of the texture.
 * @param x The x coordinate of the texture.
 * @param y The y coordinate of the texture.
 */
fun Batch.draw(id: NamespaceID, x: Float, y: Float) {
  draw(quantum.textureManager[id], x, y)
}

/**
 * Draws a texture with the specified [id] at the specified [x] and [y] coordinates with the specified [width] and [height].
 *
 * @param id The id of the texture.
 * @param x The x coordinate of the texture.
 * @param y The y coordinate of the texture.
 * @param width The width of the texture.
 * @param height The height of the texture.
 */
fun Batch.draw(id: NamespaceID, x: Float, y: Float, width: Float, height: Float) {
  draw(quantum.textureManager[id], x, y, width, height)
}

/**
 * Draws a texture with the specified [id] at the specified [x] and [y] coordinates with the specified [width] and [height].
 * Also allows for specifying the [u], [v], [uSize], [vSize], [texWidth], and [texHeight] for the texture.
 *
 * @param id The id of the texture.
 * @param x The x coordinate of the texture.
 * @param y The y coordinate of the texture.
 * @param width The width of the texture.
 * @param height The height of the texture.
 * @param u The x of the texture's UV
 * @param v The y of the texture's UV
 * @param uSize The width of the texture's UV
 * @param vSize The height of the texture's UV
 * @param texWidth The width of the texture
 * @param texHeight The height of the texture
 */
fun Batch.draw(id: NamespaceID, x: Float, y: Float, width: Float, height: Float, u: Float = 0F, v: Float = 0F, uSize: Float = width, vSize: Float = height, texWidth: Float = 256F, texHeight: Float = 256F) {
  val textureRegion = quantum.textureManager[id]
  draw(textureRegion, x, y, (textureRegion.regionX.toFloat() / textureRegion.regionWidth) + (u / texWidth), (textureRegion.regionY.toFloat() / textureRegion.regionHeight) + (v / texHeight), width, height, uSize / texWidth, vSize / texHeight, 0F)
}
