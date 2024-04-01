package com.github.cfogrady.vitalwear

import androidx.compose.animation.core.KeyframesSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.SnapSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.ImageDecoderDecoder
import kotlinx.coroutines.CoroutineDispatcher

const val LOADING_TEXT = "Loading..."

@Preview
@Composable
fun TestLoading() {
    Loading {

    }
}

@Composable
fun Loading(loadingText: String = LOADING_TEXT, scope: CoroutineDispatcher = Dispatchers.Default, work: suspend () -> Unit) {
    var targetValue by remember { mutableFloatStateOf(0f) }
    val rotation by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = SnapSpec(100),
//        animationSpec = infiniteRepeatable(
//            animation = tween(2500, easing = LinearEasing),
//            repeatMode = RepeatMode.Restart),
        label = "",
        finishedListener = {
            if (it == 360f) {
                targetValue = 45f
            } else {
                targetValue += 45f
            }
        })
    LaunchedEffect(true) {
        targetValue = 45f
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp))
    {
        Text(text = loadingText, color = Color.White)
        Image(
            painter = painterResource(id = com.github.cfogrady.vitalwear.common.R.drawable.loading_spinner),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .rotate(rotation)
        )
    }
    LaunchedEffect(key1 = loadingText) {
        withContext(scope) {
            work.invoke()
        }
    }
}