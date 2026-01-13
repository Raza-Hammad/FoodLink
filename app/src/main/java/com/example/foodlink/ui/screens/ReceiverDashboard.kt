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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.foodlink.data.model.DonationRequest
import com.example.foodlink.data.model.FoodPost
import com.example.foodlink.data.model.User
import com.example.foodlink.ui.components.CommonStatusChip
import com.example.foodlink.ui.viewmodel.FoodViewModel

/**
 * The Receiver Dashboard is the main screen for users looking to find surplus food.
 * It features two main tabs: "Find Food" to browse items, and "My Requests" to track pickups.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiverDashboard(
    userId: Int,                 // ID of the logged-in receiver.
    onLogout: () -> Unit,        // Function to sign out.
    onContactDonor: (Int) -> Unit, // Function to open a chat with a specific donor.
    viewModel: FoodViewModel = viewModel() // Access to app data and logic.
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 for Finding Food, 1 for Requests.
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    
    // Collecting data streams from the database.
    val availableFood by viewModel.availablePosts.collectAsState(initial = emptyList())
    val myRequests by viewModel.getRequestsByReceiver(userId).collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            // Header showing the screen title and Logout button.
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primary)) {
                CenterAlignedTopAppBar(
                    title = { Text("Receiver Hub", fontWeight = FontWeight.Black, color = Color.White) },
                    actions = {
                        IconButton(onClick = { showLogoutConfirmation = true }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
                
                // Tab switching between browsing food and checking request history.
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
                        text = { Text("FIND FOOD", fontWeight = FontWeight.Bold, color = Color.White) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("MY REQUESTS", fontWeight = FontWeight.Bold, color = Color.White) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 0: Browse Available Food items.
                    Column {
                        // Search bar (visual placeholder).
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            placeholder = { Text("Search by food or location") },
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color.White, focusedContainerColor = Color.White)
                        )

                        if (availableFood.isEmpty()) {
                            ReceiverEmptyState("No active donations", Icons.Default.SoupKitchen)
                        } else {
                            // Vertical scrollable list of food items.
                            LazyColumn(
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                items(availableFood, key = { it.id }) { post ->
                                    var showClaimDialog by remember { mutableStateOf(false) }
                                    var showDonorInfo by remember { mutableStateOf(false) }
                                    
                                    // A card showing the food details.
                                    ReceiverFoodCard(
                                        post = post,
                                        onClaim = { showClaimDialog = true },
                                        onInfo = { showDonorInfo = true }
                                    )
                                    
                                    // Popup for confirming the pickup request.
                                    if (showClaimDialog) {
                                        ClaimConfirmDialog(
                                            post = post,
                                            onConfirm = {
                                                // Create a new request in the database.
                                                viewModel.claimFood(post.id, userId, post.donorId)
                                                showClaimDialog = false
                                                Toast.makeText(context, "Request Sent!", Toast.LENGTH_SHORT).show()
                                            },
                                            onDismiss = { showClaimDialog = false }
                                        )
                                    }

                                    // Popup showing details about who is donating the food.
                                    if (showDonorInfo) {
                                        DonorInfoDialog(
                                            donorId = post.donorId,
                                            viewModel = viewModel,
                                            onMessage = { onContactDonor(post.donorId) },
                                            onDismiss = { showDonorInfo = false }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // TAB 1: Shows the status of food items this receiver has already requested.
                    if (myRequests.isEmpty()) {
                        ReceiverEmptyState("No requests made yet", Icons.Default.History)
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(myRequests, key = { it.id }) { req ->
                                var showDeleteDialog by remember { mutableStateOf(false) }
                                ReceiverRequestCard(
                                    request = req,
                                    onDelete = { showDeleteDialog = true },
                                    onContact = { onContactDonor(req.donorId) }
                                )
                                // Confirmation before removing a pending request.
                                if (showDeleteDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showDeleteDialog = false },
                                        title = { Text("Delete Request?", fontWeight = FontWeight.Bold) },
                                        text = { Text("Are you sure you want to remove this request?") },
                                        confirmButton = {
                                            Button(onClick = {
                                                viewModel.deleteRequest(req)
                                                showDeleteDialog = false
                                                Toast.makeText(context, "Request Removed", Toast.LENGTH_SHORT).show()
                                            }) { Text("Confirm") }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation dialog for logging out.
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
 * A dialog that pops up when a receiver wants to know more about a Donor.
 * Shows the donor's name and allows the receiver to start a chat.
 */
@Composable
fun DonorInfoDialog(
    donorId: Int,
    viewModel: FoodViewModel,
    onMessage: () -> Unit,
    onDismiss: () -> Unit
) {
    var donor by remember { mutableStateOf<User?>(null) }
    // Fetch the donor's profile from the database using their ID.
    LaunchedEffect(donorId) {
        donor = viewModel.getUserById(donorId)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Donor Details", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Name: ${donor?.name ?: "Loading..."}", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Text("Role: ${donor?.role ?: ""}", fontSize = 14.sp, color = Color.Gray)
                if (donor?.isBlocked == true) {
                    Text("This user is currently blocked.", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onMessage() // Navigates to the chat screen.
                },
                enabled = donor?.isBlocked == false
            ) {
                Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Message Donor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

/**
 * Placeholder UI shown when there are no items to display.
 */
@Composable
fun ReceiverEmptyState(msg: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
        Text(msg, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}

/**
 * Dialog to confirm that the user really wants to claim a food item.
 */
@Composable
fun ClaimConfirmDialog(post: FoodPost, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request Pickup?", fontWeight = FontWeight.Bold) },
        text = { Text("This will send a request to the donor for '${post.foodName}'.") },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Send Request") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * A beautiful card component representing an available food item.
 * Displays image, name, location, and quantity.
 */
@Composable
fun ReceiverFoodCard(post: FoodPost, onClaim: () -> Unit, onInfo: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Food Image and floating tags.
            Box {
                AsyncImage(
                    model = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop
                )
                // Expiry time overlay.
                Surface(
                    modifier = Modifier.padding(12.dp).align(Alignment.TopEnd),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = CircleShape
                ) {
                    Text(
                        text = "⏳ Exp: ${post.expiryTime}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Button to view Donor info.
                IconButton(
                    onClick = onInfo,
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                        .background(Color.White.copy(alpha = 0.8f), CircleShape)
                        .size(36.dp)
                ) {
                    Icon(Icons.Default.ContactPage, contentDescription = "Contact Donor", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }
            // Food details and Claim button.
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = post.foodName, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Text(text = post.location, fontSize = 14.sp, color = Color.DarkGray)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("QUANTITY", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(text = post.quantity, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Button(onClick = onClaim, shape = RoundedCornerShape(12.dp)) {
                        Text("CLAIM NOW")
                    }
                }
            }
        }
    }
}

/**
 * A simpler card representing a request already made.
 * Shows the status of the request and provides chat access.
 */
@Composable
fun ReceiverRequestCard(request: DonationRequest, onDelete: () -> Unit, onContact: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Post ID: #${request.postId}", fontWeight = FontWeight.Bold, color = Color.Black)
                CommonStatusChip(request.status) // Reusable chip for showing PENDING/APPROVED status.
            }
            
            Row {
                // Button to talk to the donor about the pickup.
                IconButton(onClick = onContact) {
                    Icon(Icons.Outlined.Chat, contentDescription = "Chat with Donor", tint = MaterialTheme.colorScheme.primary)
                }
                // Allow deletion only if the request hasn't been handled yet.
                if (request.status == "PENDING") {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove Request", tint = Color.Red)
                    }
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Finalized", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}
