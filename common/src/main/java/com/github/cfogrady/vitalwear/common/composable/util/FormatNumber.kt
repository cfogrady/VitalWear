package com.github.cfogrady.vitalwear.common.composable.util

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

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

@Preview
@Composable
fun TextSpacingTest() {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text(text = "Tpq^1", fontSize = 3.em)
        Text(text = "Tq^2p\nq^", fontSize = 3.em, textAlign = TextAlign.Center)
        Text(text = "^3", fontSize = 3.em)
    }
}