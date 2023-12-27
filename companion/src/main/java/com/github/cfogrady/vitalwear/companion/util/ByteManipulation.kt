package com.github.cfogrady.vitalwear.companion.util

import java.lang.IllegalArgumentException

enum class Endian {
    Big,
    Little
}
fun ByteArray.getUInt32(index: Int = 0, endian: Endian = Endian.Little): UInt {
    if (this.size < index + 4) {
        throw IllegalArgumentException("Must be 4 bytes from index to get a UInt")
    }
    var result: UInt = 0u
    for (i in 0 until 4) {
        result = if (endian == Endian.Big) {
            result or ((this[index+i].toUInt() and 0xFFu) shl 8*(3 - i))
        } else {
            result or ((this[index+i].toUInt() and 0xFFu) shl 8*(i))
        }
    }
    return result
}

fun UInt.toByteArray(endian: Endian = Endian.Little): ByteArray {
    val byteArray = byteArrayOf(0, 0, 0, 0)
    for(i in 0 until 4) {
        if(endian == Endian.Little) {
            byteArray[i] = ((this shr 8*i) and 255u).toByte()
        } else {
            byteArray[3-i] = ((this shr 8*i) and 255u).toByte()
        }
    }
    return byteArray
}

fun ByteArray.getUInt16(index: Int = 0, endian: Endian = Endian.Little): UShort {
    if (this.size < index + 2) {
        throw IllegalArgumentException("Must be 2 bytes from index to get a UInt")
    }
    var result: UInt = 0u
    for (i in 0 until 2) {
        result = if (endian == Endian.Big) {
            result or ((this[index+i].toUInt() and 0xFFu) shl 8*(1 - i))
        } else {
            result or ((this[index+i].toUInt() and 0xFFu) shl 8*(i))
        }
    }
    return result.toUShort()
}

fun UShort.toByteArray(endian: Endian = Endian.Little): ByteArray {
    val byteArray = byteArrayOf(0, 0)
    val asUInt = this.toUInt()
    for(i in 0 until 2) {
        if(endian == Endian.Little) {
            byteArray[i] = ((asUInt shr 8*i) and 255u).toByte()
        } else {
            byteArray[1-i] = ((asUInt shr 8*i) and 255u).toByte()
        }
    }
    return byteArray
}
