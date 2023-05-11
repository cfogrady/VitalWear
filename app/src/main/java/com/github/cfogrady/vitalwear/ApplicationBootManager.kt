package com.github.cfogrady.vitalwear

import com.github.cfogrady.vitalwear.character.BEMUpdater
import com.github.cfogrady.vitalwear.character.CharacterManagerImpl
import com.github.cfogrady.vitalwear.card.CardLoader
import com.github.cfogrady.vitalwear.debug.ExceptionService
import com.github.cfogrady.vitalwear.steps.SensorStepService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class ApplicationBootManager(private val cardLoader: CardLoader,
                             private val characterManager: CharacterManagerImpl,
                             private val stepService: SensorStepService,
                             private val bemUpdater: BEMUpdater,
                             private val saveService: SaveService,) {
    @Synchronized
    fun onStartup() {
        GlobalScope.launch {
            try {
                // characterManager init will load WorkManager configuration
                characterManager.init(bemUpdater)
                bemUpdater.scheduleExactMoodUpdates()
                if(stepService.handleBoot(LocalDate.now())) {
                    saveService.save()
                }
            } catch (e: java.lang.Exception) {
                ExceptionService.instance!!.logException(e)
            }
        }
    }
}