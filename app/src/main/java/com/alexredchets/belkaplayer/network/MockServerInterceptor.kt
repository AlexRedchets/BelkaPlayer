package com.alexredchets.belkaplayer.network

import okhttp3.Interceptor
import okhttp3.Response

class MockServerInterceptor: Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val path = request.url.toUrl().path
        val url = "http://192.168.2.13:4000${path}"
        request = request.newBuilder()
            .url(url)
            .build()
        return chain.proceed(request)
    }
}