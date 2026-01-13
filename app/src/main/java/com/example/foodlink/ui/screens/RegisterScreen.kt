package com.example.foodlink.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodlink.data.model.User
import com.example.foodlink.ui.viewmodel.FoodViewModel
import com.example.foodlink.utils.NotificationHelper
import kotlinx.coroutines.launch

/**
 * This screen allows new users (Donors or Receivers) to create an account.
 * It includes input validation, password matching, and a simulated OTP verification.
 */
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,      // Callback for when registration finishes.
    onNavigateToLogin: () -> Unit,      // Function to go back to the Login screen.
    viewModel: FoodViewModel = viewModel() // App logic reference.
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // State variables for the input fields.
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var retypePassword by remember { mutableStateOf("") }
    
    // Controls whether passwords are shown or hidden with dots.
    var passwordVisible by remember { mutableStateOf(false) }
    var retypePasswordVisible by remember { mutableStateOf(false) }
    
    // Default role is DONOR. Users can choose between Donor and Receiver.
    var selectedRole by remember { mutableStateOf("DONOR") }
    
    // OTP (One-Time Password) logic states.
    var showOtpDialog by remember { mutableStateOf(false) }
    var enteredOtp by remember { mutableStateOf("") }
    var currentGeneratedOtp by remember { mutableStateOf("") }

    // Logic to check if both password fields match.
    val passwordsMatch = password.isNotEmpty() && password == retypePassword
    val passwordStatusColor = if (passwordsMatch) Color(0xFF2E7D32) else Color(0xFFC62828)
    val passwordStatusText = if (retypePassword.isEmpty()) "" else if (passwordsMatch) "Passwords Match" else "Passwords Do Not Match"

    // This handles asking the user for permission to show Notifications.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    // Ask for Notification permission when this screen first opens (for Android 13+).
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Main layout container with a subtle background gradient.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // App Icon Logo (Shaking hands represents community).
            Surface(
                modifier = Modifier.size(90.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🤝", fontSize = 44.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Create Account",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Join the community of surplus sharing",
                fontSize = 15.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(40.dp))

            // Username Input Field.
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Input Field.
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Input Field.
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(image, null)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Retype Password Input Field (to ensure user typed it correctly).
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = retypePassword,
                    onValueChange = { retypePassword = it },
                    label = { Text("Retype Password") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    trailingIcon = {
                        val image = if (retypePasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { retypePasswordVisible = !retypePasswordVisible }) {
                            Icon(image, null)
                        }
                    },
                    visualTransformation = if (retypePasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    isError = retypePassword.isNotEmpty() && !passwordsMatch
                )
                // Helpful text showing if passwords match or not.
                if (passwordStatusText.isNotEmpty()) {
                    Text(
                        text = passwordStatusText,
                        color = passwordStatusColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Radio Buttons to choose the User Role.
            Text(
                "Register as a:",
                modifier = Modifier.align(Alignment.Start),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.Start
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedRole == "DONOR",
                        onClick = { selectedRole = "DONOR" }
                    )
                    Text("Donor", fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                }
                Spacer(modifier = Modifier.width(32.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = selectedRole == "RECEIVER",
                        onClick = { selectedRole = "RECEIVER" }
                    )
                    Text("Receiver", fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Register Button with validation checks.
            Button(
                onClick = { 
                    scope.launch {
                        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
                        } else if (!email.endsWith("@gmail.com")) {
                            Toast.makeText(context, "Must be a valid @gmail.com", Toast.LENGTH_SHORT).show()
                        } else if (password.length < 8) {
                            Toast.makeText(context, "Password too weak (min 8 chars)", Toast.LENGTH_SHORT).show()
                        } else if (!passwordsMatch) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        } else if (viewModel.isUsernameTaken(username)) {
                            Toast.makeText(context, "Username already taken!", Toast.LENGTH_LONG).show()
                        } else {
                            // Step 1: Generate a random OTP code.
                            currentGeneratedOtp = viewModel.generateOtp()
                            // Step 2: Show the OTP in a system notification (simulating an email).
                            NotificationHelper.showOtpNotification(context, currentGeneratedOtp)
                            // Step 3: Show the popup to enter that code.
                            showOtpDialog = true
                            Toast.makeText(context, "Verification code sent to $email (Check Notifications)", Toast.LENGTH_LONG).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(18.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text("Verify & Register", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Link back to Login screen if user already has an account.
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = "Already have an account? Sign In",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // OTP Verification Popup Dialog.
    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false },
            title = { Text("Email Verification", fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text("We've sent a code to $email. Please enter it below to verify your account.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = { if (it.length <= 6) enteredOtp = it },
                        label = { Text("Enter OTP") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Check if the code typed matches the one we generated.
                        if (enteredOtp == currentGeneratedOtp) {
                            scope.launch {
                                // Save the new user to the local database.
                                val result = viewModel.registerUser(
                                    User(name = username, email = email, password = password, role = selectedRole)
                                )
                                if (result.isSuccess) {
                                    showOtpDialog = false
                                    Toast.makeText(context, "Registration Success! Admin must approve your account.", Toast.LENGTH_LONG).show()
                                    onNavigateToLogin() // Go to Login screen after success.
                                } else {
                                    Toast.makeText(context, result.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Invalid Verification Code", Toast.LENGTH_SHORT).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Verify Account")
                }
            },
            dismissButton = {
                // Allows user to close the popup and correct their info.
                TextButton(onClick = { showOtpDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }
}
