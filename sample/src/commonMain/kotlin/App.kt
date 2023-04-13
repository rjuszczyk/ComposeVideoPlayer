// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.material.MaterialTheme

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rjuszczyk.compose.OnTimeChangedListener
import com.rjuszczyk.compose.VideoPlayer
import com.rjuszczyk.compose.rememberVideoPlayerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.max
import kotlin.math.min

@Composable
fun App(videoFile: String) {

    MaterialTheme {

        Box {

            val videoPlayerState = rememberVideoPlayerState()

            var isPlaying by remember(videoFile) { mutableStateOf(false) }
            val timeMillisStateFlow = remember(videoFile) { MutableStateFlow(-1L) }
            val lengthMillisStateFlow = remember(videoFile) { MutableStateFlow(-1L) }

            val timeMillis by timeMillisStateFlow.collectAsState()
            val lengthMillis by lengthMillisStateFlow.collectAsState()

            LaunchedEffect(videoFile) {
                videoPlayerState.doWithMediaPlayer { mediaPlayer ->
                    lengthMillisStateFlow.value = mediaPlayer.getLengthMillis()
                    mediaPlayer.addOnTimeChangedListener(object : OnTimeChangedListener{
                        override fun onTimeChanged(timeMillis: Long) {
                            timeMillisStateFlow.value = timeMillis
                        }
                    })
                }
            }

            VideoPlayer(
                mrl = videoFile,
                state = videoPlayerState,
            )

            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp),
            ) {
                Row(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            videoPlayerState.doWithMediaPlayer { mediaPlayer ->
                                mediaPlayer.setTimeAccurate( max(mediaPlayer.getTimeMillis() - 5000, 0) )
                            }
                        }) {

                        Text("Backward")
                    }
                    Button(
                        onClick = {
                            // show =  !show
                            videoPlayerState.doWithMediaPlayer { mediaPlayer ->
                                if(mediaPlayer.isPlaying) {
                                    isPlaying = false
                                    mediaPlayer.pause()
                                } else {
                                    mediaPlayer.play()
                                    isPlaying = true
                                }
                            }
                        }) {

                        Text(if(isPlaying) "Pause" else "Play")
                    }
                    Button(
                        onClick = {
                            videoPlayerState.doWithMediaPlayer { mediaPlayer ->
                                mediaPlayer.setTimeAccurate( min(mediaPlayer.getTimeMillis() + 5000, mediaPlayer.getLengthMillis()) )
                            }
                        }) {

                        Text("Forward")
                    }
                }

                if(lengthMillis != -1L) {
                    Slider(
                        value = timeMillis/lengthMillis.toFloat(),
                        onValueChange = {
                            videoPlayerState.doWithMediaPlayer {mediaPlayer ->
                                timeMillisStateFlow.value = (it*lengthMillis).toLong()
                                mediaPlayer.setTime((it*lengthMillis).toLong())
                            }
                        },
                        modifier= Modifier.fillMaxWidth()
                    )
                }

            }


        }

    }
}
