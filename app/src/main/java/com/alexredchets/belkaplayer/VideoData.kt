package com.alexredchets.belkaplayer

import android.net.Uri
import androidx.media3.common.MediaItem

class VideoData(
    val title: String,
    val author: String,
    val description: String,
    val uri: Uri,
    val mediaItem: MediaItem
)