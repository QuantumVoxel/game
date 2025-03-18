package dev.ultreon.quantum.client.gui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack
import com.github.tommyettinger.textra.Layout
import com.github.tommyettinger.textra.TypingLabel
import dev.ultreon.quantum.client.QuantumVoxel
import dev.ultreon.quantum.client.quantum
import dev.ultreon.quantum.util.NamespaceID

/**
 * A renderer for GUIs.
 *
 * @param batch The sprite batch to render with.
 * @property font The font to render text with.
 * @constructor Creates a new GUI renderer with the specified parameters.
 * @see SpriteBatch
 */
class GuiRenderer(private val batch: SpriteBatch) {
  private val translation: Vector2 = Vector2()
  val font = QuantumVoxel.instance.font

  private val tmpRegion = TextureRegion()
  private val layout = Layout()

  private fun blit(
    texture: TextureRegion,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    u: Float,
    v: Float,
    uSize: Float,
    vSize: Float,
    texWidth: Int,
    texHeight: Int,
  ) {
    val uDiff = texture.u2 - texture.u
    val vDiff = texture.v2 - texture.v

    tmpRegion.texture = texture.texture
    tmpRegion.u = texture.u + (u / texWidth) * uDiff
    tmpRegion.v = texture.v + (v / texHeight) * vDiff
    tmpRegion.u2 = (u + uSize) / texWidth * uDiff + texture.u
    tmpRegion.v2 = (v + vSize) / texHeight * vDiff + texture.v
    batch.draw(tmpRegion, x, y, width, height)
  }

  private fun drawTexture(
    texture: TextureRegion,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    u: Float = 0F,
    v: Float = 0F,
    uSize: Float,
    vSize: Float,
    texWidth: Int,
    texHeight: Int,
  ) {
    blit(texture, x, y, width, height, u, v, uSize, vSize, texWidth, texHeight)
  }

  /**
   * Draws a texture at the specified position with the specified size.
   *
   * @param texture The texture to draw.
   * @param x The x-coordinate of the texture.
   * @param y The y-coordinate of the texture.
   * @param width The width of the texture.
   * @param height The height of the texture.
   */
  fun drawTexture(
    texture: NamespaceID,
    x: Float,
    y: Float,
    width: Float,
    height: Float
  ) {
    val texture1 = quantum.textureManager[texture]
    drawTexture(
      texture1,
      x,
      y,
      width,
      height,
      0F,
      0F,
      texture1.regionWidth.toFloat(),
      texture1.regionHeight.toFloat(),
      texture1.regionWidth,
      texture1.regionHeight
    )
  }

  /**
   * Draws a texture at the specified position with the specified size.
   *
   * @param texture The texture to draw.
   * @param x The x-coordinate of the texture.
   * @param y The y-coordinate of the texture.
   * @param width The width of the texture.
   * @param height The height of the texture.
   * @param texWidth The width of the texture.
   * @param texHeight The height of the texture.
   */
  fun drawTexture(
    texture: NamespaceID,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    texWidth: Int,
    texHeight: Int
  ) {
    drawTexture(quantum.textureManager[texture], x, y, width, height, 0F, 0F, width, height, texWidth, texHeight)
  }

  /**
   * Draws a texture at the specified position with the specified size.
   *
   * @param texture The texture to draw.
   * @param x The x-coordinate of the texture.
   * @param y The y-coordinate of the texture.
   * @param width The width of the texture.
   * @param height The height of the texture.
   * @param u The u-coordinate of the texture.
   * @param v The v-coordinate of the texture.
   * @param texWidth The width of the texture.
   * @param texHeight The height of the texture.
   */
  fun drawTexture(
    texture: NamespaceID,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    u: Float,
    v: Float,
    texWidth: Int,
    texHeight: Int
  ) {
    drawTexture(quantum.textureManager[texture], x, y, width, height, u, v, width, height, texWidth, texHeight)
  }

  /**
   * Draws a texture at the specified position with the specified size.
   *
   * @param texture The texture to draw.
   * @param x The x-coordinate of the texture.
   * @param y The y-coordinate of the texture.
   * @param width The width of the texture.
   * @param height The height of the texture.
   * @param u The x of the texture's UV.
   * @param v The y of the texture's UV.
   * @param uSize The width of the texture's UV.
   * @param vSize The height of the texture's UV.
   * @param texWidth The width of the texture.
   * @param texHeight The height of the texture.
   */
  fun drawTexture(
    texture: NamespaceID,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    u: Float,
    v: Float,
    uSize: Float,
    vSize: Float,
    texWidth: Int,
    texHeight: Int
  ) {
    drawTexture(quantum.textureManager[texture], x, y, width, height, u, v, uSize, vSize, texWidth, texHeight)
  }

