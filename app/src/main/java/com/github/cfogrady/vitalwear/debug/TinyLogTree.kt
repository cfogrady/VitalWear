package com.github.cfogrady.vitalwear.debug

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import org.tinylog.Logger
import org.tinylog.configuration.Configuration
import org.tinylog.provider.ProviderRegistry
import timber.log.Timber
import java.io.File

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

        private fun getLogsDir(context: Context): File {
            return File(context.filesDir, "logs")
        }

        private val logFileRegexp = Regex("""log_(\d{4}-\d{2}-\d{2})-(\d+)\.txt""")
        private const val START_DATE = "0000-00-00"

        fun getMostRecentLogFile(context: Context): File? {
            val logsDir = getLogsDir(context)
            return getMostRecentLogFileFromFiles(logsDir.listFiles()!!)
        }

        internal fun getMostRecentLogFileFromFiles(files: Array<out File>): File? {
            var date = START_DATE
            var count = 0
            var mostRecentFile: File? = null
            for(child in files) {
                val matchResult = logFileRegexp.find(child.name)
                if(matchResult != null) {
                    val childDate = matchResult.groups[1]?.value ?: START_DATE
                    val childCount = matchResult.groups[2]?.value?.toInt() ?: 0
                    if(childDate > date) {
                        mostRecentFile = child
                        date = childDate
                        count = childCount
                    } else if (childDate == date && childCount > count) {
                        mostRecentFile = child
                        count = childCount
                    }
                }
            }
            return mostRecentFile
        }
    }

    init {
        val logsDir = getLogsDir(context).absolutePath
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
            else -> taggedLogger.error {"Invalid priority $priority For Message! $message"}
        }
    }
}