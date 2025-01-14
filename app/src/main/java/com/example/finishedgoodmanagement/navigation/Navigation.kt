package com.example.finishedgoodmanagement.navigation



import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.finishedgoodmanagement.FinishedGoodsScreen.Bottles
import com.example.finishedgoodmanagement.FinishedGoodsScreen.InstitutionalScreen
import com.example.finishedgoodmanagement.FinishedGoodsScreen.PastaFinished
import com.example.finishedgoodmanagement.FinishedGoodsScreen.PortionPackScreen
import com.example.finishedgoodmanagement.FinishedGoodsScreen.PouchesFinished
import com.example.finishedgoodmanagement.FinishedGoodsScreen.RetailScreen
import com.example.finishedgoodmanagement.FinishedGoodsScreen.SeasoningsScreen
import com.example.finishedgoodmanagement.FinishedViewModel
import com.example.finishedgoodmanagement.screens.FinishedGoodScreen
import com.example.finishedgoodmanagement.screens.LoginScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun SetupNavGraph(
    navController: NavHostController,
    firestore: FirebaseFirestore,
) {
    val activityContext = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val sharedPreferences = activityContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
    val startDestination = when {
        isLoggedIn  -> Screen.FinishedGood.route
        else -> Screen.LoginScreen.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.fillMaxSize()
    ){
        composable(route = Screen.FinishedGood.route) {
            val viewModel: FinishedViewModel = viewModel()
            FinishedGoodScreen(navController =navController , viewModel = viewModel,activityContext = activityContext)
        }
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(
                navController = navController,
                onLoginSuccess = {
                    navController.navigate(Screen.FinishedGood.route) {
                        // Remove LoginScreen from the back stack
                        popUpTo(Screen.LoginScreen.route) { inclusive = true }
                    }
                },
                activityContext = LocalContext.current // Pass the activity context here
            )
        }

        composable(route = Screen.Bottles.route) {
            val viewModel1: FinishedViewModel = viewModel()
            Bottles(navController = navController, viewModel = viewModel1)
        }
        composable(route = Screen.Pasta.route) {
            val viewModel1: FinishedViewModel = viewModel()
            PastaFinished(navController = navController, viewModel = viewModel1)
        }
        composable(route = Screen.Pouches.route) {
            val viewModel1: FinishedViewModel = viewModel()
            PouchesFinished(navController = navController, viewModel = viewModel1)
        }
        composable(route = Screen.BulkPack.route) {
            val viewModel1: FinishedViewModel = viewModel()
            InstitutionalScreen(navController = navController, viewModel = viewModel1)
        }
        composable(route = Screen.RetailPack.route) {
            val viewModel1: FinishedViewModel = viewModel()
            RetailScreen(navController = navController, viewModel = viewModel1)
        }
        composable(route = Screen.PortionPack.route) {
            val viewModel1: FinishedViewModel = viewModel()
            PortionPackScreen(navController = navController, viewModel = viewModel1)
        }
        composable(route = Screen.Seasonings.route) {
            val viewModel1: FinishedViewModel = viewModel()
            SeasoningsScreen(navController = navController, viewModel = viewModel1)
        }
    }
}
