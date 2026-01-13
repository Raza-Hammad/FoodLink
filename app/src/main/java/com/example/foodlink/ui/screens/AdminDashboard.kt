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
import com.example.foodlink.data.model.User
import com.example.foodlink.ui.viewmodel.FoodViewModel

/**
 * The Admin Dashboard is the control center for the app administrator.
 * It allows the admin to verify new users and block/unblock existing ones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboard(
    onLogout: () -> Unit,      // Function to sign out and go back to Login.
    viewModel: FoodViewModel = viewModel() // Accessing our database and logic.
) {
    val context = LocalContext.current
    
    // Getting live updates of users from the database.
    val pendingUsers by viewModel.getPendingUsers().collectAsState(initial = emptyList())
    val allUsers by viewModel.getAllUsers().collectAsState(initial = emptyList())
    
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var selectedAdminTab by remember { mutableStateOf(0) } // 0 for Approvals, 1 for Management.

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text("Admin Console", fontWeight = FontWeight.Black, fontSize = 28.sp)
                        Text("Ecosystem Management", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                },
                actions = {
                    // Logout button in the top corner.
                    IconButton(onClick = { showLogoutConfirmation = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Stats Section: Shows counts of verified vs pending users.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Statistics cards for quick overview.
                AdminStatCard("Verified", "${allUsers.size - pendingUsers.size}", Icons.Default.VerifiedUser, Modifier.weight(1f), MaterialTheme.colorScheme.primary)
                AdminStatCard("Pending", "${pendingUsers.size}", Icons.Default.HourglassEmpty, Modifier.weight(1f), MaterialTheme.colorScheme.tertiary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TabRow allows switching between viewing "New Approvals" and "Manage All Users".
            TabRow(
                selectedTabIndex = selectedAdminTab,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedAdminTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                Tab(
                    selected = selectedAdminTab == 0, 
                    onClick = { selectedAdminTab = 0 }, 
                    text = { Text("APPROVALS", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedAdminTab == 1, 
                    onClick = { selectedAdminTab = 1 }, 
                    text = { Text("MANAGEMENT", fontWeight = FontWeight.Bold) }
                )
            }

            // Decide which list to show based on the selected tab.
            val displayUsers = if (selectedAdminTab == 0) pendingUsers else allUsers

            if (displayUsers.isEmpty()) {
                // If no users are found in the database.
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Inbox, 
                            contentDescription = null, 
                            modifier = Modifier.size(64.dp), 
                            tint = Color.LightGray
                        )
                        Text("No users here", color = Color.Gray)
                    }
                }
            } else {
                // Scrollable list of user cards.
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(displayUsers, key = { it.id }) { user ->
                        AdminUserControlCard(
                            user = user,
                            isPending = selectedAdminTab == 0,
                            onApprove = { viewModel.approveUser(user) },
                            onToggleBlock = { viewModel.toggleBlockUser(user) }
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog before logging out.
    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            title = { Text("Logout Admin?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to end your administration session?") },
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
 * A specific card component representing a single user.
 * Displays user info and buttons to Verify or Block them.
 */
@Composable
fun AdminUserControlCard(user: User, isPending: Boolean, onApprove: () -> Unit, onToggleBlock: () -> Unit) {
    var showActionConfirmation by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile icon placeholder.
            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = if (user.isBlocked) Color.Red.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Person, 
                        contentDescription = null, 
                        modifier = Modifier.size(28.dp),
                        tint = if (user.isBlocked) Color.Red else MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User name, email, and role badge.
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.Black)
                Text(text = user.email, fontSize = 12.sp, color = Color.Gray)
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = user.role, 
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Control buttons (Checkmark for verify, Lock for block).
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isPending) {
                    IconButton(
                        onClick = { showActionConfirmation = "APPROVE" },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).size(36.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Approve", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                IconButton(onClick = { showActionConfirmation = "BLOCK" }) {
                    Icon(
                        if (user.isBlocked) Icons.Default.LockOpen else Icons.Default.Block, 
                        contentDescription = "Block", 
                        tint = if (user.isBlocked) Color.Green else Color.Red
                    )
                }
            }
        }
    }

    // Confirmation popup for sensitive actions like blocking a user.
    if (showActionConfirmation != null) {
        val title = if (showActionConfirmation == "APPROVE") "Verify User?" else if (user.isBlocked) "Unblock User?" else "Restrict User?"
        val msg = if (showActionConfirmation == "APPROVE") "This will allow ${user.name} to access their dashboard." else "Are you sure you want to change access for ${user.name}?"
        
        AlertDialog(
            onDismissRequest = { showActionConfirmation = null },
            title = { Text(title, fontWeight = FontWeight.Bold) },
            text = { Text(msg) },
            confirmButton = {
                Button(onClick = {
                    if (showActionConfirmation == "APPROVE") onApprove() else onToggleBlock()
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

/**
 * A reusable small card showing statistics like "Total Users" or "Verified".
 */
@Composable
fun AdminStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, fontSize = 28.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = label, fontSize = 12.sp, color = color.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
        }
    }
}
