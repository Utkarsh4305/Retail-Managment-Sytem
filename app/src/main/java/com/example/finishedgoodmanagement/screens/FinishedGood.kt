package com.example.finishedgoodmanagement.screens



import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.finishedgoodmanagement.FinishedViewModel
import com.example.finishedgoodmanagement.navigation.Screen
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FinishedGoodScreen(
    navController: NavHostController,
    viewModel: FinishedViewModel,
    activityContext: Context
) {
    // Adjust button height and spacing as needed
    var showDialog by remember { mutableStateOf(false) }
    val context = viewModel.getApplicationContext().applicationContext
    val buttonHeight = 60.dp
    val buttonSpacing = 8.dp  // Increased spacing between buttons
    var hasNavigated by remember { mutableStateOf(false) }
    // Retrieve the stored user name from SharedPreferences
    val sharedPreferences = activityContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    val userName = sharedPreferences.getString("userName", "Unknown User") ?: "Unknown User"
    val isDataLoaded by viewModel.isDataLoaded.observeAsState(false)
    var selectedDistributor by remember { mutableStateOf("Select Distributor") }
    val currentDate = remember {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formatter.format(Date())
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // Space between rows
        ) {
            // Row for Finished Goods text and Confirm FG button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title Text
                Text(
                    text = "Finished Goods",
                    color = Color.White,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(start = 16.dp)
                )

                // Confirm FG Button
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F3F)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .defaultMinSize(minWidth = 120.dp)
                        .height(48.dp)
                        .padding(end = 16.dp)
                ) {
                    Text("Confirm FG", fontSize = 19.sp)
                }
            }

            // Distributor Dropdown Button
            DistributorDropdownButton(
                firestore = FirebaseFirestore.getInstance(), // Pass the Firestore instance
                buttonHeight = 50.dp,
                activityContext = activityContext
            )
        }



        Spacer(modifier = Modifier.height(15.dp))
        Button(
            onClick = { navController.navigate(Screen.RetailPack.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .padding(vertical = buttonSpacing)
        ) {
            Text("36g - Retail Pack", color = Color.Black, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate(Screen.Seasonings.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .padding(vertical = buttonSpacing)
        ) {
            Text("Seasoning Pouches", color = Color.Black, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate(Screen.Pouches.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .padding(vertical = buttonSpacing)
        ) {
            Text("80g Pouches", color = Color.Black, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate(Screen.Bottles.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .padding(vertical = buttonSpacing)
        ) {
            Text("120g - Glass Bottle", color = Color.Black, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate(Screen.Pasta.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .padding(vertical = buttonSpacing)
        ) {
            Text("Pasta Kits", color = Color.Black, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate(Screen.PortionPack.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .padding(vertical = buttonSpacing)
        ) {
            Text("28g - Portion Pack", color = Color.Black, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate(Screen.BulkPack.route) },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
                .padding(vertical = buttonSpacing)
        ) {
            Text("1 Kg Institutional Pack", color = Color.Black, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(24.dp))

        Spacer(modifier = Modifier.weight(1f))

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = "Confirm FG Update") },
                text = { Text("Are you sure you want to confirm and update the data?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDialog = false

                            // Launch coroutine directly within onClick to avoid issues
                            viewModel.viewModelScope.launch {
                                try {
                                    // Update data in Firestore
                                    viewModel.updateTotalData(
                                        onSuccess = {
                                            // Fetch data from Firestore
                                            viewModel.exportProductsData(
                                                onSuccess = { productsData ->
                                                    // Use a coroutine to call generateCsvFileFG and share the file
                                                    viewModel.viewModelScope.launch {
                                                        val csvFile = generateCsvFileFG(
                                                            context,
                                                            productsData,
                                                            selectedDistributor,
                                                            userName,
                                                            viewModel
                                                        )
                                                        if (csvFile != null) {
                                                            // Share the CSV file via WhatsApp
                                                            shareCsvFileViaWhatsApp(activityContext, csvFile)
                                                            Log.d("FinishedGoodScreen", "CSV file generated and shared successfully via WhatsApp")
                                                        } else {
                                                            Log.e("FinishedGoodScreen", "Failed to generate CSV file")
                                                        }
                                                    }
                                                },
                                                onFailure = { exception ->
                                                    Log.e("FinishedGoodScreen", "Error fetching products data", exception)
                                                }
                                            )
                                        },
                                        onFailure = { error ->
                                            Log.e("FinishedGoodScreen", "Failed to update Total data: ${error.message}", error)
                                        }
                                    )
                                } catch (e: Exception) {
                                    Log.e("FinishedGoodScreen", "Error occurred", e)
                                }
                            }

                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
@Composable
fun DistributorDropdownButton(
    firestore: FirebaseFirestore,
    buttonHeight: Dp = 50.dp,
    activityContext: Context // Pass Context to access SharedPreferences
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    var selectedDistributor by remember { mutableStateOf("Select Distributor") }
    var distributors by remember { mutableStateOf<List<String>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Fetch distributor data from Firestore
    LaunchedEffect(Unit) {
        firestore.collection("Distributor")
            .get()
            .addOnSuccessListener { result ->
                distributors = result.documents.map { document ->
                    document.id  // Fetch the document name
                }
            }
            .addOnFailureListener { e ->
                errorMessage = "Error fetching distributors: ${e.message}"
            }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { isDropdownExpanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F3F)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight)
        ) {
            Text(selectedDistributor, fontSize = 18.sp, color = Color.White)
        }

        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            distributors.forEach { distributor ->
                DropdownMenuItem(
                    text = { Text(distributor, fontSize = 18.sp) },
                    onClick = {
                        selectedDistributor = distributor
                        isDropdownExpanded = false

                        // Save the selected distributor in SharedPreferences
                        val sharedPreferences =
                            activityContext.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                        sharedPreferences.edit()
                            .putString("selectedDistributor", distributor)
                            .apply()
                    }
                )
            }
        }
    }
}

fun shareCsvFileViaWhatsApp(activity: Context, csvFile: File) {
    val uri: Uri = FileProvider.getUriForFile(
        activity,
        "${activity.packageName}.provider",
        csvFile
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "Please find the attached products data CSV file.")
        setPackage("com.whatsapp")  // Set the package to WhatsApp
    }

    try {
        activity.startActivity(Intent.createChooser(intent, "Share CSV file via WhatsApp"))
        Log.d("shareCsvFileViaWhatsApp", "Chooser launched with WhatsApp option")
    } catch (e: ActivityNotFoundException) {
        Log.e("shareCsvFileViaWhatsApp", "WhatsApp not installed", e)
        Toast.makeText(activity, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e("shareCsvFileViaWhatsApp", "Error sharing CSV file", e)
        Toast.makeText(activity, "Error sharing CSV file", Toast.LENGTH_SHORT).show()
    }
}

private suspend fun generateCsvFileFG(
    context: Context,
    data: Map<String, Map<String, Any>>,
    distributorName: String,
    userName: String,
    viewModel: FinishedViewModel
): File? {
    Log.d("generateCsvFileFG", "Starting CSV generation for distributor: $distributorName")
    Log.d("generateCsvFileFG", "Input data size: ${data.size} categories")

    return try {
        val dateFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
        val currentDate = Date()
        val previousDate = Date(currentDate.time - 24 * 60 * 60 * 1000)
        val previouslyDate = Date(currentDate.time - 2 * 24 * 60 * 60 * 1000)

        val formattedCurrentDate = dateFormatter.format(currentDate)
        val formattedPreviousDate = dateFormatter.format(previousDate)
        val formattedPreviouslyDate = dateFormatter.format(previouslyDate)

        Log.d("generateCsvFileFG", "Date ranges: Current=$formattedCurrentDate, Previous=$formattedPreviousDate, Previously=$formattedPreviouslyDate")

        val csvFileName = "FG(${formattedCurrentDate.replace("/", "-")}).csv"
        Log.d("generateCsvFileFG", "Creating CSV file: $csvFileName")

        val directory = context.getExternalFilesDir(null)
        if (directory == null || (!directory.exists() && !directory.mkdirs())) {
            Log.e("generateCsvFileFG", "Failed to access or create directory at path: ${directory?.absolutePath}")
            return null
        }
        val csvFile = File(directory, csvFileName)
        Log.d("generateCsvFileFG", "CSV file path: ${csvFile.absolutePath}")

        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val documentName = sharedPreferences.getString("selectedDistributor", null)
        if (documentName.isNullOrEmpty()) {
            Log.e("generateCsvFileFG", "No distributor selected in SharedPreferences")
            throw IllegalArgumentException("No distributor selected in SharedPreferences.")
        }
        Log.d("generateCsvFileFG", "Found distributor document name: $documentName")

        val firestore = FirebaseFirestore.getInstance()
        val subDocumentsCollection = firestore.collection("DistributorData")
            .document(documentName)
            .collection("subDocuments")

        val allData = mutableMapOf<String, Any?>()
        val documents = subDocumentsCollection.get().await().documents
        Log.d("generateCsvFileFG", "Retrieved ${documents.size} documents from Firestore")

        for (document in documents) {
            Log.d("generateCsvFileFG", "Processing document: ${document.id} with ${document.data?.size ?: 0} fields")
            document.data?.let { data ->
                allData.putAll(data)
            }
        }

        if (allData.isEmpty()) {
            Log.e("generateCsvFileFG", "No data found for distributor: $documentName")
            return null
        }
        Log.d("generateCsvFileFG", "Total data entries retrieved: ${allData.size}")

        val sortOrder = mutableMapOf<String, List<String>>()
        data.forEach { (category, _) ->
            try {
                val orderDocument = firestore.collection("Order").document(category).get().await()
                val sortOrderFromFirestore = orderDocument["sortorder"] as? List<String>
                if (sortOrderFromFirestore != null) {
                    sortOrder[category] = sortOrderFromFirestore
                    Log.d("generateCsvFileFG", "Retrieved sort order for $category: ${sortOrderFromFirestore.size} items")
                } else {
                    Log.w("generateCsvFileFG", "Sort order missing for category: $category")
                }
            } catch (e: Exception) {
                Log.e("generateCsvFileFG", "Error fetching sort order for $category: ${e.message}", e)
            }
        }

        Log.d("generateCsvFileFG", "Generating CSV content")
        val rows = mutableListOf<String>()
        rows.add("Distributor: $documentName")

        documents.forEach { document ->
            rows.add(document.id)

            document.data?.let { documentData ->
                // Get all case fields
                val caseFields = documentData.filterKeys {
                    it.endsWith("Cases", ignoreCase = true)
                }

                // Group fields by category
                val categorizedFields = caseFields.entries.groupBy { entry ->
                    entry.key.split("_")[1]
                }

                // Process each category in the existing data
                categorizedFields.forEach { (category, fields) ->
                    rows.add("")  // Add blank line before category

                    // Calculate category total
                    val categoryTotal = fields.sumOf { field ->
                        when (val fieldValue = field.value) {
                            is Number -> fieldValue.toDouble()
                            is String -> fieldValue.toDoubleOrNull() ?: 0.0
                            else -> 0.0
                        }
                    }

                    rows.add("$category, $categoryTotal")

                    // Get the sort order for this category (if it exists)
                    val sortOrderForCategory = sortOrder[category] ?: listOf()

                    // Create a map of fields by item name
                    val fieldsByItem = fields.associate { field ->
                        val itemName = field.key.split("_").getOrNull(2) ?: ""
                        itemName to field
                    }

                    // First write items that are in the sort order (in order)
                    sortOrderForCategory.forEach { itemName ->
                        fieldsByItem[itemName]?.let { field ->
                            val value = when (val fieldValue = field.value) {
                                is Number -> fieldValue.toDouble()
                                is String -> fieldValue.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            rows.add("${field.key}, $value")
                        }
                    }

                    // Then write any remaining items that weren't in the sort order
                    fieldsByItem.forEach { (itemName, field) ->
                        if (!sortOrderForCategory.contains(itemName)) {
                            val value = when (val fieldValue = field.value) {
                                is Number -> fieldValue.toDouble()
                                is String -> fieldValue.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                        }
                    }
                }
            }
        }

        Log.d("generateCsvFileFG", "Writing ${rows.size} rows to CSV file")
        if (rows.isEmpty() || rows.all { it.isBlank() }) {
            Log.e("generateCsvFileFG", "No rows to write to CSV file")
            return null
        }

        FileWriter(csvFile).use { writer ->
            rows.forEach { row ->
                writer.append(row).append("\n")
                Log.d("generateCsvFileFG", "CSV row: $row")
            }
        }

        Log.d("generateCsvFileFG", "Successfully generated CSV file at: ${csvFile.absolutePath}")
        csvFile

    } catch (e: Exception) {
        Log.e("generateCsvFileFG", "Error generating CSV file", e)
        Log.e("generateCsvFileFG", "Stack trace: ${e.stackTrace.joinToString("\n")}")
        null
    }
}














