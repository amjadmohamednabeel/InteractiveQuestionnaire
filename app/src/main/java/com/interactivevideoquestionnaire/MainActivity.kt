//MainActivity.kt

//////////////////////////////////////////////////////////////////////////////
//Author : Amjad Mohamed Nabeel                                           ////
//Date : 2025-08-13                                                       ////
//Version : 1.0.2                                                         ////
//This file is part of the Interactive Video Questionnaire project.       ////
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

// Add these constants and sealed classes
private object Constants {
    const val INACTIVITY_TIMEOUT = 20000L // 20 seconds
    const val TOUCH_ENABLE_DELAY = 850L    // Increased to 0.85 seconds
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
    val lifestyle: Lifestyle? = null,
    val problemOption: Int? = null
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
                // Problem video URIs - you'll need to add these videos to your resources
                problemUriMap = mapOf(
                    "male_18-30_athlete" to "${base}${R.raw.p1}".toUri(),
                    "male_18-30_normal" to "${base}${R.raw.p2}".toUri(),
                    "male_18-30_sedentary" to "${base}${R.raw.p3}".toUri(),
                    "male_30-45_athlete" to "${base}${R.raw.p4}".toUri(),
                    "male_30-45_normal" to "${base}${R.raw.p5}".toUri(),
                    "male_30-45_sedentary" to "${base}${R.raw.p6}".toUri(),
                    "male_above45_athlete" to "${base}${R.raw.p7}".toUri(),
                    "male_above45_normal" to "${base}${R.raw.p8}".toUri(),
                    "male_above45_sedentary" to "${base}${R.raw.p9}".toUri(),
                    "female_18-30_athlete" to "${base}${R.raw.p10}".toUri(),
                    "female_18-30_normal" to "${base}${R.raw.p11}".toUri(),
                    "female_18-30_sedentary" to "${base}${R.raw.p12}".toUri(),
                    "female_30-45_athlete" to "${base}${R.raw.p13}".toUri(),
                    "female_30-45_normal" to "${base}${R.raw.p14}".toUri(),
                    "female_30-45_sedentary" to "${base}${R.raw.p15}".toUri(),
                    "female_above45_athlete" to "${base}${R.raw.p16}".toUri(),
                    "female_above45_normal" to "${base}${R.raw.p17}".toUri(),
                    "female_above45_sedentary" to "${base}${R.raw.p18}".toUri()
                ),

