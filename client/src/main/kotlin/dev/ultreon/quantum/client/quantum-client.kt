@file:Suppress("t", "GDXKotlinStaticResource")
@file:JvmName("QuantumClientKt")

package dev.ultreon.quantum.client

import com.artemis.Component
import com.artemis.World
import com.artemis.utils.Bag
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.Queue
import com.badlogic.gdx.utils.async.AsyncExecutor
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.tommyettinger.textra.Font
import com.github.tommyettinger.textra.KnownFonts
import dev.ultreon.quantum.*
import dev.ultreon.quantum.blocks.Blocks
import dev.ultreon.quantum.blocks.PropertyKeys
import dev.ultreon.quantum.client.debug.DebugRenderer
import dev.ultreon.quantum.client.gui.GuiRenderer
import dev.ultreon.quantum.client.gui.screens.PlaceholderScreen
import dev.ultreon.quantum.client.gui.screens.Screen
import dev.ultreon.quantum.client.input.*
import dev.ultreon.quantum.client.model.JsonModelLoader
import dev.ultreon.quantum.client.model.ModelRegistry
import dev.ultreon.quantum.client.network.ClientBaseSocket
import dev.ultreon.quantum.client.network.ClientConnection
import dev.ultreon.quantum.client.scripting.ClientContextTypes
import dev.ultreon.quantum.client.scripting.cond.ClientConditions
import dev.ultreon.quantum.client.texture.TextureManager
import dev.ultreon.quantum.client.world.ClientDimension
import dev.ultreon.quantum.client.world.LocalPlayer
import dev.ultreon.quantum.network.Connection
import dev.ultreon.quantum.resource.ResourceManager
import dev.ultreon.quantum.scripting.ContextAware
import dev.ultreon.quantum.scripting.ContextType
import dev.ultreon.quantum.scripting.ContextValue
import dev.ultreon.quantum.scripting.PersistentData
import dev.ultreon.quantum.scripting.function.function
import dev.ultreon.quantum.async.Future
import dev.ultreon.quantum.client.gui.screens.Text
import dev.ultreon.quantum.client.gui.screens.TextButton
import dev.ultreon.quantum.client.gui.widget.GuiContainer
import dev.ultreon.quantum.network.ConnectionStage
import dev.ultreon.quantum.util.NamespaceID
import ktx.app.*
import ktx.assets.disposeSafely
import ktx.async.MainDispatcher
import space.earlygrey.shapedrawer.ShapeDrawer
import java.util.zip.ZipInputStream
import kotlin.math.min

const val MINIMUM_WIDTH = 550
const val MINIMUM_HEIGHT = 300

const val TPS = 20
const val SPT = 1F / TPS


/**
 * The [QuantumVoxel] object represents the main entry point for the Quantum Voxel application.
 * It extends the [KtxGame] class, managing the lifecycle of the game, including initialization,
 * rendering, and resource management.
 *
 * Functionality:
 * - Manages [Screen], [World], and [ClientConnection] instances.
 * - Initializes and loads resources using managers like [TextureManager], [ResourceManager], and [JsonModelLoader].
 * - Handles application crashes by displaying the crash stack trace on the screen.
 * - Provides access to core components like the [clientResources], [jsonModelLoader], [textureManager],
 *   and the game [world].
 *
 * Lifecycle:
 * - The [create] method initializes the game environment, including graphics, texture management,
 *   resource loading, and sets up the initial screen.
 * - The [render] method manages the drawing lifecycle,
 *   including handling and rendering crash details if any exception occurs.
 * - The [dispose] method cleans up resources and disposes of components safely when the game is terminated.
 *
 * @property instance The singleton instance of the [QuantumVoxel] class.
 * @property screen The current screen instance.
 * @property world The main game world.
 * @property modelBatch The model batch used for rendering 3D models in the game world.
 * @property font The font used for rendering text in the game world.
 * @property spriteBatch The sprite batch used for rendering 2D elements in the game world.
 * @property shapes The shape drawer used for rendering shapes in the game world.
 * @property textureManager The texture manager used for managing textures in the game world.
 * @property clientResources The resource manager used for managing resources in the game world.
 * @property jsonModelLoader The JSON model loader used for loading models in the game world.
 * @property gameInput The game input manager used for handling user input in the game world.
 * @property guiCam The GUI camera used for rendering GUI elements in the game world.
 * @property guiViewport The GUI viewport used for rendering GUI elements in the game world.
 * @property isTouch A flag indicating whether the game is running on a touch-enabled device.
 * @property guiScale The GUI scale factor used for rendering GUI elements in the game world.
 * @property backgroundRenderer The background renderer used for rendering the game world background.
 * @property environmentRenderer The environment renderer used for rendering the game world environment.
 * @property debug A flag indicating whether debug mode is enabled in the game world.
 * @property debugRenderer The debug renderer used for rendering debug information in the game world.
 * @property player The local player instance in the game world.
 * @property dimension The client dimension instance in the game world.
 * @property globalBatch The global sprite batch used for rendering 2D elements in the game world.
 * @property executor The async executor used for running asynchronous tasks in the game world.
 * @property bitmapFont The bitmap font used for rendering text in the game world.
 * @property font The font used for rendering text in the game world.
 * @property chat The chat GUI used for displaying chat messages in the game world.
 * @property connection The client connection instance used for connecting to a server in the game world.
 * @property chunkQueue The number of chunks to be loaded in the game world.
 * @property setGuiScale The GUI scale factor used for rendering GUI elements in the game world.
 *
 * @constructor Creates a new instance of the [QuantumVoxel] class.
 */
