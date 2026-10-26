package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Rose400
import com.example.ui.theme.Rose950
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900

@Composable
fun ClipboardCountdownBar(
    remainingSeconds: Int,
    label: String,
    totalSeconds: Int = 30,
    onClearNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = remainingSeconds > 0,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it }),
        modifier = modifier
    ) {
        val progress by animateFloatAsState(
            targetValue = remainingSeconds.toFloat() / totalSeconds.toFloat(),
            label = "clipboardProgress"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .background(Slate900, RoundedCornerShape(16.dp))
                .border(1.dp, Emerald400.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$label disalin!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald400,
                            maxLines = 1
                        )
                    }

                    Box(
                        modifier = Modifier
                            .testTag("clear_clipboard_now_button")
                            .background(Rose950.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .border(1.dp, Rose400.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { onClearNow() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Hapus Sekarang",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Rose400
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Clipboard akan dibersihkan otomatis dalam:",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                    Text(
                        text = "${remainingSeconds}d",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Cyan400
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = Emerald400,
                    trackColor = Slate800,
                )
            }
        }
    }
}
