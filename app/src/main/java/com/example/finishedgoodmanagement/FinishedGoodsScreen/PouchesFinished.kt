package com.example.finishedgoodmanagement.FinishedGoodsScreen






import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.finishedgoodmanagement.FinishedViewModel
import com.example.finishedgoodmanagement.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun PouchesFinished(navController: NavHostController, viewModel: FinishedViewModel) {
    val scrollState = rememberScrollState()
    var bottlesData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var updatedData by remember { mutableStateOf<Map<String, Any>>(emptyMap()) }
    val dateFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
    val currentDate = Date()
    val formattedDate = dateFormatter.format(currentDate)
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.fetchPouchesTemporaryData(
            onSuccess = { data ->
                bottlesData = data
            },
            onFailure = { exception ->
                errorMessage = "Failed to load data: ${exception.message}"
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "80g Pouches",
                color = Color.Black,
                fontSize = 24.sp,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Button(
                onClick = {
                    // Save each item with packed and unpacked totals
                    updatedData.forEach { (key, value) ->
                        val itemName = key.substringBefore("_Packed") // Extract item name
                        val packed = updatedData["${itemName}_Packed"] as? Int ?: 0
                        val unpacked = updatedData["${itemName}_Unpacked"] as? Int ?: 0

                        // Call the function to store totals for each item
                        viewModel.fetchAndStoreTotalForAllItemsBottles(
                            onSuccess = {
                                // Optionally, handle success for each item
                            }
                        ) { exception ->
                            errorMessage = "Failed to save totals: ${exception.message}"
                        }
                    }

                    // Navigate back after saving all items
                    navController.navigate(Screen.FinishedGood.route)

                    // Save updated data temporarily (existing function in viewModel)
                    viewModel.updatePouchesDataTemp(context,updatedData,
                        onSuccess = {
                            // Optionally, handle final success
                        },
                        onFailure = { exception ->
                            errorMessage = "Failed to save data: ${exception.message}"
                        }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F3F)),
                modifier = Modifier.height(48.dp)
            ) {
                Text(text = "Done", color = Color.White)
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = Color.Red,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        bottlesData?.entries?.forEachIndexed { index, (key, value) ->
            ItemRowPouches(
                itemName = key,
                itemValue = value.toString(),
                rowIndex = index,  // Pass the current index for alternating color
            ) { itemName, packedQuantity, unpackedQuantity ->
                updatedData = updatedData.toMutableMap().apply {
                    this["${formattedDate}_${itemName}_Boxes"] = packedQuantity
                    this["${formattedDate}_${itemName}_Cases"] = unpackedQuantity
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                // Repeat the same logic as above when clicking the bottom "Done" button
                updatedData.forEach { (key, value) ->
                    val itemName = key.substringBefore("_Packed")
                    val packed = updatedData["${itemName}_Packed"] as? Int ?: 0
                    val unpacked = updatedData["${itemName}_Unpacked"] as? Int ?: 0

                    viewModel.fetchAndStoreTotalForAllItemsBottles(
                        onSuccess = {
                            // Optionally, handle success for each item
                        }
                    ) { exception ->
                        errorMessage = "Failed to save totals: ${exception.message}"
                    }
                }

                navController.navigate(Screen.FinishedGood.route)

                viewModel.updatePouchesDataTemp(context,updatedData,
                    onSuccess = {
                        // Optionally, handle final success
                    },
                    onFailure = { exception ->
                        errorMessage = "Failed to save data: ${exception.message}"
                    }
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF001F3F)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .height(48.dp)
        ) {
            Text(text = "Done", color = Color.White)
        }
    }
}
@Composable
fun ItemRowPouches(
    itemName: String,
    itemValue: String,
    rowIndex: Int,  // Add rowIndex to determine the color of the row
    onQuantityChange: (String, Int, Double) -> Unit
) {
    var packedQuantity by remember { mutableStateOf(itemValue.toIntOrNull() ?: 0) }  // Default to 0 if not valid
    var unpackedQuantity by remember { mutableStateOf(packedQuantity / 10.0) }
    var packedTextFieldValue by remember { mutableStateOf(packedQuantity.toString()) }

    // Update unpacked quantity when packed quantity changes
    LaunchedEffect(packedQuantity) {
        unpackedQuantity = packedQuantity / 10.0
        onQuantityChange(itemName, packedQuantity, unpackedQuantity)
    }

    // Subtle background color for alternating rows
    val backgroundColor = if (rowIndex % 2 == 0) Color.White else Color.White

    // Card for elevation and rounded corners
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp), // Adding horizontal padding for better layout
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFF808080)) // Rounded corners for a modern look
    ) {
        Row(
            modifier = Modifier
                .background(backgroundColor)
                .padding(16.dp), // Increased padding for a cleaner look
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = itemName,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold, // Bold item name for better readability
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis // Handle long item names gracefully
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Packed quantity section
                QuantityColumnBulkPack(
                    label = "Boxes",
                    textFieldValue = packedTextFieldValue,
                    onValueChange = { newValue ->
                        packedTextFieldValue = newValue
                        packedQuantity = newValue.toIntOrNull() ?: 0 // Default to 0 if empty or invalid
                        onQuantityChange(itemName, packedQuantity, unpackedQuantity)
                    },
                    onIncrease = {
                        packedQuantity++
                        packedTextFieldValue = packedQuantity.toString()
                        onQuantityChange(itemName, packedQuantity, unpackedQuantity)
                    },
                    onDecrease = {
                        if (packedQuantity > 0) {
                            packedQuantity--
                            packedTextFieldValue = packedQuantity.toString()
                            onQuantityChange(itemName, packedQuantity, unpackedQuantity)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp)) // Increased spacing between packed and unpacked fields

                // Unpacked quantity section (non-editable)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Cases",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format("%.1f", unpackedQuantity),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