  /**
   * Draws text at the specified position.
   *
   * @param text The text to draw.
   * @param x The x-coordinate of the text.
   * @param y The y-coordinate of the text.
   */
  fun drawText(text: String, x: Float, y: Float) {
    layout.reset()
    font.markup(text, layout)
    font.drawGlyphs(batch, layout, x, y)
  }

  fun drawText(text: String, x: Float, y: Float, width: Float, height: Float) {
    layout.reset()
    layout.targetWidth = width
    font.markup(text, layout)
    font.drawGlyphs(batch, layout, x, y)
  }

  fun drawText(text: TypingLabel, x: Float, y: Float) {
    text.setPosition(x, y)
    text.act(Gdx.graphics.deltaTime)
    text.draw(batch, 1f)
  }

  fun drawText(text: Actor) {
    text.act(Gdx.graphics.deltaTime)
    text.draw(batch, 1f)
  }

  /**
   * Draws text at the specified position.
   *
   * @param layout The layout of the text.
   * @param x The x-coordinate of the text.
   * @param y The y-coordinate of the text.
   */
  fun drawText(layout: Layout, x: Float, y: Float) {
    font.drawGlyphs(batch, layout, x, y)
  }

  /**
   * Calculates the width of the text.
   *
   * @param text The text to calculate the width of.
   * @return The width of the text.
   */
  fun textWidth(text: String): Float {
    layout.reset()
    return font.markup(text, layout).let { layout.width }
  }

  /**
   * Calculates the height of the text.
   *
   * @param text The text to calculate the height of.
   * @return The height of the text.
   */
  fun textHeight(text: String): Float {
    layout.reset()
    return font.markup(text, layout).let { layout.height }
  }

  /**
   * Begins drawing.
   */
  fun begin() {
    batch.setBlendFunctionSeparate(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, GL20.GL_ONE, GL20.GL_ONE_MINUS_SRC_ALPHA)
    batch.begin()
  }

  /**
   * Ends drawing.
   */
  fun end() {
    batch.end()
  }

  /**
   * Disposes the GUI renderer and its resources.
   */
  fun dispose() {
    batch.dispose()
  }

  /**
   * Uses the GUI renderer in a block.
   *
   * @param block The block to use the GUI renderer in.
   * @param T The return type of the [block].
   * @return The value returned by the [block].
   */
  fun <T : Any> use(block: (GuiRenderer) -> T): T {
    begin()
    val ret = block(this)
    end()
    return ret
  }

  /**
   * Draws a nine-patch at the specified position with the specified size.
   *
   * @param texture The texture to draw.
   * @param leftInset The left inset of the nine-patch.
   * @param topInset The top inset of the nine-patch.
   * @param rightInset The right inset of the nine-patch.
   * @param bottomInset The bottom inset of the nine-patch.
   * @param x The x-coordinate of the nine-patch.
   * @param y The y-coordinate of the nine-patch.
   * @param width The width of the nine-patch.
   * @param height The height of the nine-patch.
   * @param texWidth The width of the texture.
   * @param texHeight The height of the texture.
   */
  private fun drawNinePatch(
    texture: TextureRegion,
    leftInset: Float,
    topInset: Float,
    rightInset: Float,
    bottomInset: Float,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    texWidth: Int,
    texHeight: Int,
  ) {
    // Corners
    drawTexture(
      texture,
      x,
      y + height - bottomInset,
      leftInset,
      topInset,
      0F,
      0F,
      leftInset,
      topInset,
      texWidth,
      texHeight
    )
    drawTexture(
      texture,
      x + width - rightInset,
      y + height - bottomInset,
      rightInset,
      topInset,
      texWidth - rightInset,
      0F,
      rightInset,
      topInset,
      texWidth,
      texHeight
    )
    drawTexture(
      texture,
      x,
      y,
      leftInset,
      bottomInset,
      0F,
      texHeight - bottomInset,
      leftInset,
      bottomInset,
      texWidth,
      texHeight
    )
    drawTexture(
      texture,
      x + width - rightInset,
      y,
      rightInset,
      bottomInset,
      texWidth - rightInset,
      texHeight - bottomInset,
      rightInset,
      bottomInset,
      texWidth,
      texHeight
    )

    // Horizontal
    for (i in leftInset.toInt() until (width - rightInset).toInt() step (texWidth - leftInset - rightInset).toInt()) {
      drawTexture(
        texture,
        x + i,
        y + height - bottomInset,
        texWidth - leftInset - rightInset,
        topInset,
        leftInset,
        0F,
        texWidth - leftInset - rightInset,
        topInset,
        texWidth,
        texHeight
      )
      drawTexture(
        texture,
        x + i,
        y,
        texWidth - leftInset - rightInset,
        bottomInset,
        leftInset,
        texHeight - bottomInset,
        texWidth - leftInset - rightInset,
        bottomInset,
        texWidth,
        texHeight
      )
    }

    // Vertical
    for (i in topInset.toInt() until (height - bottomInset).toInt() step (texHeight - topInset - bottomInset).toInt()) {
      drawTexture(
        texture,
        x,
        y + i,
        leftInset,
        texHeight - topInset - bottomInset,
        0F,
        topInset,
        leftInset,
        texHeight - topInset - bottomInset,
        texWidth,
        texHeight
      )
      drawTexture(
        texture,
        x + width - rightInset,
        y + i,
        rightInset,
        texHeight - topInset - bottomInset,
        texWidth - rightInset,
        topInset,
        rightInset,
        texHeight - topInset - bottomInset,
        texWidth,
        texHeight
      )
    }

    // Center
    for (i in leftInset.toInt() until (width - rightInset).toInt() step (texWidth - leftInset - rightInset).toInt()) {
      for (j in topInset.toInt() until (height - bottomInset).toInt() step (texHeight - topInset - bottomInset).toInt()) {
        drawTexture(
          texture,
          x + i,
          y + j,
          texWidth - leftInset - rightInset,
          texHeight - topInset - bottomInset,
          leftInset,
          topInset,
          texWidth - leftInset - rightInset,
          texHeight - topInset - bottomInset,
          texWidth,
          texHeight
        )
      }
    }
  }

