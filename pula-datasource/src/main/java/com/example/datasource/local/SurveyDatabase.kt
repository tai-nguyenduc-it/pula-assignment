package com.example.datasource.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.datasource.local.model.SurveyDatabaseModel

@Database(
    entities = [SurveyDatabaseModel::class],
    version = 1,
    exportSchema = false
)
abstract class SurveyDatabase : RoomDatabase() {
    abstract fun surveyDao(): SurveyDao
}
