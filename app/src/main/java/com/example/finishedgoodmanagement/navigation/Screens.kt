package com.example.finishedgoodmanagement.navigation




sealed class Screen(val route: String) {
    object FinishedGood : Screen("Finished_good")
    object LoginScreen : Screen("login_screen")
    object HomeScreen : Screen("home_screen")
    object Seasonings: Screen("Seasonings")
    object Bottles : Screen("Bottles")
    object RetailPack : Screen("Retail_Pack")
    object PortionPack : Screen("Portion_Pack")
    object BulkPack : Screen("Bulk_Pack")
    object AdminScreen : Screen("Admin_screen")
    object Pasta : Screen("Pasta_Screen")
    object Pouches : Screen("Pouches_screen")



}
