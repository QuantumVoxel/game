package dev.ultreon.quantum.client.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys

/**
 * Key bindings for the game
 *
 * @see KeyBind
 * @see Keys
 * @see Gdx.input
 */
object KeyBinds {
  /**
   * The key bind for walking forwards.
   * Default key: W
   */
  val walkForwardsKey = KeyBind("walkForwardsKey", Keys.W)

  /**
   * The key bind for walking backwards.
   * Default key: S
   */
  val walkBackwardsKey = KeyBind("walkBackwardsKey", Keys.S)

  /**
   * The key bind for walking left.
   * Default key: A
   */
  val walkLeftKey = KeyBind("walkLeftKey", Keys.A)

  /**
   * The key bind for walking right.
   * Default key: D
   */
  val walkRightKey = KeyBind("walkRightKey", Keys.D)

  /**
   * The key bind for jumping.
   * Default key: Space
   */
  val jumpKey = KeyBind("jumpKey", Keys.SPACE)

  /**
   * The key bind for crouching.
   * Default key: Shift
   */
  val crouchKey = KeyBind("crouchKey", Keys.SHIFT_LEFT)

  /**
   * The key bind for running.
   * Default key: Alt
   */
  val runningKey = KeyBind("runningKey", Keys.ALT_LEFT)

  /**
   * The key binds for hotbar keys.
   * Default keys: 0 to 9
   */
  val hotbarKeys = (0 until 10) .map { KeyBind("hotbarKey$it", if (it == 9) Keys.NUM_0 else Keys.NUM_1 + it) }

  /**
   * The key bind for opening the inventory.
   * Default key: I
   */
  val inventoryKey = KeyBind("inventoryKey", Keys.X)
}

/**
 * Represents a key bind
 *
 * @param name The name of the key bind
 * @param key The key code of the key bind
 *
 * @property name The name of the key bind
 * @property key The key code of the key bind
 *
 * @see KeyBinds
 * @see Keys
 * @see Gdx.input
 */
class KeyBind(val name: String, var key: Int) {
  fun isPressed() = Gdx.input.isKeyPressed(key)
  fun isReleased() = !Gdx.input.isKeyPressed(key)
  fun isJustPressed() = Gdx.input.isKeyJustPressed(key)
}
