package com.example.domain.model

enum class WordLearningStatus(val displayName: String, val level: Int) {
    NEW("New", 0),
    LEARNING("Learning", 1),
    REVIEW("Review Due", 2),
    FAMILIAR("Familiar", 3),
    MASTERED("Mastered", 4);

    companion object {
        fun fromString(status: String?): WordLearningStatus {
            return when (status?.uppercase()) {
                "LEARNING" -> LEARNING
                "REVIEW" -> REVIEW
                "FAMILIAR" -> FAMILIAR
                "MASTERED" -> MASTERED
                else -> NEW
            }
        }
    }
}

data class RepetitionResult(
    val newStatus: WordLearningStatus,
    val nextReviewDate: String,
    val confidence: Int
)

object SpacedRepetitionCalculator {
    fun calculateNextReview(
        currentStatus: WordLearningStatus,
        isCorrect: Boolean,
        todayDate: String = DateUtils.getTodayDate()
    ): RepetitionResult {
        if (!isCorrect) {
            // Drop back to LEARNING and schedule next review tomorrow
            return RepetitionResult(
                newStatus = WordLearningStatus.LEARNING,
                nextReviewDate = DateUtils.addDays(todayDate, 1),
                confidence = 25
            )
        }

        return when (currentStatus) {
            WordLearningStatus.NEW -> RepetitionResult(
                newStatus = WordLearningStatus.LEARNING,
                nextReviewDate = DateUtils.addDays(todayDate, 1),
                confidence = 40
            )
            WordLearningStatus.LEARNING -> RepetitionResult(
                newStatus = WordLearningStatus.REVIEW,
                nextReviewDate = DateUtils.addDays(todayDate, 3),
                confidence = 65
            )
            WordLearningStatus.REVIEW -> RepetitionResult(
                newStatus = WordLearningStatus.FAMILIAR,
                nextReviewDate = DateUtils.addDays(todayDate, 7),
                confidence = 80
            )
            WordLearningStatus.FAMILIAR -> RepetitionResult(
                newStatus = WordLearningStatus.MASTERED,
                nextReviewDate = DateUtils.addDays(todayDate, 14),
                confidence = 95
            )
            WordLearningStatus.MASTERED -> RepetitionResult(
                newStatus = WordLearningStatus.MASTERED,
                nextReviewDate = DateUtils.addDays(todayDate, 30),
                confidence = 100
            )
        }
    }
}
