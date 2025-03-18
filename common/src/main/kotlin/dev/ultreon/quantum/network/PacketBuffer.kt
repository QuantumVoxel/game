package dev.ultreon.quantum.network

import dev.ultreon.quantum.ExperimentalApi
import ktx.collections.GdxByteArray
import java.io.EOFException

@ExperimentalApi
class PacketBuffer(val data: GdxByteArray, val mode: Mode = Mode.READ) {
  var pos: Int = 0
    private set

  fun readBytes(length: Int): ByteArray {
    if (pos + length > data.size) {
      throw EOFException()
    }
    val bytes = data.items.slice(pos until pos + length).toByteArray()
    pos += length
    return bytes
  }

  fun readByte(): Byte {
    if (pos >= data.size) {
      throw EOFException()
    } else {
      return data[pos++]
    }
  }

  fun writeBytes(bytes: ByteArray) {
    ensureCapacity(bytes.size)

    data.addAll(bytes, pos, bytes.size)
  }

  fun writeByte(byte: Byte) {
    ensureCapacity(1)
    data.add(byte)
  }

  fun readShort(): Short {
    return (data[pos++].toInt() and 0xFF shl 8 or
            (data[pos++].toInt() and 0xFF)).toShort()
  }

  fun writeShort(short: Short) {
    ensureCapacity(2)
    data[pos++] = (short.toInt() shr 8 and 0xFF).toByte()
    data[pos++] = (short.toInt() and 0xFF).toByte()
  }

  fun readInt(): Int {
    return (data[pos++].toInt() and 0xFF shl 24 or
            (data[pos++].toInt() and 0xFF shl 16) or
            (data[pos++].toInt() and 0xFF shl 8) or
            (data[pos++].toInt() and 0xFF)).toInt()
  }

  fun writeInt(int: Int) {
    ensureCapacity(4)
    data[pos++] = (int shr 24 and 0xFF).toByte()
    data[pos++] = (int shr 16 and 0xFF).toByte()
    data[pos++] = (int shr 8 and 0xFF).toByte()
    data[pos++] = (int and 0xFF).toByte()
  }

  fun readLong(): Long {
    return (data[pos++].toLong() and 0xFF shl 56 or
            (data[pos++].toLong() and 0xFF shl 48) or
            (data[pos++].toLong() and 0xFF shl 40) or
            (data[pos++].toLong() and 0xFF shl 32) or
            (data[pos++].toLong() and 0xFF shl 24) or
            (data[pos++].toLong() and 0xFF shl 16) or
            (data[pos++].toLong() and 0xFF shl 8) or
            (data[pos++].toLong() and 0xFF)).toLong()
  }

  fun writeLong(long: Long) {
    ensureCapacity(8)
    data[pos++] = (long shr 56 and 0xFF).toByte()
    data[pos++] = (long shr 48 and 0xFF).toByte()
    data[pos++] = (long shr 40 and 0xFF).toByte()
    data[pos++] = (long shr 32 and 0xFF).toByte()
    data[pos++] = (long shr 24 and 0xFF).toByte()
    data[pos++] = (long shr 16 and 0xFF).toByte()
    data[pos++] = (long shr 8 and 0xFF).toByte()
    data[pos++] = (long and 0xFF).toByte()
  }

  fun readFloat(): Float {
    val bits = readInt()
    return Float.fromBits(bits)
  }

  fun writeFloat(float: Float) {
    ensureCapacity(4)
    writeInt(float.toBits())
  }

  fun readDouble(): Double {
    val bits = readLong()
    return Double.fromBits(bits)
  }

  fun writeDouble(double: Double) {
    ensureCapacity(8)
    writeLong(double.toBits())
  }

  fun readString(): String {
    val length = readShort().toInt()
    val bytes = readBytes(length)
    return String(bytes)
  }

  fun writeString(string: String) {
    ensureCapacity(2 + string.length * 2)
    val bytes = string.toByteArray()
    writeShort(bytes.size.toShort())
    writeBytes(bytes)
  }

  fun readBoolean(): Boolean {
    return data[pos++].toInt() != 0
  }

  fun writeBoolean(boolean: Boolean) {
    ensureCapacity(1)
    data[pos++] = if (boolean) 1.toByte() else 0.toByte()
  }

  fun clear() {
    data.clear()
  }

  fun capacity(): Int {
    return data.size
  }

  private fun ensureCapacity(length: Int) {
    if (mode == Mode.WRITE) {
      if (pos + length > data.size) {
        data.ensureCapacity(pos + length)
      }
    } else if (mode == Mode.READ && pos + length > data.size) {
      throw EOFException()
    }
  }

  enum class Mode {
    READ, WRITE;
  }
}
