package android.util

class Log {
    companion object {
        @JvmStatic
        fun i(tag: String, message: String): Int {
            println("$tag: $message")
            return 0
        }
    }
}