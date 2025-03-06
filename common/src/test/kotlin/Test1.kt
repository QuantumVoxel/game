import dev.ultreon.quantum.ExperimentalApi
import dev.ultreon.quantum.server.PaletteStorage
import kotlin.test.Test
import kotlin.test.assertEquals

class Test1 {
  @OptIn(ExperimentalApi::class)
  @Test
  fun test1() {
    // Test creating a map with maximum size
    val storageMapMaxSize = PaletteStorage(65536, "none")
    storageMapMaxSize[1] = "value1"
    storageMapMaxSize[32767] = "value2"
    assertEquals("value1", storageMapMaxSize[1])
    assertEquals("value2", storageMapMaxSize[32767])

    // Test adding and retrieving values within the specified size
    val storageMap = PaletteStorage(1000, "none")
    storageMap[100] = "valueA"
    storageMap[200] = "valueB"
    assertEquals("valueA", storageMap[100])
    assertEquals("valueB", storageMap[200])

    // Test duplicate value with different keys
    storageMap[300] = "valueA"
    assertEquals("valueA", storageMap[300])

    // Test updating an existing key with a new value
    storageMap[100] = "valueC"
    assertEquals("valueC", storageMap[100])
    assertEquals("valueB", storageMap[200])
    assertEquals("valueA", storageMap[300])

    println("All tests passed!")
  }
}

fun main() = Test1().test1()
