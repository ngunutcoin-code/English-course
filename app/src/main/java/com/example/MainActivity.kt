package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.StimulerBottomNavBar
import com.example.ui.components.StimulerNavDestination
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.EvaluationResultScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.VocabularyVaultScreen
import com.example.ui.theme.StimulerTheme
import com.example.viewmodel.StimulerViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle audio recording permission callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            StimulerTheme {
                val viewModel: StimulerViewModel = viewModel()
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: StimulerNavDestination.HOME.route

                val showBottomBar = currentRoute in listOf(
                    StimulerNavDestination.HOME.route,
                    StimulerNavDestination.PRACTICE.route,
                    StimulerNavDestination.VOCABULARY.route,
                    StimulerNavDestination.ANALYTICS.route
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            StimulerBottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { destination ->
                                    navController.navigate(destination.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = StimulerNavDestination.HOME.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(StimulerNavDestination.HOME.route) {
                            HomeScreen(
                                viewModel = viewModel,
                                onSelectTopic = { topic ->
                                    viewModel.selectTopic(topic)
                                    navController.navigate(StimulerNavDestination.PRACTICE.route)
                                },
                                onViewSessionDetail = { session ->
                                    viewModel.setSelectedSession(session)
                                    navController.navigate("evaluation_detail")
                                },
                                onNavigateToVocab = {
                                    navController.navigate(StimulerNavDestination.VOCABULARY.route)
                                }
                            )
                        }

                        composable(StimulerNavDestination.PRACTICE.route) {
                            PracticeScreen(
                                viewModel = viewModel,
                                onBackClick = {
                                    navController.popBackStack()
                                },
                                onEvaluationComplete = {
                                    navController.navigate("evaluation_detail")
                                }
                            )
                        }

                        composable("evaluation_detail") {
                            EvaluationResultScreen(
                                viewModel = viewModel,
                                onBackClick = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(StimulerNavDestination.VOCABULARY.route) {
                            VocabularyVaultScreen(
                                viewModel = viewModel
                            )
                        }

                        composable(StimulerNavDestination.ANALYTICS.route) {
                            AnalyticsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