                // New sequential result mapping based on your requirement
                resultMap = mapOf(
                    // Male 18-30 Athletic
                    "male_18-30_athlete_1" to R.raw.v1,
                    "male_18-30_athlete_2" to R.raw.v2,
                    "male_18-30_athlete_3" to R.raw.v3,
                    // Male 18-30 Normal
                    "male_18-30_normal_1" to R.raw.v4,
                    "male_18-30_normal_2" to R.raw.v5,
                    "male_18-30_normal_3" to R.raw.v6,
                    // Male 18-30 Sedentary
                    "male_18-30_sedentary_1" to R.raw.v7,
                    "male_18-30_sedentary_2" to R.raw.v8,
                    "male_18-30_sedentary_3" to R.raw.v9,
                    "male_18-30_sedentary_4" to R.raw.v10,
                    // Male 30-45 Athletic
                    "male_30-45_athlete_1" to R.raw.v11,
                    "male_30-45_athlete_2" to R.raw.v12,
                    "male_30-45_athlete_3" to R.raw.v13,
                    "male_30-45_athlete_4" to R.raw.v14,
                    // Male 30-45 Normal
                    "male_30-45_normal_1" to R.raw.v15,
                    "male_30-45_normal_2" to R.raw.v16,
                    "male_30-45_normal_3" to R.raw.v17,
                    // Male 30-45 Sedentary
                    "male_30-45_sedentary_1" to R.raw.v18,
                    "male_30-45_sedentary_2" to R.raw.v19,
                    "male_30-45_sedentary_3" to R.raw.v20,
                    "male_30-45_sedentary_4" to R.raw.v21,
                    // Male Above 45 Athletic
                    "male_above45_athlete_1" to R.raw.v22,
                    "male_above45_athlete_2" to R.raw.v23,
                    "male_above45_athlete_3" to R.raw.v24,
                    // Male Above 45 Normal
                    "male_above45_normal_1" to R.raw.v25,
                    "male_above45_normal_2" to R.raw.v26,
                    "male_above45_normal_3" to R.raw.v27,
                    "male_above45_normal_4" to R.raw.v28,
                    // Male Above 45 Sedentary
                    "male_above45_sedentary_1" to R.raw.v29,
                    "male_above45_sedentary_2" to R.raw.v30,
                    "male_above45_sedentary_3" to R.raw.v31,
                    "male_above45_sedentary_4" to R.raw.v32,
                    "male_above45_sedentary_5" to R.raw.v33,
                    // Female 18-30 Athletic
                    "female_18-30_athlete_1" to R.raw.v34,
                    "female_18-30_athlete_2" to R.raw.v35,
                    "female_18-30_athlete_3" to R.raw.v36,
                    "female_18-30_athlete_4" to R.raw.v37,
                    // Female 18-30 Normal
                    "female_18-30_normal_1" to R.raw.v38,
                    "female_18-30_normal_2" to R.raw.v39,
                    "female_18-30_normal_3" to R.raw.v40,
                    "female_18-30_normal_4" to R.raw.v41,
                    // Female 18-30 Sedentary
                    "female_18-30_sedentary_1" to R.raw.v42,
                    "female_18-30_sedentary_2" to R.raw.v43,
                    "female_18-30_sedentary_3" to R.raw.v44,
                    "female_18-30_sedentary_4" to R.raw.v45,
                    // Female 30-45 Athletic
                    "female_30-45_athlete_1" to R.raw.v46,
                    "female_30-45_athlete_2" to R.raw.v47,
                    "female_30-45_athlete_3" to R.raw.v48,
                    "female_30-45_athlete_4" to R.raw.v49,
                    // Female 30-45 Normal
                    "female_30-45_normal_1" to R.raw.v50,
                    "female_30-45_normal_2" to R.raw.v51,
                    "female_30-45_normal_3" to R.raw.v52,
                    "female_30-45_normal_4" to R.raw.v53,
                    // Female 30-45 Sedentary
                    "female_30-45_sedentary_1" to R.raw.v54,
                    "female_30-45_sedentary_2" to R.raw.v55,
                    "female_30-45_sedentary_3" to R.raw.v56,
                    "female_30-45_sedentary_4" to R.raw.v57,
                    // Female Above 45 Athletic
                    "female_above45_athlete_1" to R.raw.v58,
                    "female_above45_athlete_2" to R.raw.v59,
                    "female_above45_athlete_3" to R.raw.v60,
                    "female_above45_athlete_4" to R.raw.v61,
                    // Female Above 45 Normal
                    "female_above45_normal_1" to R.raw.v62,
                    "female_above45_normal_2" to R.raw.v63,
                    "female_above45_normal_3" to R.raw.v64,
                    "female_above45_normal_4" to R.raw.v65,
                    // Female Above 45 Sedentary
                    "female_above45_sedentary_1" to R.raw.v66,
                    "female_above45_sedentary_2" to R.raw.v67,
                    "female_above45_sedentary_3" to R.raw.v68,
                    "female_above45_sedentary_4" to R.raw.v69,
                    "female_above45_sedentary_5" to R.raw.v70
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
    problemUriMap: Map<String, Uri>,
    resultMap: Map<String, Int>
) {
    val context = LocalContext.current
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }

    var screenState by remember { mutableStateOf("start") }
    var isTouchEnabled by remember { mutableStateOf(true) }
    var isVideoReady by remember { mutableStateOf(false) }  // NEW: Track video readiness
    val coroutineScope = rememberCoroutineScope()

    // State for tracking product video playback and inactivity
    var isProductVideoPlaying by remember { mutableStateOf(false) }
    var inactivityJob by remember { mutableStateOf<Job?>(null) }

    var userSelection by remember { mutableStateOf(UserSelection()) }
    var currentResultResId by remember { mutableStateOf<Int?>(null) }

    // For tracking video end callbacks
    var currentOnEnded: (() -> Unit)? by remember { mutableStateOf(null) }

