package com.github.cfogrady.vitalwear

import com.github.cfogrady.vitalwear.debug.TinyLogTree

class ShutdownManager(private val saveService: SaveService) {
    fun shutdown() {
        saveService.saveBlocking()
        TinyLogTree.shutdown()
    }
}