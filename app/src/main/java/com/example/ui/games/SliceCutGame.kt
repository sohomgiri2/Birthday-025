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
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlowingPillButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.CandleAmber
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.RoseQuartz
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold
import kotlinx.coroutines.delay
import kotlin.math.hypot
import kotlin.random.Random

private data class SliceOrb(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    val color: Color,
    val isHazard: Boolean = false
)

private data class SliceSpark(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    val color: Color
)

@Composable
fun SliceCutGame(
    bestScore: Int,
    onSliceSfx: () -> Unit,
    onGameOver: (score: Int, starsEarned: Int) -> Unit
) {
    var arenaSize by remember { mutableStateOf(IntSize(600, 800)) }
    var score by remember { mutableIntStateOf(0) }
    var combo by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var gameOver by remember { mutableStateOf(false) }
    var nextId by remember { mutableIntStateOf(1) }
    var comboTimer by remember { mutableFloatStateOf(0f) }

    val orbs = remember { mutableStateListOf<SliceOrb>() }
    val sparks = remember { mutableStateListOf<SliceSpark>() }
    val swipeTrail = remember { mutableStateListOf<Offset>() }

    fun resetGame() {
        orbs.clear()
        sparks.clear()
        swipeTrail.clear()
        score = 0
        combo = 0
        lives = 3
        gameOver = false
        comboTimer = 0f
    }

    LaunchedEffect(gameOver, arenaSize) {
        if (gameOver || arenaSize.width == 0) return@LaunchedEffect
        var spawnAccum = 0f
        while (!gameOver) {
            val dt = 0.016f
            val difficulty = 1f + (score / 25f).coerceAtMost(2.2f)
            spawnAccum += dt * difficulty
            if (spawnAccum >= 0.85f) {
                spawnAccum = 0f
                val w = arenaSize.width.toFloat()
                val h = arenaSize.height.toFloat()
                val startX = w * (0.15f + Random.nextFloat() * 0.7f)
                val targetX = w * 0.5f + (Random.nextFloat() - 0.5f) * w * 0.35f
                val palette = listOf(NebulaViolet, SkyCyan, StarlightGold, AuroraMint)
                val hazard = Random.nextFloat() < 0.14f && score >= 5
                orbs.add(
                    SliceOrb(
                        id = nextId++,
                        x = startX,
                        y = h + 40f,
                        vx = (targetX - startX) * 0.85f,
                        vy = -(h * (1.05f + Random.nextFloat() * 0.25f)),
                        radius = 34f,
                        color = if (hazard) RoseQuartz else palette.random(),
                        isHazard = hazard
                    )
                )
            }

            if (comboTimer > 0f) {
                comboTimer -= dt
                if (comboTimer <= 0f) combo = 0
            }

            val gravity = arenaSize.height * 1.15f
            val toRemove = mutableListOf<SliceOrb>()
            for (orb in orbs) {
                orb.x += orb.vx * dt
                orb.y += orb.vy * dt
                orb.vy += gravity * dt
                if (orb.y > arenaSize.height + 90f && orb.vy > 0f) {
                    toRemove.add(orb)
                    if (!orb.isHazard) {
                        lives -= 1
                        if (lives <= 0) {
                            gameOver = true
                            onGameOver(score, (score / 5).coerceAtLeast(1))
                        }
                    }
                }
            }
            orbs.removeAll(toRemove)

            val deadSparks = mutableListOf<SliceSpark>()
            for (sp in sparks) {
                sp.x += sp.vx * dt
                sp.y += sp.vy * dt
                sp.life -= dt * 2.2f
                if (sp.life <= 0f) deadSparks.add(sp)
            }
            sparks.removeAll(deadSparks)

            if (swipeTrail.size > 12) {
                swipeTrail.removeAt(0)
            }
            delay(16)
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Score: $score  |  Best: $bestScore", style = MaterialTheme.typography.titleMedium, color = SilverMist)
            Text(
                text = if (combo > 1) "${combo}x Combo!" else "Lives: ${"♥".repeat(lives.coerceAtLeast(0))}",
                style = MaterialTheme.typography.labelLarge,
                color = if (combo > 1) StarlightGold else RoseQuartz
            )
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
                    .pointerInput(gameOver) {
                        if (gameOver) return@pointerInput
                        detectDragGestures(
                            onDragEnd = { swipeTrail.clear() },
                            onDragCancel = { swipeTrail.clear() }
                        ) { change, _ ->
                            change.consume()
                            val pos = change.position
                            swipeTrail.add(pos)
                            if (swipeTrail.size > 14) swipeTrail.removeAt(0)

                            val hit = orbs.firstOrNull { hypot(it.x - pos.x, it.y - pos.y) <= it.radius + 22f }
                            if (hit != null) {
                                orbs.remove(hit)
                                onSliceSfx()
                                repeat(12) {
                                    sparks.add(
                                        SliceSpark(
                                            x = hit.x,
                                            y = hit.y,
                                            vx = (Random.nextFloat() - 0.5f) * 340f,
                                            vy = (Random.nextFloat() - 0.5f) * 340f,
                                            life = 1f,
                                            color = hit.color
                                        )
                                    )
                                }
                                if (hit.isHazard) {
                                    lives -= 1
                                    combo = 0
                                    if (lives <= 0) {
                                        gameOver = true
                                        onGameOver(score, (score / 5).coerceAtLeast(1))
                                    }
                                } else {
                                    combo += 1
                                    comboTimer = 1.1f
                                    score += if (combo >= 3) 2 else 1
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    orbs.forEach { orb ->
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color.White.copy(alpha = 0.85f), orb.color, orb.color.copy(alpha = 0.4f)),
                                center = Offset(orb.x - orb.radius * 0.25f, orb.y - orb.radius * 0.25f),
                                radius = orb.radius
                            ),
                            radius = orb.radius,
                            center = Offset(orb.x, orb.y)
                        )
                        if (orb.isHazard) {
                            drawCircle(
                                color = Color.White.copy(alpha = 0.7f),
                                radius = orb.radius * 0.4f,
                                center = Offset(orb.x, orb.y),
                                style = Stroke(width = 3f)
                            )
                        }
                    }

                    sparks.forEach { sp ->
                        drawCircle(
                            color = sp.color.copy(alpha = sp.life.coerceIn(0f, 1f)),
                            radius = 5f,
                            center = Offset(sp.x, sp.y)
                        )
                    }

                    if (swipeTrail.size > 1) {
                        val path = Path().apply {
                            moveTo(swipeTrail.first().x, swipeTrail.first().y)
                            for (i in 1 until swipeTrail.size) {
                                lineTo(swipeTrail[i].x, swipeTrail[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = SoftLavender,
                            style = Stroke(width = 7f, cap = StrokeCap.Round)
                        )
                    }
                }

                if (!gameOver && orbs.isEmpty()) {
                    Text(
                        text = "Swipe across glowing orbs • Avoid red cores",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SilverMist.copy(alpha = 0.65f),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }

                if (gameOver) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Game Over", style = MaterialTheme.typography.headlineMedium, color = SilverMist)
                        Text("Final Score: $score", style = MaterialTheme.typography.titleLarge, color = StarlightGold)
                        GlowingPillButton(
                            text = "Play Again",
                            icon = Icons.Rounded.Refresh,
                            onClick = { resetGame() }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}
