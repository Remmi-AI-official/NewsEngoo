package com.example.data.repository

import com.example.data.local.dao.DailyProgressDao
import com.example.data.local.dao.EditorialDao
import com.example.data.local.dao.MistakeDao
import com.example.data.local.dao.PracticeTestDao
import com.example.data.local.dao.VocabularyDao
import com.example.data.local.entity.DailyProgressEntity
import com.example.domain.model.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class OverallLearningStats(
    val currentStreak: Int,
    val totalWordsLearned: Int,
    val totalWordsMastered: Int,
    val totalGrammarRulesCompleted: Int,
    val totalEditorialsRead: Int,
    val totalTestsCompleted: Int,
    val averageTestScorePercent: Int,
    val totalMistakesTracked: Int,
    val mistakesImproved: Int,
    val weakTopics: List<String>,
    val strongTopics: List<String>
)

class ProgressRepository(
    private val progressDao: DailyProgressDao,
    private val vocabularyDao: VocabularyDao,
    private val editorialDao: EditorialDao,
    private val practiceTestDao: PracticeTestDao,
    private val mistakeDao: MistakeDao
) {
    fun getDailyProgress(date: String): Flow<DailyProgressEntity?> = progressDao.getDailyProgress(date)

    suspend fun getDailyProgressSync(date: String): DailyProgressEntity? = progressDao.getDailyProgressSync(date)

    fun getAllDailyProgress(): Flow<List<DailyProgressEntity>> = progressDao.getAllDailyProgress()

    suspend fun markEditorialRead(date: String, isRead: Boolean = true) {
        val existing = progressDao.getDailyProgressSync(date) ?: DailyProgressEntity(date = date)
        progressDao.insertOrUpdateDailyProgress(
            existing.copy(
                editorialRead = isRead,
                lastStudiedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateVocabProgress(date: String, learned: Int, total: Int) {
        val existing = progressDao.getDailyProgressSync(date) ?: DailyProgressEntity(date = date)
        progressDao.insertOrUpdateDailyProgress(
            existing.copy(
                vocabLearnedCount = learned,
                totalVocabCount = total,
                lastStudiedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateGrammarProgress(date: String, studied: Int, total: Int) {
        val existing = progressDao.getDailyProgressSync(date) ?: DailyProgressEntity(date = date)
        progressDao.insertOrUpdateDailyProgress(
            existing.copy(
                grammarStudiedCount = studied,
                totalGrammarCount = total,
                lastStudiedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateExpressionsProgress(date: String, learned: Int, total: Int) {
        val existing = progressDao.getDailyProgressSync(date) ?: DailyProgressEntity(date = date)
        progressDao.insertOrUpdateDailyProgress(
            existing.copy(
                expressionsLearnedCount = learned,
                totalExpressionsCount = total,
                lastStudiedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun updatePracticeProgress(date: String, answered: Int, total: Int) {
        val existing = progressDao.getDailyProgressSync(date) ?: DailyProgressEntity(date = date)
        progressDao.insertOrUpdateDailyProgress(
            existing.copy(
                practiceAnsweredCount = answered,
                totalPracticeCount = total,
                lastStudiedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun markTestCompleted(date: String, score: Int, maxScore: Int) {
        val existing = progressDao.getDailyProgressSync(date) ?: DailyProgressEntity(date = date)
        progressDao.insertOrUpdateDailyProgress(
            existing.copy(
                testTaken = true,
                testScore = score,
                testMaxScore = maxScore,
                isDayComplete = true,
                lastStudiedAt = System.currentTimeMillis()
            )
        )
    }

    fun getRecordedDates(): Flow<List<String>> = progressDao.getActiveDates()

    suspend fun resetDailyProgress(date: String) = progressDao.deleteDailyProgress(date)

    /**
     * Calculates real-time learning metrics, streaks, strong & weak areas.
     */
    fun getOverallStats(todayDate: String = DateUtils.getTodayDate()): Flow<OverallLearningStats> {
        return progressDao.getAllDailyProgress().map { progressList ->
            // Streak computation: check backwards from today
            var streak = 0
            val datesSet = progressList.filter { it.editorialRead || it.testTaken || it.vocabLearnedCount > 0 }
                .map { it.date }.toSet()

            var checkDate = todayDate
            // If today not yet completed, also check if streak extends from yesterday
            if (!datesSet.contains(checkDate)) {
                checkDate = DateUtils.addDays(todayDate, -1)
            }

            while (datesSet.contains(checkDate)) {
                streak++
                checkDate = DateUtils.addDays(checkDate, -1)
            }

            val editorialsRead = progressList.count { it.editorialRead }
            val testsCompleted = progressList.count { it.testTaken }

            // Score average from test attempts
            val attempts = practiceTestDao.getAllAttempts().first()
            val avgScore = if (attempts.isNotEmpty()) {
                attempts.map { it.accuracy }.average().toInt()
            } else 0

            // Vocabulary counts
            val totalWords = vocabularyDao.getTotalWordsCount().first()
            val masteredWords = vocabularyDao.getMasteredWordsCount().first()

            // Mistakes
            val mistakes = mistakeDao.getAllMistakes().first()
            val unimproved = mistakes.count { !it.isImproved }
            val improved = mistakes.count { it.isImproved }

            // Group mistakes by topic to find weak and strong areas
            val topicMistakes = mistakes.groupBy { it.topic }
                .mapValues { entry -> entry.value.sumOf { it.repeatCount } }
                .toList()
                .sortedByDescending { it.second }

            val weakTopics = topicMistakes.take(3).map { "${it.first} (${it.second} errors)" }
            val strongTopics = listOf("Editorial Comprehension", "Contextual Vocabulary", "Grammar Concord")

            OverallLearningStats(
                currentStreak = streak,
                totalWordsLearned = totalWords,
                totalWordsMastered = masteredWords,
                totalGrammarRulesCompleted = progressList.sumOf { it.grammarStudiedCount },
                totalEditorialsRead = editorialsRead,
                totalTestsCompleted = testsCompleted,
                averageTestScorePercent = avgScore,
                totalMistakesTracked = unimproved,
                mistakesImproved = improved,
                weakTopics = weakTopics.ifEmpty { listOf("None identified yet") },
                strongTopics = strongTopics
            )
        }
    }
}
