package com.github.cfogrady.vitalwear

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Text
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import kotlinx.coroutines.CoroutineDispatcher

const val LOADING_TEXT = "Loading..."

@Composable
fun Loading(loadingText: String = LOADING_TEXT, scope: CoroutineDispatcher = Dispatchers.Default, work: () -> Unit) {
    val imageLoader = ImageLoader.Builder(LocalContext.current).components {
        add(ImageDecoderDecoder.Factory())
    }.build()

    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp))
    {
        Text(text = loadingText)
        Image(
            painter = rememberAsyncImagePainter(R.drawable.loading_icon, imageLoader),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
    LaunchedEffect(key1 = loadingText) {
        withContext(scope) {
            work.invoke()
        }
    }
}