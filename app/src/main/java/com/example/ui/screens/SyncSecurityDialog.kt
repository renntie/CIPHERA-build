package com.example.ui.screens

import android.util.Base64
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CipheraContainer
import com.example.ui.components.PasswordStrengthMeter
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun SyncSecurityDialog(
    storedContainer: CipheraContainer,
    isCryptoBusy: Boolean,
    onClose: () -> Unit,
    onExportFileClicked: () -> Unit,
    onPickIncomingFileClicked: () -> Unit,
    incomingContainer: CipheraContainer?,
    onClearIncomingContainer: () -> Unit,
    onExecuteSmartMerge: (incomingContainer: CipheraContainer, incomingPassword: String) -> Unit,
    onChangeMasterPassword: (newPassword: String) -> Unit,
    onCopy: (text: String, label: String) -> Unit
) {
    var activeTab by remember { mutableStateOf("sync") }

    // Smart Merge
    var incomingPassword by remember { mutableStateOf("") }
    var mergeError by remember { mutableStateOf<String?>(null) }

    // Quick-Share
    var pastedBlob by remember { mutableStateOf("") }
    var parsedBlobContainer by remember { mutableStateOf<CipheraContainer?>(null) }
    var blobError by remember { mutableStateOf<String?>(null) }

    // Change Password
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmNewPasswordInput by remember { mutableStateOf("") }
    var passwordChangeError by remember { mutableStateOf<String?>(null) }

    val base64Blob = remember(storedContainer) {
        val jsonBytes = storedContainer.toJsonString().toByteArray(Charsets.UTF_8)
        Base64.encodeToString(jsonBytes, Base64.NO_WRAP)
    }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .background(Slate900)
                .border(1.dp, Slate800, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("sync_security_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 680.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(22.dp)
                        )
                        Column {
                            Text(
                                text = "Pusat Sinkronisasi & Keamanan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Backup .ciphera, Smart Merge & Rotasi Kunci Master",
                                fontSize = 11.sp,
                                color = Slate400
                            )
                        }
                    }

                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = Slate400)
                    }
                }

                // Navigation Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Slate950)
                        .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeTab == "sync") Emerald400.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (activeTab == "sync") Emerald400 else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { activeTab = "sync" }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "File & Merge",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "sync") Emerald400 else Slate400
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeTab == "quickshare") Cyan400.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (activeTab == "quickshare") Cyan400 else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { activeTab = "quickshare" }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Quick-Share",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "quickshare") Cyan400 else Slate400
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeTab == "password") Amber400.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (activeTab == "password") Amber400 else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { activeTab = "password" }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Ubah Master PW",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "password") Amber400 else Slate400
                        )
                    }
                }

                // TAB 1: FILE .CIPHERA & SMART MERGE
                if (activeTab == "sync") {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Export Section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Slate950)
                                .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Emerald400, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Ekspor Cadangan Terenkripsi (.ciphera)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Text(
                                    text = "Simpan salinan brankas terenkripsi dengan AES-256-GCM + AAD ke folder unduhan atau cloud drive via Storage Access Framework.",
                                    fontSize = 11.sp,
                                    color = Slate400,
                                    lineHeight = 16.sp
                                )

                                Button(
                                    onClick = onExportFileClicked,
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald400, contentColor = Slate950),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("export_ciphera_button")
                                ) {
                                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Unduh File .ciphera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Smart Merge Section
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Slate950)
                                .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, tint = Cyan400, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Smart Merge (Sinkronisasi Tanpa Konflik)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Text(
                                    text = "Pilih file .ciphera dari perangkat lain. Algoritma Smart Merge akan menggabungkan item baru, menghormati item yang dihapus melalui Tombstones, dan menyimpan konflik versi ke Riwayat.",
                                    fontSize = 11.sp,
                                    color = Slate400,
                                    lineHeight = 16.sp
                                )

                                if (mergeError != null) {
                                    Text(text = mergeError ?: "", fontSize = 11.sp, color = Rose400)
                                }

                                // Incoming File Status Box / Picker Button
                                val activeIncoming = incomingContainer ?: parsedBlobContainer
                                if (activeIncoming == null) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Slate900)
                                            .border(1.dp, Cyan400.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                            .clickable { onPickIncomingFileClicked() }
                                            .padding(16.dp)
                                            .testTag("pick_incoming_file_button")
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Upload, contentDescription = null, tint = Cyan400, modifier = Modifier.size(24.dp))
                                            Text(
                                                text = "Pilih File .ciphera untuk Digabungkan",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Mendukung format kontainer resmi CIPHERA v1",
                                                fontSize = 10.sp,
                                                color = Slate500
                                            )
                                        }
                                    }
                                } else {
                                    Surface(
                                        color = Cyan950.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Cyan400.copy(alpha = 0.4f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "File Incoming Siap Digabungkan",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Cyan400
                                                )
                                                Text(
                                                    text = "Ganti File",
                                                    fontSize = 11.sp,
                                                    color = Slate400,
                                                    modifier = Modifier.clickable {
                                                        onClearIncomingContainer()
                                                        parsedBlobContainer = null
                                                    }
                                                )
                                            }

                                            OutlinedTextField(
                                                value = incomingPassword,
                                                onValueChange = { incomingPassword = it },
                                                label = { Text("Master Password File Incoming") },
                                                placeholder = { Text("Masukkan Master Password file tersebut...") },
                                                singleLine = true,
                                                visualTransformation = PasswordVisualTransformation(),
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = Cyan400,
                                                    unfocusedBorderColor = Slate800,
                                                    focusedContainerColor = Slate950,
                                                    unfocusedContainerColor = Slate950,
                                                    focusedTextColor = Color.White,
                                                    unfocusedTextColor = Color.White
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Button(
                                                onClick = {
                                                    if (incomingPassword.isBlank()) {
                                                        mergeError = "Masukkan Master Password file incoming."
                                                    } else {
                                                        mergeError = null
                                                        onExecuteSmartMerge(activeIncoming, incomingPassword)
                                                    }
                                                },
                                                enabled = !isCryptoBusy && incomingPassword.isNotBlank(),
                                                colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = Slate950),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().testTag("execute_merge_button")
                                            ) {
                                                if (isCryptoBusy) {
                                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Slate950, strokeWidth = 2.dp)
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Memproses Penggabungan...", fontSize = 12.sp)
                                                } else {
                                                    Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Jalankan Smart Merge Sekarang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // TAB 2: CIPHERTEXT QUICK-SHARE
                if (activeTab == "quickshare") {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Section 1: Copy My Blob
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Slate950)
                                .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "1. Salin Blob Terenkripsi Perangkat Ini",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Emerald950.copy(alpha = 0.5f))
                                            .border(1.dp, Emerald400.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                            .clickable { onCopy(base64Blob, "CipherText Base64 Blob") }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Emerald400, modifier = Modifier.size(12.dp))
                                            Text("Salin Blob", fontSize = 11.sp, color = Emerald400, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Text(
                                    text = "Kode Base64 ini berisi seluruh brankas terenkripsi AES-256-GCM. Anda dapat menyalin dan mengirimkannya melalui pesan teks atau catatan tanpa perlu memindahkan file.",
                                    fontSize = 11.sp,
                                    color = Slate400,
                                    lineHeight = 15.sp
                                )

                                Text(
                                    text = base64Blob.take(180) + "...",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Slate500,
                                    maxLines = 3,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Slate900, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                )
                            }
                        }

                        // Section 2: Paste Blob from other device
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Slate950)
                                .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "2. Tempel (Paste) Blob dari Perangkat Lain",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                if (blobError != null) {
                                    Text(text = blobError ?: "", fontSize = 11.sp, color = Rose400)
                                }

                                OutlinedTextField(
                                    value = pastedBlob,
                                    onValueChange = { pastedBlob = it },
                                    placeholder = { Text("Tempel teks Base64 CipherText di sini...", fontSize = 11.sp) },
                                    minLines = 3,
                                    maxLines = 5,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Cyan400,
                                        unfocusedBorderColor = Slate800,
                                        focusedContainerColor = Slate900,
                                        unfocusedContainerColor = Slate900,
                                        focusedTextColor = Cyan400,
                                        unfocusedTextColor = Cyan400
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        try {
                                            val decodedBytes = Base64.decode(pastedBlob.trim(), Base64.NO_WRAP)
                                            val jsonString = String(decodedBytes, Charsets.UTF_8)
                                            val container = CipheraContainer.fromJsonString(jsonString)
                                            parsedBlobContainer = container
                                            blobError = null
                                            activeTab = "sync"
                                        } catch (e: Exception) {
                                            blobError = "Format CipherText Blob tidak valid: ${e.message}"
                                        }
                                    },
                                    enabled = pastedBlob.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = Slate950),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Muat Blob ke Antrean Smart Merge", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // TAB 3: UBAH MASTER PASSWORD
                if (activeTab == "password") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Slate950)
                                .border(1.dp, Slate800, RoundedCornerShape(14.dp))
                                .padding(14.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.LockReset, contentDescription = null, tint = Amber400, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Rotasi Kunci Master & Enkripsi Ulang",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Text(
                                    text = "Mengganti Master Password akan menghasilkan Salt 16-byte acak baru, Nonce 12-byte baru, dan mengenkripsi ulang seluruh brankas lokal menggunakan 600.000 iterasi PBKDF2-SHA256.",
                                    fontSize = 11.sp,
                                    color = Slate400,
                                    lineHeight = 16.sp
                                )

                                if (passwordChangeError != null) {
                                    Text(text = passwordChangeError ?: "", fontSize = 11.sp, color = Rose400)
                                }

                                OutlinedTextField(
                                    value = newPasswordInput,
                                    onValueChange = { newPasswordInput = it },
                                    label = { Text("Master Password Baru") },
                                    placeholder = { Text("Minimal 8 karakter...") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Amber400,
                                        unfocusedBorderColor = Slate800,
                                        focusedContainerColor = Slate900,
                                        unfocusedContainerColor = Slate900,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                PasswordStrengthMeter(password = newPasswordInput)

                                OutlinedTextField(
                                    value = confirmNewPasswordInput,
                                    onValueChange = { confirmNewPasswordInput = it },
                                    label = { Text("Konfirmasi Master Password Baru") },
                                    placeholder = { Text("Ulangi Master Password baru...") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Amber400,
                                        unfocusedBorderColor = Slate800,
                                        focusedContainerColor = Slate900,
                                        unfocusedContainerColor = Slate900,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        when {
                                            newPasswordInput.length < 8 -> {
                                                passwordChangeError = "Master Password minimal 8 karakter."
                                            }
                                            newPasswordInput != confirmNewPasswordInput -> {
                                                passwordChangeError = "Konfirmasi Master Password tidak cocok."
                                            }
                                            else -> {
                                                passwordChangeError = null
                                                onChangeMasterPassword(newPasswordInput)
                                                onClose()
                                            }
                                        }
                                    },
                                    enabled = !isCryptoBusy && newPasswordInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Amber400, contentColor = Slate950),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().testTag("confirm_change_password_button")
                                ) {
                                    if (isCryptoBusy) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Slate950, strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Mengenkripsi Ulang...", fontSize = 12.sp)
                                    } else {
                                        Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Perbarui Master Password Sekarang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
