package com.example.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlowingPillButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.random.Random

private data class PopStar(
    val id: Int,
    val x: Float,
    val y: Float,
    val radius: Float,
    val isSuper: Boolean
)

@Composable
fun TapStarsGame(
    bestScore: Int,
    onStarTapSfx: () -> Unit,
    onFinish: (score: Int, starsEarned: Int) -> Unit
) {
    var arenaSize by remember { mutableStateOf(IntSize(600, 800)) }
    var running by remember { mutableStateOf(false) }
    var finished by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(20) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var nextId by remember { mutableIntStateOf(1) }
    val activeStars = remember { mutableStateListOf<PopStar>() }

    fun start() {
        activeStars.clear()
        score = 0
        combo = 0
        timeLeft = 20
        finished = false
        running = true
    }

    LaunchedEffect(running, arenaSize) {
        if (!running || arenaSize.width == 0) return@LaunchedEffect
        var tick = 0
        while (running && timeLeft > 0) {
            if (activeStars.size < 4) {
                activeStars.add(
                    PopStar(
                        id = nextId++,
                        x = 56f + Random.nextFloat() * (arenaSize.width - 112f).coerceAtLeast(10f),
                        y = 56f + Random.nextFloat() * (arenaSize.height - 112f).coerceAtLeast(10f),
                        radius = 32f,
                        isSuper = Random.nextFloat() < 0.2f
                    )
                )
            }
            delay(250)
            tick++
            if (tick % 4 == 0) {
                timeLeft--
                if (activeStars.size > 3) activeStars.removeAt(0)
            }
        }
        if (running) {
            running = false
            finished = true
            onFinish(score, (score / 4).coerceAtLeast(1))
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Score: $score  |  Best: $bestScore", style = MaterialTheme.typography.titleMedium, color = SilverMist)
            Text("Time: ${timeLeft}s", style = MaterialTheme.typography.labelLarge, color = AuroraMint)
        }

        LiquidGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .onSizeChanged { arenaSize = it }
                    .pointerInput(running) {
                        if (!running) return@pointerInput
                        detectTapGestures { offset ->
                            val hit = activeStars.lastOrNull { hypot(it.x - offset.x, it.y - offset.y) <= it.radius + 18f }
                            if (hit != null) {
                                activeStars.remove(hit)
                                combo++
                                score += if (hit.isSuper) 3 else if (combo >= 5) 2 else 1
                                onStarTapSfx()
                            } else {
                                combo = 0
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    activeStars.forEach { st ->
                        val c = if (st.isSuper) StarlightGold else SoftLavender
                        drawCircle(
                            brush = Brush.radialGradient(listOf(c, c.copy(alpha = 0f))),
                            radius = st.radius * 1.4f,
                            center = Offset(st.x, st.y)
                        )
                        drawCircle(
                            color = c,
                            radius = st.radius * 0.65f,
                            center = Offset(st.x, st.y)
                        )
                    }
                }

                if (!running && !finished) {
                    GlowingPillButton(
                        text = "Start 20s Star Blitz",
                        icon = Icons.Rounded.PlayArrow,
                        accentColor = NebulaViolet,
                        onClick = { start() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                if (finished) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Time's Up!", style = MaterialTheme.typography.headlineMedium, color = SilverMist)
                        Text("Final Score: $score", style = MaterialTheme.typography.titleLarge, color = StarlightGold)
                        GlowingPillButton(
                            text = "Play Again",
                            icon = Icons.Rounded.Refresh,
                            onClick = { start() }
                        )
                    }
                }
            }
        }
    }
}
