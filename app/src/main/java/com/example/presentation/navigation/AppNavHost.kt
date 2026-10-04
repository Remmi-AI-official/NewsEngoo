package com.example.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.EditorialApplication
import com.example.domain.model.DateUtils
import com.example.presentation.archive.CalendarArchiveScreen
import com.example.presentation.dictionary.CategoriesScreen
import com.example.presentation.dictionary.DictionaryScreen
import com.example.presentation.dictionary.WordDetailScreen
import com.example.presentation.favorites.FavoritesScreen
import com.example.presentation.home.HomeScreen
import com.example.presentation.learn.DailyLearnFlowScreen
import com.example.presentation.learn.GrammarScreen
import com.example.presentation.learn.PhraseScreen
import com.example.presentation.learn.RevisionScreen
import com.example.presentation.manager.ContentManagerScreen
import com.example.presentation.reader.EditorialReaderScreen
import com.example.presentation.search.GlobalSearchScreen
import com.example.presentation.settings.SettingsScreen
import com.example.presentation.tests.MistakesBookScreen
import com.example.presentation.tests.TakeTestScreen
import com.example.presentation.tests.TestResultScreen
import com.example.presentation.tests.TestsListScreen

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
    BottomNavItem(Screen.Learn.route, "Learn", Icons.Filled.School, Icons.Outlined.School, "nav_learn"),
    BottomNavItem(Screen.Archive.route, "Archive", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_archive"),
    BottomNavItem(Screen.Dictionary.route, "Dictionary", Icons.Filled.Translate, Icons.Outlined.Translate, "nav_dictionary"),
    BottomNavItem(Screen.Tests.route, "Tests", Icons.Filled.Quiz, Icons.Outlined.Quiz, "nav_tests")
)

