package com.example.foodlink.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.foodlink.data.model.FoodPost
import com.example.foodlink.ui.viewmodel.FoodViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * This screen allows Donors to create a new food listing.
 * It includes a form for food name, quantity, location, image, and expiry details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    userId: Int,                 // The ID of the donor currently logged in.
    onBack: () -> Unit,          // Function to go back to the previous screen.
    onSuccess: () -> Unit,       // Function to run after successful posting.
    viewModel: FoodViewModel = viewModel() // Access to app logic.
) {
    val context = LocalContext.current
    
    // State variables for the form fields.
    var foodName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) } // Holds the selected photo URI.

    // Expiry Logic: Donors can set expiry using a countdown (Time) or a specific date.
    var expiryType by remember { mutableStateOf("Time") } 
    var timeValue by remember { mutableStateOf("") }
    var timeUnit by remember { mutableStateOf("hrs") } // Units like minutes, hours, or days.
    
    // Date Picker state for selecting a specific calendar date.
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    var showDatePicker by remember { mutableStateOf(false) }

    // Launcher to open the phone's gallery to pick an image.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    Scaffold(
        topBar = {
            // Standard Top Bar with a title and back button.
            TopAppBar(
                title = { Text("Post Surplus Food", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "What are you donating today?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )

            // Image Selection Area.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    .clickable { launcher.launch("image/*") }, // Clicking opens the gallery.
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    // Show the selected image if one exists.
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Show a "Add Photo" placeholder if no image is selected yet.
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Color.White)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Add Food Photo", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Food Name Field.
            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("Food Item Name") },
                placeholder = { Text("e.g. 5 Boxes of Pizza") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            // Quantity Field.
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            // Expiry Section: Switching between "Duration" (Time) and "Date".
            Text("Set Expiry By:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = expiryType == "Time",
                    onClick = { expiryType = "Time" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { Icon(Icons.Default.Timer, null) }
                ) {
                    Text("Duration")
                }
                SegmentedButton(
                    selected = expiryType == "Date",
                    onClick = { expiryType = "Date" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(Icons.Default.CalendarMonth, null) }
                ) {
                    Text("Date")
                }
            }

            // UI changes based on if user selected "Time" or "Date".
            if (expiryType == "Time") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Number input for duration.
                    OutlinedTextField(
                        value = timeValue,
                        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) timeValue = it },
                        label = { Text("Value") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    // Dropdown for picking hrs/mins/days.
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = timeUnit,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            listOf("min", "hrs", "days").forEach { unit ->
                                DropdownMenuItem(
                                    text = { Text(unit) },
                                    onClick = {
                                        timeUnit = unit
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                // Clicking this field opens the Date Picker dialog.
                OutlinedTextField(
                    value = datePickerState.selectedDateMillis?.let {
                        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(it))
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Select Expiry Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        }
                    },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
            }

            // Location Input Field.
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Pickup Location") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            // Info tip for the user.
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Accurate expiry selection helps receivers pick up food on time.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main "Post Donation" Button.
            Button(
                onClick = {
                    // Combine time value and unit into a single string for storage.
                    val finalExpiry = if (expiryType == "Time") {
                        if (timeValue.isNotEmpty()) "$timeValue $timeUnit" else ""
                    } else {
                        datePickerState.selectedDateMillis?.let {
                            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(it))
                        } ?: ""
                    }

                    // Ensure required fields are filled before saving.
                    if (foodName.isNotEmpty() && quantity.isNotEmpty() && finalExpiry.isNotEmpty()) {
                        viewModel.addFoodPost(
                            FoodPost(
                                donorId = userId,
                                foodName = foodName,
                                quantity = quantity,
                                expiryTime = finalExpiry,
                                location = location,
                                imageUrl = imageUri?.toString(),
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        Toast.makeText(context, "Food Posted Successfully!", Toast.LENGTH_LONG).show()
                        onSuccess() // Triggers navigation back to the dashboard.
                    } else {
                        Toast.makeText(context, "Please fill all details including expiry", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("Post Donation", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    // The actual Date Picker Dialog component.
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
