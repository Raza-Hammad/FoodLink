package com.example.foodlink.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodlink.ui.viewmodel.FoodViewModel
import kotlinx.coroutines.launch

/**
 * This screen allows existing users to log into the app.
 * It handles email/password input and checks if the user is verified or blocked.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: (String, Int) -> Unit, // Callback function to run after successful login.
    onNavigateToRegister: () -> Unit,      // Function to move to the Registration screen.
    viewModel: FoodViewModel = viewModel() // Accessing our app logic.
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope() // Used for running background tasks like database lookups.
    
    // State variables to remember what the user is typing.
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // Toggles showing/hiding password characters.
    var showBlockedDialog by remember { mutableStateOf(false) } // Shows a popup if the user is banned.

    // Main layout container with a nice gradient background.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Scrollable column so the UI doesn't break on small screens.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo Icon (Apple represents food/health).
            Surface(
                modifier = Modifier.size(100.dp),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🍎", fontSize = 50.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main welcome headings.
            Text(
                text = "Welcome Back",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = (-1).sp
            )
            
            Text(
                text = "Sign in to continue your impact",
                fontSize = 16.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            // Email Input Field.
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Password Input Field with visibility toggle.
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, contentDescription = null)
                    }
                },
                // Hides text with dots if visibility is off.
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Login Button.
            Button(
                onClick = { 
                    // basic validation to ensure fields aren't empty.
                    if (email.isEmpty() || password.isEmpty()) {
                        Toast.makeText(context, "Please enter all details", Toast.LENGTH_SHORT).show()
                    } else {
                        // Start the login process in a background coroutine.
                        scope.launch {
                            val result = viewModel.loginUser(email, password)
                            if (result.isSuccess) {
                                val user = result.getOrNull()!!
                                // Logic: If not Admin, must be verified by Admin first.
                                if (user.role != "ADMIN" && !user.isVerified) {
                                    Toast.makeText(context, "Waiting for Admin Approval...", Toast.LENGTH_LONG).show()
                                } else {
                                    // Navigate to the dashboard.
                                    onLoginSuccess(user.role, user.id)
                                    Toast.makeText(context, "Welcome, ${user.name}!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Handle specific errors like 'Blocked' or 'Incorrect Password'.
                                val errorMsg = result.exceptionOrNull()?.message ?: "Login failed"
                                if (errorMsg == "BLOCKED_BY_ADMIN") {
                                    showBlockedDialog = true
                                } else {
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(18.dp)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text("Sign In", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Link to Register Screen.
            TextButton(onClick = onNavigateToRegister) {
                Text(
                    text = "New to FoodLink? Create Account",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }

    // Popup alert shown only if the Admin has blocked this user account.
    if (showBlockedDialog) {
        AlertDialog(
            onDismissRequest = { showBlockedDialog = false },
            title = { Text("Account Restricted", color = Color.Red, fontWeight = FontWeight.Black) },
            text = { Text("Your account has been blocked by the FoodLink administration for policy violations.") },
            confirmButton = {
                Button(
                    onClick = { showBlockedDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Close", color = Color.White)
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
