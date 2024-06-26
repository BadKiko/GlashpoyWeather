package com.glashpoy.weather.data.local.weather

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [SavedWeatherLocationEntity::class], version = 1)
abstract class ArcusDatabase : RoomDatabase() {

    abstract fun getDao(): ArcusDatabaseDao

    companion object {
        const val DATABASE_NAME = "Weather_Database"
    }
}