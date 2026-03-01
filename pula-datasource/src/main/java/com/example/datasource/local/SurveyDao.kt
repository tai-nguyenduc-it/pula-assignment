package com.example.datasource.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.datasource.local.model.SurveyDatabaseModel
import kotlinx.coroutines.flow.Flow

@Dao
interface SurveyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(model: SurveyDatabaseModel)

    @Update
    suspend fun update(model: SurveyDatabaseModel)

    @Query("SELECT * FROM survey_response WHERE id = :id")
    suspend fun getById(id: String): SurveyDatabaseModel?

    @Query("SELECT * FROM survey_response WHERE syncStatus IN ('Pending', 'Failed') ORDER BY createdAtMillis ASC")
    fun getPendingFlow(): Flow<List<SurveyDatabaseModel>>

    @Query("SELECT * FROM survey_response WHERE syncStatus IN ('Pending', 'Failed') ORDER BY createdAtMillis ASC")
    suspend fun getPendingOnce(): List<SurveyDatabaseModel>

    @Query("UPDATE survey_response SET syncStatus = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("SELECT COUNT(*) FROM survey_response WHERE syncStatus IN ('Pending', 'Failed')")
    fun observePendingCount(): Flow<Int>
}
