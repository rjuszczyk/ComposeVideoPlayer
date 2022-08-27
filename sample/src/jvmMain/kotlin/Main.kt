// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.rjuszczyk.compose.VideoPlayer
import com.rjuszczyk.compose.rememberVideoPlayerState

@Composable
@Preview
fun App() {


    MaterialTheme {



        Box {

            val videoPlayerState = rememberVideoPlayerState(
                time = 0L,
                isPlaying = false,
            )
            val time by videoPlayerState.time.collectAsState()
            val isPlaying by videoPlayerState.isPlaying.collectAsState()
            val length by videoPlayerState.length.collectAsState()

            VideoPlayer(
                mrl = "/Users/r.juszczyk/Movies/seaplane.mp4",
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
                            videoPlayerState.seekTo(videoPlayerState.time.value.time - 5000)
                        }) {

                        Text("Backward")
                    }
                    Button(
                        onClick = {
                            // show =  !show
                            if(isPlaying) {
                                videoPlayerState.pause()
                            } else {
                                videoPlayerState.play()
                            }
                        }) {

                        Text(if(isPlaying) "Pause" else "Play")
                    }
                    Button(
                        onClick = {
                            videoPlayerState.seekTo(videoPlayerState.time.value.time + 5000)
                        }) {

                        Text("Forward")
                    }
                }

                Slider(
                    value = time.time/length.length.toFloat(),
                    onValueChange = {videoPlayerState.seekTo((it*length.length).toLong())},
                    modifier= Modifier.fillMaxWidth()
                )
            }


        }

    }
}

fun main() = application {
    Window(
        title = "Video Test",
        onCloseRequest = ::exitApplication,
    ) {
        App()
    }
}
