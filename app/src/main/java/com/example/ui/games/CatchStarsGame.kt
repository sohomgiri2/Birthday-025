package com.example.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlowingPillButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.RoseQuartz
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

private data class FallingStar(
    var x: Float,
    var y: Float,
    val speed: Float,
    val isGolden: Boolean
)

@Composable
fun CatchStarsGame(
    bestScore: Int,
    onCatchSfx: () -> Unit,
    onGameOver: (score: Int, starsEarned: Int) -> Unit
) {
    var arenaSize by remember { mutableStateOf(IntSize(600, 800)) }
    var basketX by remember { mutableFloatStateOf(300f) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }
    var gameOver by remember { mutableStateOf(false) }
    val stars = remember { mutableStateListOf<FallingStar>() }

    val level = (score / 8) + 1

    fun reset() {
        stars.clear()
        score = 0
        lives = 3
        gameOver = false
    }

    LaunchedEffect(arenaSize) {
        if (arenaSize.width > 0) basketX = arenaSize.width / 2f
    }

    LaunchedEffect(gameOver, arenaSize) {
        if (gameOver || arenaSize.width == 0) return@LaunchedEffect
        var spawnTimer = 0f
        while (!gameOver) {
            val dt = 0.016f
            spawnTimer += dt * (1f + level * 0.22f)
            if (spawnTimer >= 0.9f) {
                spawnTimer = 0f
                stars.add(
                    FallingStar(
                        x = 40f + Random.nextFloat() * (arenaSize.width - 80f).coerceAtLeast(10f),
                        y = -20f,
                        speed = 210f + level * 42f + Random.nextFloat() * 80f,
                        isGolden = Random.nextFloat() < 0.25f
                    )
                )
            }

            val basketY = arenaSize.height - 70f
            val basketHalfW = 72f
            val removeList = mutableListOf<FallingStar>()
            for (s in stars) {
                s.y += s.speed * dt
                if (s.y in (basketY - 26f)..(basketY + 26f) && abs(s.x - basketX) <= basketHalfW) {
                    removeList.add(s)
                    score += if (s.isGolden) 2 else 1
                    onCatchSfx()
                } else if (s.y > arenaSize.height + 24f) {
                    removeList.add(s)
                    lives -= 1
                    if (lives <= 0) {
                        gameOver = true
                        onGameOver(score, score.coerceAtLeast(1))
                    }
                }
            }
            stars.removeAll(removeList)
            delay(16)
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Score: $score (Lv $level) | Best: $bestScore", style = MaterialTheme.typography.titleMedium, color = SilverMist)
            Text("Lives: ${"★".repeat(lives.coerceAtLeast(0))}", style = MaterialTheme.typography.labelLarge, color = StarlightGold)
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
                        detectDragGestures { change, _ ->
                            change.consume()
                            basketX = change.position.x.coerceIn(70f, (arenaSize.width - 70f).coerceAtLeast(70f))
                        }
                    }
                    .pointerInput(gameOver) {
                        if (gameOver) return@pointerInput
                        detectTapGestures { offset ->
                            basketX = offset.x.coerceIn(70f, (arenaSize.width - 70f).coerceAtLeast(70f))
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    stars.forEach { star ->
                        val color = if (star.isGolden) StarlightGold else SoftLavender
                        drawCircle(
                            brush = Brush.radialGradient(listOf(color, color.copy(alpha = 0f))),
                            radius = 22f,
                            center = Offset(star.x, star.y)
                        )
                        drawCircle(color = color, radius = 9f, center = Offset(star.x, star.y))
                    }

                    // Glass Basket
                    val by = size.height - 70f
                    drawRoundRect(
                        brush = Brush.horizontalGradient(listOf(NebulaViolet, SoftLavender, NebulaViolet)),
                        topLeft = Offset(basketX - 66f, by - 14f),
                        size = Size(132f, 28f),
                        cornerRadius = CornerRadius(14f)
                    )
                }

                if (gameOver) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Catch Complete", style = MaterialTheme.typography.headlineMedium, color = SilverMist)
                        Text("Stars Caught: $score", style = MaterialTheme.typography.titleLarge, color = StarlightGold)
                        GlowingPillButton(
                            text = "Play Again",
                            icon = Icons.Rounded.Refresh,
                            onClick = { reset() }
                        )
                    }
                }
            }
        }
    }
}
