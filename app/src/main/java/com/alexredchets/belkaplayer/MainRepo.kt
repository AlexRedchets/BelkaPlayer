package com.alexredchets.belkaplayer

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.flow

@Singleton
class MainRepo @Inject constructor(
    private val apiService: BelkaPlayerApi
) {

    suspend fun loadVideos() = flow {
        emit(
            State.Success(
                apiService.getVideos().body() as VideoResponse
            )
        )
    }

}