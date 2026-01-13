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
import com.example.foodlink.data.model.Message
import com.example.foodlink.data.model.User
import com.example.foodlink.ui.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * The ChatScreen allows two users (e.g., a Donor and a Receiver) to message each other.
 * It shows the conversation history and provides a text field to send new messages.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentUserId: Int,         // The ID of the person currently using the phone.
    otherUserId: Int,           // The ID of the person they are talking to.
    onBack: () -> Unit,          // Function to go back to the previous screen.
    viewModel: FoodViewModel = viewModel() // Access to messaging logic in the ViewModel.
) {
    val context = LocalContext.current
    
    // Live stream of messages from the database between these two users.
    val messages by viewModel.getMessages(currentUserId, otherUserId).collectAsState(initial = emptyList())
    
    // State variables to store user details and the text being typed.
    var otherUser by remember { mutableStateOf<User?>(null) }
    var currentUser by remember { mutableStateOf<User?>(null) }
    var messageText by remember { mutableStateOf("") }
    var showDeleteConfirmation by remember { mutableStateOf(false) } // For clearing chat history.

    // Load user names and details when the screen first opens.
    LaunchedEffect(otherUserId) {
        otherUser = viewModel.getUserById(otherUserId)
        currentUser = viewModel.getUserById(currentUserId)
    }

    // Messaging is disabled if either person is blocked by the Admin.
    val isBlocked = otherUser?.isBlocked == true || currentUser?.isBlocked == true

    Scaffold(
        topBar = {
            // Header showing the name of the person we are chatting with.
            TopAppBar(
                title = {
                    Column {
                        Text(otherUser?.name ?: "Chat", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (otherUser?.isBlocked == true) {
                            Text("Account Blocked", color = Color.Red, fontSize = 10.sp)
                        } else {
                            Text("Online", color = Color(0xFF4CAF50), fontSize = 10.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Option to delete the entire conversation.
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Delete Conversation", tint = Color.Gray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            // The message input area at the bottom.
            if (!isBlocked) {
                Surface(tonalElevation = 8.dp, color = Color.White) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .navigationBarsPadding()
                            .imePadding(), // Adjusts UI when the keyboard pops up.
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Type a message...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // The Send Button.
                        FloatingActionButton(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    // Save the message to the database.
                                    viewModel.sendMessage(currentUserId, otherUserId, messageText)
                                    messageText = "" // Clear field after sending.
                                }
                            },
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                        }
                    }
                }
            } else {
                // Show a red warning if messaging is disabled.
                Surface(
                    color = Color.Red.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding()
                ) {
                    Text(
                        text = "Messaging disabled. One of the accounts is blocked.",
                        modifier = Modifier.padding(16.dp),
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    ) { padding ->
        // The main chat area where bubbles are displayed.
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF5F5F5))) {
            if (messages.isEmpty()) {
                // Show placeholder if no messages exist yet.
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp), 
                        tint = Color.LightGray
                    )
                    Text("No messages yet. Say hello!", color = Color.Gray)
                }
            } else {
                // Display the list of message bubbles.
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages) { message ->
                        // Check if I sent the message or the other person sent it.
                        val isMe = message.senderId == currentUserId
                        ChatBubble(message, isMe)
                    }
                }
            }
        }
    }

    // Confirmation popup for deleting conversation.
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Conversation?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to clear all messages with ${otherUser?.name}?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteConversation(currentUserId, otherUserId)
                    showDeleteConfirmation = false
                    Toast.makeText(context, "Conversation Cleared", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * A UI component representing a single message bubble.
 * 'isMe' determines if it appears on the right (blue) or left (white).
 */
@Composable
fun ChatBubble(message: Message, isMe: Boolean) {
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (isMe) MaterialTheme.colorScheme.primary else Color.White
    val contentColor = if (isMe) Color.White else Color.Black
    // Different rounded corners for bubbles based on who sent it.
    val shape = if (isMe) {
        RoundedCornerShape(16.dp, 16.dp, 0.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 0.dp)
    }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Surface(
                color = bgColor,
                shape = shape,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp),
                    color = contentColor,
                    fontSize = 15.sp
                )
            }
            // Show the time the message was sent at the bottom of the bubble.
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
