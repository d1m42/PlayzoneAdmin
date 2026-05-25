package com.playzone.admin.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.playzone.admin.R
import com.playzone.admin.ui.theme.*
import com.playzone.admin.ui.viewmodel.AdminAuthViewModel

@Composable
fun AdminLoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AdminAuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var navigated by remember { mutableStateOf(false) }
    var notAdminError by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedIn, userProfile) {
        if (isLoggedIn && userProfile != null && !navigated) {
            if (userProfile?.role == "admin") {
                navigated = true
                onLoginSuccess()
            } else {
                notAdminError = true
                viewModel.logout()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBackground, SurfaceDark)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo Playzone PNG ──────────────────────────────────────
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.logo_playzone)
                    .crossfade(true)
                    .build(),
                contentDescription = "Playzone Logo",
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(20.dp))
            Text("Playzone Admin", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Portal Kasir & Manajemen", fontSize = 13.sp, color = TextSecondary)
            Spacer(Modifier.height(40.dp))

            // Email
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email Admin") },
                leadingIcon = { Icon(Icons.Default.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpleLight, focusedLabelColor = PurpleLight,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                )
            )
            Spacer(Modifier.height(12.dp))

            // Password
            OutlinedTextField(
                value = password, onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            null
                        )
                    }
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PurpleLight, focusedLabelColor = PurpleLight,
                    focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary
                )
            )
            Spacer(Modifier.height(8.dp))

            // Error
            AnimatedVisibility(visible = uiState.error != null || notAdminError) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedCancel.copy(0.1f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = RedCancel, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (notAdminError)
                                "Akun ini bukan admin. Hubungi pengelola."
                            else uiState.error ?: "",
                            color = RedCancel, fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = { notAdminError = false; viewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = email.isNotBlank() && password.isNotBlank() && !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = TextPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Login, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Masuk sebagai Admin", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))

            Card(colors = CardDefaults.cardColors(containerColor = CardDark), shape = RoundedCornerShape(10.dp)) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = BlueInfo, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Hanya akun dengan role admin yang bisa masuk", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}
