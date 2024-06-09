package com.glashpoy.weather.di

import com.glashpoy.weather.BuildConfig
import com.glashpoy.weather.data.remote.languagemodel.GeminiTextGeneratorClient
import com.glashpoy.weather.data.remote.languagemodel.TextGeneratorClient
import com.glashpoy.weather.data.remote.languagemodel.TextGeneratorClientConstants
import com.glashpoy.weather.data.remote.location.LocationClient
import com.glashpoy.weather.data.remote.location.LocationClientConstants
import com.glashpoy.weather.data.remote.weather.WeatherClient
import com.glashpoy.weather.data.remote.weather.WeatherClientConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
annotation class OpenAIClient

@Qualifier
annotation class GeminiClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideWeatherClient(): WeatherClient = Retrofit.Builder()
        .baseUrl(WeatherClientConstants.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(WeatherClient::class.java)

    @Provides
    @Singleton
    fun provideLocationClient(): LocationClient = Retrofit.Builder()
        .baseUrl(LocationClientConstants.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(LocationClient::class.java)

    @Provides
    @Singleton
    @OpenAIClient
    fun provideOpenAITextGeneratorClient(): TextGeneratorClient = Retrofit.Builder()
        .client(
            OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer ${BuildConfig.OPEN_AI_API_TOKEN}")
                        .build()
                    chain.proceed(newRequest)
                }
                .build()
        )
        .baseUrl(TextGeneratorClientConstants.BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(TextGeneratorClient::class.java)

    @Provides
    @Singleton
    @GeminiClient
    fun provideGeminiTextGeneratorClient(): TextGeneratorClient = GeminiTextGeneratorClient()
}