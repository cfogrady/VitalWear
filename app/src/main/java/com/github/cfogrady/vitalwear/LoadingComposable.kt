package com.github.cfogrady.vitalwear

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

const val LOADING_TEXT = "Loading..."

@Composable
fun Loading(work: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
        .fillMaxWidth()
        .padding(20.dp)) {
        Text(text = LOADING_TEXT)
    }
    LaunchedEffect(key1 = LOADING_TEXT) {
        withContext(Dispatchers.Default) {
            work.invoke()
        }
    }
}