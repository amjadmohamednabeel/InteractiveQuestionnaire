//MainActivity.kt

/////////////////////////////////////////////////////////////////////////////
//Author : Amjad Mohamed Nabeel
//Date : 2025-07-10
//Version : 1.0.0
//This file is part of the Interactive Video Questionnaire project.
//////////////////////////////////////////////////////////////////////////////

@file:androidx.media3.common.util.UnstableApi
package com.interactivevideoquestionnaire

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

// Add these constants and sealed classes at the top after the imports
private object Constants {
    const val INACTIVITY_TIMEOUT = 12000L // 12 seconds
    const val TOUCH_ENABLE_DELAY = 800L    // 0.8 seconds
    const val STATE_CHANGE_DELAY = 4000L   // 4 seconds
}

sealed class Gender {
    object Male : Gender()
    object Female : Gender()
    
    fun toKey(): String = when (this) {
        is Male -> "male"
        is Female -> "female"
    }
}

sealed class AgeGroup {
    object Young : AgeGroup()      // 18-30
    object Middle : AgeGroup()     // 30-45
    object Senior : AgeGroup()     // above45
    
    fun toKey(): String = when (this) {
        is Young -> "18-30"
        is Middle -> "30-45"
        is Senior -> "above45"
    }
}

sealed class Lifestyle {
    object Sedentary : Lifestyle()
    object Normal : Lifestyle()
    object Athlete : Lifestyle()
    
    fun toKey(): String = when (this) {
        is Sedentary -> "sedentary"
        is Normal -> "normal"
        is Athlete -> "athlete"
    }
}

data class UserSelection(
    val gender: Gender? = null,
    val ageGroup: AgeGroup? = null,
    val lifestyle: Lifestyle? = null
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable decor fitting system windows to false for edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())


        setContent {
            val base = "android.resource://${packageName}/"
            VideoTouchSelector(
                startUri = "${base}${R.raw.start}".toUri(),
                genderUri = "${base}${R.raw.gender}".toUri(),
                ageUri = "${base}${R.raw.age}".toUri(),
                lifestyleUri = "${base}${R.raw.lifestyle}".toUri(),
                resultMap = mapOf(
                    "athlete" to mapOf(
                        "18-30" to mapOf("male" to R.raw.v1, "female" to R.raw.v4),
                        "30-45" to mapOf("male" to R.raw.v2, "female" to R.raw.v5),
                        "above45" to mapOf("male" to R.raw.v3, "female" to R.raw.v6)
                    ),
                    "normal" to mapOf(
                        "18-30" to mapOf("male" to R.raw.v7, "female" to R.raw.v10),
                        "30-45" to mapOf("male" to R.raw.v8, "female" to R.raw.v11),
                        "above45" to mapOf("male" to R.raw.v9, "female" to R.raw.v12)
                    ),
                    "sedentary" to mapOf(
                        "18-30" to mapOf("male" to R.raw.v13, "female" to R.raw.v16),
                        "30-45" to mapOf("male" to R.raw.v14, "female" to R.raw.v17),
                        "above45" to mapOf("male" to R.raw.v15, "female" to R.raw.v18)
                    )
                )
            )
        }
    }
}

