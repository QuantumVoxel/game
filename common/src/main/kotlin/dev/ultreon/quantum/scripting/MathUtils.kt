package dev.ultreon.quantum.scripting

import com.badlogic.gdx.math.RandomXS128
import com.badlogic.gdx.utils.JsonValue
import dev.ultreon.quantum.logger
import dev.ultreon.quantum.scripting.function.function
import kotlin.math.*

object MathUtils : ContextAware<MathUtils> {
  const val PI = 3.141592653589793
  const val E = 2.718281828459045

  val randint = function(
    ContextParam("min", ContextType.int),
    ContextParam("max", ContextType.int),
    function = {
      it.getInt("min")?.let { min ->
        it.getInt("max")?.let { max ->
          if (min > max) {
            logger.warn("randint: min is greater than max")
            return@function ContextValue(ContextType.int, min)
          }
          return@function ContextValue(ContextType.int, RandomXS128().nextInt(max - (min - 1)) + (min - 1))
        }
      }
      null
    }
  )

  val randrange = function(
    ContextParam("min", ContextType.double),
    ContextParam("max", ContextType.double),
    function = {
      it.getDouble("min")?.let { min ->
        it.getDouble("max")?.let { max ->
          if (min > max) {
            logger.warn("randrange: min is greater than max")
            return@function ContextValue(ContextType.float, min)
          }
          return@function ContextValue(ContextType.float, RandomXS128().nextFloat() * (max - min) + min)
        }
      }
      null
    }
  )

  val randbool = function(
    ContextParam("chance", ContextType.float),
    function = {
      it.getFloat("chance")?.let { chance ->
        return@function ContextValue(ContextType.boolean, RandomXS128().nextFloat() < chance)
      }
      null
    }
  )

  val sqrt = function(
    ContextParam("x", ContextType.int),
    function = {
      it.getInt("x")?.let { x ->
        return@function ContextValue(ContextType.int, sqrt(x.toDouble()).toInt())
      }
      null
    }
  )

  val pow = function(
    ContextParam("x", ContextType.int),
    ContextParam("y", ContextType.int),
    function = {
      it.getInt("x")?.let { x ->
        it.getInt("y")?.let { y ->
          return@function ContextValue(ContextType.int, x.toDouble().pow(y.toDouble()).toInt())
        }
      }
      null
    }
  )

  val absInt = function(
    ContextParam("x", ContextType.int),
    function = {
      it.getInt("x")?.let { x ->
        return@function ContextValue(ContextType.int, abs(x.toDouble()).toInt())
      }
      null
    }
  )

  val abs = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, abs(x))
      }
      null
    }
  )

  val round = function(
    ContextParam("x", ContextType.int),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.int, x.roundToLong())
      }
      null
    }
  )

  val ceil = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.int, ceil(x).toInt())
      }
      null
    }
  )

  val floor = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.int, floor(x).toInt())
      }
    }
  )

  val log10 = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.int, log(x, 10.0).toInt())
      }
    }
  )

  val log = function(
    ContextParam("x", ContextType.float),
    ContextParam("base", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        it.getDouble("base")?.let { base ->
          return@function ContextValue(ContextType.int, log(x, base).toInt())
        }
      }
      null
    }
  )

  val log2 = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.int, log(x, 2.0).toInt())
      }
    }
  )

  val sin = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, sin(x))
      }
    }
  )

  val cos = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, cos(x))
      }
    }
  )

  val tan = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, tan(x))
      }
    }
  )

  val asin = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, asin(x))
      }
    }
  )

  val acos = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, acos(x))
      }
    }
  )

  val atan = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, atan(x))
      }
    }
  )

  val sinh = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, sinh(x))
      }
    }
  )

  val cosh = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, cosh(x))
      }
    }
  )

  val tanh = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, tanh(x))
      }
    }
  )

  val asinh = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, asinh(x))
      }
    }
  )

  val acosh = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, acosh(x))
      }
    }
  )

  val atanh = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, atanh(x))
      }
    }
  )

  val degrees = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, x * 180 / PI)
      }
    }
  )

  val radians = function(
    ContextParam("x", ContextType.float),
    function = {
      it.getDouble("x")?.let { x ->
        return@function ContextValue(ContextType.float, x * PI / 180)
      }
    }
  )

  override val persistentData: PersistentData = PersistentData()

  override fun contextType(): ContextType<MathUtils> {
    return ContextType.math
  }

  override fun fieldOf(name: String, contextJson: JsonValue?): ContextValue<*>? {
    return when (name) {
      "randint" -> ContextValue(ContextType.function, randint)
      "randrange" -> ContextValue(ContextType.function, randrange)
      "randbool" -> ContextValue(ContextType.function, randbool)
      "abs_int" -> ContextValue(ContextType.function, absInt)
      "pi" -> ContextValue(ContextType.float, PI)
      "e" -> ContextValue(ContextType.float, E)
      "sqrt" -> ContextValue(ContextType.function, sqrt)
      "pow" -> ContextValue(ContextType.function, pow)
      "abs" -> ContextValue(ContextType.function, abs)
      "round" -> ContextValue(ContextType.function, round)
      "ceil" -> ContextValue(ContextType.function, ceil)
      "floor" -> ContextValue(ContextType.function, floor)
      "log" -> ContextValue(ContextType.function, log)
      "log2" -> ContextValue(ContextType.function, log2)
      "log10" -> ContextValue(ContextType.function, log10)
      "sin" -> ContextValue(ContextType.function, sin)
      "cos" -> ContextValue(ContextType.function, cos)
      "tan" -> ContextValue(ContextType.function, tan)
      "asin" -> ContextValue(ContextType.function, asin)
      "acos" -> ContextValue(ContextType.function, acos)
      "atan" -> ContextValue(ContextType.function, atan)
      "sinh" -> ContextValue(ContextType.function, sinh)
      "cosh" -> ContextValue(ContextType.function, cosh)
      "tanh" -> ContextValue(ContextType.function, tanh)
      "asinh" -> ContextValue(ContextType.function, asinh)
      "acosh" -> ContextValue(ContextType.function, acosh)
      "atanh" -> ContextValue(ContextType.function, atanh)
      "degrees" -> ContextValue(ContextType.function, degrees)
      "radians" -> ContextValue(ContextType.function, radians)
      else -> null
    }
  }
}
