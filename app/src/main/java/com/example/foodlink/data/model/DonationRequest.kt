package com.example.foodlink.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// This class represents the "Donation Request" table.
// It tracks when a Receiver asks for a specific food item from a Donor.
@Entity(tableName = "donation_requests")
data class DonationRequest(
    // Unique ID for the request.
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    
    // The ID of the food post being requested.
    val postId: Int,
    
    // The ID of the person (Receiver) making the request.
    val receiverId: Int,
    
    // The ID of the person (Donor) who owns the food.
    val donorId: Int,
    
    // Status of the request:
    // "PENDING" = waiting for donor's response.
    // "APPROVED" = donor said yes.
    // "REJECTED" = donor said no.
    val status: String = "PENDING", 
    
    // Time when the request was made.
    val requestTime: Long = System.currentTimeMillis()
)