class QuantumVoxel : KtxApplicationAdapter, KtxInputAdapter, ContextAware<QuantumVoxel> {
  init {
    instance = this
  }

  var chunkQueue: Int = 0
  val chat: ChatGui = ChatGui()
  var connection: Connection? = null

  //  private lateinit var gen: Gen
//  private lateinit var v8Runtime: V8Runtime
  private var init: Boolean = false
  private var deferResize: Boolean = false
  private var loaded: Boolean = false

  // Movement
  lateinit var keyMovement: KeyMovement
  lateinit var controllerMovement: ControllerMovement

  @Deprecated("Use keyMovement instead", ReplaceWith("keyMovement"), DeprecationLevel.ERROR)
  lateinit var touchMovement: TouchMovement

  var movement: PlayerMovement = KeyMovement()

  @Deprecated("Not used anymore", level = DeprecationLevel.ERROR)
  lateinit var touchpad: Touchpad

  var backgroundRenderer: BackgroundRenderer? = null
  private val tasks: Queue<() -> Unit> = Queue()
  val isTouch: Boolean
    get() = Gdx.input.isPeripheralAvailable(Input.Peripheral.MultitouchScreen) && !Gdx.input.isPeripheralAvailable(
      Input.Peripheral.HardwareKeyboard
    )
  var gameInput: GameInput = GameInput()
  internal val debugRenderer: DebugRenderer by lazy { DebugRenderer() }
  private val bag: Bag<Component> by lazy { Bag() }
  private val tmpTransform by lazy { Matrix4() }
  lateinit var globalBatch: SpriteBatch
    private set
  lateinit var shapes: ShapeDrawer
    private set
  var debug = true
  var environmentRenderer: EnvironmentRenderer? = null
  var player: LocalPlayer? = null
  var dimension: ClientDimension? = null
  private lateinit var crashFont: BitmapFont
  private lateinit var crashSpriteBatch: SpriteBatch
  private var crash: Throwable? = null
  var setGuiScale = 0
    set(value) {
      field = value.coerceAtLeast(0)
    }
  private val texture by lazy { texture(NamespaceID.of(path = "textures/blocks/soil.png")) }
  val material by lazy {
    material {
      diffuse(texture.texture)
      cullFace(GL20.GL_BACK)
      blendMode(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
      alphaTest(0.01f)
      depthTest(true, GL20.GL_LESS, 0.01f, 1000f)
    }
  }

  lateinit var guiRenderer: GuiRenderer
    private set

  private var width = 1
  private var height = 1

  var frameTick = 0F

  val executor: AsyncExecutor by lazy { AsyncExecutor(4, "QV:Async Worker") }

  /**
   * Manages and organizes resources such as textures, models, and shaders.
   * Provides a centralized framework for accessing and registering resource categories.
   * Plays a key role in resource loading, initialization, and memory management.
   * Integrally involved in the application lifecycle to ensure all necessary assets are available.
   */
  val clientResources: ResourceManager by lazy { ResourceManager("client") }

  val bitmapFont by lazy {
    try {
      BitmapFont(
        Gdx.files.internal("client/quantum/fonts/luna_pixel.fnt"),
        Gdx.files.internal("client/quantum/textures/font/luna_pixel.png"),
        false
      )
    } catch (e: Throwable) {
      logger.error("Failed to load font:\n${e.stackTraceToString()}")
      BitmapFont()
    }
  }

  val font by lazy {
    try {
      Font(bitmapFont).also {
        KnownFonts.addEmoji(it, -36F, 16F, -4F)
      }
    } catch (e: Throwable) {
      try {
        logger.error("Failed to load font:\n${e.stackTraceToString()}")
        Font(BitmapFont())
      } catch (e: Throwable) {
        logger.error("Failed to load font:\n${e.stackTraceToString()}")
        Font()
      }
    }
  }

  /**
   * A loader specifically designed to manage the parsing and loading of models from JSON files.
   * Utilizes the provided `ResourceManager` to access and organize relevant resources needed for model generation.
   * Facilitates the integration of JSON-based assets into the application's resource pipeline.
   */
  val jsonModelLoader: JsonModelLoader by lazy { JsonModelLoader(clientResources) }

  /**
   * An instance of `TextureManager` responsible for handling texture-related tasks within the application.
   * Coordinates the loading, initialization, registration, and organization of texture assets.
   * Works in conjunction with the `ResourceManager` to manage texture resources efficiently.
   * Plays a crucial role in maintaining texture data during the application's lifecycle.
   */
  val textureManager: TextureManager by lazy { TextureManager(clientResources) }

  @JvmField
  var world: World? = null

  /**
   * Initializes the application and sets up the necessary resources, screens, and configurations.
   */
  override fun create() {
    super.create()

    MainDispatcher.initiate()

    globalBatch = spriteBatch()
    guiRenderer = GuiRenderer(globalBatch)
    shapes = ShapeDrawer(globalBatch, Pixmap(1, 1, Pixmap.Format.RGB888).let { pixmap ->
      pixmap.setColor(1f, 1f, 1f, 1f)
      pixmap.drawPixel(0, 0)

      Texture(pixmap).let {
        pixmap.disposeSafely()
        TextureRegion(it, 0, 0, 1, 1)
      }
    })
    Gdx.graphics.setVSync(false)

    registerWidgets()

    width = Gdx.graphics.width.coerceAtLeast(MINIMUM_WIDTH)
    height = Gdx.graphics.height.coerceAtLeast(MINIMUM_HEIGHT)

    Blocks.init()
    PropertyKeys.init()

    gamePlatform.loadResources(clientResources)
    gamePlatform.loadResources(commonResources)

    Gdx.files.local("content-packs").let { path ->
      if (path.exists()) {
        path.list().forEach {
          if (it.extension() == "qvcontent") {
            ZipInputStream(it.read()).use { zip -> clientResources.loadZip(zip) }
            ZipInputStream(it.read()).use { zip -> commonResources.loadZip(zip) }
          }
        }
      } else {
        path.mkdirs()
      }
    }

    ClientConditions

    doContentRegistration()
    clientEvents.load()

    guiCam = OrthographicCamera()
    guiViewport = ScreenViewport(guiCam)

    loaded = true

    Gdx.input.setCatchKey(Input.Keys.BACK, true)
    Gdx.input.inputProcessor = this

//    val host = V8Host.getNodeInstance()
//    this.v8Runtime = host.createV8Runtime()
//    this.gen = Gen(v8Runtime, JNEventLoop(v8Runtime)).apply { prepare() }
//
//    gen.importZip(Gdx.files.internal("internal/quantum.zip"))
//
//    for (file in Gdx.files.local("modules").list()) {
//      gen.loadDirectory(file.path())
//    }

    logger.debug("Quantum Voxel started!", this)
  }

  private fun registerWidgets() {
    WidgetFactories.register { Text(it) }
    WidgetFactories.register { TextButton(it) }
    WidgetFactories.register { GuiContainer(it) }
  }

  fun startWorld() {
//    world = World()

    dimension = ClientDimension(material)
    player = /*world!!.createEntity().also { entity ->
      val positionComponent = PositionComponent(vec3d(0, 128, 0))
      entity.edit()
        .add(LocalPlayerComponent("Player"))
        .add(RunningComponent(1.6F))
        .add(positionComponent)
        .add(InventoryComponent())
        .add(CollisionComponent().also {
          it.positionComponent = positionComponent
          it.dimension = dimension!!
        })

      environmentRenderer?.lastRefreshPosition?.set(entity.getComponent(PositionComponent::class.java).position)
    }*/ dimension!!.spawnPlayer(vec3d(0, 128, 0))

    backgroundRenderer.disposeSafely()
    backgroundRenderer = null

    environmentRenderer = EnvironmentRenderer()
    environmentRenderer?.resize(width, height)

    Gdx.input.isCursorCatched = true
    quantum.showScreen(PlaceholderScreen)
  }

  fun stopWorld(after: () -> Unit) {
    player = null
    dimension.disposeSafely()
    dimension = null
    world?.dispose()
    world = null
    environmentRenderer.disposeSafely()
    environmentRenderer = null
    backgroundRenderer = BackgroundRenderer()
    after()
  }

  /**
   * Frees all resources tied to this instance to avoid memory leaks.
   * This function runs when the app is closed or stopped.
   *
   * The [disposeSafely] method is used to safely dispose of objects.
   */
  override fun dispose() {
    super.dispose()

    textureManager.dispose()
    environmentRenderer.disposeSafely()
    dimension.disposeSafely()
    world?.dispose()

    Future.dispose()

    player = null
    dimension = null
    world = null
    environmentRenderer = null

    ModelRegistry.dispose()

    gamePlatform.dispose()
  }

  /**
   * Draws the application frame, handling both normal operation and crash scenarios.
   *
   * If a crash exception is present, the method clears the screen, switches to a crash rendering mode,
   * and prints the stack trace of the exception to the screen for debugging purposes.
   *
   * In the normal operation case (no crash), it does the normal rendering process.
   *
   * @see EnvironmentRenderer
   * @see PlaceholderScreen
   * @see DebugRenderer
   * @see GameInput
   */
  @OptIn(InternalApi::class)
  override fun render() {
    clearScreen(0f, 0f, 0f, 1f)

    if (this.width != Gdx.graphics.width || this.height != Gdx.graphics.height) {
      resize(Gdx.graphics.width, Gdx.graphics.height)
    }

    if (!loaded) {
      return
    }

    if (!init) {
      showScreen(LoadScreen())
    }
    this.init = true

    if (deferResize) {
      resize(this.width, this.height)
    }

    if (frameTick > SPT) {
      frameTick -= SPT

      doTick()
    }

    gameInput.update()

    if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
      debug = !debug
    }

    frameTick += Gdx.graphics.deltaTime

    synchronized(tasks) {
      if (!tasks.isEmpty) {
        var polled: (() -> Unit)? = tasks.removeFirst()
        while (polled != null) {
          polled()
          if (tasks.isEmpty) break
          polled = tasks.removeFirst()
        }

      }
    }

    environmentRenderer?.apply {
      render(Gdx.graphics.deltaTime)
      PlaceholderScreen.render(Gdx.graphics.deltaTime)
    } ?: run {
      clearScreen(0.1f, 0.1f, 0.1f, 1f)
      backgroundRenderer?.render()
    }

    tmpTransform.set(globalBatch.transformMatrix)
    try {
      globalBatch.transformMatrix.scale(guiScale, guiScale, 1f)
      guiRenderer.use {
        screen?.render(Gdx.graphics.deltaTime)!!
      }

      this.debugRenderer.render()
    } finally {
      globalBatch.transformMatrix = tmpTransform.cpy()
    }
  }

