package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.crypto.KeyGenEngine
import com.example.data.model.VaultItem
import com.example.data.model.VaultItemType
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun ItemEditorDialog(
    initialItem: VaultItem?,
    onClose: () -> Unit,
    onSave: (
        id: String?,
        type: VaultItemType,
        title: String,
        username: String?,
        secret: String,
        totpSecret: String?,
        recoveryCodes: List<String>?,
        notes: String,
        tags: List<String>,
        favorite: Boolean
    ) -> Unit
) {
    var type by remember { mutableStateOf(initialItem?.type ?: VaultItemType.PASSWORD) }
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var username by remember { mutableStateOf(initialItem?.username ?: "") }
    var secret by remember { mutableStateOf(initialItem?.secret ?: "") }
    var totpSecret by remember { mutableStateOf(initialItem?.totp_secret ?: "") }
    var recoveryCodesRaw by remember { mutableStateOf(initialItem?.recovery_codes?.joinToString("\n") ?: "") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }
    var tagsRaw by remember { mutableStateOf(initialItem?.tags?.joinToString(", ") ?: "") }
    var favorite by remember { mutableStateOf(initialItem?.favorite ?: false) }
    var validationError by remember { mutableStateOf<String?>(null) }

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
                .testTag("item_editor_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .heightIn(max = 700.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialItem != null) "Edit Item Brankas" else "Tambah Rahasia Baru",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Slate400
                        )
                    }
                }

                // Type Selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    VaultItemType.entries.forEach { t ->
                        val isSelected = type == t
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Emerald400.copy(alpha = 0.2f) else Slate950)
                                .border(
                                    1.dp,
                                    if (isSelected) Emerald400 else Slate800,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable { type = t }
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = when (t) {
                                    VaultItemType.PASSWORD -> "Password"
                                    VaultItemType.CODE -> "PIN / Code"
                                    VaultItemType.SEED_PHRASE -> "Seed (12/24)"
                                    VaultItemType.NOTE -> "Catatan"
                                },
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Emerald400 else Slate400
                            )
                        }
                    }
                }

                if (validationError != null) {
                    Text(
                        text = validationError ?: "",
                        fontSize = 12.sp,
                        color = Rose400
                    )
                }

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Judul Item *") },
                    placeholder = { Text("Cth: BCA Prioritas / Ledger Nano") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, autoCorrectEnabled = false),
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

                // Username
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username / Alamat / Akun (Opsional)") },
                    placeholder = { Text("user@email.com / 0x...") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, autoCorrectEnabled = false),
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

                // Secret Textarea + Quick Gen Button
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (type == VaultItemType.SEED_PHRASE) {
                                "KATA-KATA SEED PHRASE (PISAHKAN DENGAN SPASI) *"
                            } else {
                                "PASSWORD / PIN / SECRET *"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate400,
                            letterSpacing = 1.sp
                        )

                        if (type == VaultItemType.PASSWORD) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.clickable {
                                    secret = KeyGenEngine.generateRandomPassword(length = 20)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Cyan400,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Generate Acak",
                                    fontSize = 11.sp,
                                    color = Cyan400,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = secret,
                        onValueChange = { secret = it },
                        placeholder = {
                            Text(
                                if (type == VaultItemType.SEED_PHRASE) {
                                    "Masukkan 12 atau 24 kata mnemonic dipisahkan spasi..."
                                } else {
                                    "Masukkan password, PIN, atau rahasia..."
                                },
                                fontSize = 12.sp
                            )
                        },
                        minLines = if (type == VaultItemType.SEED_PHRASE || type == VaultItemType.NOTE) 3 else 2,
                        maxLines = 5,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Emerald400,
                            unfocusedBorderColor = Slate800,
                            focusedContainerColor = Slate950,
                            unfocusedContainerColor = Slate950,
                            focusedTextColor = Emerald400,
                            unfocusedTextColor = Emerald400
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // TOTP Secret
                OutlinedTextField(
                    value = totpSecret,
                    onValueChange = { totpSecret = it },
                    label = { Text("TOTP 2FA Secret Key (Base32 Opsional)") },
                    placeholder = { Text("Cth: JBSWY3DPEHPK3PXP") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, autoCorrectEnabled = false),
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

                // Tags
                OutlinedTextField(
                    value = tagsRaw,
                    onValueChange = { tagsRaw = it },
                    label = { Text("Tags (Pisahkan dengan koma)") },
                    placeholder = { Text("Finance, Crypto, Work") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, autoCorrectEnabled = false),
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

                // Recovery Codes
                OutlinedTextField(
                    value = recoveryCodesRaw,
                    onValueChange = { recoveryCodesRaw = it },
                    label = { Text("Recovery / Backup Codes (1 per baris)") },
                    placeholder = { Text("8492-1102\n9941-2310") },
                    minLines = 2,
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(autoCorrectEnabled = false),
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

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Tambahan Terenkripsi") },
                    placeholder = { Text("Petunjuk keamanan, pertanyaan pemulihan, dll...") },
                    minLines = 2,
                    maxLines = 4,
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

                // Favorite Checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { favorite = !favorite }
                ) {
                    Checkbox(
                        checked = favorite,
                        onCheckedChange = { favorite = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Emerald400,
                            checkmarkColor = Slate950
                        )
                    )
                    Text(
                        text = "Tandai sebagai Item Favorit",
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onClose,
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Batal", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (title.isBlank() || secret.isBlank()) {
                                validationError = "Judul dan Rahasia Utama wajib diisi."
                            } else {
                                val tagList = tagsRaw.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                val rcList = recoveryCodesRaw.split("\n").map { it.trim() }.filter { it.isNotBlank() }
                                onSave(
                                    initialItem?.id,
                                    type,
                                    title.trim(),
                                    username.trim().ifEmpty { null },
                                    secret.trim(),
                                    totpSecret.trim().ifEmpty { null },
                                    if (rcList.isNotEmpty()) rcList else null,
                                    notes.trim(),
                                    tagList,
                                    favorite
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald400, contentColor = Slate950),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_secret_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan & Enkripsi", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
