// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        title = "Video Test",
        onCloseRequest = ::exitApplication,
    ) {
        App("/Users/radoslaw/Movies/IMG_8523.MOV")
    }
}