  private fun doTick() {
    clientEvents["game_tick"]
      ?.callSync("client" to ContextValue(ClientContextTypes.client, this))

    dimension?.tick()
    bag.clear()
    player?.tick()
  }

  /**
   * Calculates the maximum GUI scale that can be applied based on the current window dimensions
   * and predefined minimum width and height constraints.
   *
   * The method determines the scale by comparing the ratios of the window's width and height
   * to the defined minimum dimensions `MINIMUM_WIDTH` and `MINIMUM_HEIGHT`. It ensures that
   * the resulting scale is at least 1 to maintain usability.
   *
   * @return The maximum allowable GUI scale as an integer value.
   */
  fun calcMaxGuiScale(): Int {
    val windowWidth = this.width
    val windowHeight = this.height

    if (windowWidth / MINIMUM_WIDTH < windowHeight / MINIMUM_HEIGHT) {
      return (windowWidth / MINIMUM_WIDTH).coerceAtLeast(1)
    }

    if (windowHeight / MINIMUM_HEIGHT < windowWidth / MINIMUM_WIDTH) {
      return (windowHeight / MINIMUM_HEIGHT).coerceAtLeast(1)
    }

    val min = min(windowWidth / MINIMUM_WIDTH, windowHeight / MINIMUM_HEIGHT)
    return min.coerceAtLeast(1)
  }

