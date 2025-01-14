package com.example.finishedgoodmanagement


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.finishedgoodmanagement.navigation.SetupNavGraph
import com.example.finishedgoodmanagement.ui.theme.FinishedGoodManagementTheme
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : ComponentActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinishedGoodManagementTheme{
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Initialize NavController
                    val navController = rememberNavController()

                    // Get Firestore instance
                    val firestore = FirebaseFirestore.getInstance()

                    // Setup the navigation graph
                    SetupNavGraph(
                        navController = navController,
                        firestore = firestore
                    )
                }
            }
        }
    }
}
