package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.VaultItem
import com.example.data.model.VaultItemHistory
import com.example.data.model.VaultItemType
import com.example.ui.components.LiveTotpWidget
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Cyan950
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose950
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ItemDetailDialog(
    item: VaultItem,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCopy: (text: String, label: String) -> Unit,
    onRestoreHistory: (VaultItemHistory) -> Unit
) {
    var showSecret by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("details") }
    var confirmDelete by remember { mutableStateOf(false) }

    val dateFormatted = remember(item.updated_at) {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        sdf.format(Date(item.updated_at))
    }

    val seedWords = remember(item.secret, item.type) {
        if (item.type == VaultItemType.SEED_PHRASE) {
            item.secret.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
        } else {
            emptyList()
        }
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
                .testTag("item_detail_dialog")
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
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Emerald950.copy(alpha = 0.5f))
                                    .border(1.dp, Emerald400.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.type.displayName,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Emerald400,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Diperbarui: $dateFormatted",
                                fontSize = 11.sp,
                                color = Slate500
                            )
                        }

                        Text(
                            text = item.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    // Action Icons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Item",
                                tint = Slate300,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = { confirmDelete = !confirmDelete },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Item",
                                tint = Rose400,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = Slate400,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Delete Confirmation Banner
                AnimatedVisibility(visible = confirmDelete) {
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
                                text = "Hapus permanen rahasia ini? ID akan dicatat pada Tombstone Log agar tidak muncul kembali saat sinkronisasi manual.",
                                fontSize = 11.sp,
                                color = Rose400
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onDelete,
                                    colors = ButtonDefaults.buttonColors(containerColor = Rose400, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Ya, Hapus", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { confirmDelete = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Batal", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Tabs: Details vs History
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeTab == "details") Emerald400.copy(alpha = 0.15f) else Slate950)
                            .border(
                                1.dp,
                                if (activeTab == "details") Emerald400.copy(alpha = 0.5f) else Slate800,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { activeTab = "details" }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (activeTab == "details") Emerald400 else Slate400,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Data Kredensial",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == "details") Emerald400 else Slate400
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeTab == "history") Cyan400.copy(alpha = 0.15f) else Slate950)
                            .border(
                                1.dp,
                                if (activeTab == "history") Cyan400.copy(alpha = 0.5f) else Slate800,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { activeTab = "history" }
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = if (activeTab == "history") Cyan400 else Slate400,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Riwayat (${item.history.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == "history") Cyan400 else Slate400
                            )
                        }
                    }
                }

                if (activeTab == "details") {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Username row
                        if (!item.username.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Slate950)
                                    .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "USERNAME / IDENTITAS",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Slate400,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = item.username,
                                            fontSize = 14.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Slate900)
                                            .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                                            .clickable { onCopy(item.username, "Username (${item.title})") }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Salin",
                                                tint = Slate300,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Text(
                                                text = "Salin",
                                                fontSize = 11.sp,
                                                color = Slate300
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Secret Block
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
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (item.type == VaultItemType.SEED_PHRASE) {
                                            "MNEMONIC SEED PHRASE (${seedWords.size} KATA)"
                                        } else {
                                            "PASSWORD / PIN / SECRET"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate400,
                                        letterSpacing = 1.sp
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        // Reveal toggle
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Slate900)
                                                .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                                                .clickable { showSecret = !showSecret }
                                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (showSecret) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = null,
                                                    tint = Slate300,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = if (showSecret) "Tutup" else "Lihat",
                                                    fontSize = 11.sp,
                                                    color = Slate300
                                                )
                                            }
                                        }

                                        // Copy
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Emerald950.copy(alpha = 0.5f))
                                                .border(1.dp, Emerald400.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .clickable { onCopy(item.secret, "Secret (${item.title})") }
                                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = null,
                                                    tint = Emerald400,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Text(
                                                    text = "Salin",
                                                    fontSize = 11.sp,
                                                    color = Emerald400,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }

                                // Secret Content Display
                                if (item.type == VaultItemType.SEED_PHRASE) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .then(if (!showSecret) Modifier.blur(14.dp) else Modifier)
                                    ) {
                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            seedWords.forEachIndexed { idx, word ->
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Slate900)
                                                        .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Text(
                                                            text = "${idx + 1}.",
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 11.sp,
                                                            color = Slate500
                                                        )
                                                        Text(
                                                            text = word,
                                                            fontFamily = FontFamily.Monospace,
                                                            fontSize = 13.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Emerald400
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Slate900)
                                            .border(1.dp, Slate800, RoundedCornerShape(10.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = if (showSecret) item.secret else "•".repeat(item.secret.length.coerceIn(8, 24)),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (showSecret) Emerald400 else Slate400,
                                            modifier = Modifier.then(if (!showSecret) Modifier.blur(4.dp) else Modifier)
                                        )
                                    }
                                }
                            }
                        }

                        // Live TOTP 2FA Widget
                        if (!item.totp_secret.isNullOrBlank()) {
                            LiveTotpWidget(
                                totpSecret = item.totp_secret,
                                onCopyCode = { code -> onCopy(code, "Kode 2FA TOTP (${item.title})") }
                            )
                        }

                        // Recovery Codes
                        if (!item.recovery_codes.isNullOrEmpty()) {
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
                                            text = "RECOVERY CODES (${item.recovery_codes.size})",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Slate400,
                                            letterSpacing = 1.sp
                                        )

                                        Text(
                                            text = "Salin Semua",
                                            fontSize = 11.sp,
                                            color = Cyan400,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                onCopy(item.recovery_codes.joinToString("\n"), "Semua Recovery Codes")
                                            }
                                        )
                                    }

                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        item.recovery_codes.forEachIndexed { i, rc ->
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Slate900)
                                                    .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                                                    .clickable { onCopy(rc, "Recovery Code #${i + 1}") }
                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = rc,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontSize = 12.sp,
                                                        color = Color.White
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.ContentCopy,
                                                        contentDescription = null,
                                                        tint = Slate500,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Notes
                        if (item.notes.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Slate950)
                                    .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = "CATATAN TERENKRIPSI",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Slate400,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = item.notes,
                                        fontSize = 13.sp,
                                        color = Color.LightGray,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }

                        // Tags
                        if (item.tags.isNotEmpty()) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                item.tags.forEach { t ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Slate950)
                                            .border(1.dp, Slate800, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "#$t",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Cyan400
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // History Tab
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Menyimpan hingga 15 versi rahasia sebelumnya secara otomatis saat item diperbarui atau saat terjadi konflik sinkronisasi.",
                            fontSize = 11.sp,
                            color = Slate400
                        )

                        if (item.history.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Slate950)
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Belum ada riwayat revisi untuk item ini.",
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                            }
                        } else {
                            item.history.forEach { hist ->
                                val histDateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                                    .format(Date(hist.updated_at))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Slate950)
                                        .border(1.dp, Slate800, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = histDateStr,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Slate400
                                            )

                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Slate900)
                                                        .border(1.dp, Slate800, RoundedCornerShape(6.dp))
                                                        .clickable { onCopy(hist.secret, "Versi Lama (${item.title})") }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Text("Salin", fontSize = 11.sp, color = Slate300)
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(Cyan950.copy(alpha = 0.5f))
                                                        .border(1.dp, Cyan400.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                                        .clickable { onRestoreHistory(hist) }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Restore,
                                                            contentDescription = null,
                                                            tint = Cyan400,
                                                            modifier = Modifier.size(11.dp)
                                                        )
                                                        Text("Restore", fontSize = 11.sp, color = Cyan400, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }

                                        Text(
                                            text = hist.secret,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 12.sp,
                                            color = Emerald400,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Slate900, RoundedCornerShape(8.dp))
                                                .padding(8.dp)
                                        )

                                        if (!hist.notes.isNullOrBlank()) {
                                            Text(
                                                text = "Catatan: ${hist.notes}",
                                                fontSize = 11.sp,
                                                color = Slate400
                                            )
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
}
