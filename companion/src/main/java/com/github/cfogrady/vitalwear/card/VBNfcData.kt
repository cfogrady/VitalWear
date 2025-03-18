package com.github.cfogrady.vitalwear.card

import android.nfc.tech.MifareUltralight
import com.github.cfogrady.vitalwear.util.Endian
import com.github.cfogrady.vitalwear.util.getUInt16
import com.github.cfogrady.vitalwear.util.getUInt32
import com.github.cfogrady.vitalwear.util.toByteArray
import kotlin.experimental.and
import kotlin.experimental.or

class VBNfcData(nfcData: MifareUltralight) {
    companion object {
        private const val ITEM_ID_BE: UShort = 4u
        private const val STATUS_READY_FLAG: Byte = 0b00000001
        private const val STATUS_DIM_READY_FLAG: Byte = 0b00000010
        private val STATUS_DIM_IS_READY = STATUS_READY_FLAG or STATUS_DIM_READY_FLAG
        private const val OPERATION_READY: Byte = 1
        private const val OPERATION_CHECK_DIM: Byte = 3
    }

    val magic: UInt
    val itemId: UShort
    val itemNumber: UShort
    val status: Byte
    val operation: Byte
    val dimId: UShort
    init {
        val readData = nfcData.transceive(byteArrayOf(0x30, 0x04))
        magic = readData.getUInt32(0, Endian.Big)
        itemId = readData.getUInt16(4, Endian.Big)
        itemNumber = readData.getUInt16(6, Endian.Big)
        status = readData[8]
        if(itemId == ITEM_ID_BE) {
            operation = readData[9]
            dimId = readData.getUInt16(10, Endian.Big) // successfully read after card insert
        } else {
            dimId = readData[9].toUShort()
            operation = readData[10]
        }
    }

    fun writeCardCheck(nfcData: MifareUltralight, dimId: UShort) {
        if (itemId == ITEM_ID_BE) {
            val dimData = dimId.toByteArray(endian = Endian.Big)
            nfcData.writePage(6, byteArrayOf(STATUS_READY_FLAG, OPERATION_CHECK_DIM, dimData[0], dimData[1]))
        } else {
            nfcData.writePage(6, byteArrayOf(STATUS_READY_FLAG, dimId.toByte(), OPERATION_CHECK_DIM, 0))
        }
    }

    fun wasCardIdValidated(dimId: UShort): Boolean {
        return dimId == this.dimId && operation == OPERATION_READY && (status and STATUS_DIM_IS_READY == STATUS_DIM_IS_READY)
    }
}