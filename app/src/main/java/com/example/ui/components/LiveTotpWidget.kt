package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.TotpEngine
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Emerald950
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate950
import kotlinx.coroutines.delay

@Composable
fun LiveTotpWidget(
    totpSecret: String,
    onCopyCode: (code: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var totpResult by remember { mutableStateOf<TotpEngine.TotpResult?>(null) }
    var isValidSecret by remember { mutableStateOf(true) }

    LaunchedEffect(totpSecret) {
        while (true) {
            try {
                totpResult = TotpEngine.generateTotp(totpSecret)
                isValidSecret = true
            } catch (_: Exception) {
                isValidSecret = false
                totpResult = null
                break
            }
            delay(500)
        }
    }

    if (!isValidSecret) {
        Surface(
            color = Color(0xFF3B1219),
            shape = RoundedCornerShape(12.dp),
            modifier = modifier.fillMaxWidth()
        ) {
            Text(
                text = "Format Base32 TOTP Secret tidak valid.",
                color = Rose400,
                fontSize = 12.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
        return
    }

    val res = totpResult ?: return
    val remaining = res.remainingSeconds
    val isExpiring = remaining <= 5
    val ringColor by animateColorAsState(if (isExpiring) Rose400 else Emerald400, label = "ringColor")
    val sweepAngle by animateFloatAsState(
        targetValue = (remaining.toFloat() / 30f) * 360f,
        label = "sweepAngle"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Slate950, RoundedCornerShape(14.dp))
            .border(1.dp, Emerald400.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Circular Countdown Timer
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(46.dp)
            ) {
                Canvas(modifier = Modifier.size(42.dp)) {
                    drawCircle(
                        color = Slate800,
                        radius = size.minDimension / 2f,
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawArc(
                        color = ringColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "${remaining}s",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = ringColor
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = Cyan400,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "LIVE 2FA AUTHENTICATOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Cyan400,
                        letterSpacing = 1.sp
                    )
                }

                val codePart1 = if (res.code.length >= 3) res.code.substring(0, 3) else res.code
                val codePart2 = if (res.code.length >= 6) res.code.substring(3) else ""

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$codePart1 ",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = codePart2,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Emerald400,
                        letterSpacing = 2.sp
                    )
                }
            }
        }

        // Quick Copy Button
        Box(
            modifier = Modifier
                .testTag("copy_totp_button")
                .background(Emerald950.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .border(1.dp, Emerald400.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                .clickable { onCopyCode(res.code) }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Salin Kode 2FA",
                    tint = Emerald400,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Salin",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Emerald400
                )
            }
        }
    }
}
