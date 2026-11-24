package com.example.ui.sections

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
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
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private data class PlanetNode(
    val name: String,
    val category: String,
    val message: String,
    val orbitRadius: Float,
    val baseAngle: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val hasRing: Boolean = false
)

@Composable
fun UniverseSection(
    reduceAnimations: Boolean,
    onPlanetTapSfx: () -> Unit
) {
    val planets = remember {
        listOf(
            PlanetNode("Lumia", "Compliment", "You carry a rare kind of warmth that puts people at ease.", 0.24f, 0.4f, 0.4f, 22f, SoftLavender),
            PlanetNode("Vesper", "Funny Fact", "Scientists agree: birthday calories officially vanish at midnight.", 0.36f, 2.1f, 0.28f, 26f, SkyCyan, hasRing = true),
            PlanetNode("Aurelia", "Little Wish", "May this year bring unexpected wins that make you genuinely proud.", 0.48f, 3.8f, 0.22f, 24f, StarlightGold),
            PlanetNode("Solace", "Hidden Message", "Even on quiet days, your presence means more to your friends than you realize.", 0.62f, 5.1f, 0.16f, 28f, AuroraMint),
            PlanetNode("Kairo", "Surprise", "You found the outer nebula! Tap the glowing star in the top bar for extra secret notes.", 0.76f, 1.3f, 0.12f, 20f, RoseQuartz, hasRing = true)
        )
    }

    val bgStars = remember {
        val rng = Random(99)
        List(70) { Triple(rng.nextFloat() * 2f - 1f, rng.nextFloat() * 2f - 1f, rng.nextFloat() * 2f - 1f) }
    }

    var yaw by remember { mutableFloatStateOf(0.25f) }
    var pitch by remember { mutableFloatStateOf(0.45f) }
    var orbitTime by remember { mutableFloatStateOf(0f) }
    var canvasSize by remember { mutableStateOf(IntSize(600, 700)) }
    var selectedPlanet by remember { mutableStateOf<PlanetNode?>(planets.first()) }

    LaunchedEffect(reduceAnimations) {
        if (reduceAnimations) return@LaunchedEffect
        while (true) {
            orbitTime += 0.014f
            delay(24)
        }
    }

    // Helper projection for 3D space
    fun project3D(x: Float, y: Float, z: Float, cx: Float, cy: Float, scale: Float): Triple<Float, Float, Float> {
        val cyaw = cos(yaw)
        val syaw = sin(yaw)
        val x1 = x * cyaw - z * syaw
        val z1 = x * syaw + z * cyaw

        val cpitch = cos(pitch)
        val spitch = sin(pitch)
        val y2 = y * cpitch - z1 * spitch
        val z2 = y * spitch + z1 * cpitch

        val perspective = 2.6f / (2.6f + z2).coerceAtLeast(0.6f)
        return Triple(cx + x1 * scale * perspective, cy + y2 * scale * perspective, perspective)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column {
            Text("INTERACTIVE 3D UNIVERSE", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
            Text("Drag to orbit • Tap any planet", style = MaterialTheme.typography.titleMedium, color = SilverMist)
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
                    .onSizeChanged { canvasSize = it }
                    .pointerInput(Unit) {
                        detectDragGestures { change, drag ->
                            change.consume()
                            yaw += drag.x * 0.006f
                            pitch = (pitch + drag.y * 0.006f).coerceIn(-1.1f, 1.1f)
                        }
                    }
                    .pointerInput(planets) {
                        detectTapGestures { tap ->
                            val cx = canvasSize.width / 2f
                            val cy = canvasSize.height / 2f
                            val scale = minOf(canvasSize.width, canvasSize.height) * 0.45f
                            var best: PlanetNode? = null
                            var bestDist = Float.MAX_VALUE
                            planets.forEach { p ->
                                val a = p.baseAngle + orbitTime * p.speed
                                val px = cos(a) * p.orbitRadius
                                val pz = sin(a) * p.orbitRadius
                                val (sx, sy, persp) = project3D(px, 0f, pz, cx, cy, scale)
                                val d = hypot(tap.x - sx, tap.y - sy)
                                if (d <= p.size * persp + 28f && d < bestDist) {
                                    bestDist = d
                                    best = p
                                }
                            }
                            if (best != null) {
                                selectedPlanet = best
                                onPlanetTapSfx()
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val scale = size.minDimension * 0.45f

                    // 3D Starfield
                    bgStars.forEach { (sx0, sy0, sz0) ->
                        val (px, py, persp) = project3D(sx0, sy0, sz0, cx, cy, scale * 1.2f)
                        drawCircle(
                            color = SilverMist.copy(alpha = (0.25f + 0.45f * persp).coerceIn(0.1f, 0.9f)),
                            radius = (1.6f * persp).dp.toPx(),
                            center = Offset(px, py)
                        )
                    }

                    // Central glowing sun/core
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(StarlightGold, CandleAmber.copy(alpha = 0.3f), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = 52.dp.toPx()
                        ),
                        radius = 52.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    drawCircle(color = StarlightGold, radius = 16.dp.toPx(), center = Offset(cx, cy))

                    // Sort planets back-to-front by perspective depth
                    val projected = planets.map { p ->
                        val a = p.baseAngle + orbitTime * p.speed
                        val x = cos(a) * p.orbitRadius
                        val z = sin(a) * p.orbitRadius
                        val (sx, sy, persp) = project3D(x, 0f, z, cx, cy, scale)
                        Triple(p, Offset(sx, sy), persp)
                    }.sortedBy { it.third }

                    projected.forEach { (p, pos, persp) ->
                        val r = (p.size * persp)
                        if (p.hasRing) {
                            drawOval(
                                color = p.color.copy(alpha = 0.5f),
                                topLeft = Offset(pos.x - r * 1.6f, pos.y - r * 0.45f),
                                size = androidx.compose.ui.geometry.Size(r * 3.2f, r * 0.9f),
                                style = Stroke(width = 2.5f)
                            )
                        }
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.9f), p.color, p.color.copy(alpha = 0.35f)),
                                center = Offset(pos.x - r * 0.3f, pos.y - r * 0.3f),
                                radius = r
                            ),
                            radius = r,
                            center = pos
                        )
                        if (selectedPlanet == p) {
                            drawCircle(
                                color = SilverMist,
                                radius = r + 6f,
                                center = pos,
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                }
            }
        }

        selectedPlanet?.let { planet ->
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = planet.color
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(planet.name, style = MaterialTheme.typography.titleMedium, color = SilverMist)
                        Text(planet.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = planet.color)
                    }
                    Text(planet.message, style = MaterialTheme.typography.bodyMedium, color = SilverMist.copy(alpha = 0.88f))
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}
