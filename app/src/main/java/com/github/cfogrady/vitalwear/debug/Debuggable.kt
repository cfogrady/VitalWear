package com.github.cfogrady.vitalwear.debug

interface Debuggable {

    fun debug(): List<Pair<String, String>>
}