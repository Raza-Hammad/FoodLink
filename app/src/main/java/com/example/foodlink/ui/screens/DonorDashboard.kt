package com.example.foodlink.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodlink.data.model.DonationRequest
import com.example.foodlink.data.model.FoodPost
import com.example.foodlink.data.model.User
import com.example.foodlink.ui.components.CommonStatusChip
import com.example.foodlink.ui.viewmodel.FoodViewModel

/**
 * The Donor Dashboard is the main screen for users who want to donate food.
 * It has two tabs: "My Listings" to see their food items, and "Requests" to see who wants to pick them up.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorDashboard(
    userId: Int,                 // ID of the logged-in donor.
    onNavigateToAddFood: () -> Unit, // Function to go to the "Post Food" screen.
    onLogout: () -> Unit,        // Function to sign out.
    onContactReceiver: (Int) -> Unit, // Function to open chat with a receiver.
    onEditPost: (Int) -> Unit,   // Function to edit an existing post.
    viewModel: FoodViewModel = viewModel() // Access to database operations.
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // Keeps track of which tab is active.
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    
    // Collecting data from the database using Flow (updates automatically).
    val myPosts by viewModel.getPostsByDonor(userId).collectAsState(initial = emptyList())
    val requests by viewModel.getRequestsByDonor(userId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            // Header with title and Logout button.
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primary)) {
                CenterAlignedTopAppBar(
                    title = { Text("Donor Hub", fontWeight = FontWeight.Black, color = Color.White) },
                    actions = {
                        IconButton(onClick = { showLogoutConfirmation = true }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
                
                // Tab switcher for Listings vs Requests.
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("MY LISTINGS", fontWeight = FontWeight.Bold, color = Color.White) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { 
                            val pendingCount = requests.count { it.status == "PENDING" }
                            Text("REQUESTS" + (if (pendingCount > 0) " ($pendingCount)" else ""), fontWeight = FontWeight.Bold, color = Color.White) 
                        }
                    )
                }
            }
        },
        floatingActionButton = {
            // Only show the "Add" button when viewing the "My Listings" tab.
            if (selectedTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddFood,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("POST SURPLUS")
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: Show list of food items the donor has posted.
                    if (myPosts.isEmpty()) {
                        DonorEmptyState("No active listings", Icons.Default.Inventory2)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(myPosts, key = { it.id }) { post ->
                                DonorFoodCard(
                                    post = post,
                                    onDelete = {
                                        viewModel.deleteFoodPost(post)
                                        Toast.makeText(context, "Donation Removed", Toast.LENGTH_SHORT).show()
                                    },
                                    onMarkDelivered = {
                                        viewModel.markAsDelivered(post)
                                        Toast.makeText(context, "Marked as Delivered!", Toast.LENGTH_SHORT).show()
                                    },
                                    onEdit = { onEditPost(post.id) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 1: Show requests from Receivers who want the food.
                    if (requests.isEmpty()) {
                        DonorEmptyState("No requests yet", Icons.Default.NotificationImportant)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(requests, key = { it.id }) { req ->
                                DonorRequestCard(
                                    request = req,
                                    viewModel = viewModel,
                                    onApprove = {
                                        // Donor accepts the request.
                                        viewModel.updateRequestStatus(req, "APPROVED")
                                    },
                                    onReject = {
                                        // Donor declines the request.
                                        viewModel.updateRequestStatus(req, "REJECTED")
                                    },
                                    onContact = { onContactReceiver(req.receiverId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Standard logout confirmation popup.
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Logout?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(onClick = { onLogout() }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Placeholder UI shown when there are no items to list.
 */
@Composable
fun DonorEmptyState(msg: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(msg, color = Color.Gray, fontWeight = FontWeight.Medium, fontSize = 18.sp)
    }
}

/**
 * UI Card representing a single food post by the donor.
 * Shows name, quantity, expiry, and buttons to Edit, Delete, or Mark as Delivered.
 */
