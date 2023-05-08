package com.github.cfogrady.vitalwear

import com.github.cfogrady.vitalwear.debug.ExceptionService
import com.github.cfogrady.vitalwear.steps.SensorStepService
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate

class ApplicationBootManager(private val stepService: SensorStepService, private val saveService: SaveService) {

    // We call this on both application.onCreate and BroadcastReceiver onBoot at the moment.
    // We do this to ensure that this is done onBoot as well as app restart, but we probably can
    // just have the boradcast receiver log and only call this from onCreate.
    // TODO: Test the above
    @Synchronized
    fun onStartup() {
        GlobalScope.launch {
            try {
                if(stepService.handleBoot(LocalDate.now())) {
                    saveService.save()
                }
            } catch (e: java.lang.Exception) {
                ExceptionService.instance!!.logException(e)
            }
        }
    }
}