package com.example.foodlink.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A 'Chip' is a small UI element that shows a status (like a label).
 * This component is shared across different screens to keep a consistent look.
 */
@Composable
fun CommonStatusChip(status: String) {
    // We choose the background color based on what the status says.
    val containerColor = when(status) {
        // Greenish colors for active/waiting states.
        "AVAILABLE", "PENDING" -> Color(0xFFE8F5E9)
        // Orange colors for things in progress.
        "CLAIMED", "APPROVED" -> Color(0xFFFFF3E0)
        // Reddish colors for bad/ended states.
        "REJECTED", "EXPIRED" -> Color(0xFFFFEBEE)
        // Gray for everything else.
        else -> Color.LightGray
    }
    
    // We also choose the text color to match the background color for better readability.
    val contentColor = when(status) {
        "AVAILABLE", "PENDING" -> Color(0xFF2E7D32) // Dark Green
        "CLAIMED", "APPROVED" -> Color(0xFFEF6C00) // Dark Orange
        "REJECTED", "EXPIRED" -> Color(0xFFC62828) // Dark Red
        else -> Color.DarkGray
    }

    // Surface creates the small rounded box.
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        // Text inside the box showing the actual status name.
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = contentColor
        )
    }
}