  val guiScale: Float
    get() =
      (if (setGuiScale <= 0) calcMaxGuiScale() else setGuiScale.coerceAtMost(calcMaxGuiScale())).toFloat()

  /**
   * Resize event handler for the application.
   * This method is called when the application window is resized.
   *
   * NOTE: This method is marked as internal and should not be called directly.
   *
   * @param width The new width of the application window.
   * @param height The new height of the application window.
   */
  @InternalApi
  override fun resize(width: Int, height: Int) {
    if (!loaded) {
      this.deferResize = true
      return
    }

    this.width = width
    this.height = height

    clientEvents["resize"]
      ?.callSync(
        "client" to ContextValue(ClientContextTypes.client, this),
        "width" to ContextValue(ContextType.int, width),
        "height" to ContextValue(ContextType.int, height)
      )

    super.resize(width, height)

    guiCam.setToOrtho(false, width.toFloat(), height.toFloat())
    guiCam.zoom = 1 / guiScale

    environmentRenderer?.resize(width, height)
    globalBatch.projectionMatrix.setToOrtho2D(0f, 0f, width.toFloat(), height.toFloat())
  }

  /**
   * Shows the specified screen, hiding the current screen if necessary.
   * The method triggers the [Screen.show] and [Screen.hide] lifecycle events for the screens involved.
   *
   * @param screen The screen to show. Hiding the current screen if necessary.
   */
  fun showScreen(screen: KtxScreen?) {
    if (this.screen == screen) {
      return
    }

    clientEvents["show_screen"]
      ?.callSync(
        "client" to ContextValue(ClientContextTypes.client, this),
        "old_screen" to ContextValue(ClientContextTypes.screen, this.screen ?: PlaceholderScreen),
        "screen" to ContextValue(ClientContextTypes.screen, screen ?: PlaceholderScreen)
      )


    this.screen?.hide()
    this.screen = screen

    screen?.show()
  }

