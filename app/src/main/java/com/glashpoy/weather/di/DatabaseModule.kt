package com.glashpoy.weather.di

import android.content.Context
import androidx.room.Room
import com.glashpoy.weather.data.local.textgeneration.ArcusGeneratedTextCacheDatabase
import com.glashpoy.weather.data.local.textgeneration.GeneratedTextCacheDatabaseDao
import com.glashpoy.weather.data.local.weather.ArcusDatabase
import com.glashpoy.weather.data.local.weather.ArcusDatabaseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideArcusDatabaseDao(
        @ApplicationContext context: Context
    ): ArcusDatabaseDao = Room.databaseBuilder(
        context = context,
        klass = ArcusDatabase::class.java,
        name = ArcusDatabase.DATABASE_NAME
    ).build().getDao()

    @Provides
    @Singleton
    fun provideGeneratedTextCacheDatabaseDao(
        @ApplicationContext context: Context
    ): GeneratedTextCacheDatabaseDao = Room.databaseBuilder(
        context = context,
        klass = ArcusGeneratedTextCacheDatabase::class.java,
        name = ArcusGeneratedTextCacheDatabase.DATABASE_NAME
    ).build().getDao()

}