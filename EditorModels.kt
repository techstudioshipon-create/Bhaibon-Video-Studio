package com.bhaibon.videostudio.data.model

import android.net.Uri

data class VideoClip(
    val id: String,
    val uri: Uri,
    val name: String,
    val startMs: Long = 0L,
    val endMs: Long = 10000L,
    val isReelsMode: Boolean = false
)

data class CaptionOverlay(
    val text: String = "",
    val fontSize: Float = 24f,
    val colorHex: Long = 0xFFFFFFFF
)

data class AudioOverlay(
    val uri: Uri? = null,
    val name: String = "No Music Selected",
    val volume: Float = 0.8f
)
