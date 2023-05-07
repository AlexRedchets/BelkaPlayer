package com.alexredchets.belkaplayer.network

import com.google.gson.annotations.SerializedName
import java.util.Date

class VideoResponse : ArrayList<VideoResponseItem>()

data class VideoResponseItem(
    @SerializedName("author") val author: Author?,
    @SerializedName("description") val description: String?,
    @SerializedName("fullURL") val fullURL: String?,
    @SerializedName("hlsURL") val hlsURL: String?,
    @SerializedName("id") val id: String?,
    @SerializedName("publishedAt") val publishedAt: Date?,
    @SerializedName("title") val title: String?
)

data class Author(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?
)
