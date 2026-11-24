package com.example.ui.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlowingPillButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.CandleAmber
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.RoseQuartz
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.StarlightGold
import kotlinx.coroutines.launch
import kotlin.random.Random

private data class WheelSlice(val label: String, val rewardText: String, val color: Color)

@Composable
fun SpinWheelGame(
    totalSpins: Int,
    onSpinStartSfx: () -> Unit,
    onSpinComplete: (reward: String) -> Unit
) {
    val slices = remember {
        listOf(
            WheelSlice("Compliment", "Compliment: Your energy makes ordinary hangouts unforgettable.", NebulaViolet),
            WheelSlice("Wish", "Birthday Wish: May every tough week end with good news.", SkyCyan),
            WheelSlice("Funny", "Funny Truth: You still owe everyone a treat for your birthday!", RoseQuartz),
            WheelSlice("Surprise", "Surprise: Check the Mystery Gift Boxes — a cosmic perk awaits.", CandleAmber),
            WheelSlice("Secret", "Secret Reward: +3 Cosmic Stars added to your vault!", AuroraMint),
            WheelSlice("Vibe", "Friendship Note: Glad you exist in this chaotic timeline.", CosmicIndigo)
        )
    }

    val rotation = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }
    var lastOutcome by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun spin() {
        if (isSpinning) return
        isSpinning = true
        onSpinStartSfx()
        scope.launch {
            val extra = 1080f + Random.nextFloat() * 720f
            val target = rotation.value + extra
            rotation.animateTo(
                targetValue = target,
                animationSpec = tween(durationMillis = 2600, easing = FastOutSlowInEasing)
            )
            // Top pointer is at 270 degrees
            val normalized = ((270f - (target % 360f)) + 360f) % 360f
            val idx = ((normalized / 60f).toInt()) % slices.size
            val outcome = slices[idx].rewardText
            lastOutcome = outcome
            isSpinning = false
            onSpinComplete(outcome)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Total Spins: $totalSpins", style = MaterialTheme.typography.labelLarge, color = StarlightGold)

        Box(
            modifier = Modifier
                .size(250.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationZ = rotation.value }
            ) {
                val sweep = 360f / slices.size
                slices.forEachIndexed { i, slice ->
                    drawArc(
                        color = slice.color.copy(alpha = 0.85f),
                        startAngle = i * sweep,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset.Zero,
                        size = Size(size.width, size.height)
                    )
                }
                drawCircle(
                    color = Color(0xFF0A0D18),
                    radius = 22.dp.toPx()
                )
            }

            // Fixed top pointer
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val pointer = Path().apply {
                    moveTo(cx - 12.dp.toPx(), 0f)
                    lineTo(cx + 12.dp.toPx(), 0f)
                    lineTo(cx, 22.dp.toPx())
                    close()
                }
                drawPath(pointer, color = StarlightGold)
            }
        }

        GlowingPillButton(
            text = if (isSpinning) "Spinning..." else "Spin the Wheel",
            icon = Icons.Rounded.AutoAwesome,
            enabled = !isSpinning,
            onClick = { spin() }
        )

        if (lastOutcome != null) {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = lastOutcome!!,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SilverMist,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}
