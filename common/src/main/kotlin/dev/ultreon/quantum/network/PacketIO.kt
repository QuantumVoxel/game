package dev.ultreon.quantum.network

import com.badlogic.gdx.utils.DataInput
import com.badlogic.gdx.utils.DataOutput
import dev.ultreon.quantum.ExperimentalApi

@ExperimentalApi
class PacketIO(val input: DataInput?, val output: DataOutput?) {
  fun readBoolean(): Boolean = input!!.readBoolean()
  fun writeBoolean(boolean: Boolean) = output!!.writeBoolean(boolean)

  fun readByte(): Byte = input!!.readByte()
  fun writeByte(byte: Byte) = output!!.writeByte(byte.toInt())

  fun readUByte(): UByte = input!!.readByte().toUByte()
  fun writeUByte(ubyte: UByte) = output!!.writeByte(ubyte.toByte().toInt())

  fun readShort(): Short = input!!.readShort()
  fun writeShort(short: Short) = output!!.writeShort(short.toInt())

  fun readUShort(): UShort = input!!.readShort().toUShort()
  fun writeUShort(ushort: UShort) = output!!.writeShort(ushort.toShort().toInt())

  fun readMedium(): Int = input!!.readUnsignedByte() shl 16 or (input.readUnsignedByte() shl 8) or input.readUnsignedByte()
  fun writeMedium(medium: Int) { output!!.writeByte(medium shr 16 and 0xFF) ; output.writeByte(medium shr 8 and 0xFF) ; output.writeByte(medium and 0xFF) }

  fun readInt(): Int = input!!.readInt()
  fun writeInt(int: Int) = output!!.writeInt(int)

  fun readUInt(): UInt = input!!.readInt(true).toUInt()
  fun writeUInt(uint: UInt) = output!!.writeInt(uint.toInt(), true)

  fun readLong(): Long = input!!.readLong()
  fun writeLong(long: Long) = output!!.writeLong(long)

  fun readULong(): ULong = input!!.readLong().toULong()
  fun writeULong(ulong: ULong) = output!!.writeLong(ulong.toLong())

  fun readFloat(): Float = input!!.readFloat()
  fun writeFloat(float: Float) = output!!.writeFloat(float)

  fun readDouble(): Double = input!!.readDouble()
  fun writeDouble(double: Double) = output!!.writeDouble(double)

  fun readString(): String = input!!.readUTF()
  fun writeString(string: String) = output!!.writeUTF(string)

  fun readBytes(length: Int): ByteArray {
    val bytes = ByteArray(length)
    input!!.readFully(bytes)
    return bytes
  }

  @ExperimentalApi
  fun readVarInt(): Int {
      var value = 0
      var position = 0
      var currentByte: Int
      while (true) {
          currentByte = input!!.readByte().toInt()
          value = value or ((currentByte and 0x7F) shl position)
          if ((currentByte and 0x80) == 0) break
          position += 7
          if (position >= 32) throw IllegalArgumentException("VarInt is too big")
      }
      return value
  }

  @ExperimentalApi
  fun writeVarInt(value: Int) {
      var temp = value
      while (true) {
          if ((temp and 0xFFFFFF80.toInt()) == 0) {
              output!!.writeByte(temp)
              return
          }
          output!!.writeByte((temp and 0x7F) or 0x80)
          temp = temp ushr 7
      }
  }


  fun writeBytes(bytes: ByteArray) = output!!.write(bytes)
  fun writeBytes(bytes: ByteArray, offset: Int, length: Int) = output!!.write(bytes, offset, length)

  fun readBytes(bytes: ByteArray) = input!!.readFully(bytes)
  fun readBytes(bytes: ByteArray, offset: Int, length: Int) = input!!.readFully(bytes, offset, length)

  fun skipBytes(length: Int) = input!!.skipBytes(length)

  fun flush() = output!!.flush()
  fun close() = output!!.close()
}
