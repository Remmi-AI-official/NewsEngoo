package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.ContentImportExportRepository
import com.example.data.repository.EditorialRepository
import com.example.data.repository.GrammarRepository
import com.example.data.repository.PhraseRepository
import com.example.data.repository.PracticeTestRepository
import com.example.data.repository.ProgressRepository
import com.example.data.repository.UserPreferencesRepository
import com.example.data.repository.VocabularyRepository
import com.example.data.seed.SeedDataLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EditorialApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getInstance(this) }

    val userPreferencesRepository by lazy { UserPreferencesRepository(this) }
    val editorialRepository by lazy { EditorialRepository(database.editorialDao()) }
    val vocabularyRepository by lazy { VocabularyRepository(database.vocabularyDao()) }
    val grammarRepository by lazy { GrammarRepository(database.grammarDao()) }
    val phraseRepository by lazy { PhraseRepository(database.phraseDao()) }
    val practiceTestRepository by lazy {
        PracticeTestRepository(database.practiceTestDao(), database.mistakeDao())
    }
    val progressRepository by lazy {
        ProgressRepository(
            database.dailyProgressDao(),
            database.vocabularyDao(),
            database.editorialDao(),
            database.practiceTestDao(),
            database.mistakeDao()
        )
    }
    val contentImportExportRepository by lazy {
        ContentImportExportRepository(
            editorialRepository,
            vocabularyRepository,
            grammarRepository,
            phraseRepository,
            practiceTestRepository,
            progressRepository,
            database.importHistoryDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            SeedDataLoader.seedInitialDataIfEmpty(database)
        }
    }
}