@Composable
fun VideoTouchSelector(
    startUri: Uri,
    genderUri: Uri,
    ageUri: Uri,
    lifestyleUri: Uri,
    resultMap: Map<String, Map<String, Map<String, Int>>>
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }

    var screenState by remember { mutableStateOf("start") }
    var isTouchEnabled by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // State for tracking product video playback and inactivity
    var isProductVideoPlaying by remember { mutableStateOf(false) }
    var inactivityJob by remember { mutableStateOf<Job?>(null) }

    var userSelection by remember { mutableStateOf(UserSelection()) }
    var currentResultResId by remember { mutableStateOf<Int?>(null) }

    // For tracking video end callbacks
    var currentOnEnded: (() -> Unit)? by remember { mutableStateOf(null) }

    // ExoPlayer listener to detect when videos finish playing
    val playbackListener = remember {
        object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    currentOnEnded?.invoke()
                    currentOnEnded = null
                }
            }
        }
    }

    data class ProductZone(val rowRange: IntRange, val col: Int, val productResId: Int)

    val productGridMap = mapOf(
        R.raw.v1 to listOf(
            ProductZone(2..4, 1, R.raw.l_carnitine),
            ProductZone(2..4, 2, R.raw.magnesium),
            ProductZone(2..4, 5, R.raw.omega3),
            ProductZone(2..4, 6, R.raw.iron)
        ),
        R.raw.v2 to listOf(
            ProductZone(2..4, 1, R.raw.l_carnitine),
            ProductZone(2..4, 2, R.raw.l_arginine),
            ProductZone(2..4, 5, R.raw.magnesium),
            ProductZone(2..4, 6, R.raw.omega3)
        ),
        R.raw.v3 to listOf(
            ProductZone(2..4, 1, R.raw.l_carnitine),
            ProductZone(2..4, 2, R.raw.l_arginine),
            ProductZone(2..4, 5, R.raw.ginkobiloba),
            ProductZone(2..4, 6, R.raw.magnesium)
        ),
        R.raw.v4 to listOf(
            ProductZone(2..4, 1, R.raw.l_carnitine),
            ProductZone(2..4, 2, R.raw.biotin),
            ProductZone(2..4, 5, R.raw.omega7),
            ProductZone(2..4, 6, R.raw.magnesium)
        ),
        R.raw.v5 to listOf(
            ProductZone(2..4, 1, R.raw.l_carnitine),
            ProductZone(2..4, 2, R.raw.biotin),
            ProductZone(2..4, 5, R.raw.omega7),
            ProductZone(2..4, 6, R.raw.glutathione)
        ),
        R.raw.v6 to listOf(
            ProductZone(2..4, 1, R.raw.l_carnitine),
            ProductZone(2..4, 2, R.raw.biotin),
            ProductZone(2..4, 5, R.raw.omega3),
            ProductZone(2..4, 6, R.raw.glutathione)
        ),
        R.raw.v7 to listOf(
            ProductZone(2..4, 1, R.raw.omega3),
            ProductZone(2..4, 2, R.raw.l_arginine),
            ProductZone(2..4, 5, R.raw.vitamin_d),
            ProductZone(2..4, 6, R.raw.iron)
        ),
        R.raw.v8 to listOf(
            ProductZone(2..4, 1, R.raw.co_q10_200),
            ProductZone(2..4, 2, R.raw.l_arginine),
            ProductZone(2..4, 5, R.raw.vitamin_b),
            ProductZone(2..4, 6, R.raw.vitamin_d)
        ),
        R.raw.v9 to listOf(
            ProductZone(2..4, 1, R.raw.ginkobiloba),
            ProductZone(2..4, 2, R.raw.co_q10_200),
            ProductZone(2..4, 5, R.raw.vitamin_b),
            ProductZone(2..4, 6, R.raw.selenium)
        ),
        R.raw.v10 to listOf(
            ProductZone(2..4, 1, R.raw.biotin),
            ProductZone(2..4, 2, R.raw.iron),
            ProductZone(2..4, 5, R.raw.omega7),
            ProductZone(2..4, 6, R.raw.vitamin_d)
        ),
        R.raw.v11 to listOf(
            ProductZone(2..4, 1, R.raw.ronzalax),
            ProductZone(2..4, 2, R.raw.iron),
            ProductZone(2..4, 5, R.raw.glutathione),
            ProductZone(2..4, 6, R.raw.vitamin_d)
        ),
        R.raw.v12 to listOf(
            ProductZone(2..4, 1, R.raw.ronzalax),
            ProductZone(2..4, 2, R.raw.ginkobiloba),
            ProductZone(2..4, 5, R.raw.iron),
            ProductZone(2..4, 6, R.raw.vitamin_b)
        ),
        R.raw.v13 to listOf(
            ProductZone(2..4, 1, R.raw.omega3),
            ProductZone(2..4, 2, R.raw.vitamin_d),
            ProductZone(2..4, 5, R.raw.iron),
            ProductZone(2..4, 6, R.raw.zinc)
        ),
        R.raw.v14 to listOf(
            ProductZone(2..4, 1, R.raw.ronzalax),
            ProductZone(2..4, 2, R.raw.vitamin_d),
            ProductZone(2..4, 5, R.raw.l_arginine),
            ProductZone(2..4, 6, R.raw.magnesium)
        ),
        R.raw.v15 to listOf(
            ProductZone(2..4, 1, R.raw.selenium),
            ProductZone(2..4, 2, R.raw.zinc),
            ProductZone(2..4, 5, R.raw.ginkobiloba),
            ProductZone(2..4, 6, R.raw.ronzalax)
        ),
        R.raw.v16 to listOf(
            ProductZone(2..4, 1, R.raw.glutathione),
            ProductZone(2..4, 2, R.raw.omega7),
            ProductZone(2..4, 5, R.raw.iron),
            ProductZone(2..4, 6, R.raw.biotin)
        ),
        R.raw.v17 to listOf(
            ProductZone(2..4, 1, R.raw.vitamin_d),
            ProductZone(2..4, 2, R.raw.omega7),
            ProductZone(2..4, 5, R.raw.biotin),
            ProductZone(2..4, 6, R.raw.co_q10_200)
        ),
        R.raw.v18 to listOf(
            ProductZone(2..4, 1, R.raw.ronzalax),
            ProductZone(2..4, 2, R.raw.vitamin_b),
            ProductZone(2..4, 5, R.raw.ginkobiloba),
            ProductZone(2..4, 6, R.raw.omega3)
        )
    )

    fun playVideo(
        uri: Uri,
        loop: Boolean = true,
        nextState: String? = null,
        onEnded: (() -> Unit)? = null
    ) {
        isTouchEnabled = false
        exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer.repeatMode = if (loop) ExoPlayer.REPEAT_MODE_ALL else ExoPlayer.REPEAT_MODE_OFF
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        // Remove any existing listener and add it only if we need to track video end
        exoPlayer.removeListener(playbackListener)
        if (onEnded != null) {
            currentOnEnded = onEnded
            exoPlayer.addListener(playbackListener)
        }

        coroutineScope.launch {
            delay(Constants.TOUCH_ENABLE_DELAY)
            isTouchEnabled = true
            nextState?.let {
                delay(Constants.STATE_CHANGE_DELAY)
                screenState = it
            }
        }
    }

    fun playResultVideo() {
        val gender = userSelection.gender?.toKey() ?: return
        val age = userSelection.ageGroup?.toKey() ?: return
        val lifestyle = userSelection.lifestyle?.toKey() ?: return
        
        val resId = resultMap[lifestyle]?.get(age)?.get(gender)
        if (resId != null) {
            currentResultResId = resId
            val resultUri = "android.resource://${context.packageName}/$resId".toUri()
            playVideo(resultUri, loop = true)
            screenState = "result"

            // Start inactivity timer when on result screen
            inactivityJob?.cancel()
            inactivityJob = coroutineScope.launch {
                delay(Constants.INACTIVITY_TIMEOUT) // 12 seconds timeout
                if (!isProductVideoPlaying && screenState == "result") {
                    screenState = "start"
                }
            }
        }
    }

    LaunchedEffect(screenState) {
        // Cancel any existing inactivity timeout
        inactivityJob?.cancel()

        when (screenState) {
            "start" -> {
                playVideo(startUri)
            }
            "gender" -> {
                playVideo(genderUri)
                inactivityJob = coroutineScope.launch {
                    delay(Constants.INACTIVITY_TIMEOUT)
                    if (screenState == "gender") {
                        screenState = "start"
                    }
                }
            }
            "age" -> {
                playVideo(ageUri)
                inactivityJob = coroutineScope.launch {
                    delay(Constants.INACTIVITY_TIMEOUT)
                    if (screenState == "age") {
                        screenState = "start"
                    }
                }
            }
            "lifestyle" -> {
                playVideo(lifestyleUri)
                inactivityJob = coroutineScope.launch {
                    delay(Constants.INACTIVITY_TIMEOUT)
                    if (screenState == "lifestyle") {
                        screenState = "start"
                    }
                }
            }
            "result" -> {
                if (!isProductVideoPlaying) {
                    playResultVideo()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.removeListener(playbackListener)
            exoPlayer.release()
            inactivityJob?.cancel()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(screenState, isTouchEnabled) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()

                        if (event.changes.isEmpty()) continue
                        if (!isTouchEnabled) {
                            event.changes.forEach { it.consume() }
                            continue
                        }

                        val pos = event.changes[0].position
                        val width = size.width
                        val height = size.height
                        if (width == 0 || height == 0) continue

                        val cols = if (screenState == "result") 6 else 3
                        val rows = if (screenState == "result") 6 else 3

                        val cellWidth = width / cols
                        val cellHeight = height / rows

                        val col = (pos.x / cellWidth).toInt() + 1
                        val row = (pos.y / cellHeight).toInt() + 1
                        val cellIndex = (row - 1) * cols + (col - 1)

                        // Reset inactivity timer on result screen touch
                        if (screenState == "result" && !isProductVideoPlaying) {
                            inactivityJob?.cancel()
                            inactivityJob = coroutineScope.launch {
                                delay(Constants.INACTIVITY_TIMEOUT)
                                if (!isProductVideoPlaying && screenState == "result") {
                                    screenState = "start"
                                }
                            }
                        }

                        when (screenState) {
                            "start" -> if (cellIndex == 4) screenState = "gender"

                            "gender" -> {
                                val selectedGender = when (cellIndex) {
                                    6 -> Gender.Female
                                    8 -> Gender.Male
                                    else -> null
                                }
                                if (selectedGender != null) {
                                    userSelection = userSelection.copy(gender = selectedGender)
                                    screenState = "age"
                                }
                            }

                            "age" -> {
                                val selectedAge = when (cellIndex) {
                                    6 -> AgeGroup.Young
                                    7 -> AgeGroup.Middle
                                    8 -> AgeGroup.Senior
                                    else -> null
                                }
                                if (selectedAge != null) {
                                    userSelection = userSelection.copy(ageGroup = selectedAge)
                                    screenState = "lifestyle"
                                }
                            }

                            "lifestyle" -> {
                                val selectedLifestyle = when (cellIndex) {
                                    6 -> Lifestyle.Sedentary
                                    7 -> Lifestyle.Normal
                                    8 -> Lifestyle.Athlete
                                    else -> null
                                }
                                if (selectedLifestyle != null) {
                                    userSelection = userSelection.copy(lifestyle = selectedLifestyle)
                                    isTouchEnabled = false
                                    playResultVideo()
                                }
                            }

                            "result" -> {
                                if (isProductVideoPlaying) {
                                    event.changes.forEach { it.consume() }
                                    continue
                                }

                                val zones = currentResultResId?.let { productGridMap[it] }
                                val touchedZone = zones?.firstOrNull { zone ->
                                    col == zone.col && row in zone.rowRange
                                }

                                touchedZone?.let { zone ->
                                    isProductVideoPlaying = true
                                    val productUri = "android.resource://${context.packageName}/${zone.productResId}".toUri()
                                    playVideo(
                                        uri = productUri,
                                        loop = false,
                                        onEnded = {
                                            isProductVideoPlaying = false
                                            playResultVideo()
                                        }
                                    )
                                }
                            }
                        }

                        event.changes.forEach { it.consume() }
                    }
                }
            }
    )
    {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}