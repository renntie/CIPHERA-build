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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.crypto.KeyGenEngine
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Slate300
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun KeyGenDialog(
    onClose: () -> Unit,
    onCopy: (key: String, label: String) -> Unit
) {
    var mode by remember { mutableStateOf("password") }
    var length by remember { mutableFloatStateOf(20f) }
    var wordCount by remember { mutableFloatStateOf(5f) }
    var useUpper by remember { mutableStateOf(true) }
    var useLower by remember { mutableStateOf(true) }
    var useDigits by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }
    var generatedKey by remember { mutableStateOf("") }

    fun generate() {
        generatedKey = if (mode == "passphrase") {
            KeyGenEngine.generatePassphrase(wordCount = wordCount.toInt())
        } else {
            KeyGenEngine.generateRandomPassword(
                length = length.toInt(),
                useUpper = useUpper,
                useLower = useLower,
                useDigits = useDigits,
                useSymbols = useSymbols
            )
        }
    }

    LaunchedEffect(mode, length, wordCount, useUpper, useLower, useDigits, useSymbols) {
        generate()
    }

    val strength = KeyGenEngine.evaluateStrength(generatedKey)

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(Slate900)
                .border(1.dp, Slate800, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("keygen_dialog")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Cyan400,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "CIPHERA KeyGen",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Slate400
                        )
                    }
                }

                // Mode Tabs
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
                            .background(if (mode == "password") Emerald400.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (mode == "password") Emerald400 else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { mode = "password" }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Random Password",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mode == "password") Emerald400 else Slate400
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (mode == "passphrase") Cyan400.copy(alpha = 0.2f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (mode == "passphrase") Cyan400 else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { mode = "passphrase" }
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Diceware Passphrase",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (mode == "passphrase") Cyan400 else Slate400
                        )
                    }
                }

                // Output Display Box
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
                            text = generatedKey,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald400,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Entropi: ~${strength.bits} bits",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Cyan400
                            )
                            Text(
                                text = strength.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald400
                            )
                        }
                    }
                }

                // Controls
                if (mode == "password") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Panjang Karakter", fontSize = 12.sp, color = Slate300)
                            Text(
                                text = "${length.toInt()} karakter",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Slider(
                            value = length,
                            onValueChange = { length = it },
                            valueRange = 8f..64f,
                            steps = 55,
                            colors = SliderDefaults.colors(
                                thumbColor = Emerald400,
                                activeTrackColor = Emerald400,
                                inactiveTrackColor = Slate800
                            )
                        )

                        // Checkbox Options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KeyOptionCheckbox("A-Z", useUpper, { useUpper = it }, Modifier.weight(1f))
                            KeyOptionCheckbox("a-z", useLower, { useLower = it }, Modifier.weight(1f))
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            KeyOptionCheckbox("0-9", useDigits, { useDigits = it }, Modifier.weight(1f))
                            KeyOptionCheckbox("!@#$", useSymbols, { useSymbols = it }, Modifier.weight(1f))
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Jumlah Kata Diceware", fontSize = 12.sp, color = Slate300)
                            Text(
                                text = "${wordCount.toInt()} kata",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Slider(
                            value = wordCount,
                            onValueChange = { wordCount = it },
                            valueRange = 3f..10f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = Cyan400,
                                activeTrackColor = Cyan400,
                                inactiveTrackColor = Slate800
                            )
                        )

                        Text(
                            text = "Passphrase menggunakan rangkaian kata acak dari Wordlist Kriptografi EFF yang sangat mudah diingat oleh manusia namun tahan terhadap serangan brute-force komputer.",
                            fontSize = 11.sp,
                            color = Slate400,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { generate() },
                        colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Acak Ulang", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onCopy(generatedKey, "Kunci KeyGen CIPHERA")
                            onClose()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald400, contentColor = Slate950),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin Kunci", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun KeyOptionCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Slate950)
            .border(1.dp, Slate800, RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = Emerald400, checkmarkColor = Slate950),
                modifier = Modifier.size(20.dp)
            )
            Text(text = label, fontSize = 12.sp, color = Slate300)
        }
    }
}
