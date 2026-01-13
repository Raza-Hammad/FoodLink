package com.example.foodlink.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// This class defines the structure of a chat message in our app's database.
// @Entity(tableName = "messages") tells Room to create a table named 'messages'.
@Entity(tableName = "messages")
data class Message(
    // Every message gets a unique ID automatically.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    
    // Stores the ID of the person who sent the message.
    val senderId: Int,
    
    // Stores the ID of the person who is supposed to receive the message.
    val receiverId: Int,
    
    // The actual text content of the message.
    val content: String,
    
    // The exact time the message was sent (in milliseconds). 
    // Defaults to the current system time.
    val timestamp: Long = System.currentTimeMillis()
)