@Composable
fun AppNavHost(
    app: EditorialApplication,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isBottomBarVisible = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            modifier = Modifier.testTag(item.tag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // HOME
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToReader = { id -> navController.navigate(Screen.Reader.createRoute(id)) },
                    onNavigateToLearnFlow = { date -> navController.navigate("learn_flow/$date") },
                    onNavigateToRevision = { navController.navigate(Screen.RevisionSession.route) },
                    onNavigateToMistakes = { navController.navigate(Screen.MistakesBook.route) },
                    onNavigateToSearch = { navController.navigate(Screen.GlobalSearch.route) },
                    onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToContentManager = { navController.navigate(Screen.ContentManager.route) }
                )
            }

            // LEARN TAB (Opens today's learning flow)
            composable(Screen.Learn.route) {
                DailyLearnFlowScreen(
                    date = DateUtils.getTodayDate(),
                    onNavigateBack = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id)
                        }
                    },
                    onNavigateToReader = { id -> navController.navigate(Screen.Reader.createRoute(id)) },
                    onNavigateToTest = { testId -> navController.navigate(Screen.TakeTest.createRoute(testId)) }
                )
            }

            composable("learn_flow/{date}") { backStackEntry ->
                val date = backStackEntry.arguments?.getString("date") ?: DateUtils.getTodayDate()
                DailyLearnFlowScreen(
                    date = date,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToReader = { id -> navController.navigate(Screen.Reader.createRoute(id)) },
                    onNavigateToTest = { testId -> navController.navigate(Screen.TakeTest.createRoute(testId)) }
                )
            }

            // ARCHIVE
            composable(Screen.Archive.route) {
                CalendarArchiveScreen(
                    onNavigateToReader = { id -> navController.navigate(Screen.Reader.createRoute(id)) },
                    onNavigateToLearnFlow = { date -> navController.navigate("learn_flow/$date") }
                )
            }

            // DICTIONARY
            composable(Screen.Dictionary.route) {
                DictionaryScreen(
                    onNavigateToWordDetail = { wordId -> navController.navigate(Screen.WordDetail.createRoute(wordId)) },
                    onNavigateToCategories = { navController.navigate(Screen.Categories.route) }
                )
            }

            // TESTS
            composable(Screen.Tests.route) {
                TestsListScreen(
                    onNavigateToTakeTest = { testId -> navController.navigate(Screen.TakeTest.createRoute(testId)) },
                    onNavigateToMistakes = { navController.navigate(Screen.MistakesBook.route) },
                    onNavigateToAttemptResult = { attemptId -> navController.navigate(Screen.TestResult.createRoute(attemptId)) }
                )
            }

            // EDITORIAL READER
            composable(
                route = Screen.Reader.route,
                arguments = listOf(navArgument("editorialId") { type = NavType.StringType })
            ) { backStackEntry ->
                val editorialId = backStackEntry.arguments?.getString("editorialId") ?: ""
                EditorialReaderScreen(
                    editorialId = editorialId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToWordDetail = { wordId -> navController.navigate(Screen.WordDetail.createRoute(wordId)) },
                    onPracticeWord = { wordId -> navController.navigate(Screen.WordDetail.createRoute(wordId)) }
                )
            }

            // WORD DETAIL
            composable(
                route = Screen.WordDetail.route,
                arguments = listOf(navArgument("wordId") { type = NavType.StringType })
            ) { backStackEntry ->
                val wordId = backStackEntry.arguments?.getString("wordId") ?: ""
                WordDetailScreen(
                    wordId = wordId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // CATEGORIES
            composable(Screen.Categories.route) {
                CategoriesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onSelectCategory = { navController.popBackStack() }
                )
            }

            // GRAMMAR LIST
            composable(
                route = Screen.GrammarList.route,
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) {
                GrammarScreen(
                    onNavigateBack = { navController.popBackStack() },
                    app = app
                )
            }

            // PHRASES LIST
            composable(
                route = Screen.PhrasesList.route,
                arguments = listOf(navArgument("date") { type = NavType.StringType })
            ) {
                PhraseScreen(
                    onNavigateBack = { navController.popBackStack() },
                    app = app
                )
            }

            // REVISION SESSION
            composable(Screen.RevisionSession.route) {
                RevisionScreen(
                    onNavigateBack = { navController.popBackStack() },
                    app = app
                )
            }

            // TAKE TEST
            composable(
                route = Screen.TakeTest.route,
                arguments = listOf(navArgument("testId") { type = NavType.StringType })
            ) { backStackEntry ->
                val testId = backStackEntry.arguments?.getString("testId") ?: ""
                TakeTestScreen(
                    testId = testId,
                    onNavigateBack = { navController.popBackStack() },
                    onTestFinished = { attemptId ->
                        navController.navigate(Screen.TestResult.createRoute(attemptId)) {
                            popUpTo(Screen.TakeTest.route) { inclusive = true }
                        }
                    }
                )
            }

            // TEST RESULT
            composable(
                route = Screen.TestResult.route,
                arguments = listOf(navArgument("attemptId") { type = NavType.StringType })
            ) { backStackEntry ->
                val attemptId = backStackEntry.arguments?.getString("attemptId") ?: ""
                TestResultScreen(
                    attemptId = attemptId,
                    onNavigateBack = {
                        navController.navigate(Screen.Tests.route) {
                            popUpTo(Screen.Tests.route) { inclusive = true }
                        }
                    },
                    onNavigateToMistakes = { navController.navigate(Screen.MistakesBook.route) }
                )
            }

            // MISTAKES BOOK
            composable(Screen.MistakesBook.route) {
                MistakesBookScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // FAVORITES
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToReader = { id -> navController.navigate(Screen.Reader.createRoute(id)) },
                    onNavigateToWordDetail = { wordId -> navController.navigate(Screen.WordDetail.createRoute(wordId)) }
                )
            }

            // GLOBAL SEARCH
            composable(Screen.GlobalSearch.route) {
                GlobalSearchScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToWordDetail = { wordId -> navController.navigate(Screen.WordDetail.createRoute(wordId)) },
                    onNavigateToReader = { id -> navController.navigate(Screen.Reader.createRoute(id)) }
                )
            }

            // CONTENT MANAGER
            composable(Screen.ContentManager.route) {
                ContentManagerScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // SETTINGS
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToContentManager = { navController.navigate(Screen.ContentManager.route) }
                )
            }
        }
    }
}
