package com.example.pulaassignment.di

import com.example.data.repository.SurveyRepositoryImpl
import com.example.data.repository.SurveyResponseUploadRepositoryImpl
import com.example.datasource.local.SurveyDao
import com.example.datasource.remote.SurveyService
import com.example.domain.repository.SurveyRepository
import com.example.domain.repository.SurveyResponseUploadRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideSurveyRepository(dao: SurveyDao): SurveyRepository = SurveyRepositoryImpl(dao)

    @Provides
    @Singleton
    fun provideSurveyResponseUploadRepository(api: SurveyService): SurveyResponseUploadRepository =
        SurveyResponseUploadRepositoryImpl(api)
}