    // ExoPlayer listener to detect when videos finish playing and are ready
    val playbackListener = remember {
        object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_IDLE -> {
                        isVideoReady = false
                    }
                    Player.STATE_BUFFERING -> {
                        isVideoReady = false
                    }
                    Player.STATE_READY -> {
                        isVideoReady = true  // Mark video as ready
                    }
                    Player.STATE_ENDED -> {
                        currentOnEnded?.invoke()
                        currentOnEnded = null
                    }
                }
            }
        }
    }

    data class ProductZone(val rowRange: IntRange, val col: Int, val productResId: Int)

    val productGridMap = mapOf(
        R.raw.v1 to listOf(
            ProductZone(2..4, 1, R.raw.carnitova),
            ProductZone(2..4, 2, R.raw.carnitova),
            ProductZone(2..4, 5, R.raw.superaktiv),
            ProductZone(2..4, 6, R.raw.superaktiv)
        ),
        R.raw.v2 to listOf(
            ProductZone(2..4, 1, R.raw.superaktiv),
            ProductZone(2..4, 2, R.raw.superaktiv),
            ProductZone(2..4, 5, R.raw.co_q10_200)
        ),
        R.raw.v3 to listOf(
            ProductZone(2..4, 1, R.raw.carnitova),
            ProductZone(2..4, 2, R.raw.carnitova),
            ProductZone(2..4, 5, R.raw.l_arginine)
        ),
        R.raw.v4 to listOf(
            ProductZone(2..4, 1, R.raw.superaktiv),
            ProductZone(2..4, 2, R.raw.superaktiv),
            ProductZone(2..4, 5, R.raw.multivitamins)
        ),
        R.raw.v5 to listOf(
            ProductZone(2..4, 1, R.raw.superaktiv),
            ProductZone(2..4, 2, R.raw.superaktiv),
            ProductZone(2..4, 5, R.raw.multivitamins)
        ),
        R.raw.v6 to listOf(
            ProductZone(2..4, 1, R.raw.rongum_ashwagandha),
            ProductZone(2..4, 2, R.raw.ashwagandha),
            ProductZone(2..4, 5, R.raw.calmtonin)
        ),
        R.raw.v7 to listOf(
            ProductZone(2..4, 2, R.raw.magnesium),
            ProductZone(2..4, 5, R.raw.superaktiv),
            ProductZone(2..4, 6, R.raw.superaktiv)
        ),
        R.raw.v8 to listOf(
            ProductZone(2..4, 1, R.raw.rongum_ashwagandha),
            ProductZone(2..4, 2, R.raw.ashwagandha),
            ProductZone(2..4, 5, R.raw.calmtonin)
        ),
        R.raw.v9 to listOf(
            ProductZone(2..4, 2, R.raw.ginkobiloba),
            ProductZone(2..4, 5, R.raw.omegat),
            ProductZone(2..4, 6, R.raw.omegat)
        ),
        R.raw.v10 to listOf(
            ProductZone(2..4, 2, R.raw.rondiet)
        ),
        R.raw.v11 to listOf(
            ProductZone(2..4, 1, R.raw.carnitova),
            ProductZone(2..4, 2, R.raw.carnitova),
            ProductZone(2..4, 5, R.raw.superaktiv),
            ProductZone(2..4, 6, R.raw.superaktiv)
        ),
        R.raw.v12 to listOf(
            ProductZone(2..4, 1, R.raw.carnitova),
            ProductZone(2..4, 2, R.raw.carnitova),
            ProductZone(2..4, 5, R.raw.co_q10_200)
        ),
        R.raw.v13 to listOf(
            ProductZone(2..4, 1, R.raw.ron_j),
            ProductZone(2..4, 2, R.raw.ron_j),
            ProductZone(2..4, 5, R.raw.glucosamine)
        ),
        R.raw.v14 to listOf(
            ProductZone(2..4, 2, R.raw.ginkobiloba),
            ProductZone(2..4, 5, R.raw.omegat),
            ProductZone(2..4, 6, R.raw.omegat)
        ),
        R.raw.v15 to listOf(
            ProductZone(2..4, 1, R.raw.superaktiv),
            ProductZone(2..4, 2, R.raw.superaktiv),
            ProductZone(2..4, 5, R.raw.multivitamins)
        ),
        R.raw.v16 to listOf(
            ProductZone(2..4, 2, R.raw.ginkobiloba),
            ProductZone(2..4, 5, R.raw.omegat),
            ProductZone(2..4, 6, R.raw.omegat)
        ),
        R.raw.v17 to listOf(
            ProductZone(2..4, 2, R.raw.multivitamins),
            ProductZone(2..4, 5, R.raw.rongum_multivitamin)
        ),
        R.raw.v18 to listOf(
            ProductZone(2..4, 2, R.raw.calmtonin),
            ProductZone(2..4, 5, R.raw.ashwagandha),
            ProductZone(2..4, 6, R.raw.rongum_ashwagandha)
        ),
        R.raw.v19 to listOf(
            ProductZone(2..4, 2, R.raw.rondiet)
        ),
        R.raw.v20 to listOf(
            ProductZone(2..4, 2, R.raw.ashwagandha),
            ProductZone(2..4, 5, R.raw.rongum_ashwagandha)
        ),
        R.raw.v21 to listOf(
            ProductZone(2..4, 1, R.raw.superaktiv),
            ProductZone(2..4, 2, R.raw.superaktiv),
            ProductZone(2..4, 5, R.raw.co_q10_200)
        ),
        R.raw.v22 to listOf(
            ProductZone(2..4, 1, R.raw.ron_j),
            ProductZone(2..4, 2, R.raw.ron_j),
            ProductZone(2..4, 5, R.raw.glucosamine)
        ),
        R.raw.v23 to listOf(
            ProductZone(2..4, 1, R.raw.carnitova),
            ProductZone(2..4, 2, R.raw.carnitova),
            ProductZone(2..4, 5, R.raw.l_carnitine)
        ),
        R.raw.v24 to listOf(
            ProductZone(2..4, 2, R.raw.ginkobiloba),
            ProductZone(2..4, 5, R.raw.omegat),
            ProductZone(2..4, 6, R.raw.omegat)
        ),
        R.raw.v25 to listOf(
            ProductZone(2..4, 2, R.raw.vitamin_d),
            ProductZone(2..4, 5, R.raw.rongum_calcium)
        ),
        R.raw.v26 to listOf(
            ProductZone(2..4, 2, R.raw.ginkobiloba),
            ProductZone(2..4, 5, R.raw.omegat),
            ProductZone(2..4, 6, R.raw.omegat)
        ),
        R.raw.v27 to listOf(
            ProductZone(2..4, 1, R.raw.superaktiv),
            ProductZone(2..4, 2, R.raw.superaktiv),
            ProductZone(2..4, 5, R.raw.multivitamins)
        ),
        R.raw.v28 to listOf(
            ProductZone(2..4, 1, R.raw.omegat),
            ProductZone(2..4, 2, R.raw.omegat)
        ),
        R.raw.v29 to listOf(
            ProductZone(2..4, 1, R.raw.rongum_ashwagandha),
            ProductZone(2..4, 2, R.raw.ashwagandha),
            ProductZone(2..4, 5, R.raw.calmtonin)
        ),
        R.raw.v30 to listOf(
            ProductZone(2..4, 1, R.raw.ron_j),
            ProductZone(2..4, 2, R.raw.ron_j),
            ProductZone(2..4, 5, R.raw.glucosamine)
        ),
        R.raw.v31 to listOf(
            ProductZone(2..4, 2, R.raw.rondiet)
        ),
        R.raw.v32 to listOf(
            ProductZone(2..4, 2, R.raw.l_carnitine),
            ProductZone(2..4, 5, R.raw.magnesium)
        ),
        R.raw.v33 to listOf(
            ProductZone(2..4, 2, R.raw.multivitamins),
            ProductZone(2..4, 5, R.raw.rongum_multivitamin)
        ),
        R.raw.v34 to listOf(
            ProductZone(2..4, 2, R.raw.biotin),
            ProductZone(2..4, 5, R.raw.ron_h)
        ),
        R.raw.v35 to listOf(
            ProductZone(2..4, 2, R.raw.l_carnitine),
            ProductZone(2..4, 5, R.raw.superaktiv),
            ProductZone(2..4, 6, R.raw.superaktiv)
        ),
        R.raw.v36 to listOf(
            ProductZone(2..4, 2, R.raw.l_carnitine),
            ProductZone(2..4, 5, R.raw.co_q10_200)
        ),
        R.raw.v37 to listOf(
            ProductZone(2..4, 1, R.raw.ron_s),
            ProductZone(2..4, 2, R.raw.ron_s),
            ProductZone(2..4, 5, R.raw.glutathione)
        ),
        R.raw.v38 to listOf(
            ProductZone(2..4, 1, R.raw.superaktiv),
            ProductZone(2..4, 2, R.raw.superaktiv),
            ProductZone(2..4, 5, R.raw.multivitamins)
        ),
        R.raw.v39 to listOf(
            ProductZone(2..4, 2, R.raw.iron)
        ),
        R.raw.v40 to listOf(
            ProductZone(2..4, 2, R.raw.rongum_hair_nail),
            ProductZone(2..4, 5, R.raw.ron_s),
            ProductZone(2..4, 6, R.raw.ron_s)
        ),
        R.raw.v41 to listOf(
            ProductZone(2..4, 2, R.raw.multivitamins),
            ProductZone(2..4, 5, R.raw.rongum_multivitamin)
        ),
        R.raw.v42 to listOf(
            ProductZone(2..4, 1, R.raw.spasmiona),
            ProductZone(2..4, 2, R.raw.spasmiona)
        ),
        R.raw.v43 to listOf(
            ProductZone(2..4, 1, R.raw.superaktiv),
            ProductZone(2..4, 2, R.raw.superaktiv),
            ProductZone(2..4, 5, R.raw.rongum_multivitamin)
        ),
        R.raw.v44 to listOf(
            ProductZone(2..4, 1, R.raw.rongum_ashwagandha),
            ProductZone(2..4, 2, R.raw.ashwagandha),
            ProductZone(2..4, 5, R.raw.calmtonin)
        ),
        R.raw.v45 to listOf(
            ProductZone(2..4, 2, R.raw.ronzalax)
        ),
        R.raw.v46 to listOf(
            ProductZone(2..4, 1, R.raw.superaktiv),
            ProductZone(2..4, 2, R.raw.superaktiv),
            ProductZone(2..4, 5, R.raw.l_carnitine)
        ),
        R.raw.v47 to listOf(
            ProductZone(2..4, 1, R.raw.superaktiv),
            ProductZone(2..4, 2, R.raw.superaktiv)
        ),
        R.raw.v48 to listOf(
            ProductZone(2..4, 2, R.raw.capillorin),
            ProductZone(2..4, 5, R.raw.ron_h),
            ProductZone(2..4, 6, R.raw.ron_h)
        ),
        R.raw.v49 to listOf(
            ProductZone(2..4, 1, R.raw.rongum_ashwagandha),
            ProductZone(2..4, 2, R.raw.ashwagandha)
        ),
        R.raw.v50 to listOf(
            ProductZone(2..4, 1, R.raw.omegat),
            ProductZone(2..4, 2, R.raw.omegat),
            ProductZone(2..4, 5, R.raw.co_q10_200)
        ),
        R.raw.v51 to listOf(
            ProductZone(2..4, 2, R.raw.rongum_hair_nail),
            ProductZone(2..4, 5, R.raw.ron_h),
            ProductZone(2..4, 6, R.raw.ron_h)
        ),
        R.raw.v52 to listOf(
            ProductZone(2..4, 2, R.raw.iron),
            ProductZone(2..4, 5, R.raw.multivitamins)
        ),
        R.raw.v53 to listOf(
            ProductZone(2..4, 2, R.raw.cronz),
            ProductZone(2..4, 5, R.raw.zinc)
        ),
        R.raw.v54 to listOf(
            ProductZone(2..4, 1, R.raw.rongum_ashwagandha),
            ProductZone(2..4, 2, R.raw.ashwagandha),
            ProductZone(2..4, 5, R.raw.calmtonin)
        ),
        R.raw.v55 to listOf(
            ProductZone(2..4, 2, R.raw.rondiet)
        ),
        R.raw.v56 to listOf(
            ProductZone(2..4, 1, R.raw.superaktiv),
            ProductZone(2..4, 2, R.raw.superaktiv),
            ProductZone(2..4, 5, R.raw.rongum_multivitamin)
        ),
        R.raw.v57 to listOf(
            ProductZone(2..4, 2, R.raw.rongum_hair_nail),
            ProductZone(2..4, 5, R.raw.ron_h),
            ProductZone(2..4, 6, R.raw.ron_h)
        ),
        R.raw.v58 to listOf(
            ProductZone(2..4, 1, R.raw.ron_j),
            ProductZone(2..4, 2, R.raw.ron_j),
            ProductZone(2..4, 5, R.raw.glucosamine)
        ),
        R.raw.v59 to listOf(
            ProductZone(2..4, 2, R.raw.vitamin_d),
            ProductZone(2..4, 5, R.raw.rongum_calcium)
        ),
        R.raw.v60 to listOf(
            ProductZone(2..4, 1, R.raw.omegat),
            ProductZone(2..4, 2, R.raw.omegat),
            ProductZone(2..4, 5, R.raw.co_q10_200)
        ),
        R.raw.v61 to listOf(
            ProductZone(2..4, 2, R.raw.rongum_hair_nail),
            ProductZone(2..4, 5, R.raw.ron_h),
            ProductZone(2..4, 6, R.raw.ron_h)
        ),
        R.raw.v62 to listOf(
            ProductZone(2..4, 1, R.raw.ron_j),
            ProductZone(2..4, 2, R.raw.ron_j),
            ProductZone(2..4, 5, R.raw.glucosamine)
        ),
        R.raw.v63 to listOf(
            ProductZone(2..4, 1, R.raw.ron_s),
            ProductZone(2..4, 2, R.raw.ron_s)
        ),
        R.raw.v64 to listOf(
            ProductZone(2..4, 2, R.raw.ginkobiloba),
            ProductZone(2..4, 5, R.raw.omegat),
            ProductZone(2..4, 6, R.raw.omegat)
        ),
        R.raw.v65 to listOf(
            ProductZone(2..4, 2, R.raw.rongum_hair_nail),
            ProductZone(2..4, 5, R.raw.ron_h),
            ProductZone(2..4, 6, R.raw.ron_h)
        ),
        R.raw.v66 to listOf(
            ProductZone(2..4, 1, R.raw.rongum_ashwagandha),
            ProductZone(2..4, 2, R.raw.ashwagandha),
            ProductZone(2..4, 5, R.raw.calmtonin)
        ),
        R.raw.v67 to listOf(
            ProductZone(2..4, 2, R.raw.multivitamins),
            ProductZone(2..4, 5, R.raw.rongum_multivitamin)
        ),
        R.raw.v68 to listOf(
            ProductZone(2..4, 1, R.raw.omegat),
            ProductZone(2..4, 2, R.raw.omegat)
        ),
        R.raw.v69 to listOf(
            ProductZone(2..4, 1, R.raw.ron_j),
            ProductZone(2..4, 2, R.raw.ron_j),
            ProductZone(2..4, 5, R.raw.glucosamine)
        ),
        R.raw.v70 to listOf(
            ProductZone(2..4, 2, R.raw.vitamin_d),
            ProductZone(2..4, 5, R.raw.rongum_calcium)
        )
    )

    fun playVideo(
        uri: Uri,
        loop: Boolean = true,
        nextState: String? = null,
        onEnded: (() -> Unit)? = null
    ) {
        // MODIFIED: Disable touch and mark video as not ready
        isTouchEnabled = false
        isVideoReady = false

        exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        exoPlayer.repeatMode = if (loop) ExoPlayer.REPEAT_MODE_ALL else ExoPlayer.REPEAT_MODE_OFF
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        // Remove any existing listener and add it back
        exoPlayer.removeListener(playbackListener)
        exoPlayer.addListener(playbackListener)

        if (onEnded != null) {
            currentOnEnded = onEnded
        }

        coroutineScope.launch {
            // Wait for video to be ready before enabling touch
            while (!isVideoReady) {
                delay(100)
            }

            // Additional delay to ensure video is fully loaded and playing
            delay(Constants.TOUCH_ENABLE_DELAY)
            isTouchEnabled = true

            nextState?.let {
                delay(Constants.STATE_CHANGE_DELAY)
                screenState = it
            }
        }
    }

    fun getProblemKey(): String? {
        val gender = userSelection.gender?.toKey() ?: return null
        val age = userSelection.ageGroup?.toKey() ?: return null
        val lifestyle = userSelection.lifestyle?.toKey() ?: return null
        return "${gender}_${age}_${lifestyle}"
    }

    fun getResultKey(): String? {
        val gender = userSelection.gender?.toKey() ?: return null
        val age = userSelection.ageGroup?.toKey() ?: return null
        val lifestyle = userSelection.lifestyle?.toKey() ?: return null
        val option = userSelection.problemOption ?: return null
        return "${gender}_${age}_${lifestyle}_${option}"
    }

    fun getMaxOptionsForCurrentSelection(): Int {
        val key = getProblemKey() ?: return 0
        return when (key) {
            "male_18-30_sedentary", "male_30-45_athlete", "male_30-45_sedentary",
            "male_above45_normal", "male_above45_sedentary", "female_18-30_athlete",
            "female_18-30_normal", "female_18-30_sedentary", "female_30-45_athlete",
            "female_30-45_normal", "female_30-45_sedentary", "female_above45_athlete",
            "female_above45_normal", "female_above45_sedentary" -> {
                if (key == "male_above45_sedentary" || key == "female_above45_sedentary") 5 else 4
            }
            else -> 3 // Default for most combinations
        }
    }

    fun playResultVideo() {
        val resultKey = getResultKey()
        val resId = resultKey?.let { resultMap[it] }
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
                    // Reset user selection when returning to start
                    userSelection = UserSelection()
                }
            }
        }
    }

    // MODIFIED: Add listener setup in LaunchedEffect
    LaunchedEffect(Unit) {
        exoPlayer.addListener(playbackListener)
    }

    LaunchedEffect(screenState) {
        // Cancel any existing inactivity timeout
        inactivityJob?.cancel()

        when (screenState) {
            "start" -> {
                // Reset user selection when returning to start
                userSelection = UserSelection()
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
            "problem" -> {
                val problemKey = getProblemKey()
                val problemUri = problemKey?.let { problemUriMap[it] }
                if (problemUri != null) {
                    playVideo(problemUri)
                    inactivityJob = coroutineScope.launch {
                        delay(Constants.INACTIVITY_TIMEOUT)
                        if (screenState == "problem") {
                            screenState = "start"
                        }
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
            .pointerInput(screenState, isTouchEnabled, isVideoReady) {  // MODIFIED: Add isVideoReady dependency
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()

                        if (event.changes.isEmpty()) continue

                        // MODIFIED: Check both touch enabled and video ready
                        if (!isTouchEnabled || !isVideoReady) {
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
                            "start" -> if (cellIndex == 4) {
                                isTouchEnabled = false  // MODIFIED: Disable immediately
                                screenState = "gender"
                            }

                            "gender" -> {
                                val selectedGender = when (cellIndex) {
                                    6 -> Gender.Female
                                    8 -> Gender.Male
                                    else -> null
                                }
                                if (selectedGender != null) {
                                    isTouchEnabled = false  // MODIFIED: Disable immediately
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
                                    isTouchEnabled = false  // MODIFIED: Disable immediately
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
                                    isTouchEnabled = false  // MODIFIED: Disable immediately
                                    userSelection = userSelection.copy(lifestyle = selectedLifestyle)
                                    screenState = "problem"
                                }
                            }

                            "problem" -> {
                                val maxOptions = getMaxOptionsForCurrentSelection()
                                val selectedOption = when (maxOptions) {
                                    3 -> when (cellIndex) {
                                        6 -> 1  // Bottom left
                                        7 -> 2  // Bottom middle
                                        8 -> 3  // Bottom right
                                        else -> null
                                    }
                                    4 -> when (cellIndex) {
                                        3 -> 1  // Top right
                                        6 -> 2  // Bottom left
                                        8 -> 3  // Bottom right
                                        5 -> 4  // Top middle right
                                        else -> null
                                    }
                                    5 -> when (cellIndex) {
                                        3 -> 1  // Top right
                                        6 -> 2  // Bottom left
                                        7 -> 3  // Bottom middle
                                        8 -> 4  // Bottom right
                                        5 -> 5  // Top middle right
                                        else -> null
                                    }
                                    else -> null
                                }
                                if (selectedOption != null) {
                                    isTouchEnabled = false  // MODIFIED: Already disabled, but keep for clarity
                                    userSelection = userSelection.copy(problemOption = selectedOption)
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