@Composable
fun DonorFoodCard(post: FoodPost, onDelete: () -> Unit, onMarkDelivered: () -> Unit, onEdit: () -> Unit) {
    var showConfirmDelete by remember { mutableStateOf(false) }
    var showConfirmDeliver by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.foodName, 
                        fontSize = 20.sp, 
                        fontWeight = FontWeight.Bold,
                        color = Color.Black 
                    )
                    Text("Qty: ${post.quantity}", color = Color.DarkGray, fontSize = 14.sp)
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showConfirmDelete = true }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Expires: ${post.expiryTime}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                CommonStatusChip(post.status)
            }

            // Once someone claims the food, the donor can mark it as delivered.
            if (post.status == "DONATED") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { showConfirmDeliver = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MARK AS DELIVERED")
                }
            }
        }
    }

    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Remove Listing?", fontWeight = FontWeight.Bold) },
            text = { Text("Do you really want to delete '${post.foodName}'?") },
            confirmButton = {
                Button(onClick = { onDelete(); showConfirmDelete = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showConfirmDeliver) {
        AlertDialog(
            onDismissRequest = { showConfirmDeliver = false },
            title = { Text("Confirm Delivery?", fontWeight = FontWeight.Bold) },
            text = { Text("By confirming, you ensure the food has reached the receiver safely.") },
            confirmButton = {
                Button(onClick = { onMarkDelivered(); showConfirmDeliver = false }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeliver = false }) {
                    Text("Not Yet")
                }
            }
        )
    }
}

/**
 * UI Card representing a pickup request from a receiver.
 * Shows receiver info and buttons for the donor to Accept or Decline.
 */
@Composable
fun DonorRequestCard(
    request: DonationRequest, 
    viewModel: FoodViewModel,
    onApprove: () -> Unit, 
    onReject: () -> Unit, 
    onContact: () -> Unit
) {
    var showActionConfirmation by remember { mutableStateOf<String?>(null) }
    var receiver by remember { mutableStateOf<User?>(null) }

    // Fetch the receiver's name from their ID to show on the card.
    LaunchedEffect(request.receiverId) {
        receiver = viewModel.getUserById(request.receiverId)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (request.status == "PENDING") MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f) else Color.White
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(receiver?.name ?: "Receiver #${request.receiverId}", fontWeight = FontWeight.Bold, color = Color.Black)
                    Text("Request for Post #${request.postId}", fontSize = 12.sp, color = Color.DarkGray)
                    if (receiver?.isBlocked == true) {
                        Text("USER BLOCKED BY ADMIN", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                // Chat button to talk to the receiver.
                IconButton(onClick = onContact, enabled = receiver?.isBlocked == false) {
                    Icon(Icons.Outlined.Chat, contentDescription = "Chat", tint = if (receiver?.isBlocked == true) Color.Gray else MaterialTheme.colorScheme.primary)
                }
                
                if (request.status != "PENDING") {
                    CommonStatusChip(request.status)
                }
            }

            // Buttons to Accept/Decline only show if the request is still pending.
            if (request.status == "PENDING") {
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { showActionConfirmation = "REJECT" }, 
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Decline")
                    }
                    Button(
                        onClick = { showActionConfirmation = "APPROVE" }, 
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = receiver?.isBlocked == false
                    ) {
                        Text("Accept")
                    }
                }
            }
        }
    }

    // Confirmation dialog before approving or rejecting a request.
    if (showActionConfirmation != null) {
        val isApprove = showActionConfirmation == "APPROVE"
        AlertDialog(
            onDismissRequest = { showActionConfirmation = null },
            title = { Text(if (isApprove) "Accept Request?" else "Decline Request?", fontWeight = FontWeight.Bold) },
            text = { Text(if (isApprove) "Approving will mark your food item as donated." else "Are you sure you want to decline this pickup request?") },
            confirmButton = {
                Button(onClick = {
                    if (isApprove) onApprove() else onReject()
                    showActionConfirmation = null
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showActionConfirmation = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
