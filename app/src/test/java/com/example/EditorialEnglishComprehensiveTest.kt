package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.EditorialEntity
import com.example.data.local.entity.MistakeEntity
import com.example.data.local.entity.QuestionEntity
import com.example.data.local.entity.VocabularyEntity
import com.example.data.repository.ContentImportExportRepository
import com.example.data.repository.EditorialRepository
import com.example.data.repository.GrammarRepository
import com.example.data.repository.PhraseRepository
import com.example.data.repository.PracticeTestRepository
import com.example.data.repository.ProgressRepository
import com.example.data.repository.VocabularyRepository
import com.example.domain.model.ContentParser
import com.example.domain.model.DateUtils
import com.example.domain.model.ImportMode
import com.example.domain.model.SpacedRepetitionCalculator
import com.example.domain.model.WordLearningStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class EditorialEnglishComprehensiveTest {

    private lateinit var database: AppDatabase
    private lateinit var editorialRepo: EditorialRepository
    private lateinit var vocabRepo: VocabularyRepository
    private lateinit var grammarRepo: GrammarRepository
    private lateinit var phraseRepo: PhraseRepository
    private lateinit var testRepo: PracticeTestRepository
    private lateinit var progressRepo: ProgressRepository
    private lateinit var importExportRepo: ContentImportExportRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        editorialRepo = EditorialRepository(database.editorialDao())
        vocabRepo = VocabularyRepository(database.vocabularyDao())
        grammarRepo = GrammarRepository(database.grammarDao())
        phraseRepo = PhraseRepository(database.phraseDao())
        testRepo = PracticeTestRepository(database.practiceTestDao(), database.mistakeDao())
        progressRepo = ProgressRepository(
            database.dailyProgressDao(),
            database.vocabularyDao(),
            database.editorialDao(),
            database.practiceTestDao(),
            database.mistakeDao()
        )
        importExportRepo = ContentImportExportRepository(
            editorialRepo,
            vocabRepo,
            grammarRepo,
            phraseRepo,
            testRepo,
            progressRepo,
            database.importHistoryDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    // 1. JSON Parsing Tests
    @Test
    fun testVocabularyJsonParsing() {
        val json = """
        {
            "date": "2026-10-04",
            "category": "Economy",
            "words": [
                {
                    "word": "pragmatic",
                    "partOfSpeech": "adjective",
                    "meaning": "practical and realistic",
                    "hindiMeaning": "व्यावहारिक",
                    "synonyms": ["practical", "realistic"],
                    "antonyms": ["idealistic"],
                    "example": "Adopted a pragmatic approach."
                }
            ]
        }
        """.trimIndent()

        val result = ContentParser.parseVocabularyJson(json)
        assertTrue(result.isValid)
        assertEquals(1, result.items.size)
        val item = result.items.first()
        assertEquals("pragmatic", item.word)
        assertEquals("adjective", item.partOfSpeech)
        assertEquals("व्यावहारिक", item.hindiMeaning)
        assertEquals(2, item.synonyms.size)
        assertEquals("Economy", item.category)
    }

    @Test
    fun testMalformedJsonHandling() {
        val malformed = "{ this is completely broken json ::: "
        val result = ContentParser.parseVocabularyJson(malformed)
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }

    @Test
    fun testGrammarJsonParsing() {
        val json = """
        {
            "date": "2026-10-04",
            "rules": [
                {
                    "title": "Despite vs Although",
                    "rule": "Despite takes noun phrase; although takes clause.",
                    "explanation": "Never write despite of.",
                    "examples": ["Despite rain, we played."],
                    "commonErrors": ["Despite of rain"]
                }
            ]
        }
        """.trimIndent()

        val result = ContentParser.parseGrammarJson(json)
        assertTrue(result.isValid)
        assertEquals(1, result.items.size)
        assertEquals("Despite vs Although", result.items.first().title)
    }

    @Test
    fun testDailyPackageJsonParsing() {
        val json = """
        {
            "date": "2026-10-04",
            "editorial": {
                "title": "Clean Energy Leap",
                "contentMarkdown": "# Clean Energy\n\nTransition is underway.",
                "source": "The Hindu",
                "readTimeMinutes": 4
            },
            "vocabulary": [
                {
                    "word": "resilient",
                    "meaning": "able to withstand shock"
                }
            ],
            "grammar": [
                {
                    "title": "Inversion",
                    "rule": "Seldom has such growth occurred."
                }
            ],
            "phrases": [
                {
                    "phrase": "Bite the bullet",
                    "meaning": "Face a tough situation"
                }
            ]
        }
        """.trimIndent()

        val result = ContentParser.parseDailyPackageJson(json)
        assertTrue(result.isValid)
        assertEquals(1, result.items.size)
        val pkg = result.items.first()
        assertEquals("2026-10-04", pkg.date)
        assertEquals("Clean Energy Leap", pkg.editorial?.title)
        assertEquals(1, pkg.vocabulary.size)
        assertEquals(1, pkg.grammar.size)
        assertEquals(1, pkg.phrases.size)
    }

    // 2. Duplicate Vocabulary Merge Logic
    @Test
    fun testVocabularyDeduplicationAndMerge() = runBlocking {
        // Insert initial canonical word
        val initialWord = VocabularyEntity(
            id = "pragmatic",
            word = "Pragmatic",
            meaning = "practical",
            synonyms = listOf("realistic"),
            learningStatus = WordLearningStatus.REVIEW.name
        )
        vocabRepo.saveWord(initialWord)

        // Parse new import containing extra synonyms and Hindi meaning
        val newImport = listOf(
            com.example.domain.model.VocabularyImportItem(
                word = "pragmatic",
                meaning = "practical and realistic",
                hindiMeaning = "व्यावहारिक",
                synonyms = listOf("sensible", "realistic"),
                example = "He took a pragmatic stance."
            )
        )

        val summary = vocabRepo.importWords(
            items = newImport,
            date = "2026-10-04",
            editorialId = "ed_2026-10-04",
            mode = ImportMode.MERGE_DUPLICATES
        )

        assertEquals(0, summary.newCount)
        assertEquals(1, summary.mergedCount)

        // Verify word kept its learning progress (REVIEW) and combined synonyms
        val updated = vocabRepo.getWordByIdSync("pragmatic")
        assertNotNull(updated)
        assertEquals(WordLearningStatus.REVIEW.name, updated?.learningStatus)
        assertEquals("व्यावहारिक", updated?.hindiMeaning)
        assertTrue(updated?.synonyms?.contains("sensible") == true)
        assertTrue(updated?.synonyms?.contains("realistic") == true)
    }

    // 3. Category Creation and Word Association
    @Test
    fun testCategoryCreationAndAssociation() = runBlocking {
        vocabRepo.addCategory("Economy & Policy", "Macroeconomic topics")
        val categories = vocabRepo.getAllCategories().first()
        assertEquals(1, categories.size)
        val cat = categories.first()

        vocabRepo.saveWord(VocabularyEntity(id = "fiscal", word = "Fiscal", meaning = "relating to revenue"))
        vocabRepo.assignWordToCategory("fiscal", cat.id)

        val wordCats = vocabRepo.getCategoriesForWord("fiscal").first()
        assertEquals(1, wordCats.size)
        assertEquals("Economy & Policy", wordCats.first().name)
    }

    // 4. Spaced Revision Scheduling
    @Test
    fun testSpacedRevisionProgression() {
        val result1 = SpacedRepetitionCalculator.calculateNextReview(WordLearningStatus.NEW, true, "2026-10-04")
        assertEquals(WordLearningStatus.LEARNING, result1.newStatus)
        assertEquals("2026-10-05", result1.nextReviewDate)

        val result2 = SpacedRepetitionCalculator.calculateNextReview(WordLearningStatus.LEARNING, true, "2026-10-05")
        assertEquals(WordLearningStatus.REVIEW, result2.newStatus)
        assertEquals("2026-10-08", result2.nextReviewDate)

        // If incorrect, should drop back to LEARNING and schedule next review tomorrow
        val resultForgot = SpacedRepetitionCalculator.calculateNextReview(WordLearningStatus.FAMILIAR, false, "2026-10-04")
        assertEquals(WordLearningStatus.LEARNING, resultForgot.newStatus)
        assertEquals("2026-10-05", resultForgot.nextReviewDate)
    }

    // 5. Test Scoring, Evaluation, and Mistake Book Tracking
    @Test
    fun testTestEvaluationAndMistakeTracking() = runBlocking {
        val q1 = QuestionEntity(
            id = "q1",
            date = "2026-10-04",
            question = "Meaning of pragmatic?",
            options = listOf("Idealistic", "Practical"),
            correctAnswerIndex = 1,
            explanation = "Practical and realistic.",
            topic = "vocabulary"
        )
        val q2 = QuestionEntity(
            id = "q2",
            date = "2026-10-04",
            question = "Antonym of exacerbate?",
            options = listOf("Worsen", "Ameliorate"),
            correctAnswerIndex = 1,
            explanation = "Ameliorate means to improve.",
            topic = "vocabulary"
        )

        // User answers q1 correctly (index 1), q2 incorrectly (index 0)
        val userAnswers = mapOf("q1" to 1, "q2" to 0)

        val eval = testRepo.submitTest(
            testId = "test_1",
            testTitle = "Sample Test",
            date = "2026-10-04",
            startedAt = System.currentTimeMillis() - 60000,
            timeSpentSeconds = 60,
            questions = listOf(q1, q2),
            userAnswers = userAnswers
        )

        assertEquals(1, eval.score)
        assertEquals(50f, eval.accuracy)

        // Verify mistake was logged for q2
        val mistakes = testRepo.getAllMistakes().first()
        assertEquals(1, mistakes.size)
        val mistake = mistakes.first()
        assertEquals("q2", mistake.questionId)
        assertEquals("Worsen", mistake.userWrongAnswer)
        assertEquals("Ameliorate", mistake.correctAnswer)
        assertFalse(mistake.isImproved)

        // Mark as improved
        testRepo.markMistakeImproved(mistake.id, true)
        val unimproved = testRepo.getUnimprovedMistakes().first()
        assertEquals(0, unimproved.size)
    }

    // 6. Editorial Reading Position & Bookmark
    @Test
    fun testEditorialReadingPositionAndBookmark() = runBlocking {
        val editorial = EditorialEntity(
            id = "ed_1",
            date = "2026-10-04",
            title = "Sample Editorial",
            source = "Editorial Daily",
            contentMarkdown = "# Sample Editorial\n\nParagraph 1.\n\nParagraph 2."
        )
        editorialRepo.saveEditorial(editorial)

        editorialRepo.updateReadingPosition("ed_1", 340)
        editorialRepo.updateBookmark("ed_1", 2, "Important paragraph on inflation")

        val fetched = editorialRepo.getEditorialByIdSync("ed_1")
        assertNotNull(fetched)
        assertEquals(340, fetched?.readingPosition)
        assertEquals(2, fetched?.bookmarkParagraph)
        assertEquals("Important paragraph on inflation", fetched?.personalNote)
    }

    // 7. Export & Backup Restoration
    @Test
    fun testExportAndRestoreBackup() = runBlocking {
        vocabRepo.saveWord(
            VocabularyEntity(
                id = "watershed",
                word = "Watershed",
                meaning = "turning point",
                synonyms = listOf("milestone")
            )
        )
        grammarRepo.saveRule(
            com.example.data.local.entity.GrammarRuleEntity(
                id = "rule_1",
                date = "2026-10-04",
                title = "Inversion",
                rule = "Rarely does X occur.",
                explanation = "Inverted subject-verb word order."
            )
        )

        val exportedJson = importExportRepo.exportAllDataJson()
        println("EXPORTED JSON: $exportedJson")
        assertTrue("Exported JSON should contain watershed: $exportedJson", exportedJson.contains("watershed", ignoreCase = true))
        assertTrue("Exported JSON should contain Inversion: $exportedJson", exportedJson.contains("Inversion", ignoreCase = true))

        // Restore into database
        val restoredCount = importExportRepo.restoreBackupJson(exportedJson)
        println("RESTORED COUNT: $restoredCount")
        assertTrue("Restored count should be >= 2, but was $restoredCount", restoredCount >= 2)
    }
}
