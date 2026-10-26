package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CipheraLogo
import com.example.ui.components.PasswordStrengthMeter
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun AuthGateScreen(
    hasStoredVault: Boolean,
    isCryptoBusy: Boolean,
    cryptoStatusMessage: String,
    errorMessage: String?,
    isBiometricAvailable: Boolean,
    isBiometricEnabled: Boolean,
    onUnlockWithPassword: (password: String) -> Unit,
    onUnlockWithBiometrics: () -> Unit,
    onCreateVault: (password: String, enableBiometrics: Boolean) -> Unit,
    onImportFileClicked: () -> Unit,
    onResetVault: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember(hasStoredVault) {
        mutableStateOf(if (hasStoredVault) "unlock" else "create")
    }

    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var enableBiometricsChecked by remember { mutableStateOf(isBiometricAvailable) }
    var localValidationErr by remember { mutableStateOf<String?>(null) }
    var showResetConfirm by remember { mutableStateOf(false) }

    val gradientBrush = Brush.horizontalGradient(listOf(Emerald400, Cyan400))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Slate950)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo & Header
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Slate900)
                    .border(1.dp, Slate800, RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                CipheraLogo(size = 56.dp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CIPHERA",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            Text(
                text = "Zero-Knowledge Offline Cryptographic Vault",
                fontSize = 12.sp,
                color = Slate400,
                modifier = Modifier.padding(top = 2.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Emerald400)
                )
                Text(
                    text = "100% Client-Side • PBKDF2-600K • AES-256-GCM",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Emerald400
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Auth Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Slate900)
                    .border(1.dp, Slate800, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Mode Switcher Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate950)
                            .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (hasStoredVault) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (mode == "unlock") Emerald400.copy(alpha = 0.15f) else Color.Transparent)
                                    .border(
                                        1.dp,
                                        if (mode == "unlock") Emerald400.copy(alpha = 0.4f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        mode = "unlock"
                                        localValidationErr = null
                                    }
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = if (mode == "unlock") Emerald400 else Slate400,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Unlock",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (mode == "unlock") Emerald400 else Slate400
                                    )
                                }
                            }
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (mode == "create") Emerald400.copy(alpha = 0.15f) else Color.Transparent)
                                .border(
                                    1.dp,
                                    if (mode == "create") Emerald400.copy(alpha = 0.4f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    mode = "create"
                                    localValidationErr = null
                                }
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = if (mode == "create") Emerald400 else Slate400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Brankas Baru",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (mode == "create") Emerald400 else Slate400
                                )
                            }
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Slate950)
                                .clickable { onImportFileClicked() }
                                .padding(vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Upload,
                                    contentDescription = null,
                                    tint = Cyan400,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Impor SAF",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Cyan400
                                )
                            }
                        }
                    }

                    // Error Box
                    val activeError = localValidationErr ?: errorMessage
                    if (activeError != null) {
                        Surface(
                            color = Color(0xFF3B1219),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Rose400.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Rose400,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = activeError,
                                    color = Rose400,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Mode: UNLOCK
                    if (mode == "unlock") {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Master Password") },
                                placeholder = { Text("Masukkan Master Password CIPHERA...") },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done,
                                    autoCorrectEnabled = false
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        if (passwordInput.isNotBlank()) onUnlockWithPassword(passwordInput)
                                    }
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility",
                                            tint = Slate400
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Emerald400,
                                    unfocusedBorderColor = Slate800,
                                    focusedContainerColor = Slate950,
                                    unfocusedContainerColor = Slate950,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("master_password_input")
                            )

                            Button(
                                onClick = {
                                    if (passwordInput.isBlank()) {
                                        localValidationErr = "Silakan masukkan Master Password."
                                    } else {
                                        localValidationErr = null
                                        onUnlockWithPassword(passwordInput)
                                    }
                                },
                                enabled = !isCryptoBusy && passwordInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald400, contentColor = Slate950),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("unlock_button")
                            ) {
                                if (isCryptoBusy) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Slate950,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cryptoStatusMessage.ifEmpty { "Memproses Kriptografi..." },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Buka Brankas CIPHERA",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            // Biometric Unlock Button
                            if (isBiometricAvailable && isBiometricEnabled) {
                                Button(
                                    onClick = onUnlockWithBiometrics,
                                    enabled = !isCryptoBusy,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Cyan400.copy(alpha = 0.15f),
                                        contentColor = Cyan400
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Cyan400.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("biometric_unlock_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Buka dengan Biometrik (Fingerprint)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Mode: CREATE VAULT
                    if (mode == "create") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (hasStoredVault) {
                                Surface(
                                    color = Color(0xFF332005),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Amber400.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "Perhatian: Membuat brankas baru akan menimpa brankas lokal saat ini. Pastikan file .ciphera lama sudah diekspor jika masih diperlukan.",
                                        color = Amber400,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Buat Master Password") },
                                placeholder = { Text("Minimal 8 karakter kuat...") },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next,
                                    autoCorrectEnabled = false
                                ),
                                trailingIcon = {
                                    IconButton(onClick = { showPassword = !showPassword }) {
                                        Icon(
                                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = "Toggle password visibility",
                                            tint = Slate400
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Emerald400,
                                    unfocusedBorderColor = Slate800,
                                    focusedContainerColor = Slate950,
                                    unfocusedContainerColor = Slate950,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            PasswordStrengthMeter(password = passwordInput)

                            OutlinedTextField(
                                value = confirmPasswordInput,
                                onValueChange = { confirmPasswordInput = it },
                                label = { Text("Konfirmasi Master Password") },
                                placeholder = { Text("Ulangi Master Password...") },
                                singleLine = true,
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done,
                                    autoCorrectEnabled = false
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Emerald400,
                                    unfocusedBorderColor = Slate800,
                                    focusedContainerColor = Slate950,
                                    unfocusedContainerColor = Slate950,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (isBiometricAvailable) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.clickable { enableBiometricsChecked = !enableBiometricsChecked }
                                ) {
                                    Checkbox(
                                        checked = enableBiometricsChecked,
                                        onCheckedChange = { enableBiometricsChecked = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Emerald400,
                                            checkmarkColor = Slate950
                                        )
                                    )
                                    Text(
                                        text = "Aktifkan Biometric Unlock (Fingerprint)",
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    when {
                                        passwordInput.length < 8 -> {
                                            localValidationErr = "Master Password minimal harus 8 karakter."
                                        }
                                        passwordInput != confirmPasswordInput -> {
                                            localValidationErr = "Konfirmasi Master Password tidak cocok."
                                        }
                                        else -> {
                                            localValidationErr = null
                                            onCreateVault(passwordInput, enableBiometricsChecked)
                                        }
                                    }
                                },
                                enabled = !isCryptoBusy && passwordInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald400, contentColor = Slate950),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("create_vault_button")
                            ) {
                                if (isCryptoBusy) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Slate950,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = cryptoStatusMessage.ifEmpty { "Mengenkripsi..." },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Inisialisasi Brankas CIPHERA",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    // Reset Vault option if existing vault
                    if (hasStoredVault) {
                        AnimatedVisibility(visible = !showResetConfirm) {
                            Text(
                                text = "Lupa Password? Hapus/Reset Brankas Lokal",
                                fontSize = 11.sp,
                                color = Slate400,
                                modifier = Modifier
                                    .clickable { showResetConfirm = true }
                                    .padding(top = 4.dp)
                            )
                        }

                        AnimatedVisibility(visible = showResetConfirm) {
                            Surface(
                                color = Color(0xFF3B1219),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Rose400.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Apakah Anda yakin ingin mereset brankas? Semua data lokal yang belum diekspor akan terhapus.",
                                        fontSize = 11.sp,
                                        color = Rose400
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                showResetConfirm = false
                                                onResetVault()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Rose400, contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Ya, Reset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { showResetConfirm = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Batal", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Cryptographic Guarantees
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Slate600,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Zero-Knowledge: Kunci master tidak pernah disimpan di server",
                    fontSize = 11.sp,
                    color = Slate600
                )
            }
        }
    }
}
