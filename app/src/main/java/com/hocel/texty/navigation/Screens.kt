package com.hocel.texty.navigation

sealed class Screens(val route: String){
    object Login : Screens(route = "sign_in")
    object Register : Screens(route = "sign_up")
    object ForgotPassword : Screens(route = "forgot_password")
    object HomeScreen: Screens(route = "home_screen")
    object ScanScreen: Screens(route = "scan_screen")
    object DetailsScreen: Screens(route = "details_screen")
}
