package com.alexredchets.belkaplayer.di

import android.app.Application
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.alexredchets.belkaplayer.network.BelkaPlayerApi
import com.alexredchets.belkaplayer.network.MainRepo
import com.alexredchets.belkaplayer.network.NetManager
import com.alexredchets.belkaplayer.network.NetworkDecorator
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.noties.markwon.Markwon
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object ViewPlayerModule {

    @Provides
    fun provideVideoPlayer(app: Application): Player {
        return ExoPlayer.Builder(app)
            .build()
    }

    @Provides
    @Singleton
    fun createOkHttpClient(networkDecorator: NetworkDecorator): OkHttpClient {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(httpLoggingInterceptor).apply {
                connectionSpecs(
                    listOf(
                        ConnectionSpec.CLEARTEXT,
                        ConnectionSpec.MODERN_TLS
                    )
                )
                networkDecorator.decorate(this)
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideApi(okHttpClient: OkHttpClient): BelkaPlayerApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
        return retrofit.create(BelkaPlayerApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNetManager(app: Application) = NetManager(app)

    @Provides
    @Singleton
    fun provideMainRepo(api: BelkaPlayerApi): MainRepo = MainRepo(api)

    @Provides
    @Singleton
    fun provideMarkwon(context: Application): Markwon = Markwon.create(context)
}