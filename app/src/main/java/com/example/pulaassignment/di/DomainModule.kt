package com.example.pulaassignment.di

import com.example.domain.repository.SurveyRepository
import com.example.domain.repository.SurveyResponseUploadRepository
import com.example.domain.usecase.GetPendingCountUseCase
import com.example.domain.usecase.GetPendingResponsesUseCase
import com.example.domain.usecase.SubmitSurveyUseCase
import com.example.domain.usecase.SyncPendingUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideSubmitSurveyUseCase(repository: SurveyRepository): SubmitSurveyUseCase =
        SubmitSurveyUseCase(repository)

    @Provides
    @Singleton
    fun provideGetPendingResponsesUseCase(repository: SurveyRepository): GetPendingResponsesUseCase =
        GetPendingResponsesUseCase(repository)

    @Provides
    @Singleton
    fun provideGetPendingCountUseCase(repository: SurveyRepository): GetPendingCountUseCase =
        GetPendingCountUseCase(repository)

    @Provides
    @Singleton
    fun provideSyncPendingUseCase(
        repository: SurveyRepository,
        uploadRepository: SurveyResponseUploadRepository
    ): SyncPendingUseCase = SyncPendingUseCase(
        repository = repository,
        uploadRepository = uploadRepository,
        dispatcher = Dispatchers.Default
    )
}