  fun submit(function: () -> Unit) {
    synchronized(tasks) {
      this.tasks.addLast(function)
    }
  }

  var screen: KtxScreen? = PlaceholderScreen

  internal lateinit var guiCam: OrthographicCamera

  lateinit var guiViewport: Viewport
    private set

  @InternalApi
  override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = handleInput {
    val scr = screen
    if (scr is Screen) {
      return@handleInput scr.touchDown(screenX / guiScale, screenY / guiScale, pointer, button)
    }

    return@handleInput super.touchDown(screenX, screenY, pointer, button)
  }

  @InternalApi
  override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = handleInput {
    val scr = screen
    var result = false
    if (scr is Screen) {
      result = result or scr.touchUp(screenX / guiScale, screenY / guiScale, pointer, button)
    }

    return@handleInput result or super.touchUp(screenX, screenY, pointer, button)
  }

  override val persistentData: PersistentData = PersistentData()

  override fun contextType(): ContextType<QuantumVoxel> {
    return ClientContextTypes.client
  }

  private val vfStartWorld = function(function = {
    return@function if (dimension == null) {
      startWorld()
      null
    } else {
      stopWorld {
        startWorld()
      }
      null
    }
  })

  override fun fieldOf(name: String, contextJson: JsonValue?): ContextValue<*>? {
    return when (name) {
      "dimension" -> dimension?.let { ContextValue(ClientContextTypes.clientDimension, it) }
      "player" -> player?.let { ContextValue(ClientContextTypes.localPlayer, it) }
      "screen" -> screen?.let { ContextValue(ClientContextTypes.screen, it) }
      "global_batch" -> ContextValue(ClientContextTypes.batch, globalBatch)
      "font" -> ContextValue(ClientContextTypes.font, quantum.font)
      "renderer" -> ContextValue(ClientContextTypes.guiRenderer, guiRenderer)
      "start_world" -> ContextValue(ContextType.function, vfStartWorld)
      "resources" -> ContextValue(ContextType.resources, clientResources)
      else -> super.fieldOf(name, contextJson)
    }
  }

