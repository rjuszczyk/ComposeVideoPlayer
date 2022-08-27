# ComposeVideoPlayer
VLCJ based video player for Jetpack Compose Desktop (no Swing Panel)

This approach uses VLCJ Direct Rendering (https://capricasoftware.co.uk/projects/vlcj-4/tutorials/direct-rendering) and a custom `CallbackVideoSurface`.

Useage:

```
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
```
