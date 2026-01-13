package com.example.foodlink.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// This class defines how a "Food Post" (a meal shared by a donor) is stored in the database.
@Entity(tableName = "food_posts")
data class FoodPost(
    // Unique ID for every food post, generated automatically.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    
    // Links this post to the User ID of the Donor who created it.
    val donorId: Int,
    
    // Details about the food item.
    val foodName: String,
    val quantity: String,
    val expiryTime: String,
    val location: String,
    
    // Optional link to an image of the food.
    val imageUrl: String? = null,
    
    // Current state of the food. 
    // "AVAILABLE" = visible to receivers.
    // "DONATED" = claimed by someone.
    // "DELIVERED" = successfully handed over.
    val status: String = "AVAILABLE",
    
    // The time when the post was created (used for sorting).
    val timestamp: Long = System.currentTimeMillis()
)