  override fun toString(): String {
    return "QuantumVoxel"
  }

  @ExperimentalApi
  fun connect(address: String) {
    val createClientSocket = ClientSocket(address, ConnectionStage.HANDSHAKE)
    connection = ClientConnection(this, createClientSocket)
  }

  companion object {
    val executor: AsyncExecutor by lazy {
      AsyncExecutor((Runtime.getRuntime().availableProcessors() * 2).coerceAtLeast(8).also {
        logger.info("Quantum Client will be using $it threads")
      })
    }
    private val mainThread = Thread.currentThread()

    private val isMainThread: Boolean
      get() = Thread.currentThread().id == mainThread.id
    lateinit var instance: QuantumVoxel

    operator fun <T> invoke(function: () -> T): T {
      return invokeAsync(function).get()
    }

    fun <T> invokeAsync(function: () -> T): Future<T> {
      if (isMainThread && !gamePlatform.isWeb) {
        return Future.completed(function())
      }

      val future = Future<T>()

      quantum.submit {
        try {
          future.complete(function())
        } catch (e: Throwable) {
          future.completeExceptionally(e)
        }
      }

      return future
    }

    fun handleInput(function: () -> Boolean): Boolean {
      invokeAsync {
        function()
      }

      return true
    }
  }
}

/**
 * Reference to the QuantumVoxel instance
 */
val quantum get() = QuantumVoxel.instance

/**
 * Reference to the sprite batch used for rendering 2D elements in the game world.
 */
val shapes get() = quantum.shapes

/**
 * Reference to the texture manager used for managing textures in the game world.
 */
val textureManager get() = quantum.textureManager

/**
 * Reference to the input manager used for handling user input in the game world.
 */
val gameInput get() = quantum.gameInput

/**
 * Reference to the GUI camera used for rendering GUI elements in the game world.
 */
val guiCam get() = quantum.guiCam

/**
 * Reference to the GUI viewport used for rendering GUI elements in the game world.
 */
val guiViewport get() = quantum.guiViewport

/**
 * Whether the game is running on a touch-enabled device
 */
val isTouch get() = quantum.isTouch

/**
 * Reference to the GUI scale factor
 */
val guiScale get() = quantum.guiScale

/**
 * Reference to the background renderer used for rendering the title screen background.
 */
var backgroundRenderer
  get() = quantum.backgroundRenderer
  set(value) {
    quantum.backgroundRenderer = value
  }

/**
 * Reference to the environment renderer used for rendering the game world environment.
 */
val environmentRenderer get() = quantum.environmentRenderer

/**
 * Flag indicating whether debug mode is enabled.
 */
val debug get() = quantum.debug

/**
 * Reference to the debug renderer used for rendering debug information.
 */
val debugRenderer get() = quantum.debugRenderer

/**
 * Reference to the player entity in the game world.
 */
val player get() = quantum.player

/**
 * Reference to the game world.
 */
val world get() = quantum.world

/**
 * Reference to the current dimension.
 */
val dimension get() = quantum.dimension

/**
 * Reference to the global sprite batch used for rendering 2D elements in the game world.
 */
val globalBatch get() = quantum.globalBatch

/**
 * Reference to the font used for rendering text in the game world.
 */
val font get() = quantum.font
