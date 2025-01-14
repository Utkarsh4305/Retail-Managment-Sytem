package com.example.finishedgoodmanagement.screens


import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.finishedgoodmanagement.R
import com.example.finishedgoodmanagement.navigation.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    navController: NavHostController,
    activityContext: Context // Add this parameter to access SharedPreferences
){
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }
    val fricBergenFontFamily = FontFamily(
        Font(R.font.panton_black_caps, FontWeight.Normal)
    )
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val sharedPreferences = activityContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    sharedPreferences.edit().putString("userName", email).apply()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Spacer to push content down
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Fric Bergen",
            color = Color.White,
            fontSize = 42.sp,
            fontFamily = fricBergenFontFamily, // Apply the custom font
            modifier = Modifier
                .padding(top = 64.dp)
        )
        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = "Finished Good Management",
            color = Color.White,
            fontSize = 20.sp,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .align(Alignment.CenterHorizontally) // Center horizontally
        )
        Spacer(modifier = Modifier.height(128.dp))

        // Replace the Box with a simple Column with black background
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterHorizontally)
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                TextField(
                    value = email,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "",
                            tint = Color.Gray
                        )
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = {
                        focusManager.moveFocus(FocusDirection.Down)
                    }),
                    label = { Text(text = "User ID") },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    onValueChange = { email = it }
                )

                Spacer(modifier = Modifier.height(10.dp))

                TextField(
                    value = password,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "",
                            tint = Color.Gray
                        )
                    },
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        validateAndSignIn(email, password,context, navController,activityContext)
                    }),
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text(text = "Password") },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    onValueChange = { password = it }
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = { validateAndSignIn(email, password,context, navController,activityContext) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1F1F1F), // Set the button color
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Log In",
                        color = White,
                        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                    )
                }
            }
        }
    }
}



private fun validateAndSignIn(
    email: String,
    password: String,
    context: Context,
    navController: NavHostController,
    activityContext: Context
) {
    // Validate inputs
    if (email.isBlank()) {
        Toast.makeText(context, "Email cannot be empty", Toast.LENGTH_SHORT).show()
        return
    }
    if (password.isBlank()) {
        Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT).show()
        return
    }

    // Firestore instance and SharedPreferences
    val db = FirebaseFirestore.getInstance()
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    // Check Admin collection
    db.collection("Admin").document(email).get()
        .addOnSuccessListener { adminDoc ->
            if (adminDoc.exists()) {
                val adminPassword = adminDoc.getString("Password") ?: ""
                val adminName = adminDoc.getString("Name") ?: "Admin"
                if (adminPassword == password.trim()) {
                    // Save admin details to SharedPreferences
                    editor.putBoolean("isLoggedIn", true)
                    editor.putBoolean("isAdmin", true)
                    editor.putString("userName", adminName)
                    editor.apply()

                    // Navigate to admin dashboard
                    Toast.makeText(context, "Welcome, $adminName!", Toast.LENGTH_SHORT).show()
                    navController.navigate(Screen.AdminScreen.route) // Replace with actual route
                } else {
                    Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Check Users collection
                db.collection("Users").document(email).get()
                    .addOnSuccessListener { userDoc ->
                        if (userDoc.exists()) {
                            val userPassword = userDoc.getString("Password") ?: ""
                            val userName = userDoc.getString("Name") ?: "User"
                            if (userPassword == password.trim()) {
                                // Save user details to SharedPreferences
                                editor.putBoolean("isLoggedIn", true)
                                editor.putBoolean("isAdmin", false)
                                editor.putString("userName", userName)
                                editor.apply()

                                // Navigate to user home
                                Toast.makeText(context, "Welcome, $userName!", Toast.LENGTH_SHORT).show()
                                navController.navigate(Screen.FinishedGood.route) // Replace with actual route
                            } else {
                                Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                    }
            }
        }
        .addOnFailureListener {
            Toast.makeText(context, "Failed to retrieve admin data", Toast.LENGTH_SHORT).show()
        }
}



@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    //Preview with a mock NavController
}
