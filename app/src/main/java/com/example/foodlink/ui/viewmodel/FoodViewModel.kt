package com.example.foodlink.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodlink.data.local.AppDatabase
import com.example.foodlink.data.local.FoodDao
import com.example.foodlink.data.model.DonationRequest
import com.example.foodlink.data.model.FoodPost
import com.example.foodlink.data.model.User
import com.example.foodlink.data.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * The ViewModel acts as a bridge between the Database (DAO) and the UI (Screens).
 * It handles the logic of the app and keeps the data even if the screen rotates.
 */
class FoodViewModel(application: Application) : AndroidViewModel(application) {
    // Getting a reference to our database operations (DAO).
    private val dao: FoodDao = AppDatabase.getDatabase(application).foodDao()

    /**
     * This variable holds all the food posts that are currently available.
     * It also filters out posts that have expired based on their 'expiryTime'.
     */
    val availablePosts: Flow<List<FoodPost>> = dao.getAllAvailablePosts().map { list ->
        list.filter { post ->
            // Extract numbers from expiry string (e.g., "5 hours" -> 5).
            val hours = post.expiryTime.filter { it.isDigit() }.toLongOrNull() ?: 24L
            // Calculate when it expires (timestamp + hours in milliseconds).
            val expiryMillis = post.timestamp + (hours * 3600000)
            // Only keep it if the current time is less than expiry time.
            System.currentTimeMillis() < expiryMillis && post.status == "AVAILABLE"
        }
    }

    // --- DATA FETCHING FUNCTIONS ---
    // These functions simply ask the DAO for data and return it to the UI.

    // Gets users waiting for Admin approval.
    fun getPendingUsers() = dao.getPendingUsers()
    
    // Gets all registered users.
    fun getAllUsers() = dao.getAllUsers()

    // Gets food items posted by a specific donor.
    fun getPostsByDonor(donorId: Int) = dao.getPostsByDonor(donorId)
    
    // Gets pickup requests sent to a donor.
    fun getRequestsByDonor(donorId: Int) = dao.getRequestsByDonor(donorId)
    
    // Gets pickup requests sent by a receiver.
    fun getRequestsByReceiver(receiverId: Int) = dao.getRequestsByReceiver(receiverId)

    // Helper to find a user's details by their ID.
    suspend fun getUserById(id: Int): User? = dao.getUserById(id)
    
    // Helper to find a specific food post by its ID.
    suspend fun getPostById(id: Int): FoodPost? = dao.getPostById(id)

    // Checks if a name is already used (to avoid duplicate usernames).
    suspend fun isUsernameTaken(username: String): Boolean {
        return dao.isUsernameTaken(username)
    }

    // Generates a random 6-digit number (could be used for email verification).
    fun generateOtp(): String {
        return (100000..999999).random().toString()
    }

    // --- USER LOGIC ---

    /**
     * Handles the registration of a new user.
     * Checks if email or username is already taken before saving.
     */
    suspend fun registerUser(user: User): Result<Long> {
        val existingEmail = dao.getUserByEmail(user.email)
        if (existingEmail != null) return Result.failure(Exception("Email already registered"))
        
        if (dao.isUsernameTaken(user.name)) return Result.failure(Exception("Username already taken"))

        return try {
            val id = dao.insertUser(user)
            Result.success(id) // Return success with the new User ID.
        } catch (e: Exception) {
            Result.failure(e) // Return error if something went wrong.
        }
    }

    /**
     * Checks if the login credentials are correct.
     * Also checks if the user is blocked or not yet verified by the Admin.
     */
    suspend fun loginUser(email: String, password: String): Result<User> {
        val user = dao.getUserByEmail(email) ?: return Result.failure(Exception("User not found"))
        if (user.isBlocked) return Result.failure(Exception("BLOCKED_BY_ADMIN"))
        if (user.password != password) return Result.failure(Exception("Incorrect password"))
        
        // Only Admin and Verified users can enter.
        if (user.role != "ADMIN" && !user.isVerified) return Result.failure(Exception("Account pending admin approval"))
        
        return Result.success(user)
    }

    // --- ADMIN ACTIONS ---

    // Admin verifies a user to let them log in.
    fun approveUser(user: User) = viewModelScope.launch {
        dao.updateUser(user.copy(isVerified = true))
    }

    // Admin blocks or unblocks a user.
    fun toggleBlockUser(user: User) = viewModelScope.launch {
        dao.updateUser(user.copy(isBlocked = !user.isBlocked))
    }

    // --- DONOR ACTIONS ---

    // Donor adds a new food listing.
    fun addFoodPost(post: FoodPost) = viewModelScope.launch {
        dao.insertFoodPost(post)
    }

    // Donor updates details of a meal.
    fun updateFoodPost(post: FoodPost) = viewModelScope.launch {
        dao.updateFoodPost(post)
    }

    // Donor deletes a post.
    fun deleteFoodPost(post: FoodPost) = viewModelScope.launch {
        dao.deleteFoodPost(post)
    }

    // Donor marks a meal as delivered to the receiver.
    fun markAsDelivered(post: FoodPost) = viewModelScope.launch {
        dao.updateFoodPost(post.copy(status = "DELIVERED"))
    }

    // --- RECEIVER ACTIONS ---

    // Receiver clicks 'Claim' to request a food item.
    fun claimFood(postId: Int, receiverId: Int, donorId: Int) = viewModelScope.launch {
        dao.insertRequest(DonationRequest(postId = postId, receiverId = receiverId, donorId = donorId))
    }

    // Receiver can cancel their request if it is still 'PENDING'.
    fun deleteRequest(request: DonationRequest) = viewModelScope.launch {
        if (request.status == "PENDING") {
            dao.deleteRequest(request)
        }
    }

    /**
     * Donor can Accept (APPROVE) or Decline (REJECT) a request.
     * If approved, the food item's status changes to 'DONATED'.
     */
    fun updateRequestStatus(request: DonationRequest, status: String) = viewModelScope.launch {
        dao.updateRequest(request.copy(status = status))
        if (status == "APPROVED") {
            val post = dao.getPostById(request.postId)
            post?.let {
                dao.updateFoodPost(it.copy(status = "DONATED"))
            }
        }
    }

    // --- MESSAGING LOGIC ---

    // Gets the live list of messages between two users.
    fun getMessages(userId: Int, otherId: Int): Flow<List<Message>> {
        return dao.getMessagesBetween(userId, otherId)
    }

    // Sends a new message and saves it to the DB.
    fun sendMessage(senderId: Int, receiverId: Int, content: String) = viewModelScope.launch {
        dao.insertMessage(Message(senderId = senderId, receiverId = receiverId, content = content))
    }

    // Clears the entire conversation history.
    fun deleteConversation(userId: Int, otherId: Int) = viewModelScope.launch {
        dao.deleteConversation(userId, otherId)
    }
}
