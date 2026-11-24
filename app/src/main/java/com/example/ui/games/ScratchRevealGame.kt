package com.example.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.config.AppConfig
import com.example.ui.components.GlowingPillButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold

@Composable
fun ScratchRevealGame(
    completedCount: Int,
    onRevealReward: () -> Unit
) {
    val cols = 14
    val rows = 9
    val totalCells = cols * rows
    val scratched = remember { mutableStateListOf<Boolean>().apply { repeat(totalCells) { add(false) } } }
    var cardSize by remember { mutableStateOf(IntSize(600, 400)) }
    var cardIndex by remember { mutableIntStateOf(0) }
    var rewardGranted by remember { mutableStateOf(false) }

    val surprises = remember {
        listOf(
            "Birthday Fortune: Unlimited laughs & zero bad Mondays this year!",
            "Hidden Gift Note: Someone genuinely appreciates having you around.",
            "Secret Sparkle: " + AppConfig.SECRET_MESSAGES[2],
            "Cosmic Blessing: Good news is closer than you think."
        )
    }

    fun nextCard() {
        for (i in 0 until totalCells) scratched[i] = false
        rewardGranted = false
        cardIndex = (cardIndex + 1) % surprises.size
    }

    val revealedFraction = scratched.count { it }.toFloat() / totalCells

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Scratch the silver foil", style = MaterialTheme.typography.titleMedium, color = SilverMist)
            Text("Cards: $completedCount", style = MaterialTheme.typography.labelLarge, color = StarlightGold)
        }

        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            glowColor = if (rewardGranted) AuroraMint else NebulaViolet,
            contentPadding = 6.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .onSizeChanged { cardSize = it }
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            if (cardSize.width == 0 || cardSize.height == 0) return@detectDragGestures
                            val cellW = cardSize.width.toFloat() / cols
                            val cellH = cardSize.height.toFloat() / rows
                            val cx = (change.position.x / cellW).toInt()
                            val cy = (change.position.y / cellH).toInt()
                            for (dy in -1..1) {
                                for (dx in -1..1) {
                                    val nx = cx + dx
                                    val ny = cy + dy
                                    if (nx in 0 until cols && ny in 0 until rows) {
                                        scratched[ny * cols + nx] = true
                                    }
                                }
                            }
                            if (!rewardGranted && scratched.count { it } >= totalCells * 0.45f) {
                                rewardGranted = true
                                onRevealReward()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("COSMIC SURPRISE", style = MaterialTheme.typography.labelSmall, color = StarlightGold)
                    Text(
                        text = surprises[cardIndex],
                        style = MaterialTheme.typography.displayMedium,
                        color = SilverMist,
                        textAlign = TextAlign.Center
                    )
                }

                // Metallic foil overlay cells
                if (revealedFraction < 0.88f) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cw = size.width / cols
                        val ch = size.height / rows
                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                if (!scratched[r * cols + c]) {
                                    drawRect(
                                        brush = Brush.linearGradient(
                                            listOf(Color(0xFF475569), Color(0xFF64748B), Color(0xFF334155))
                                        ),
                                        topLeft = Offset(c * cw, r * ch),
                                        size = Size(cw + 1f, ch + 1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${(revealedFraction * 100).toInt()}% revealed",
                style = MaterialTheme.typography.labelLarge,
                color = if (rewardGranted) AuroraMint else SoftLavender
            )
            GlowingPillButton(
                text = "Next Scratch Card",
                icon = if (rewardGranted) Icons.Rounded.AutoAwesome else Icons.Rounded.Refresh,
                onClick = { nextCard() }
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}
