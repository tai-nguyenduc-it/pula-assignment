package com.example.pulaassignment.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pulaassignment.createsurvey.CreateSurveyScreen
import com.example.pulaassignment.createsurvey.CreateSurveyViewModel
import com.example.pulaassignment.home.HomeScreen
import com.example.pulaassignment.home.HomeViewModel
import com.example.pulaassignment.surveydetail.SurveyDetailScreen
import com.example.pulaassignment.surveylist.SurveyListScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSurveyList = { navController.navigate(Routes.SURVEY_LIST) },
                onNavigateToSyncDashboard = { }
            )
        }
        composable(Routes.SURVEY_LIST) {
            SurveyListScreen(
                onNavigateBack = navController::navigateUp,
                onNavigateToCreate = { navController.navigate(Routes.CREATE_SURVEY) },
                onNavigateToDetail = { id -> navController.navigate(Routes.surveyDetail(id)) }
            )
        }
        composable(Routes.CREATE_SURVEY) {
            val viewModel: CreateSurveyViewModel = hiltViewModel()
            CreateSurveyScreen(
                viewModel = viewModel,
                onNavigateBack = navController::navigateUp,
            )
        }
        composable(
            route = Routes.SURVEY_DETAIL,
            arguments = listOf(navArgument("surveyId") { defaultValue = "" })
        ) {
            SurveyDetailScreen(
                onNavigateBack = navController::navigateUp,
            )
        }
    }
}
