package com.github.cfogrady.vitalwear.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em

class ConfirmationComposableFactory {

    companion object {

        @Composable
        fun Confirmation(prompt: String, onResult: (Boolean)->Unit) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = prompt, color = Color.White, modifier = Modifier.padding(10.dp), fontSize = 6.em)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    Button(onClick = { onResult.invoke(true) }) {
                        Text(text = "Ok")
                    }
                    Button(onClick = { onResult.invoke(false) }) {
                        Text(text = "Cancel")
                    }
                }
            }
        }
    }
}