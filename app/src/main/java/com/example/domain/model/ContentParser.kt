package com.example.domain.model

import org.json.JSONArray
import org.json.JSONObject

data class VocabularyImportItem(
    val word: String,
    val partOfSpeech: String = "word",
    val meaning: String,
    val hindiMeaning: String = "",
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val example: String = "",
    val wordFamily: String = "",
    val difficulty: String = "medium",
    val source: String = "",
    val category: String = ""
)

data class GrammarImportItem(
    val title: String,
    val rule: String,
    val explanation: String = "",
    val correctExamples: List<String> = emptyList(),
    val incorrectExamples: List<String> = emptyList(),
    val commonMistakes: String = "",
    val editorialExample: String = "",
    val difficulty: String = "medium",
    val tags: List<String> = emptyList()
)

data class PhraseImportItem(
    val phrase: String,
    val type: String = "idiom",
    val meaning: String,
    val hindiMeaning: String = "",
    val example: String = "",
    val category: String = "General",
    val difficulty: String = "medium",
    val source: String = ""
)

data class QuestionImportItem(
    val id: String = "",
    val type: String = "mcq",
    val question: String,
    val options: List<String> = emptyList(),
    val answerIndex: Int = 0,
    val answerText: String = "",
    val explanation: String = "",
    val topic: String = "vocabulary",
    val difficulty: String = "medium",
    val relatedConcept: String = ""
)

data class EditorialImportItem(
    val title: String,
    val source: String = "Daily Editorial",
    val contentMarkdown: String,
    val readTimeMinutes: Int = 5
)

data class TestImportItem(
    val testId: String = "",
    val title: String = "Daily English Test",
    val durationMinutes: Int = 15,
    val questions: List<QuestionImportItem> = emptyList()
)

data class DailyPackageImport(
    val date: String,
    val editorial: EditorialImportItem? = null,
    val vocabulary: List<VocabularyImportItem> = emptyList(),
    val grammar: List<GrammarImportItem> = emptyList(),
    val phrases: List<PhraseImportItem> = emptyList(),
    val questions: List<QuestionImportItem> = emptyList(),
    val dailyTest: TestImportItem? = null
)

enum class ImportMode {
    IMPORT_NEW,
    MERGE_DUPLICATES,
    SKIP_DUPLICATES,
    REPLACE_EXISTING
}

data class ValidationResult<T>(
    val isValid: Boolean,
    val items: List<T>,
    val date: String,
    val totalCount: Int,
    val errorMessage: String? = null
)

object ContentParser {

    fun parseVocabularyJson(jsonStr: String): ValidationResult<VocabularyImportItem> {
        val trimmed = jsonStr.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(false, emptyList(), DateUtils.getTodayDate(), 0, "Input is empty")
        }

