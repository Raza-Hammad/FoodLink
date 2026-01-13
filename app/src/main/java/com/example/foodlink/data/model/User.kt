package com.example.foodlink.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// This class represents a "User" table in our database.
// @Entity tells Room database that this class will be a table.
@Entity(tableName = "users")
data class User(
    // PrimaryKey means this ID is unique for every user. 
    // autoGenerate = true means the database will give a new ID automatically.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    
    // Storing the user's basic details.
    val name: String,
    val email: String,
    val password: String,
    
    // Role can be 'ADMIN', 'DONOR', or 'RECEIVER'.
    val role: String,
    
    // Used by Admin to verify new accounts.
    val isVerified: Boolean = false,
    
    // Used by Admin to block/unblock problematic users.
    val isBlocked: Boolean = false
)