  /**
   * Draws a nine-patch at the specified position with the specified size.
   *
   * @param texture The texture to draw.
   * @param inset The inset of the nine-patch.
   * @param x The x-coordinate of the nine-patch.
   * @param y The y-coordinate of the nine-patch.
   * @param width The width of the nine-patch.
   * @param height The height of the nine-patch.
   * @param texWidth The width of the texture.
   * @param texHeight The height of the texture.
   */
  fun drawNinePatch(
    texture: NamespaceID,
    inset: Float,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    texWidth: Int,
    texHeight: Int,
  ) {
    val textureRegion = quantum.textureManager[texture]
    drawNinePatch(textureRegion, inset, inset, inset, inset, x, y - height, width, height, texWidth, texHeight)
  }

  /**
   * Draws a nine-patch at the specified position with the specified size.
   *
   * @param texture The texture to draw.
   * @param leftInset The left inset of the nine-patch.
   * @param topInset The top inset of the nine-patch.
   * @param rightInset The right inset of the nine-patch.
   * @param bottomInset The bottom inset of the nine-patch.
   * @param x The x-coordinate of the nine-patch.
   * @param y The y-coordinate of the nine-patch.
   * @param width The width of the nine-patch.
   * @param height The height of the nine-patch.
   * @param texWidth The width of the texture.
   * @param texHeight The height of the texture.
   */
  fun drawNinePatch(
    texture: NamespaceID,
    leftInset: Float,
    topInset: Float,
    rightInset: Float,
    bottomInset: Float,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    texWidth: Int,
    texHeight: Int,
  ) {
    val textureRegion = quantum.textureManager[texture]
    drawNinePatch(textureRegion, leftInset, topInset, rightInset, bottomInset, x, y - height, width, height, texWidth, texHeight)
  }

  fun translate(x: Float, y: Float) {
    val translate = batch.transformMatrix.translate(x, y, 0F)
    translation.add(x, y)
    batch.transformMatrix = translate
  }

  fun translation(vector2: Vector2): Vector2 {
    return vector2.set(translation)
  }

  fun translated(x: Float, y: Float, function: () -> Unit) {
    translate(x, y)
    try {
      function()
    } finally {
      translate(-x, -y)
    }
  }

  fun subInstance(x: Float, y: Float, width: Float, height: Float, function: (GuiRenderer) -> Unit) {
    translate(x, y)
    batch.flush()
    try {
      val guiScale = QuantumVoxel.instance.guiScale
      val fl: Float = translation.y * guiScale
      val rectangle = Rectangle(translation.x * guiScale, fl, width * guiScale, height * guiScale)
      if (ScissorStack.pushScissors(rectangle)) {
        function(this)
        batch.flush()
        ScissorStack.popScissors()
      }
    } finally {
      translate(-x, -y)
    }
  }

  fun withoutBlending(function: () -> Unit) {
    batch.disableBlending()
    try {
      function()
    } finally {
      batch.enableBlending()
    }
  }
}
