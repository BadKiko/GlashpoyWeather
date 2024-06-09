package com.glashpoy.weather.di

import com.glashpoy.weather.data.remote.location.ArcusReverseGeocoder
import com.glashpoy.weather.data.remote.location.ReverseGeocoder
import com.glashpoy.weather.domain.location.CurrentLocationProvider
import com.glashpoy.weather.domain.location.ArcusCurrentLocationProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
abstract class LocationServicesModule {

    @Binds
    abstract fun bindCurrentLocationProvider(impl: ArcusCurrentLocationProvider): CurrentLocationProvider

    @Binds
    abstract fun bindReverseGeocoder(impl: ArcusReverseGeocoder): ReverseGeocoder
}