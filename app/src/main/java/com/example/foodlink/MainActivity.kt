package com.example.foodlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.foodlink.ui.navigation.Screen
import com.example.foodlink.ui.screens.*
import com.example.foodlink.ui.theme.FoodLinkTheme

// This is the main entry point of the Android application.
class MainActivity : ComponentActivity() {
    // This function runs when the app starts for the first time.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // This makes the app content go behind the status bar and navigation bar (full screen look).
        enableEdgeToEdge()
        
        // This tells the app what UI to show using Jetpack Compose.
        setContent {
            // Applying our custom theme to the whole app.
            FoodLinkTheme {
                // Starting our main navigation function.
                FoodLinkApp()
            }
        }
    }
}

// This function handles the navigation (moving between different screens) of the app.
@Composable
fun FoodLinkApp() {
    // This controller manages our app's backstack and keeps track of which screen we are on.
    val navController = rememberNavController()

    // NavHost is like a container that switches between different screens.
    // We start with the Splash screen.
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Defining the route for the Splash screen (the opening screen with logo).
        composable(Screen.Splash.route) {
            SplashScreen(onTimeout = {
                // When splash screen finishes, move to the Login screen.
                navController.navigate(Screen.Login.route) {
                    // Remove splash screen from history so user can't go back to it.
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            })
        }

        // Defining the route for the Login screen.
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = { role, userId ->
                    // Based on the user's role, we decide which dashboard to show.
                    val destination = when(role) {
                        "ADMIN" -> Screen.AdminDashboard.route
                        "DONOR" -> Screen.DonorDashboard.createRoute(userId)
                        else -> Screen.ReceiverDashboard.createRoute(userId)
                    }
                    // Move to the decided dashboard.
                    navController.navigate(destination) {
                        // Clear login from history.
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    // Go to registration screen if user clicks "Create Account".
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // Defining the route for the Registration screen.
        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    // After successful registration, go back to login screen.
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    // Go back if they already have an account.
                    navController.popBackStack()
                }
            )
        }

        // Route for the Donor Dashboard, it takes 'userId' as a parameter.
        composable(
            route = Screen.DonorDashboard.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            // Extracting the userId from the navigation parameters.
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            DonorDashboard(
                userId = userId,
                onNavigateToAddFood = {
                    // Navigate to screen where donor can add a new food item.
                    navController.navigate(Screen.AddFood.createRoute(userId))
                },
                onLogout = {
                    // Go back to login and clear all previous screens.
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                },
                onContactReceiver = { receiverId ->
                    // Open the chat screen with a specific receiver.
                    navController.navigate("chat/$userId/$receiverId")
                },
                onEditPost = { postId ->
                    // Go to screen to edit an existing food post.
                    navController.navigate(Screen.EditFood.createRoute(postId))
                }
            )
        }

        // Route for the Receiver Dashboard, also needs 'userId'.
        composable(
            route = Screen.ReceiverDashboard.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            ReceiverDashboard(
                userId = userId,
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                },
                onContactDonor = { donorId ->
                    // Open chat with the donor of a specific food post.
                    navController.navigate("chat/$userId/$donorId")
                }
            )
        }

        // Route for the Admin Dashboard (simple route, no arguments needed).
        composable(Screen.AdminDashboard.route) {
            AdminDashboard(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0)
                    }
                }
            )
        }

        // Screen for adding new food items.
        composable(
            route = Screen.AddFood.route,
            arguments = listOf(navArgument("userId") { type = NavType.IntType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            AddFoodScreen(
                userId = userId,
                onBack = { navController.popBackStack() }, // Go back to previous screen.
                onSuccess = { navController.popBackStack() } // Go back after success.
            )
        }

        // Screen for editing existing food posts.
        composable(
            route = Screen.EditFood.route,
            arguments = listOf(navArgument("postId") { type = NavType.IntType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getInt("postId") ?: 0
            EditFoodScreen(
                postId = postId,
                onBack = { navController.popBackStack() },
                onSuccess = { navController.popBackStack() }
            )
        }

        // Screen for real-time messaging between users.
        composable(
            route = "chat/{myId}/{otherId}",
            arguments = listOf(
                navArgument("myId") { type = NavType.IntType },
                navArgument("otherId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val myId = backStackEntry.arguments?.getInt("myId") ?: 0
            val otherId = backStackEntry.arguments?.getInt("otherId") ?: 0
            ChatScreen(
                currentUserId = myId,
                otherUserId = otherId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
