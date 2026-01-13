package com.example.foodlink.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * The SplashScreen is the very first screen shown when the app opens.
 * It displays a cool animation to build the brand and then moves to the Login screen.
 */
@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // This scope allows us to run animations and delays in the background.
    val scope = rememberCoroutineScope()
    
    // Animation States: These variables control how things move on the screen.
    val donorOffsetX = remember { Animatable(-300f) } // Starts from the left side.
    val receiverOffsetX = remember { Animatable(300f) } // Starts from the right side.
    val mergedScale = remember { Animatable(0f) } // Starts hidden (size 0).
    val contentAlpha = remember { Animatable(0f) } // Starts invisible.
    val pulseScale = remember { Animatable(1f) } // For the heart-beat effect.

    // LaunchedEffect runs as soon as the screen is displayed.
    LaunchedEffect(Unit) {
        // Step 1: Donor and Receiver icons slide in from opposite sides and meet in the center.
        launch {
            donorOffsetX.animateTo(0f, tween(1000, easing = FastOutSlowInEasing))
        }
        launch {
            receiverOffsetX.animateTo(0f, tween(1000, easing = FastOutSlowInEasing))
        }
        
        // Wait a bit before starting the next part of the animation.
        delay(1100)
        
        // Step 2: Once they meet, they merge and transform into a big Heart.
        launch {
            mergedScale.animateTo(1.5f, tween(400, easing = LinearOutSlowInEasing))
            mergedScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
        }
        
        // Step 3: Make the "FoodLink" text and tagline fade into view.
        launch {
            contentAlpha.animateTo(1f, tween(1000))
        }
        
        // Step 4: Make the heart "pulse" like it's beating forever.
        launch {
            pulseScale.animateTo(1.1f, infiniteRepeatable(tween(800), RepeatMode.Reverse))
        }

        // Keep the splash screen visible for 2 seconds total, then move to the next screen.
        delay(2000)
        onTimeout() // This triggers 'navController.navigate(Screen.Login.route)' in MainActivity.
    }

    // The main UI layout for the Splash Screen.
    Box(
        modifier = Modifier
            .fillMaxSize()
            // Gradient background from dark primary to secondary color.
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        MaterialTheme.colorScheme.secondary
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // A soft white glow in the background for a premium look.
        Box(
            modifier = Modifier
                .size(400.dp)
                .alpha(0.2f)
                .blur(60.dp)
                .background(Color.White, CircleShape)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // If the icons have merged, show the white heart circle.
                if (mergedScale.value > 0.1f) {
                    Surface(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(mergedScale.value * pulseScale.value),
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 20.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(70.dp),
                                tint = MaterialTheme.colorScheme.tertiary // The Orange brand color.
                            )
                        }
                    }
                }

                // Show the "Donor" (Give) icon while it is sliding in.
                if (mergedScale.value < 0.5f) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .offset(x = donorOffsetX.value.dp),
                        tint = Color.White
                    )
                }

                // Show the "Receiver" (Handshake) icon while it is sliding in.
                if (mergedScale.value < 0.5f) {
                    Icon(
                        imageVector = Icons.Default.Handshake,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .offset(x = receiverOffsetX.value.dp),
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Text Branding: "FoodLink" and its slogan.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(contentAlpha.value)
            ) {
                Text(
                    text = "FoodLink",
                    color = Color.White,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp
                )
                
                Text(
                    text = "BRIDGE TO BETTER LIVES",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp
                )

                Spacer(modifier = Modifier.height(60.dp))

                // A simple minimalist loading progress bar at the bottom.
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    val progress = remember { Animatable(0f) }
                    LaunchedEffect(Unit) {
                        progress.animateTo(1f, tween(2500)) // Fills the bar in 2.5 seconds.
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.value)
                            .background(Color.White, CircleShape)
                    )
                }
            }
        }
    }
}
