package com.alexredchets.belkaplayer

import javax.inject.Inject
import okhttp3.OkHttpClient

class NetworkDecorator @Inject constructor() {

    fun decorate(builder: OkHttpClient.Builder): OkHttpClient.Builder {
        builder.addInterceptor(MockServerInterceptor())
        return builder
    }
}