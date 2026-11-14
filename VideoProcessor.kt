package com.bhaibon.videostudio.utils

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.annotation.OptIn
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import java.io.File

object VideoProcessor {

    @OptIn(UnstableApi::class)
    fun exportVideo(
        context: Context,
        videoUri: Uri,
        startMs: Long,
        endMs: Long,
        isReels9x16: Boolean,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val outputDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
            "BhaibonVideoStudio"
        )
        if (!outputDir.exists()) outputDir.mkdirs()
        val outputFile = File(outputDir, "Bhaibon_Edit_${System.currentTimeMillis()}.mp4")

        val clippingConfig = MediaItem.ClippingConfiguration.Builder()
            .setStartPositionMs(startMs)
            .setEndPositionMs(endMs)
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(videoUri)
            .setClippingConfiguration(clippingConfig)
            .build()

        val videoEffects = mutableListOf<Effect>()
        if (isReels9x16) {
            // 9:16 Vertical Reels Resolution (1080x1920)
            videoEffects.add(Presentation.createForWidthAndHeight(1080, 1920, Presentation.LAYOUT_SCALE_TO_FIT_WITH_CROP))
        }

        val editedMediaItem = EditedMediaItem.Builder(mediaItem)
            .setEffects(Effects(emptyList(), videoEffects))
            .build()

        val transformer = Transformer.Builder(context)
            .addListener(object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    onSuccess(outputFile.absolutePath)
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    onError(exportException.localizedMessage ?: "Export failed")
                }
            })
            .build()

        transformer.start(editedMediaItem, outputFile.absolutePath)
    }
}
