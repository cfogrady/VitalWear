package com.github.cfogrady.vitalwear.companion.util

import org.junit.Assert
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class ByteManipulationTest {

    @Test
    fun testGetUInt32Little() {
        var bytes = byteArrayOf(127, 0, 0, 0)
        assertEquals(127u, bytes.getUInt32(0, Endian.Little))
        bytes = byteArrayOf(0, 1, 0,0)
        assertEquals(256u, bytes.getUInt32(0, Endian.Little))
        bytes = byteArrayOf(1, 1, 0,0)
        assertEquals(257u, bytes.getUInt32(0, Endian.Little))
        bytes = byteArrayOf(0, 0, 1,0)
        assertEquals(65536u, bytes.getUInt32(0, Endian.Little))
        bytes = byteArrayOf(0, 0, 0,1)
        assertEquals(16777216u, bytes.getUInt32(0, Endian.Little))
        bytes = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),0xFF.toByte())
        assertEquals(4294967295u, bytes.getUInt32(0, Endian.Little))
        bytes = byteArrayOf(0, 127, 0, 0, 0)
        assertEquals(127u, bytes.getUInt32(1, Endian.Little))
    }

    @Test
    fun testUIntToByteArrayLittle() {
        assertArrayEquals(byteArrayOf(127, 0, 0, 0), 127u.toByteArray())
        assertArrayEquals(byteArrayOf(0, 1, 0, 0), 256u.toByteArray())
        assertArrayEquals(byteArrayOf(1, 1, 0, 0), 257u.toByteArray())
        assertArrayEquals(byteArrayOf(0, 0, 1, 0), 65536u.toByteArray())
        assertArrayEquals(byteArrayOf(0, 0, 0, 1), 16777216u.toByteArray())
        assertArrayEquals(byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),0xFF.toByte()), 4294967295u.toByteArray())
    }

    @Test
    fun testGetUInt32Big() {
        var bytes = byteArrayOf(0, 0, 0, 127)
        assertEquals(127u, bytes.getUInt32(0, Endian.Big))
        bytes = byteArrayOf(0, 0, 1, 0)
        assertEquals(256u, bytes.getUInt32(0, Endian.Big))
        bytes = byteArrayOf(0, 0, 1, 1)
        assertEquals(257u, bytes.getUInt32(0, Endian.Big))
        bytes = byteArrayOf(0, 1, 0, 0)
        assertEquals(65536u, bytes.getUInt32(0, Endian.Big))
        bytes = byteArrayOf(1, 0, 0, 0)
        assertEquals(16777216u, bytes.getUInt32(0, Endian.Big))
        bytes = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),0xFF.toByte())
        assertEquals(4294967295u, bytes.getUInt32(0, Endian.Big))
        bytes = byteArrayOf(0, 0, 0, 0, 127)
        assertEquals(127u, bytes.getUInt32(1, Endian.Big))
    }

    @Test
    fun testUIntToByteArrayBig() {
        assertArrayEquals(byteArrayOf(0, 0, 0, 127), 127u.toByteArray(Endian.Big))
        assertArrayEquals(byteArrayOf(0, 0, 1, 0), 256u.toByteArray(Endian.Big))
        assertArrayEquals(byteArrayOf(0, 0, 1, 1), 257u.toByteArray(Endian.Big))
        assertArrayEquals(byteArrayOf(0, 1, 0, 0), 65536u.toByteArray(Endian.Big))
        assertArrayEquals(byteArrayOf(1, 0, 0, 0), 16777216u.toByteArray(Endian.Big))
        assertArrayEquals(byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(),0xFF.toByte()), 4294967295u.toByteArray(Endian.Big))
    }

    @Test
    fun testGetUInt16Little() {
        var bytes = byteArrayOf(127, 0)
        assertEquals(127u.toUShort(), bytes.getUInt16(0, Endian.Little))
        bytes = byteArrayOf(0, 1)
        assertEquals(256u.toUShort(), bytes.getUInt16(0, Endian.Little))
        bytes = byteArrayOf(1, 1)
        assertEquals(257u.toUShort(), bytes.getUInt16(0, Endian.Little))
        bytes = byteArrayOf(0, 0, 1,0)
        assertEquals(1u.toUShort(), bytes.getUInt16(2, Endian.Little))
    }

    @Test
    fun testUShortToByteArrayLittle() {
        assertArrayEquals(byteArrayOf(127, 0), 127u.toUShort().toByteArray())
        assertArrayEquals(byteArrayOf(0, 1), 256u.toUShort().toByteArray())
        assertArrayEquals(byteArrayOf(1, 1), 257u.toUShort().toByteArray())
    }

    @Test
    fun testGetUInt16Big() {
        var bytes = byteArrayOf(0, 127)
        assertEquals(127u.toUShort(), bytes.getUInt16(0, Endian.Big))
        bytes = byteArrayOf(1, 0)
        assertEquals(256u.toUShort(), bytes.getUInt16(0, Endian.Big))
        bytes = byteArrayOf(1, 1)
        assertEquals(257u.toUShort(), bytes.getUInt16(0, Endian.Big))
        bytes = byteArrayOf(0, 0, 0,1)
        assertEquals(1u.toUShort(), bytes.getUInt16(2, Endian.Big))
    }

    @Test
    fun testUShortToByteArrayBig() {
        assertArrayEquals(byteArrayOf(0, 127), 127u.toUShort().toByteArray(Endian.Big))
        assertArrayEquals(byteArrayOf(1, 0), 256u.toUShort().toByteArray(Endian.Big))
        assertArrayEquals(byteArrayOf(1, 1), 257u.toUShort().toByteArray(Endian.Big))
    }
}