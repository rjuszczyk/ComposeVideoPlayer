package com.rjuszczyk.compose


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo

import uk.co.caprica.vlcj.factory.MediaPlayerFactory
import uk.co.caprica.vlcj.player.base.MediaPlayer
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface
import uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat
import java.nio.ByteBuffer

@Composable
fun rememberVideoPlayerState(
    time: Long = 0L,
    isPlaying: Boolean = true,
) = remember { VideoPlayerState(time, isPlaying) }

class VideoPlayerState(
    time: Long,
    isPlaying: Boolean,
) {
    internal val lengthMutable = MutableStateFlow(VideoLength())

    val time: MutableStateFlow<TimeState> = MutableStateFlow(TimeState.time(time))
    val isPlaying: MutableStateFlow<Boolean> = MutableStateFlow(isPlaying)
    val length: StateFlow<VideoLength> = lengthMutable

    fun seekTo(time: Long) {
        this.time.value = TimeState.time(time)
    }
    fun play() {
        isPlaying.value = true
    }
    fun pause() {
        isPlaying.value = false
    }
}

@Composable
fun VideoPlayer(
    mrl: String,
    state: VideoPlayerState = rememberVideoPlayerState(),
    modifier: Modifier = Modifier,
) {
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    imageBitmap?.let {
        androidx.compose.foundation.Image(
            bitmap = it,
            contentDescription = "Video",
            modifier = modifier
        )
    }?:run {
        Box(modifier = modifier.background(Color.Gray))
    }

    val mediaPlayer = remember {
        var byteArray :ByteArray? = null
        var info: ImageInfo? = null
        val factory = MediaPlayerFactory()
        val embeddedMediaPlayer = factory.mediaPlayers().newEmbeddedMediaPlayer()
        val callbackVideoSurface = CallbackVideoSurface(
            object : BufferFormatCallback {
                override fun getBufferFormat(sourceWidth: Int, sourceHeight: Int): BufferFormat {
                    info = ImageInfo.makeN32(sourceWidth, sourceHeight, ColorAlphaType.OPAQUE)
                    return RV32BufferFormat(sourceWidth, sourceHeight)
                }

                override fun allocatedBuffers(buffers: Array<out ByteBuffer>) {
                    byteArray =  ByteArray(buffers[0].limit())
                }
            },
            object : RenderCallback {
                override fun display(
                    mediaPlayer: MediaPlayer,
                    nativeBuffers: Array<out ByteBuffer>,
                    bufferFormat: BufferFormat?
                ) {
                    val byteBuffer = nativeBuffers[0]

                    byteBuffer.get(byteArray)
                    byteBuffer.rewind()

                    val bmp = Bitmap()
                    bmp.allocPixels(info!!)
                    bmp.installPixels(byteArray)
                    imageBitmap = bmp.asComposeImageBitmap()
                }
            },
            true,
            VideoSurfaceAdapters.getVideoSurfaceAdapter(),
        )
        embeddedMediaPlayer.videoSurface().set(callbackVideoSurface)
        embeddedMediaPlayer
    }

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = mrl) {
        mediaPlayer.media().play(mrl)
        mediaPlayer.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {

            override fun mediaPlayerReady(mediaPlayer: MediaPlayer) {
                super.mediaPlayerReady(mediaPlayer)
                state.lengthMutable.value = VideoLength(mediaPlayer.status().length())
                if(!state.isPlaying.value) {
                    mediaPlayer.controls().pause()
                }
            }

            override fun finished(mediaPlayer: MediaPlayer) {
                super.finished(mediaPlayer)
                state.isPlaying.value = false
            }

            override fun timeChanged(mediaPlayer: MediaPlayer, newTime: Long) {
                super.timeChanged(mediaPlayer, newTime)
                state.time.value = TimeState.internalTime(newTime)
            }
        })

        coroutineScope.launch {
            state.isPlaying.collect {
                if (it) {
                    mediaPlayer.controls().start()
                } else {
                    mediaPlayer.controls().pause()
                }
            }
        }

        var firstUpdate = true
        coroutineScope.launch {
            state.time.collect {
                if (firstUpdate || !it.updatedInternally) {
                    mediaPlayer.controls().setTime(it.time)
                }
                firstUpdate = false
            }
        }
    }

    DisposableEffect(key1 = mrl, effect = {
        this.onDispose {
            mediaPlayer.release()
        }
    })
}
data class VideoLength(
    val length: Long = -1L
) {
    fun isKnown() = length != -1L
}

data class TimeState internal constructor(
    val time: Long,
    internal val updatedInternally : Boolean,
) {
    companion object {
        fun time(time: Long): TimeState {
            return TimeState(time, false)
        }

        internal fun internalTime(time: Long): TimeState {
            return TimeState(time, true)
        }
    }
}
