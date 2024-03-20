package com.github.cfogrady.vitalwear.debug

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import org.tinylog.Logger
import org.tinylog.configuration.Configuration
import org.tinylog.provider.ProviderRegistry
import timber.log.Timber

@SuppressLint("LogNotTimber")
// extend DebugTree instead of raw Tree so we get classes as tags
class TinyLogTree(context: Context): Timber.DebugTree() {

    companion object {
        fun shutdown() {
            ProviderRegistry.getLoggingProvider().shutdown();
        }

        private fun setupRollingLogs(logsDir: String) {
            Configuration.set("writer", "rolling file")
            Configuration.set("writer.level", "info")
            Configuration.set("writer.backups", "3")
            Configuration.set("writer.format", "{date: HH:mm:ss.SSS}{pipe}{tag}{pipe}{level}{pipe}{message}")
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
        val taggedLogger = Logger.tag(tag)
        when(priority) {
            // DebugTree already appended the throwable to the message
            Log.VERBOSE -> taggedLogger.debug {message}
            Log.ASSERT -> taggedLogger.error {message}
            Log.DEBUG -> taggedLogger.debug {message}
            Log.INFO -> taggedLogger.info {message}
            Log.WARN -> taggedLogger.warn {message}
            Log.ERROR -> taggedLogger.error { message }
            else -> Log.e("TinyLogTree", "Failed to interpret priority")
        }
    }
}