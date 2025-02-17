package dev.ultreon.quantum.resource

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.LoggerFactory
import dev.ultreon.quantum.scripting.*
import dev.ultreon.quantum.scripting.function.function
import dev.ultreon.quantum.async.Future
import dev.ultreon.quantum.util.NamespaceID
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import javax.naming.Name

private val logger = LoggerFactory["QV:Resources"]

/**
 * The `ResourceManager` class is responsible for managing and organizing resources within a specified asset root.
 * It provides functionality to register, load, and retrieve resources and resource categories.
 *
 * @constructor Initializes the `ResourceManager` with the specified root for asset management.
 * @param assetRoot The root directory for managing assets.
 */
class ResourceManager(
  private val assetRoot: String,
) : ContextAware<ResourceManager> {
  internal val root = ResourceRoot(assetRoot)

  override val persistentData: PersistentData = PersistentData()
  override fun contextType(): ContextType<ResourceManager> = ContextType.resources

  private val get = function(
    ContextParam("location", ContextType.id),
    function = {
      it.get<NamespaceID>("location")?.let { location ->
        return@function ContextValue(ContextType.resource, get(location) ?: return@function null)
      } ?: run {
        throw IllegalArgumentException("Missing parameter: location")
      }
    }
  )

  private val require = function(
    ContextParam("location", ContextType.id),
    function = {
      it.get<NamespaceID>("location")?.let { location ->
        return@function ContextValue(ContextType.resource, require(location))
      } ?: run {
        throw IllegalArgumentException("Missing parameter: location")
      }
    }
  )


  override fun fieldOf(name: String, contextJson: JsonValue?): ContextValue<*>? {
    return when (name) {
      "root" -> ContextValue(ContextType.resourceRoot, root)
      "get" -> ContextValue(ContextType.function, get)
      "require" -> ContextValue(ContextType.function, require)
      else -> null
    }
  }

  /**
   * Retrieves a `ResourceCategory` by its name.
   *
   * @param name The name of the resource category to retrieve.
   * @return The `ResourceCategory` associated with the given name.
   * @throws NoSuchResourceDirectoryException If no category with the specified name is found.
   */
  operator fun get(name: String): ResourceNode? {
    return root[name]
  }

  /**
   * Retrieves a `Resource` from the resources using the given `NamespaceID`.
   *
   * @param location The `NamespaceID` representing the hierarchical location of the resource
   * to retrieve, including its domain and path.
   * @return The `Resource` corresponding to the specified `NamespaceID`.
   * @throws NoSuchResourceDirectoryException If a required resource category is not found
   * while navigating the hierarchy.
   * @throws NoSuchResourceException If the desired resource is not found in the specified
   * category or subcategory.
   */
  @Throws(NoSuchResourceDirectoryException::class, NoSuchResourceException::class)
  operator fun get(location: NamespaceID): Resource? {
    val resourcesAt = resourcesAt(location)
    return if (resourcesAt.isEmpty()) run {
      logger.error("No resources found at location: $location")
      null
    } else run {
      resourcesAt[resourcesAt.size - 1]
    }
  }

  infix fun require(location: NamespaceID): Resource {
    return resourcesAt(location).lastOrNull() ?: throw NoSuchResourceException(location.toString())
  }

  fun resourcesAt(location: NamespaceID): List<Resource> {
    val path = location.path.split('/')
    var node: ResourceDir = root

    for (i in 0 until path.size - 1) {
      val resourceNode = node[path[i]]
      node = resourceNode as ResourceDir? ?: run {
        logger.error("Resource directory not found: ${path[i]}")
        return emptyList()
      }
    }

    val resourceNode = node[path[path.size - 1]]
    if (resourceNode == null) {
      logger.error("Resource node not found: ${path[path.size - 1]}")
      return emptyList()
    }
    val asLeafOrNull = resourceNode.asLeafOrNull()
    if (asLeafOrNull == null) {
      logger.error("Resource leaf not found: ${path[path.size - 1]}")
      return emptyList()
    }
    return asLeafOrNull[location.domain]
  }

  operator fun contains(path: String): Boolean {
    return getNodeOrNull(path) != null
  }

  operator fun contains(location: NamespaceID): Boolean {
    return resourcesAt(location).isNotEmpty()
  }

  fun getNode(path: String): ResourceNode {
    return getNodeOrNull(path) ?: throw NoSuchResourceException(path)
  }

  fun getNodeOrNull(path: String): ResourceNode? {
    val elems = path.split('/')
    var node: ResourceDir = root ?: throw NoSuchResourceDirectoryException(path)

    for (i in 0 until elems.size - 1) {
      val resourceNode = node[elems[i]] as ResourceDir? ?: return null
      node = resourceNode
    }

    return node[elems.last()]
  }

  /**
   * Loads resources from the specified zip file or directory.
   *
   * @param file The `FileHandle` representing the file or directory to load resources from.
   */
  fun load(file: FileHandle) {
    when {
      file.isDirectory -> {
        loadDirectory(file)
      }
      file.extension() == "zip" -> ZipInputStream(file.read()).use { loadZip(it) }
      else -> logger.error("Unknown file type: ${file.extension()}")
    }
  }

  /**
   * Loads and processes resources from the provided `ZipInputStream`. The method iterates through
   * all entries in the zip stream, extracting and categorizing resources based on their file paths.
   *
   * Only files within a designated asset root are loaded. Files are categorized based on their
   * hierarchical paths and associated with specific resource domains. Resource categories and
   * filenames are processed to organize resources appropriately.
   *
   * If an entry represents a directory, it is skipped. For valid file entries, the method determines
   * the domain, category path, and filename to load the resource correctly.
   *
   * Resource categories referenced in the hierarchy must exist; otherwise, an error is logged.
   *
   * @param zip The `ZipInputStream` to read and extract resources from.
   * @throws NoSuchResourceDirectoryException If an unknown resource category is encountered.
   */
  fun loadZip(zip: ZipInputStream) {
    while (true) {
      val entry = zip.nextEntry ?: break

      if (!entry.isDirectory) {
        val name = entry.name
        if (name.startsWith("$assetRoot/")) {
          val domain = name.substring(assetRoot.length + 1)
          val domainId = domain.substring(0, domain.indexOf('/'))
          val path = domain.substring(domain.indexOf('/') + 1).split('/')
          val categories = path.dropLast(1)
          val filename = path.last()
          loadDirectory(zip, categories.toMutableList(), filename, domainId)
        }
      }
    }
  }

  private fun loadDirectory(
    zip: ZipInputStream,
    pathElements: MutableList<String>,
    filename: String,
    domain: String,
    parentPath: String = "",
    parent: ResourceDir = root,
  ) {
    val path = if (parentPath == "") "" else parentPath

    if (pathElements.isEmpty()) {
      parent ?: throw NoSuchResourceDirectoryException(path)
      val resourceNode = if (filename in parent) {
        parent[filename]
      } else {
        val resourceLeaf = ResourceLeaf(parent, filename)
        parent[filename] = resourceLeaf
        resourceLeaf
      }

      val byteArrayOutputStream = ByteArrayOutputStream()
      var b = zip.read()
      while (b != -1) {
        byteArrayOutputStream.write(b)
        b = zip.read()
      }
      resourceNode?.asLeafOrNull()?.addResource(
        StaticResource(
          NamespaceID.of(domain, if (path == "") filename else "$path/$filename"),
          byteArrayOutputStream.toByteArray()
        )
      ) ?: throw ResourceOverwriteException("$domain:$path/$filename")
    } else {
      val categoryName = pathElements.removeAt(0)

      val s = if (parentPath == "") categoryName else "$parentPath/$categoryName"

      loadDirectory(
        zip,
        pathElements,
        filename,
        domain,
        s,
        if (categoryName !in parent) {
          parent.mkdir(categoryName)
          parent[categoryName] as ResourceDir
        } else {
          parent[categoryName] as? ResourceDir ?: throw NoSuchResourceDirectoryException(s)
        }
      )
    }
  }

  private fun loadDirectory(file: FileHandle) {
    val list = file.child(assetRoot).list()
    if (list.isEmpty()) {
      logger.warn("No files in directory: ${file.path()}")
      return
    }
    list.forEach {
      if (it.isDirectory) {
        val domain = it.name()
        it.list().forEach last@{ category ->
          if (category.isDirectory) {
            root.mkdir(category.name())
            val domainName = root[category.name()]!!.asDir().asDirectory()
            loadDirectory(category, domain, domainName, category.name() + "/")
          }
        }
      } else {
        val domain = it.name().substring(0, it.name().indexOf('/'))
        val path = it.name().substring(it.name().indexOf('/') + 1).split('/')
        val categories = path.dropLast(1)
        val filename = path.last()
        loadResource(
          it,
          root,
          domain,
          if (categories.isEmpty()) filename else "${categories.joinToString("/")}/$filename"
        )
      }
    }
  }

  private fun loadDirectory(file: FileHandle, domain: String, dir: ResourceDir, path: String) {
    file.list().forEach { child ->
      when {
        child.isDirectory -> {
          if (child.name() !in dir) {
            dir.mkdir(child.name())
          }
          loadDirectory(
            child,
            domain,
            dir[child.name()]?.asDir()?.asDirectory() ?: throw NoSuchResourceDirectoryException(path),
            "$path${child.name()}/"
          )
        }

        dir != null -> {
          loadResource(child, dir, domain, path + child.name())
        }

        else -> {
          logger.warn("No category for file: $child")
        }
      }
    }
  }

  private fun loadResource(file: FileHandle, directory: ResourceDir, domain: String, path: String) {
    val readBytes = file.readBytes()
    if (readBytes.isEmpty()) {
      logger.warn("Empty file: $file")
    }
    if (file.name() !in directory) {
      if (file.name() !in directory) {
        directory[file.name()] = ResourceLeaf(directory, file.name()).also {
        }
      }
      directory[file.name()]!!.asLeaf().addResource(StaticResource(NamespaceID.of(domain, path), readBytes)).also {
      }
    } else {
      logger.warn("Could not add resource: $domain:$path")
    }
  }

  fun loadFromAssetsTxt(internal: FileHandle) {
    val fileList = internal.readString().split('\n')
    if (fileList.isEmpty()) {
      logger.warn("No files in assets.txt: ${internal.path()}")
      return
    }

    for (file in fileList) {
      if (file.startsWith(assetRoot + "/")) {
        val domain = file.substring(assetRoot.length + 1)
        val domainId = domain.substring(0, domain.indexOf('/'))
        val path = domain.substring(domain.indexOf('/') + 1).split('/')
        val categories = path.dropLast(1)
        val filename = path.last()

        var category: ResourceDir = root
        for (categoryName in categories) {
          category = category[categoryName] as? ResourceDir ?: run {
            category.mkdir(categoryName)
            category[categoryName] as ResourceDir
          }
        }

        val resourceNode = if (filename in category) {
          category[filename]
        } else {
          val resourceLeaf = ResourceLeaf(category, filename)
          category[filename] = resourceLeaf
          resourceLeaf
        }

        resourceNode?.asLeafOrNull()?.addResource(
          StaticResource(
            NamespaceID.of(
              domainId,
              if (categories.isEmpty()) filename else "${categories.joinToString("/")}/$filename"
            ), Gdx.files.internal(file).readBytes()
          )
        )
      }
    }
  }
}