        try {
            var date = DateUtils.getTodayDate()
            val list = mutableListOf<VocabularyImportItem>()

            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    parseVocabItem(obj, "")?.let { list.add(it) }
                }
            } else if (trimmed.startsWith("{")) {
                val root = JSONObject(trimmed)
                date = root.optString("date", date)
                val defaultCategory = root.optString("category", "")
                val wordsArray = root.optJSONArray("words") 
                    ?: root.optJSONArray("vocabulary")
                    ?: JSONArray()
                
                for (i in 0 until wordsArray.length()) {
                    val obj = wordsArray.optJSONObject(i) ?: continue
                    parseVocabItem(obj, defaultCategory)?.let { list.add(it) }
                }
            } else {
                return ValidationResult(false, emptyList(), date, 0, "Invalid JSON: Must start with '{' or '['")
            }

            if (list.isEmpty()) {
                return ValidationResult(false, emptyList(), date, 0, "No valid vocabulary items found in JSON")
            }

            return ValidationResult(true, list, date, list.size)
        } catch (e: Exception) {
            return ValidationResult(false, emptyList(), DateUtils.getTodayDate(), 0, "JSON parse error: ${e.message}")
        }
    }

    private fun parseVocabItem(obj: JSONObject, defaultCat: String): VocabularyImportItem? {
        val word = obj.optString("word", "").trim()
        if (word.isEmpty()) return null
        val meaning = obj.optString("meaning", "").trim()
        if (meaning.isEmpty()) return null

        val synonyms = toStringList(obj.opt("synonyms"))
        val antonyms = toStringList(obj.opt("antonyms"))

        return VocabularyImportItem(
            word = word,
            partOfSpeech = obj.optString("partOfSpeech", obj.optString("pos", "word")),
            meaning = meaning,
            hindiMeaning = obj.optString("hindiMeaning", obj.optString("hindi", "")),
            synonyms = synonyms,
            antonyms = antonyms,
            example = obj.optString("example", obj.optString("exampleSentence", "")),
            wordFamily = obj.optString("wordFamily", ""),
            difficulty = obj.optString("difficulty", "medium"),
            source = obj.optString("source", ""),
            category = obj.optString("category", defaultCat)
        )
    }

    fun parseGrammarJson(jsonStr: String): ValidationResult<GrammarImportItem> {
        val trimmed = jsonStr.trim()
        if (trimmed.isEmpty()) return ValidationResult(false, emptyList(), DateUtils.getTodayDate(), 0, "Input is empty")

        try {
            var date = DateUtils.getTodayDate()
            val list = mutableListOf<GrammarImportItem>()

            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    parseGrammarItem(obj)?.let { list.add(it) }
                }
            } else if (trimmed.startsWith("{")) {
                val root = JSONObject(trimmed)
                date = root.optString("date", date)
                val rulesArray = root.optJSONArray("rules") 
                    ?: root.optJSONArray("grammar") 
                    ?: JSONArray()
                for (i in 0 until rulesArray.length()) {
                    val obj = rulesArray.optJSONObject(i) ?: continue
                    parseGrammarItem(obj)?.let { list.add(it) }
                }
            }

            if (list.isEmpty()) {
                return ValidationResult(false, emptyList(), date, 0, "No grammar rules found")
            }
            return ValidationResult(true, list, date, list.size)
        } catch (e: Exception) {
            return ValidationResult(false, emptyList(), DateUtils.getTodayDate(), 0, "Grammar parse error: ${e.message}")
        }
    }

    private fun parseGrammarItem(obj: JSONObject): GrammarImportItem? {
        val title = obj.optString("title", "").trim()
        val rule = obj.optString("rule", "").trim()
        if (title.isEmpty() || rule.isEmpty()) return null

        val correctList = mutableListOf<String>()
        val incorrectList = mutableListOf<String>()

        val examples = obj.opt("examples")
        if (examples is JSONArray) {
            for (i in 0 until examples.length()) {
                val exObj = examples.opt(i)
                if (exObj is JSONObject) {
                    val correct = exObj.optString("correct", "")
                    val incorrect = exObj.optString("incorrect", "")
                    if (correct.isNotBlank()) correctList.add(correct)
                    if (incorrect.isNotBlank()) incorrectList.add(incorrect)
                } else if (exObj is String) {
                    correctList.add(exObj)
                }
            }
        }

        val commonErrors = obj.opt("commonErrors")
        val commonErrorsStr = if (commonErrors is JSONArray) {
            toStringList(commonErrors).joinToString("; ")
        } else {
            obj.optString("commonErrors", obj.optString("commonMistakes", ""))
        }

        return GrammarImportItem(
            title = title,
            rule = rule,
            explanation = obj.optString("explanation", ""),
            correctExamples = if (correctList.isNotEmpty()) correctList else toStringList(obj.opt("correctExamples")),
            incorrectExamples = if (incorrectList.isNotEmpty()) incorrectList else toStringList(obj.opt("incorrectExamples")),
            commonMistakes = commonErrorsStr,
            editorialExample = obj.optString("editorialExample", ""),
            difficulty = obj.optString("difficulty", "medium"),
            tags = toStringList(obj.opt("tags"))
        )
    }

    fun parsePhrasesJson(jsonStr: String): ValidationResult<PhraseImportItem> {
        val trimmed = jsonStr.trim()
        if (trimmed.isEmpty()) return ValidationResult(false, emptyList(), DateUtils.getTodayDate(), 0, "Input is empty")

        try {
            var date = DateUtils.getTodayDate()
            val list = mutableListOf<PhraseImportItem>()

            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    parsePhraseItem(obj)?.let { list.add(it) }
                }
            } else if (trimmed.startsWith("{")) {
                val root = JSONObject(trimmed)
                date = root.optString("date", date)
                val phrasesArray = root.optJSONArray("phrases") 
                    ?: root.optJSONArray("expressions") 
                    ?: JSONArray()
                for (i in 0 until phrasesArray.length()) {
                    val obj = phrasesArray.optJSONObject(i) ?: continue
                    parsePhraseItem(obj)?.let { list.add(it) }
                }
            }

            if (list.isEmpty()) {
                return ValidationResult(false, emptyList(), date, 0, "No phrases found")
            }
            return ValidationResult(true, list, date, list.size)
        } catch (e: Exception) {
            return ValidationResult(false, emptyList(), DateUtils.getTodayDate(), 0, "Phrases parse error: ${e.message}")
        }
    }

    private fun parsePhraseItem(obj: JSONObject): PhraseImportItem? {
        val phrase = obj.optString("phrase", "").trim()
        val meaning = obj.optString("meaning", "").trim()
        if (phrase.isEmpty() || meaning.isEmpty()) return null

        return PhraseImportItem(
            phrase = phrase,
            type = obj.optString("type", "idiom"),
            meaning = meaning,
            hindiMeaning = obj.optString("hindiMeaning", ""),
            example = obj.optString("example", ""),
            category = obj.optString("category", "General"),
            difficulty = obj.optString("difficulty", "medium"),
            source = obj.optString("source", "")
        )
    }

    fun parseQuestionsJson(jsonStr: String): ValidationResult<QuestionImportItem> {
        val trimmed = jsonStr.trim()
        if (trimmed.isEmpty()) return ValidationResult(false, emptyList(), DateUtils.getTodayDate(), 0, "Input is empty")

        try {
            var date = DateUtils.getTodayDate()
            val list = mutableListOf<QuestionImportItem>()

            if (trimmed.startsWith("[")) {
                val array = JSONArray(trimmed)
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    parseQuestionItem(obj, i)?.let { list.add(it) }
                }
            } else if (trimmed.startsWith("{")) {
                val root = JSONObject(trimmed)
                date = root.optString("date", date)
                val qArray = root.optJSONArray("questions") ?: JSONArray()
                for (i in 0 until qArray.length()) {
                    val obj = qArray.optJSONObject(i) ?: continue
                    parseQuestionItem(obj, i)?.let { list.add(it) }
                }
            }

            if (list.isEmpty()) {
                return ValidationResult(false, emptyList(), date, 0, "No practice questions found")
            }
            return ValidationResult(true, list, date, list.size)
        } catch (e: Exception) {
            return ValidationResult(false, emptyList(), DateUtils.getTodayDate(), 0, "Questions parse error: ${e.message}")
        }
    }

    private fun parseQuestionItem(obj: JSONObject, index: Int): QuestionImportItem? {
        val question = obj.optString("question", "").trim()
        if (question.isEmpty()) return null

        val options = toStringList(obj.opt("options"))
        var answerIndex = obj.optInt("answer", obj.optInt("correctAnswer", 0))
        val answerText = obj.optString("answerText", "")

        // Support 1-based indexing if >= 1 and <= options.size
        if (options.isNotEmpty() && answerIndex >= 1 && answerIndex <= options.size && !obj.has("answerIndexZeroBased")) {
            // Check if user provided 0-based or 1-based; if answer == options.size it must be 1-based
            if (answerIndex == options.size) {
                answerIndex -= 1
            }
        }
        if (answerIndex < 0 || (options.isNotEmpty() && answerIndex >= options.size)) {
            answerIndex = 0
        }

        return QuestionImportItem(
            id = obj.optString("id", "q_${System.currentTimeMillis()}_$index"),
            type = obj.optString("type", "mcq"),
            question = question,
            options = options,
            answerIndex = answerIndex,
            answerText = answerText,
            explanation = obj.optString("explanation", ""),
            topic = obj.optString("topic", "vocabulary"),
            difficulty = obj.optString("difficulty", "medium"),
            relatedConcept = obj.optString("relatedConcept", obj.optString("relatedWord", ""))
        )
    }

    fun parseDailyPackageJson(jsonStr: String): ValidationResult<DailyPackageImport> {
        val trimmed = jsonStr.trim()
        if (trimmed.isEmpty()) return ValidationResult(false, emptyList(), DateUtils.getTodayDate(), 0, "Empty package")

        try {
            val root = JSONObject(trimmed)
            val date = root.optString("date", DateUtils.getTodayDate())

            // Editorial
            val edObj = root.optJSONObject("editorial")
            val editorial = if (edObj != null) {
                val title = edObj.optString("title", "Editorial")
                val content = edObj.optString("contentMarkdown", edObj.optString("content", ""))
                val source = edObj.optString("source", "Daily Editorial")
                val time = edObj.optInt("readTimeMinutes", 5)
                EditorialImportItem(title, source, content, time)
            } else null

            // Vocabulary
            val vocabList = mutableListOf<VocabularyImportItem>()
            val vocabArray = root.optJSONArray("vocabulary") ?: JSONArray()
            for (i in 0 until vocabArray.length()) {
                val obj = vocabArray.optJSONObject(i) ?: continue
                parseVocabItem(obj, "")?.let { vocabList.add(it) }
            }

            // Grammar
            val grammarList = mutableListOf<GrammarImportItem>()
            val grammarArray = root.optJSONArray("grammar") ?: JSONArray()
            for (i in 0 until grammarArray.length()) {
                val obj = grammarArray.optJSONObject(i) ?: continue
                parseGrammarItem(obj)?.let { grammarList.add(it) }
            }

            // Phrases
            val phrasesList = mutableListOf<PhraseImportItem>()
            val phrasesArray = root.optJSONArray("phrases") ?: JSONArray()
            for (i in 0 until phrasesArray.length()) {
                val obj = phrasesArray.optJSONObject(i) ?: continue
                parsePhraseItem(obj)?.let { phrasesList.add(it) }
            }

            // Questions
            val questionsList = mutableListOf<QuestionImportItem>()
            val questionsArray = root.optJSONArray("questions") ?: JSONArray()
            for (i in 0 until questionsArray.length()) {
                val obj = questionsArray.optJSONObject(i) ?: continue
                parseQuestionItem(obj, i)?.let { questionsList.add(it) }
            }

            // Daily Test
            val testObj = root.optJSONObject("dailyTest") ?: root.optJSONObject("test")
            val dailyTest = if (testObj != null) {
                val testId = testObj.optString("testId", "test_$date")
                val title = testObj.optString("title", "Daily English Test")
                val duration = testObj.optInt("durationMinutes", 15)
                val testQuestions = mutableListOf<QuestionImportItem>()
                val tqArray = testObj.optJSONArray("questions") ?: JSONArray()
                for (i in 0 until tqArray.length()) {
                    val obj = tqArray.optJSONObject(i) ?: continue
                    parseQuestionItem(obj, i)?.let { testQuestions.add(it) }
                }
                TestImportItem(testId, title, duration, testQuestions)
            } else null

            val packageImport = DailyPackageImport(
                date = date,
                editorial = editorial,
                vocabulary = vocabList,
                grammar = grammarList,
                phrases = phrasesList,
                questions = questionsList,
                dailyTest = dailyTest
            )

            return ValidationResult(true, listOf(packageImport), date, 1)
        } catch (e: Exception) {
            return ValidationResult(false, emptyList(), DateUtils.getTodayDate(), 0, "Package parse error: ${e.message}")
        }
    }

    private fun toStringList(obj: Any?): List<String> {
        if (obj == null) return emptyList()
        if (obj is JSONArray) {
            val list = mutableListOf<String>()
            for (i in 0 until obj.length()) {
                list.add(obj.optString(i, ""))
            }
            return list.filter { it.isNotBlank() }
        }
        if (obj is String) {
            return obj.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
        return emptyList()
    }
}
