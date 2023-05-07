package com.alexredchets.belkaplayer

import retrofit2.Response
import retrofit2.http.GET

interface BelkaPlayerApi {

    @GET("videos")
    suspend fun getVideos(): Response<VideoResponse>
}