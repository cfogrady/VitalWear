package com.github.cfogrady.vitalwear.debug

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import org.tinylog.Logger
import org.tinylog.configuration.Configuration
import org.tinylog.provider.ProviderRegistry
import timber.log.Timber

@SuppressLint("LogNotTimber")
class TinyLogTree(context: Context): Timber.DebugTree() {

    companion object {
        fun shutdown() {
            ProviderRegistry.getLoggingProvider().shutdown();
        }

        private fun setupRollingLogs(logsDir: String) {
            Configuration.set("writer", "rolling file")
            Configuration.set("writer.level", "info")
            Configuration.set("writer.backups", "3")
            Configuration.set("writer.format", "{date: HH:mm:ss.SSS}{pipe}{level}{pipe}{message}")
            Configuration.set("writer.file", "$logsDir/log_{date:yyyy-MM-dd}_{count}.txt")
            Configuration.set("writer.policies", "daily, size: 10mb")
            Configuration.set("writer.buffered", "true")
        }
    }

    init {
        val logsDir = "${context.filesDir.absolutePath}/logs"
        setupRollingLogs(logsDir)
        Configuration.set("writingthread", "true")
        Log.i("TinyLogTree", "Log Dir: $logsDir")
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        when(priority) {
            Log.VERBOSE -> Logger.debug(t, "$tag|$message")
            Log.ASSERT -> Logger.error(t, "$tag|$message")
            Log.DEBUG -> Logger.debug(t, "$tag|$message")
            Log.INFO -> Logger.info(t, "$tag|$message")
            Log.WARN -> Logger.warn(t, "$tag|$message")
            Log.ERROR -> Logger.error(t, "$tag|$message")
            else -> Log.e("TinyLogTree", "Failed to interpret priority")
        }
    }
}