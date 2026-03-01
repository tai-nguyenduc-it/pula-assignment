package com.example.datasource.remote

import com.example.datasource.remote.model.SurveyUploadRequestApiModel
import com.example.datasource.remote.model.UploadFailureException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class MockSyncApi(
    private val dispatcher: CoroutineContext,
    private val config: MockConfig = MockConfig()
) : SurveyService {

    data class MockConfig(
        val failCallIndex: Int = 0,
        val failWith: UploadFailureException = UploadFailureException.ServerError(
            500,
            "Server error"
        ),
        val timeoutAfterCalls: Int = 0,
        val timeoutDelayMs: Long = 100
    )

    private var callCount = 0

    override suspend fun upload(payload: SurveyUploadRequestApiModel): Result<Unit> =
        withContext(dispatcher) {
            callCount++
            if (config.timeoutAfterCalls > 0 && callCount >= config.timeoutAfterCalls) {
                delay(config.timeoutDelayMs)
                return@withContext Result.failure(UploadFailureException.Timeout)
            }
            if (config.failCallIndex > 0 && callCount == config.failCallIndex) {
                return@withContext Result.failure(config.failWith)
            }
            Result.success(Unit)
        }

    fun resetCallCount() {
        callCount = 0
    }
}
