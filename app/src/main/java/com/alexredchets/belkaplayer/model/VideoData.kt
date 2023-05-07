package com.alexredchets.belkaplayer.model

import android.net.Uri
import androidx.media3.common.MediaItem

class VideoData(
    val title: String,
    val author: String,
    val description: CharSequence,
    val uri: Uri,
    val mediaItem: MediaItem
)