package com.github.cfogrady.vitalwear

class ShutdownManager(private val saveService: SaveService) {
    fun shutdown() {
        saveService.saveBlocking()
    }
}