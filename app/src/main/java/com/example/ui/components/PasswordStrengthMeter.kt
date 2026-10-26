package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.KeyGenEngine
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Rose400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

@Composable
fun PasswordStrengthMeter(
    password: String,
    modifier: Modifier = Modifier
) {
    val eval = KeyGenEngine.evaluateStrength(password)

    val meterColor = when {
        eval.scorePercent == 0 -> Slate700
        eval.scorePercent <= 25 -> Rose400
        eval.scorePercent <= 50 -> Amber400
        eval.scorePercent <= 75 -> Cyan400
        else -> Emerald400
    }

    val animatedColor by animateColorAsState(meterColor, label = "meterColor")
    val animatedProgress by animateFloatAsState(
        targetValue = eval.scorePercent / 100f,
        label = "meterProgress"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kekuatan: ${eval.label}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = animatedColor
            )
            Text(
                text = "~${eval.bits} bits entropi",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = Cyan400
            )
        }

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = animatedColor,
            trackColor = Slate800,
            strokeCap = StrokeCap.Round
        )
    }
}
