package com.glashpoy.weather.di

import com.glashpoy.weather.data.repositories.location.ArcusLocationServicesRepository
import com.glashpoy.weather.data.repositories.location.LocationServicesRepository
import com.glashpoy.weather.data.repositories.textgenerator.ArcusGenerativeTextRepository
import com.glashpoy.weather.data.repositories.textgenerator.GenerativeTextRepository
import com.glashpoy.weather.data.repositories.weather.ArcusWeatherRepository
import com.glashpoy.weather.data.repositories.weather.WeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class RepositoriesModule {

    @Binds
    abstract fun bindLocationServicesRepository(
        impl: ArcusLocationServicesRepository
    ): LocationServicesRepository

    @Binds
    abstract fun bindWeatherRepository(
        impl: ArcusWeatherRepository
    ): WeatherRepository

    @Binds
    abstract fun bindGenerativeTextRepository(
        impl: ArcusGenerativeTextRepository
    ): GenerativeTextRepository
}