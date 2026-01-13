package com.example.foodlink.data.local

import androidx.room.*
import com.example.foodlink.data.model.DonationRequest
import com.example.foodlink.data.model.FoodPost
import com.example.foodlink.data.model.User
import com.example.foodlink.data.model.Message
import kotlinx.coroutines.flow.Flow

/**
 * DAO stands for Data Access Object. 
 * This interface defines all the database operations (SQL queries) for our app.
 * Room will automatically generate the code to run these queries.
 */
@Dao
interface FoodDao {
    
    // --- USER OPERATIONS ---

    // This adds a new user to the database. 
    // If a user with the same ID exists, it cancels the operation (ABORT).
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    // This fetches all users who are not Admins and haven't been verified yet.
    // Flow means the UI will update automatically if the data changes in the DB.
    @Query("SELECT * FROM users WHERE role != 'ADMIN' AND isVerified = 0")
    fun getPendingUsers(): Flow<List<User>>

    // This gets a list of everyone except the Admin.
    @Query("SELECT * FROM users WHERE role != 'ADMIN'")
    fun getAllUsers(): Flow<List<User>>

    // Used during login to check if the email exists.
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    // Finds a specific user based on their unique ID.
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Int): User?

    // Finds a user by their name (useful for display or checks).
    @Query("SELECT * FROM users WHERE name = :name LIMIT 1")
    suspend fun getUserByName(name: String): User?

    // Checks if a username is already taken during registration.
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE name = :name)")
    suspend fun isUsernameTaken(name: String): Boolean

    // Updates existing user info (like changing verification status or blocking).
    @Update
    suspend fun updateUser(user: User)

    // Removes a user from the system.
    @Delete
    suspend fun deleteUser(user: User)


    // --- FOOD POST OPERATIONS ---

    // Donor uses this to post new surplus food items.
    @Insert
    suspend fun insertFoodPost(post: FoodPost): Long

    // Fetches all food items that are still 'AVAILABLE' for pickup.
    @Query("SELECT * FROM food_posts WHERE status = 'AVAILABLE' ORDER BY timestamp DESC")
    fun getAllAvailablePosts(): Flow<List<FoodPost>>

    // Gets all the food items posted by a specific donor.
    @Query("SELECT * FROM food_posts WHERE donorId = :donorId")
    fun getPostsByDonor(donorId: Int): Flow<List<FoodPost>>

    // Used to update food details or change status (e.g., AVAILABLE to DONATED).
    @Update
    suspend fun updateFoodPost(post: FoodPost)

    // Deletes a specific food listing.
    @Delete
    suspend fun deleteFoodPost(post: FoodPost)


    // --- DONATION REQUEST OPERATIONS ---

    // Receiver uses this to ask for a specific food item.
    @Insert
    suspend fun insertRequest(request: DonationRequest)

    // Donor uses this to see who is asking for their food.
    @Query("SELECT * FROM donation_requests WHERE donorId = :donorId")
    fun getRequestsByDonor(donorId: Int): Flow<List<DonationRequest>>

    // Receiver uses this to see the status of their requests.
    @Query("SELECT * FROM donation_requests WHERE receiverId = :receiverId")
    fun getRequestsByReceiver(receiverId: Int): Flow<List<DonationRequest>>

    // Helper to get full details of a post linked to a request.
    @Query("SELECT * FROM food_posts WHERE id = :postId LIMIT 1")
    suspend fun getPostById(postId: Int): FoodPost?

    // Update status of a request (e.g., PENDING to APPROVED).
    @Update
    suspend fun updateRequest(request: DonationRequest)

    // Delete a request if it's no longer needed.
    @Delete
    suspend fun deleteRequest(request: DonationRequest)


    // --- MESSAGING OPERATIONS ---

    // Saves a new chat message to the database.
    @Insert
    suspend fun insertMessage(message: Message)

    // Fetches the chat history between two specific users in order of time.
    @Query("SELECT * FROM messages WHERE (senderId = :userId AND receiverId = :otherId) OR (senderId = :otherId AND receiverId = :userId) ORDER BY timestamp ASC")
    fun getMessagesBetween(userId: Int, otherId: Int): Flow<List<Message>>

    // Deletes the whole chat history between two users.
    @Query("DELETE FROM messages WHERE (senderId = :userId AND receiverId = :otherId) OR (senderId = :otherId AND receiverId = :userId)")
    suspend fun deleteConversation(userId: Int, otherId: Int)
}
