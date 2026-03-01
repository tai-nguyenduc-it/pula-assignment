package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.data.repository.SurveyRepositoryImpl
import com.example.datasource.local.SurveyDao
import com.example.datasource.local.SurveyDatabase
import com.example.domain.model.QuestionAnswerDomainModel
import com.example.domain.model.RepeatingSectionDomainModel
import com.example.domain.model.SurveyResponseDomainModel
import com.example.domain.model.SyncStatusDomainModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SurveyRepositoryTest {

    private lateinit var db: SurveyDatabase
    private lateinit var dao: SurveyDao
    private lateinit var repository: SurveyRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SurveyDatabase::class.java).build()
        dao = db.surveyDao()
        repository = SurveyRepositoryImpl(dao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun saveAndRetrieve_responsePersisted() = runBlocking {
        val response = SurveyResponseDomainModel(
            id = "r1",
            farmerId = "f1",
            surveyId = "s1",
            answers = listOf(QuestionAnswerDomainModel("q1", "a1")),
            repeatingSections = listOf(RepeatingSectionDomainModel("sec1", listOf(QuestionAnswerDomainModel("q2", "v2")))),
            attachmentPaths = listOf("/path/photo.jpg"),
            syncStatus = SyncStatusDomainModel.Pending,
            createdAtMillis = 1000L
        )
        repository.saveResponse(response)
        val loaded = repository.getResponseById("r1")
        assertEquals(response.id, loaded?.id)
        assertEquals(response.answers.size, loaded?.answers?.size)
        assertEquals(response.repeatingSections.size, loaded?.repeatingSections?.size)
        assertEquals(response.attachmentPaths, loaded?.attachmentPaths)
        assertEquals(SyncStatusDomainModel.Pending, loaded?.syncStatus)
    }

    @Test
    fun getPendingResponses_onlyPendingAndFailed() = runBlocking {
        repository.saveResponse(createResponse("1", SyncStatusDomainModel.Pending))
        repository.saveResponse(createResponse("2", SyncStatusDomainModel.Failed))
        repository.saveResponse(createResponse("3", SyncStatusDomainModel.Synced))
        val pending = repository.getPendingResponsesOnce()
        assertEquals(2, pending.size)
        assertEquals(setOf("1", "2"), pending.map { it.id }.toSet())
    }

    @Test
    fun updateSyncStatus_statusPersisted() = runBlocking {
        repository.saveResponse(createResponse("1", SyncStatusDomainModel.Pending))
        repository.updateSyncStatus("1", SyncStatusDomainModel.Synced)
        assertEquals(SyncStatusDomainModel.Synced, repository.getResponseById("1")?.syncStatus)
    }

    @Test
    fun observePendingCount_emitsCorrectCount() = runBlocking {
        repository.saveResponse(createResponse("1", SyncStatusDomainModel.Pending))
        assertEquals(1, repository.observePendingCount().first())
        repository.saveResponse(createResponse("2", SyncStatusDomainModel.Pending))
        assertEquals(2, repository.observePendingCount().first())
        repository.updateSyncStatus("1", SyncStatusDomainModel.Synced)
        assertEquals(1, repository.observePendingCount().first())
    }

    private fun createResponse(id: String, status: SyncStatusDomainModel) = SurveyResponseDomainModel(
        id = id,
        farmerId = "f1",
        surveyId = "s1",
        answers = emptyList(),
        repeatingSections = emptyList(),
        attachmentPaths = emptyList(),
        syncStatus = status,
        createdAtMillis = System.currentTimeMillis()
    )
}
