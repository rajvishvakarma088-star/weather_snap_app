package com.weathersnap.di

import android.content.Context
import androidx.room.Room
import com.weathersnap.data.local.WeatherDatabase
import com.weathersnap.data.local.WeatherReportDao
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
    fun provideWeatherDatabase(@ApplicationContext context: Context): WeatherDatabase {
        return Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "weather_snap.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideWeatherReportDao(database: WeatherDatabase): WeatherReportDao {
        return database.reportDao()
    }
}
