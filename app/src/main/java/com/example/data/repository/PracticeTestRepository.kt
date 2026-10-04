package com.example.data.repository

import com.example.data.local.dao.MistakeDao
import com.example.data.local.dao.PracticeTestDao
import com.example.data.local.entity.MistakeEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.TestAttemptEntity
import com.example.data.local.entity.TestEntity
import com.example.data.local.entity.TestQuestionCrossRef
import com.example.domain.model.DateUtils
import com.example.domain.model.QuestionImportItem
import com.example.domain.model.TestImportItem
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class TestEvaluationResult(
    val attemptId: String,
    val totalQuestions: Int,
    val score: Int,
    val accuracy: Float,
    val timeSpentSeconds: Int,
    val topicScores: Map<String, Pair<Int, Int>>, // Topic -> Pair(Correct, Total)
    val detailedAnswers: List<UserAnswerEvaluation>
)

data class UserAnswerEvaluation(
    val questionId: String,
    val questionText: String,
    val selectedOptionIndex: Int,
    val correctOptionIndex: Int,
    val isCorrect: Boolean,
    val explanation: String,
    val topic: String
)

class PracticeTestRepository(
    private val testDao: PracticeTestDao,
    private val mistakeDao: MistakeDao
) {
    fun getQuestionsForDate(date: String): Flow<List<QuestionEntity>> = testDao.getQuestionsForDate(date)

    fun getQuestionsForTest(testId: String): Flow<List<QuestionEntity>> = testDao.getQuestionsForTest(testId)

    suspend fun getQuestionsForTestSync(testId: String): List<QuestionEntity> =
        testDao.getQuestionsForTestSync(testId)

    fun getAllQuestions(): Flow<List<QuestionEntity>> = testDao.getAllQuestions()

    fun getAllTests(): Flow<List<TestEntity>> = testDao.getAllTests()

    fun getTestsForDate(date: String): Flow<List<TestEntity>> = testDao.getTestsForDate(date)

    fun getTestById(testId: String): Flow<TestEntity?> = testDao.getTestById(testId)

    suspend fun getTestByIdSync(testId: String): TestEntity? = testDao.getTestByIdSync(testId)

    fun getAllAttempts(): Flow<List<TestAttemptEntity>> = testDao.getAllAttempts()

    fun getAttemptsForTest(testId: String): Flow<List<TestAttemptEntity>> = testDao.getAttemptsForTest(testId)

    fun getAttemptById(attemptId: String): Flow<TestAttemptEntity?> = testDao.getAttemptById(attemptId)

    suspend fun toggleQuestionFavorite(id: String, isFavorite: Boolean) =
        testDao.setQuestionFavorite(id, isFavorite)

    fun getFavoriteQuestions(): Flow<List<QuestionEntity>> = testDao.getFavoriteQuestions()

    fun searchQuestions(query: String): Flow<List<QuestionEntity>> = testDao.searchQuestions(query)

    // Mistake Book
    fun getAllMistakes(): Flow<List<MistakeEntity>> = mistakeDao.getAllMistakes()

    fun getUnimprovedMistakes(): Flow<List<MistakeEntity>> = mistakeDao.getUnimprovedMistakes()

    suspend fun markMistakeImproved(id: String, isImproved: Boolean) =
        mistakeDao.markMistakeImproved(id, isImproved, System.currentTimeMillis())

    suspend fun deleteMistake(id: String) = mistakeDao.deleteMistake(id)

    suspend fun deleteTest(testId: String) {
        testDao.deleteTestQuestionRefs(testId)
        testDao.deleteTest(testId)
    }

    suspend fun deleteTestsForDate(date: String) {
        testDao.deleteTestsForDate(date)
        testDao.deleteQuestionsForDate(date)
    }

    suspend fun deleteAllTests() {
        testDao.deleteAllTests()
    }

    suspend fun deleteAllQuestions() {
        testDao.deleteAllQuestions()
    }

    suspend fun deleteQuestion(questionId: String) {
        testDao.deleteQuestion(questionId)
    }

    /**
     * Evaluates a completed test, records the attempt, and logs mistakes.
     */
    suspend fun submitTest(
        testId: String,
        testTitle: String,
        date: String,
        startedAt: Long,
        timeSpentSeconds: Int,
        questions: List<QuestionEntity>,
        userAnswers: Map<String, Int> // QuestionId -> selectedIndex
    ): TestEvaluationResult {
        var score = 0
        val detailedEvaluations = mutableListOf<UserAnswerEvaluation>()
        val topicScores = mutableMapOf<String, Pair<Int, Int>>() // Topic -> Pair(Correct, Total)
        val answersJsonArray = JSONArray()

        val today = DateUtils.getTodayDate()

        for (q in questions) {
            val selected = userAnswers[q.id] ?: -1
            val isCorrect = selected == q.correctAnswerIndex

            if (isCorrect) score++

            // Track topic score
            val currentTopicScore = topicScores[q.topic] ?: Pair(0, 0)
            topicScores[q.topic] = Pair(
                currentTopicScore.first + (if (isCorrect) 1 else 0),
                currentTopicScore.second + 1
            )

            detailedEvaluations.add(
                UserAnswerEvaluation(
                    questionId = q.id,
                    questionText = q.question,
                    selectedOptionIndex = selected,
                    correctOptionIndex = q.correctAnswerIndex,
                    isCorrect = isCorrect,
                    explanation = q.explanation,
                    topic = q.topic
                )
            )

            val answerObj = JSONObject()
            answerObj.put("questionId", q.id)
            answerObj.put("question", q.question)
            answerObj.put("selected", selected)
            answerObj.put("correct", q.correctAnswerIndex)
            answerObj.put("isCorrect", isCorrect)
            answersJsonArray.put(answerObj)

            // If incorrect, record in Mistake Book
            if (!isCorrect) {
                val existingMistake = mistakeDao.getMistakeByQuestionIdSync(q.id)
                val userChoiceText = if (selected in q.options.indices) q.options[selected] else "No Answer"
                val correctChoiceText = if (q.correctAnswerIndex in q.options.indices) q.options[q.correctAnswerIndex] else q.correctAnswerText

                if (existingMistake == null) {
                    mistakeDao.insertOrUpdateMistake(
                        MistakeEntity(
                            id = "mistake_${q.id}",
                            questionId = q.id,
                            questionText = q.question,
                            topic = q.topic,
                            userWrongAnswer = userChoiceText,
                            correctAnswer = correctChoiceText,
                            explanation = q.explanation,
                            relatedConcept = q.relatedWordOrRule,
                            repeatCount = 1,
                            isImproved = false,
                            firstMistakeDate = today,
                            lastMistakeDate = today
                        )
                    )
                } else {
                    mistakeDao.insertOrUpdateMistake(
                        existingMistake.copy(
                            repeatCount = existingMistake.repeatCount + 1,
                            userWrongAnswer = userChoiceText,
                            lastMistakeDate = today,
                            isImproved = false
                        )
                    )
                }
            }
        }

        val accuracy = if (questions.isNotEmpty()) (score.toFloat() / questions.size) * 100f else 0f
        val attemptId = "att_${System.currentTimeMillis()}"

        // Topic breakdown JSON
        val topicBreakdownObj = JSONObject()
        topicScores.forEach { (topic, pair) ->
            val tObj = JSONObject()
            tObj.put("correct", pair.first)
            tObj.put("total", pair.second)
            topicBreakdownObj.put(topic, tObj)
        }

        val attempt = TestAttemptEntity(
            id = attemptId,
            testId = testId,
            testTitle = testTitle,
            date = date,
            startedAt = startedAt,
            completedAt = System.currentTimeMillis(),
            totalQuestions = questions.size,
            score = score,
            accuracy = accuracy,
            timeSpentSeconds = timeSpentSeconds,
            answersJson = answersJsonArray.toString(),
            topicBreakdownJson = topicBreakdownObj.toString()
        )
        testDao.insertAttempt(attempt)

        return TestEvaluationResult(
            attemptId = attemptId,
            totalQuestions = questions.size,
            score = score,
            accuracy = accuracy,
            timeSpentSeconds = timeSpentSeconds,
            topicScores = topicScores,
            detailedAnswers = detailedEvaluations
        )
    }

    /**
     * Import practice questions and optionally link to a test.
     */
    suspend fun importQuestions(items: List<QuestionImportItem>, date: String, testId: String = ""): Int {
        val entities = items.mapIndexed { idx, item ->
            val id = item.id.ifBlank { "q_${date}_${System.currentTimeMillis()}_$idx" }
            QuestionEntity(
                id = id,
                date = date,
                testId = testId,
                type = item.type,
                question = item.question.trim(),
                options = item.options,
                correctAnswerIndex = item.answerIndex,
                correctAnswerText = item.answerText,
                explanation = item.explanation.trim(),
                topic = item.topic,
                difficulty = item.difficulty,
                relatedWordOrRule = item.relatedConcept
            )
        }
        testDao.insertQuestions(entities)

        if (testId.isNotBlank()) {
            val refs = entities.mapIndexed { i, q ->
                TestQuestionCrossRef(testId = testId, questionId = q.id, orderIndex = i)
            }
            testDao.linkQuestionsToTest(refs)
        }
        return entities.size
    }

    /**
     * Import a complete Test with its questions.
     */
    suspend fun importTest(testItem: TestImportItem, date: String): String {
        val testId = testItem.testId.ifBlank { "daily_$date" }
        val testEntity = TestEntity(
            id = testId,
            date = date,
            title = testItem.title,
            type = if (testId.startsWith("weekly")) "weekly" else "daily",
            durationMinutes = testItem.durationMinutes,
            totalQuestions = testItem.questions.size
        )
        testDao.insertTest(testEntity)
        importQuestions(testItem.questions, date, testId)
        return testId
    }

    /**
     * Synthesizes a Weekly Test using previous available questions.
     */
    suspend fun generateWeeklyTest(currentDate: String = DateUtils.getTodayDate()): TestEntity {
        val weeklyTestId = "weekly_${currentDate}"
        val existing = testDao.getTestByIdSync(weeklyTestId)
        if (existing != null) return existing

        val allQuestions = testDao.getAllQuestionsSync()
        // Take up to 25-30 diverse questions
        val selected = allQuestions.shuffled().take(25)

        val testEntity = TestEntity(
            id = weeklyTestId,
            date = currentDate,
            title = "Weekly Editorial Comprehensive Test",
            type = "weekly",
            durationMinutes = 25,
            totalQuestions = selected.size,
            instructions = "Comprehensive 25-question test covering editorial vocabulary, grammar nuances, comprehension, and idiomatic usage from the past week."
        )
        testDao.insertTest(testEntity)

        val refs = selected.mapIndexed { idx, q ->
            TestQuestionCrossRef(testId = weeklyTestId, questionId = q.id, orderIndex = idx)
        }
        testDao.linkQuestionsToTest(refs)
        return testEntity
    }
}
