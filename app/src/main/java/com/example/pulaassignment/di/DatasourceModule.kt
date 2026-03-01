package com.example.pulaassignment.di

import android.content.Context
import androidx.room.Room
import com.example.datasource.local.SurveyDao
import com.example.datasource.local.SurveyDatabase
import com.example.datasource.remote.MockSyncApi
import com.example.datasource.remote.SurveyService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatasourceModule {

    @Provides
    @Singleton
    fun provideSurveyDatabase(
        @ApplicationContext context: Context
    ): SurveyDatabase = Room.databaseBuilder(
        context,
        SurveyDatabase::class.java,
        "survey_db"
    ).build()

    @Provides
    @Singleton
    fun provideSurveyDao(database: SurveyDatabase): SurveyDao = database.surveyDao()

    @Provides
    @Singleton
    fun provideSurveyService(): SurveyService = MockSyncApi(
        dispatcher = Dispatchers.IO,
        config = MockSyncApi.MockConfig()
    )
}
