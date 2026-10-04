package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.DailyProgressDao
import com.example.data.local.dao.EditorialDao
import com.example.data.local.dao.GrammarDao
import com.example.data.local.dao.ImportHistoryDao
import com.example.data.local.dao.MistakeDao
import com.example.data.local.dao.PhraseDao
import com.example.data.local.dao.PracticeTestDao
import com.example.data.local.dao.VocabularyDao
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.Converters
import com.example.data.local.entity.DailyProgressEntity
import com.example.data.local.entity.EditorialEntity
import com.example.data.local.entity.EditorialVocabularyCrossRef
import com.example.data.local.entity.GrammarRuleEntity
import com.example.data.local.entity.ImportHistoryEntity
import com.example.data.local.entity.MistakeEntity
import com.example.data.local.entity.PhraseEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.TestAttemptEntity
import com.example.data.local.entity.TestEntity
import com.example.data.local.entity.TestQuestionCrossRef
import com.example.data.local.entity.VocabularyEntity
import com.example.data.local.entity.WordCategoryCrossRef

@Database(
    entities = [
        EditorialEntity::class,
        VocabularyEntity::class,
        CategoryEntity::class,
        WordCategoryCrossRef::class,
        EditorialVocabularyCrossRef::class,
        GrammarRuleEntity::class,
        PhraseEntity::class,
        QuestionEntity::class,
        TestEntity::class,
        TestQuestionCrossRef::class,
        TestAttemptEntity::class,
        MistakeEntity::class,
        DailyProgressEntity::class,
        ImportHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun editorialDao(): EditorialDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun grammarDao(): GrammarDao
    abstract fun phraseDao(): PhraseDao
    abstract fun practiceTestDao(): PracticeTestDao
    abstract fun mistakeDao(): MistakeDao
    abstract fun dailyProgressDao(): DailyProgressDao
    abstract fun importHistoryDao(): ImportHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "editorial_english.db"
                ).fallbackToDestructiveMigration(false)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
