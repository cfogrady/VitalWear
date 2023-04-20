package com.github.cfogrady.vitalwear.composable.util

fun formatNumber(value: Int, digits: Int): String {
    var str = value.toString()
    if(str.length < digits) {
        val zeroesBuilder = StringBuilder()
        for(i in 1..(digits - str.length)) {
            zeroesBuilder.append("0")
        }
        str = zeroesBuilder.append(str).toString()
    }
    return str
}