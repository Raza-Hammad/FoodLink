package com.example.foodlink.ui.navigation

/**
 * This sealed class defines all the screens in our app and their navigation paths (routes).
 * Using a sealed class helps us avoid typos when navigating between screens.
 */
sealed class Screen(val route: String) {
    // The path for the opening splash screen.
    object Splash : Screen("splash")
    
    // The path for the login screen.
    object Login : Screen("login")
    
    // The path for the user registration screen.
    object Register : Screen("register")
    
    // The Donor's main screen. It expects a 'userId' so we know who is logged in.
    object DonorDashboard : Screen("donor_dashboard/{userId}") {
        // Helper function to create the actual path with a specific user ID.
        fun createRoute(userId: Int) = "donor_dashboard/$userId"
    }
    
    // The Receiver's main screen. Also needs a 'userId'.
    object ReceiverDashboard : Screen("receiver_dashboard/{userId}") {
        fun createRoute(userId: Int) = "receiver_dashboard/$userId"
    }
    
    // The Admin's control panel.
    object AdminDashboard : Screen("admin_dashboard")
    
    // Screen to add new food items. Needs the donor's ID.
    object AddFood : Screen("add_food/{userId}") {
        fun createRoute(userId: Int) = "add_food/$userId"
    }
    
    // Screen to edit an existing food post. Needs the specific post's ID.
    object EditFood : Screen("edit_food/{postId}") {
        fun createRoute(postId: Int) = "edit_food/$postId"
    }
}
