package com.example.presentation.navigation

sealed class Screen(val route: String) {
    // Bottom Nav Tabs
    object Home : Screen("home")
    object Learn : Screen("learn")
    object Archive : Screen("archive")
    object Dictionary : Screen("dictionary")
    object Tests : Screen("tests")

    // Reader & Details
    object Reader : Screen("reader/{editorialId}") {
        fun createRoute(editorialId: String) = "reader/$editorialId"
    }
    object WordDetail : Screen("word/{wordId}") {
        fun createRoute(wordId: String) = "word/$wordId"
    }
    object Categories : Screen("categories")
    object CategoryDetail : Screen("category/{categoryId}") {
        fun createRoute(categoryId: String) = "category/$categoryId"
    }

    // Learn sub-flows
    object GrammarList : Screen("grammar_list/{date}") {
        fun createRoute(date: String) = "grammar_list/$date"
    }
    object PhrasesList : Screen("phrases_list/{date}") {
        fun createRoute(date: String) = "phrases_list/$date"
    }
    object RevisionSession : Screen("revision_session")

    // Tests & Mistakes
    object TakeTest : Screen("take_test/{testId}") {
        fun createRoute(testId: String) = "take_test/$testId"
    }
    object TestResult : Screen("test_result/{attemptId}") {
        fun createRoute(attemptId: String) = "test_result/$attemptId"
    }
    object MistakesBook : Screen("mistakes_book")

    // Tools & Utility
    object Favorites : Screen("favorites")
    object GlobalSearch : Screen("search")
    object ContentManager : Screen("content_manager")
    object Settings : Screen("settings")
}
