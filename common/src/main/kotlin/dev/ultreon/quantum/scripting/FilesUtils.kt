package dev.ultreon.quantum.scripting

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.utils.JsonReader
import com.badlogic.gdx.utils.JsonValue
import com.badlogic.gdx.utils.UBJsonReader
import com.badlogic.gdx.utils.UBJsonWriter
import dev.ultreon.quantum.scripting.function.function

object FilesUtils : ContextAware<FilesUtils> {
  override val persistentData = PersistentData()

  override fun contextType(): ContextType<FilesUtils> {
    return ContextType.files
  }

  val readText = function(
    ContextParam("file", ContextType.string),
    function = {
      ContextValue(ContextType.string, Gdx.files.local(it.getString("file") ?: "null").readString())
    }
  )

  val writeText = function(
    ContextParam("file", ContextType.string),
    ContextParam("text", ContextType.string),
    function = {
      Gdx.files.local(it.getString("file") ?: "null").writeString(it.getString("text") ?: "null", false)
      null
    }
  )

  val readJson = function(
    ContextParam("file", ContextType.string),
    function = {
      ContextValue(ContextType.json, JsonReader().parse(Gdx.files.local(it.getString("file") ?: "null")))
    }
  )

  val writeJson = function(
    ContextParam("file", ContextType.string),
    ContextParam("json", ContextType.json),
    function = {
      Gdx.files.local(it.getString("file") ?: "null").writeString(it.getJson("json")?.toString() ?: "null", false)
      null
    }
  )

  val readBinary = function(
    ContextParam("file", ContextType.string),
    function = {
      ContextValue(ContextType.json, UBJsonReader().parse(Gdx.files.local(it.getString("file") ?: "null")))
    }
  )

  val writeBinary = function(
    ContextParam("file", ContextType.string),
    ContextParam("json", ContextType.json),
    function = {
      UBJsonWriter(Gdx.files.local(it.getString("file") ?: "null").write(false)).value(it.getJson("json"))
      null
    }
  )

  override fun fieldOf(name: String, contextJson: JsonValue?): ContextValue<*>? {
    return when (name) {
      "read_text" -> ContextValue(ContextType.function, ::readText)
      "write_text" -> ContextValue(ContextType.function, ::writeText)
      "read_json" -> ContextValue(ContextType.function, ::readJson)
      "write_json" -> ContextValue(ContextType.function, ::writeJson)
      "read_data" -> ContextValue(ContextType.function, ::readBinary)
      "write_data" -> ContextValue(ContextType.function, ::writeBinary)
      else -> null
    }
  }
}
