package com.example.finishedgoodmanagement



import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FinishedViewModel(application: Application) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val _isDataLoaded = MutableLiveData<Boolean>()
    val isDataLoaded: LiveData<Boolean> get() = _isDataLoaded
    init {
        firestore.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
    }

    // Function to fetch initial laminate data from Products collection


    // Function to fetch temporary laminate data from temporaryData collection
    fun fetchFinishedBottlesTemporaryData(
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("FinishedData")
                    .document("FinishedBottles")
                    .get()
                    .await()
                if (document.exists()) {
                    val data = document.data ?: emptyMap()

                    // Check if "sortOrder" exists and is a list of strings
                    val sortOrder = data["sortorder"] as? List<String>

                    // Sort the data based on "sortOrder" if it exists
                    val sortedData = if (sortOrder != null) {
                        // Create a LinkedHashMap to preserve the order
                        val sortedMap = linkedMapOf<String, Any>()
                        sortOrder.forEach { key ->
                            // Only add keys that are part of the original data
                            if (data.containsKey(key)) {
                                sortedMap[key] = data[key] ?: 0
                            }
                        }
                        sortedMap
                    } else {
                        // If "sortOrder" is absent, return the unsorted data
                        data
                    }

                    onSuccess(sortedData)
                } else {
                    onSuccess(emptyMap())
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }


    // Function to update Laminate data in the temporaryData collection
    fun updateBulkPackDataTemp(
        context: Context, // Pass the context to access SharedPreferences
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Retrieve document name from SharedPreferences
                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val documentName = sharedPreferences.getString("selectedDistributor", null)

                if (documentName.isNullOrEmpty()) {
                    throw IllegalArgumentException("No distributor selected in SharedPreferences.")
                }

                // Update the '7. Institutional Pack' sub-document
                firestore.collection("DistributorData")
                    .document(documentName)
                    .collection("subDocuments") // Optional: If '7. Institutional Pack' is in a sub-collection
                    .document("7. Institutional Pack")
                    .set(data, SetOptions.merge()) // Merge new data with existing data
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }


    // Function to fetch temporary laminate data from temporaryData collection
    fun fetchBulkPackTemporaryData(
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("FinishedData")
                    .document("InstitutionalPack")
                    .get()
                    .await()

                if (document.exists()) {
                    val data = document.data ?: emptyMap()

                    // Check if "sortOrder" exists and is a list of strings
                    val sortOrder = data["sortorder"] as? List<String>

                    // Sort the data based on "sortOrder" if it exists
                    val sortedData = if (sortOrder != null) {
                        // Create a LinkedHashMap to preserve the order
                        val sortedMap = linkedMapOf<String, Any>()
                        sortOrder.forEach { key ->
                            // Only add keys that are part of the original data
                            if (data.containsKey(key)) {
                                sortedMap[key] = data[key] ?: 0
                            }
                        }
                        sortedMap
                    } else {
                        // If "sortOrder" is absent, return the unsorted data
                        data
                    }

                    onSuccess(sortedData)
                } else {
                    onSuccess(emptyMap())
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
    fun setupRealTimeListenerForTotalUpdates(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        // Set up a listener on all documents in the "Packaging Old" collection
        firestore.collection("Packaging Old")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onFailure(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    // When any document in the collection changes, fetch and store the updated totals
                    fetchAndStoreTotalForAllItems(onSuccess, onFailure)
                    fetchAndStoreTotalForAllItemsBottles(onSuccess,onFailure)
                    fetchAndStoreTotalForAllItemsRetail(onSuccess,onFailure)
                    fetchAndStoreTotalForAllItemsPortion(onSuccess,onFailure)
                    fetchAndStoreTotalForAllItemsSeasoning(onSuccess,onFailure)
                } else {
                    onSuccess() // If no documents exist in the collection, treat as success without error
                }
            }
    }


    fun fetchAndStoreTotalForAllItems(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Fetch the specified product data from the "Packaging Old" collection
                val document = firestore.collection("Packaging Old")
                    .document("7. Institutional Pack")
                    .get()
                    .await()

                if (document.exists()) {
                    val data = document.data ?: emptyMap()
                    val totalData = mutableMapOf<String, Any>()

                    // Iterate over all entries in the document data
                    for ((key, value) in data) {
                        if (key.endsWith("_Packed") && value is Number) {
                            // Derive the item name by removing the "_Packed" suffix
                            val itemName = key.removeSuffix("_Packed")
                            val packedAmount = value.toInt()

                            // Find the corresponding unpacked amount if it exists
                            val unpackedKey = "${itemName}_Unpacked"
                            val unpackedAmount = (data[unpackedKey] as? Number)?.toInt() ?: 0

                            // Calculate the combined total
                            val combinedTotal = packedAmount + unpackedAmount

                            // Store the combined total under the "itemName_Total" field
                            totalData["${itemName}_Total"] = combinedTotal
                        }
                    }

                    // Store the combined totals in the "total" collection with "5. Institutional Pack" as the document name
                    firestore.collection("total old")
                        .document("7. Institutional Pack")
                        .set(totalData, SetOptions.merge())
                        .await()

                    onSuccess()
                } else {
                    onSuccess() // If document doesn't exist, treat as success without error
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
    fun fetchAndStoreTotalForAllItemsPortion(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Fetch the specified product data from the "Packaging Old" collection
                val document = firestore.collection("Packaging Old")
                    .document("6. Portion Pack 28g")
                    .get()
                    .await()

                if (document.exists()) {
                    val data = document.data ?: emptyMap()
                    val totalData = mutableMapOf<String, Any>()

                    // Iterate over all entries in the document data
                    for ((key, value) in data) {
                        if (key.endsWith("_Packed") && value is Number) {
                            // Derive the item name by removing the "_Packed" suffix
                            val itemName = key.removeSuffix("_Packed")
                            val packedAmount = value.toInt()

                            // Find the corresponding unpacked amount if it exists
                            val unpackedKey = "${itemName}_Unpacked"
                            val unpackedAmount = (data[unpackedKey] as? Number)?.toInt() ?: 0

                            // Calculate the combined total
                            val combinedTotal = packedAmount + unpackedAmount

                            // Store the combined total under the "itemName_Total" field
                            totalData["${itemName}_Total"] = combinedTotal
                        }
                    }

                    // Store the combined totals in the "total" collection with "5. Institutional Pack" as the document name
                    firestore.collection("total old")
                        .document("6. Portion Pack 28g")
                        .set(totalData, SetOptions.merge())
                        .await()

                    onSuccess()
                } else {
                    onSuccess() // If document doesn't exist, treat as success without error
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun fetchAndStoreTotalForAllItemsRetail(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Fetch the specified product data from the "Packaging Old" collection
                val document = firestore.collection("Packaging Old")
                    .document("1. Retail Pack 36g")
                    .get()
                    .await()

                if (document.exists()) {
                    val data = document.data ?: emptyMap()
                    val totalData = mutableMapOf<String, Any>()

                    // Iterate over all entries in the document data
                    for ((key, value) in data) {
                        if (key.endsWith("_Packed") && value is Number) {
                            // Derive the item name by removing the "_Packed" suffix
                            val itemName = key.removeSuffix("_Packed")
                            val packedAmount = value.toInt()

                            // Find the corresponding unpacked amount if it exists
                            val unpackedKey = "${itemName}_Unpacked"
                            val unpackedAmount = (data[unpackedKey] as? Number)?.toInt() ?: 0

                            // Calculate the combined total
                            val combinedTotal = packedAmount + unpackedAmount

                            // Store the combined total under the "itemName_Total" field
                            totalData["${itemName}_Total"] = combinedTotal
                        }
                    }

                    // Store the combined totals in the "total" collection with "5. Institutional Pack" as the document name
                    firestore.collection("total old")
                        .document("1. Retail Pack 36g")
                        .set(totalData, SetOptions.merge())
                        .await()

                    onSuccess()
                } else {
                    onSuccess() // If document doesn't exist, treat as success without error
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }
    fun fetchAndStoreTotalForAllItemsSeasoning(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Fetch the specified product data from the "Packaging Old" collection
                val document = firestore.collection("Packaging Old")
                    .document("2. Seasonings & Pouches")
                    .get()
                    .await()

                if (document.exists()) {
                    val data = document.data ?: emptyMap()
                    val totalData = mutableMapOf<String, Any>()

                    // Iterate over all entries in the document data
                    for ((key, value) in data) {
                        if (key.endsWith("_Packed") && value is Number) {
                            // Derive the item name by removing the "_Packed" suffix
                            val itemName = key.removeSuffix("_Packed")
                            val packedAmount = value.toInt()

                            // Find the corresponding unpacked amount if it exists
                            val unpackedKey = "${itemName}_Unpacked"
                            val unpackedAmount = (data[unpackedKey] as? Number)?.toInt() ?: 0

                            // Calculate the combined total
                            val combinedTotal = packedAmount + unpackedAmount

                            // Store the combined total under the "itemName_Total" field
                            totalData["${itemName}_Total"] = combinedTotal
                        }
                    }

                    // Store the combined totals in the "total" collection with "5. Institutional Pack" as the document name
                    firestore.collection("total old")
                        .document("2. Seasonings & Pouches")
                        .set(totalData, SetOptions.merge())
                        .await()

                    onSuccess()
                } else {
                    onSuccess() // If document doesn't exist, treat as success without error
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun fetchAndStoreTotalForAllItemsBottles(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Fetch the specified product data from the "Packaging Old" collection
                val document = firestore.collection("Packaging Old")
                    .document("4. Finished Bottles 120g")
                    .get()
                    .await()

                if (document.exists()) {
                    val data = document.data ?: emptyMap()
                    val totalData = mutableMapOf<String, Any>()

                    // Iterate over all entries in the document data
                    for ((key, value) in data) {
                        if (key.endsWith("_Packed") && value is Number) {
                            // Derive the item name by removing the "_Packed" suffix
                            val itemName = key.removeSuffix("_Packed")
                            val packedAmount = value.toInt()

                            // Find the corresponding unpacked amount if it exists
                            val unpackedKey = "${itemName}_Unpacked"
                            val unpackedAmount = (data[unpackedKey] as? Number)?.toInt() ?: 0

                            // Calculate the combined total
                            val combinedTotal = packedAmount + unpackedAmount

                            // Store the combined total under the "itemName_Total" field
                            totalData["${itemName}_Total"] = combinedTotal
                        }
                    }

                    // Store the combined totals in the "total" collection with "5. Institutional Pack" as the document name
                    firestore.collection("total old")
                        .document("4. Finished Bottles 120g")
                        .set(totalData, SetOptions.merge())
                        .await()

                    onSuccess()
                } else {
                    onSuccess() // If document doesn't exist, treat as success without error
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    // Function to update Laminate data in the temporaryData collection
    fun updatePortionPackDataTemp(
        context: Context,
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Retrieve document name from SharedPreferences
                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val documentName = sharedPreferences.getString("selectedDistributor", null)

                if (documentName.isNullOrEmpty()) {
                    throw IllegalArgumentException("No distributor selected in SharedPreferences.")
                }

                // Update the '7. Institutional Pack' sub-document
                firestore.collection("DistributorData")
                    .document(documentName)
                    .collection("subDocuments") // Optional: If '7. Institutional Pack' is in a sub-collection
                    .document("6. Portion Pack 28g")
                    .set(data, SetOptions.merge()) // Merge new data with existing data
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    // Function to fetch temporary laminate data from temporaryData collection
    fun fetchPortionPackTemporaryData(
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("FinishedData")
                    .document("PortionPack")
                    .get()
                    .await()

                if (document.exists()) {
                    val data = document.data ?: emptyMap()

                    // Check if "sortOrder" exists and is a list of strings
                    val sortOrder = data["sortedorder"] as? List<String>

                    // Sort the data based on "sortOrder" if it exists
                    val sortedData = if (sortOrder != null) {
                        // Create a LinkedHashMap to preserve the order
                        val sortedMap = linkedMapOf<String, Any>()
                        sortOrder.forEach { key ->
                            // Only add keys that are part of the original data
                            if (data.containsKey(key)) {
                                sortedMap[key] = data[key] ?: 0
                            }
                        }
                        sortedMap
                    } else {
                        // If "sortOrder" is absent, return the unsorted data
                        data
                    }

                    onSuccess(sortedData)
                } else {
                    onSuccess(emptyMap())
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    // Function to update Laminate data in the temporaryData collection
    fun updateSaucesDataTemp(
        context: Context,
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Retrieve document name from SharedPreferences
                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val documentName = sharedPreferences.getString("selectedDistributor", null)

                if (documentName.isNullOrEmpty()) {
                    throw IllegalArgumentException("No distributor selected in SharedPreferences.")
                }

                // Update the '7. Institutional Pack' sub-document
                firestore.collection("DistributorData")
                    .document(documentName)
                    .collection("subDocuments") // Optional: If '7. Institutional Pack' is in a sub-collection
                    .document("1. Retail Pack 36g")
                    .set(data, SetOptions.merge()) // Merge new data with existing data
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    // Function to fetch temporary laminate data from temporaryData collection
    fun fetchSaucesTemporaryData(
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("FinishedData")
                    .document("RetailPack")
                    .get()
                    .await()

                if (document.exists()) {
                    val data = document.data ?: emptyMap()

                    // Check if "sortOrder" exists and is a list of strings
                    val sortOrder = data["sortorder"] as? List<String>

                    // Sort the data based on "sortOrder" if it exists
                    val sortedData = if (sortOrder != null) {
                        // Create a LinkedHashMap to preserve the order
                        val sortedMap = linkedMapOf<String, Any>()
                        sortOrder.forEach { key ->
                            // Only add keys that are part of the original data
                            if (data.containsKey(key)) {
                                sortedMap[key] = data[key] ?: 0
                            }
                        }
                        sortedMap
                    } else {
                        // If "sortOrder" is absent, return the unsorted data
                        data
                    }

                    onSuccess(sortedData)
                } else {
                    onSuccess(emptyMap())
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    // Function to update Laminate data in the temporaryData collection
    fun updateSeasoningDataTemp(
        context: Context,
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Retrieve document name from SharedPreferences
                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val documentName = sharedPreferences.getString("selectedDistributor", null)

                if (documentName.isNullOrEmpty()) {
                    throw IllegalArgumentException("No distributor selected in SharedPreferences.")
                }

                // Update the '7. Institutional Pack' sub-document
                firestore.collection("DistributorData")
                    .document(documentName)
                    .collection("subDocuments") // Optional: If '7. Institutional Pack' is in a sub-collection
                    .document("2. Seasonings & Pouches")
                    .set(data, SetOptions.merge()) // Merge new data with existing data
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    // Function to fetch temporary laminate data from temporaryData collection
    fun fetchPouchesTemporaryData(
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("FinishedData")
                    .document("Pouches")
                    .get()
                    .await()

                if (document.exists()) {
                    val data = document.data ?: emptyMap()

                    // Check if "sortOrder" exists and is a list of strings
                    val sortOrder = data["sortorder"] as? List<String>

                    // Sort the data based on "sortOrder" if it exists
                    val sortedData = if (sortOrder != null) {
                        // Create a LinkedHashMap to preserve the order
                        val sortedMap = linkedMapOf<String, Any>()
                        sortOrder.forEach { key ->
                            // Only add keys that are part of the original data
                            if (data.containsKey(key)) {
                                sortedMap[key] = data[key] ?: 0
                            }
                        }
                        sortedMap
                    } else {
                        // If "sortOrder" is absent, return the unsorted data
                        data
                    }

                    onSuccess(sortedData)
                } else {
                    onSuccess(emptyMap())
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun updatePouchesDataTemp(
        context: Context,
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Retrieve document name from SharedPreferences
                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val documentName = sharedPreferences.getString("selectedDistributor", null)

                if (documentName.isNullOrEmpty()) {
                    throw IllegalArgumentException("No distributor selected in SharedPreferences.")
                }

                // Update the '7. Institutional Pack' sub-document
                firestore.collection("DistributorData")
                    .document(documentName)
                    .collection("subDocuments") // Optional: If '7. Institutional Pack' is in a sub-collection
                    .document("3. 80g Pouches")
                    .set(data, SetOptions.merge()) // Merge new data with existing data
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    // Function to fetch temporary laminate data from temporaryData collection
    fun fetchPastaKitTemporaryData(
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("FinishedData")
                    .document("PastaKits")
                    .get()
                    .await()

                if (document.exists()) {
                    val data = document.data ?: emptyMap()

                    // Check if "sortOrder" exists and is a list of strings
                    val sortOrder = data["sortorder"] as? List<String>

                    // Sort the data based on "sortOrder" if it exists
                    val sortedData = if (sortOrder != null) {
                        // Create a LinkedHashMap to preserve the order
                        val sortedMap = linkedMapOf<String, Any>()
                        sortOrder.forEach { key ->
                            // Only add keys that are part of the original data
                            if (data.containsKey(key)) {
                                sortedMap[key] = data[key] ?: 0
                            }
                        }
                        sortedMap
                    } else {
                        // If "sortOrder" is absent, return the unsorted data
                        data
                    }

                    onSuccess(sortedData)
                } else {
                    onSuccess(emptyMap())
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun updatePastaKitDataTemp(
        context: Context,
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Retrieve document name from SharedPreferences
                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val documentName = sharedPreferences.getString("selectedDistributor", null)

                if (documentName.isNullOrEmpty()) {
                    throw IllegalArgumentException("No distributor selected in SharedPreferences.")
                }

                // Update the '7. Institutional Pack' sub-document
                firestore.collection("DistributorData")
                    .document(documentName)
                    .collection("subDocuments") // Optional: If '7. Institutional Pack' is in a sub-collection
                    .document("5. Pasta Kits")
                    .set(data, SetOptions.merge()) // Merge new data with existing data
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    // Function to fetch temporary laminate data from temporaryData collection
    fun fetchSeasoningTemporaryData(
        onSuccess: (Map<String, Any>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("FinishedData")
                    .document("SeasoningsPouches")
                    .get()
                    .await()

                if (document.exists()) {
                    val data = document.data ?: emptyMap()

                    // Check if "sortOrder" exists and is a list of strings
                    val sortOrder = data["sortorder"] as? List<String>

                    // Sort the data based on "sortOrder" if it exists
                    val sortedData = if (sortOrder != null) {
                        // Create a LinkedHashMap to preserve the order
                        val sortedMap = linkedMapOf<String, Any>()
                        sortOrder.forEach { key ->
                            // Only add keys that are part of the original data
                            if (data.containsKey(key)) {
                                sortedMap[key] = data[key] ?: 0
                            }
                        }
                        sortedMap
                    } else {
                        // If "sortOrder" is absent, return the unsorted data
                        data
                    }

                    onSuccess(sortedData)
                } else {
                    onSuccess(emptyMap())
                }
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    // Function to update Laminate data in the temporaryData collection
    fun updateFinishedBottlesDataTemp(
        context: Context,
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Retrieve document name from SharedPreferences
                val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                val documentName = sharedPreferences.getString("selectedDistributor", null)

                if (documentName.isNullOrEmpty()) {
                    throw IllegalArgumentException("No distributor selected in SharedPreferences.")
                }

                // Update the '7. Institutional Pack' sub-document
                firestore.collection("DistributorData")
                    .document(documentName)
                    .collection("subDocuments") // Optional: If '7. Institutional Pack' is in a sub-collection
                    .document("4. Finished Bottles 120g")
                    .set(data, SetOptions.merge()) // Merge new data with existing data
                    .await()

                onSuccess()
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

    fun checkAndLoadData(userName: String) {
        if (_isDataLoaded.value == true) return  // Exit if already loaded
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            val data = mapOf("userName" to userName, "date" to currentDate)
            val db = FirebaseFirestore.getInstance()
            val dispatchedCollection = db.collection("Packaging")

            // Check if the document for the current date already exists
            dispatchedCollection.document(currentDate).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        updateData(dispatchedCollection, currentDate, data)  // Update with the current date
                    } else {
                        archiveOldData(db, dispatchedCollection, data, currentDate)  // Archive old data and add new date
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("checkAndLoadData", "Failed to get document: ${exception.message}")
                    _isDataLoaded.value = false
                }
        }
    }
    fun resetDataLoadedState() {
        _isDataLoaded.value = false
    }
    suspend fun updateTotalData(
        onSuccess: (Map<String, Map<String, Any>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            // Date formatter and date calculation
            val dateFormatter = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            val currentDate = Date()
            val previousDate = Date(currentDate.time - 24 * 60 * 60 * 1000)
            val previouslyDate = Date(currentDate.time - 2 * 24 * 60 * 60 * 1000)

            val formattedCurrentDate = dateFormatter.format(currentDate)
            val formattedPreviousDate = dateFormatter.format(previousDate)
            val formattedPreviouslyDate = dateFormatter.format(previouslyDate)

            // Fetch all documents from the 'Distribution' collection
            val productsCollection = firestore.collection("Distribution").get().await()

            // Combine all data into a map
            val allPackagingData = mutableMapOf<String, Any>()
            productsCollection.forEach { document ->
                allPackagingData[document.id] = document.data ?: emptyMap<String, Any>()
            }

            // Process data for each date
            val previouslyPackagingData = allPackagingData
                .filterKeys { it.startsWith(formattedPreviouslyDate) }
                .mapKeys { it.key.substringAfter("${formattedPreviouslyDate}_") }
                .mapValues { (it.value as? Number)?.toLong() ?: 0L }

            val previousPackagingData = allPackagingData
                .filterKeys { it.startsWith(formattedPreviousDate) }
                .mapKeys { it.key.substringAfter("${formattedPreviousDate}_") }
                .mapValues { (it.value as? Number)?.toLong() ?: 0L }

            val currentPackagingData = allPackagingData
                .filterKeys { it.startsWith(formattedCurrentDate) }
                .mapKeys { it.key.substringAfter("${formattedCurrentDate}_") }
                .mapValues { (it.value as? Number)?.toLong() ?: 0L }

            // Extract packed data
            val previouslyPackedData = previouslyPackagingData
                .filterKeys { it.endsWith("_Cases") }
                .mapKeys { it.key.substringBefore("_Cases") }

            val previousPackedData = previousPackagingData
                .filterKeys { it.endsWith("_Cases") }
                .mapKeys { it.key.substringBefore("_Cases") }

            val currentPackedData = currentPackagingData
                .filterKeys { it.endsWith("_Cases") }
                .mapKeys { it.key.substringBefore("_Cases") }

            // Combine all processed data into a single map
            val processedData = mapOf(
                "previouslyPackedData" to previouslyPackedData,
                "previousPackedData" to previousPackedData,
                "currentPackedData" to currentPackedData
            )

            // Debugging: Log the processed data
            Log.d("updateTotalData", "Processed data: $processedData")

            // Trigger success callback
            onSuccess(processedData)
        } catch (e: Exception) {
            // Log the error and trigger failure callback
            Log.e("updateTotalData", "Error updating total data: ${e.message}", e)
            onFailure(e)
        }
    }


    // Helper function to fetch collection data as a Map
    suspend fun fetchCollectionData(collectionName: String): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        try {
            val snapshot = firestore.collection(collectionName).get().await()
            for (document in snapshot.documents) {
                val categoryName = document.id
                val fields = document.data ?: emptyMap<String, Any>()
                for ((key, value) in fields) {
                    val combinedKey = "${categoryName}_$key"
                    result[combinedKey] = value
                }
            }
            Log.d("fetchCollectionData", "Fetched Data: $result")
        } catch (e: Exception) {
            Log.e("fetchCollectionData", "Error fetching data: ${e.message}", e)
        }
        return result
    }




    fun checkAndLoadDataTotal(userName: String) {
        if (_isDataLoaded.value == true) return  // Exit if already loaded
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = dateFormat.format(Date())
            val data = mapOf("userName" to userName, "date" to currentDate)
            val db = FirebaseFirestore.getInstance()
            val dispatchedCollection = db.collection("total")

            // Check if the document for the current date already exists
            dispatchedCollection.document(currentDate).get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        updateDataTotal(dispatchedCollection, currentDate, data)  // Update with the current date
                    } else {
                        archiveOldDataTotal(db, dispatchedCollection, data, currentDate)  // Archive old data and add new date
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("checkAndLoadData", "Failed to get document: ${exception.message}")
                    _isDataLoaded.value = false
                }
        }
    }
    suspend fun getCollectionData(collectionName: String): Map<String, Long> {
        return try {
            firestore.collection(collectionName).get().await().documents.associate { doc ->
                doc.id to (doc.getLong("value") ?: 0L)
            }
        } catch (e: Exception) {
            Log.e("FinishedViewModel", "Failed to fetch $collectionName data: ${e.message}", e)
            emptyMap()
        }
    }

    private fun updateDataTotal(
        dispatchedCollection: CollectionReference,
        currentDate: String,
        data: Map<String, Any>
    ) {
        dispatchedCollection.document(currentDate)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("updateData", "Data for $currentDate successfully updated.")
                _isDataLoaded.value = true
            }
            .addOnFailureListener { exception ->
                Log.e("updateData", "Failed to update data for $currentDate: ${exception.message}")
                _isDataLoaded.value = false
            }
    }

    private fun archiveOldDataTotal(
        db: FirebaseFirestore,
        dispatchedCollection: CollectionReference,
        data: Map<String, Any>,
        currentDate: String
    ) {
        db.collection("total").get()
            .addOnSuccessListener { querySnapshot ->
                val dispatchedOldCollection = db.collection("total old")
                Log.d("archiveOldData", "Archiving old documents...")

                querySnapshot.documents.forEach { document ->
                    val documentData = document.data ?: return@forEach
                    dispatchedOldCollection.document(document.id).set(documentData)
                        .addOnSuccessListener {
                            Log.d("archiveOldData", "Document ${document.id} archived.")
                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                "archiveOldData",
                                "Failed to archive document ${document.id}: ${exception.message}"
                            )
                        }
                    dispatchedCollection.document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("archiveOldData", "Document ${document.id} deleted from Dispatched.")
                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                "archiveOldData",
                                "Failed to delete document ${document.id}: ${exception.message}"
                            )
                        }
                }

                // Set new data for the current date in "Dispatched"
                dispatchedCollection.document(currentDate).set(data, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("archiveOldData", "New data added for current date $currentDate.")
                        _isDataLoaded.value = true
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "archiveOldData",
                            "Failed to add data for current date $currentDate: ${exception.message}"
                        )
                        _isDataLoaded.value = false
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "archiveOldData",
                    "Failed to retrieve documents for archiving: ${exception.message}"
                )
                _isDataLoaded.value = false
            }
    }
    private fun updateData(
        dispatchedCollection: CollectionReference,
        currentDate: String,
        data: Map<String, Any>
    ) {
        dispatchedCollection.document(currentDate)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("updateData", "Data for $currentDate successfully updated.")
                _isDataLoaded.value = true
            }
            .addOnFailureListener { exception ->
                Log.e("updateData", "Failed to update data for $currentDate: ${exception.message}")
                _isDataLoaded.value = false
            }
    }

    private fun archiveOldData(
        db: FirebaseFirestore,
        dispatchedCollection: CollectionReference,
        data: Map<String, Any>,
        currentDate: String
    ) {
        db.collection("Packaging").get()
            .addOnSuccessListener { querySnapshot ->
                val dispatchedOldCollection = db.collection("Packaging Old")
                Log.d("archiveOldData", "Archiving old documents...")

                querySnapshot.documents.forEach { document ->
                    val documentData = document.data ?: return@forEach
                    dispatchedOldCollection.document(document.id).set(documentData)
                        .addOnSuccessListener {
                            Log.d("archiveOldData", "Document ${document.id} archived.")
                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                "archiveOldData",
                                "Failed to archive document ${document.id}: ${exception.message}"
                            )
                        }
                    dispatchedCollection.document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("archiveOldData", "Document ${document.id} deleted from Dispatched.")
                        }
                        .addOnFailureListener { exception ->
                            Log.e(
                                "archiveOldData",
                                "Failed to delete document ${document.id}: ${exception.message}"
                            )
                        }
                }

                // Set new data for the current date in "Dispatched"
                dispatchedCollection.document(currentDate).set(data, SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("archiveOldData", "New data added for current date $currentDate.")
                        _isDataLoaded.value = true
                    }
                    .addOnFailureListener { exception ->
                        Log.e(
                            "archiveOldData",
                            "Failed to add data for current date $currentDate: ${exception.message}"
                        )
                        _isDataLoaded.value = false
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "archiveOldData",
                    "Failed to retrieve documents for archiving: ${exception.message}"
                )
                _isDataLoaded.value = false
            }
    }

    fun getApplicationContext(): Application {
        return getApplication()
    }

    fun exportProductsData(
        onSuccess: (Map<String, Map<String, Any>>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Fetch all documents from the Products collection
                val productsCollection = firestore.collection("Packaging").get().await()

                // Prepare data to be exported
                val productsData = mutableMapOf<String, Map<String, Any>>()
                productsCollection.forEach { document ->
                    productsData[document.id] = document.data
                }

                // Notify success with fetched data
                onSuccess(productsData)
            } catch (e: Exception) {
                onFailure(e)
            }
        }
    }

}