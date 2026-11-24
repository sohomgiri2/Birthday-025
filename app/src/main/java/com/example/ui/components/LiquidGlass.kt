package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CosmicIndigo
import com.example.ui.theme.GlassBorderSilver
import com.example.ui.theme.GlassHighlight
import com.example.ui.theme.MidnightBlue
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SoftLavender
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Translucent Liquid Glass panel with subtle top specular reflection and glowing edge.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    glowColor: Color = NebulaViolet,
    borderColor: Color = GlassBorderSilver,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val glassBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xD9161D36),
            Color(0xB80B1021),
            Color(0xCC12172E)
        ),
        start = Offset.Zero,
        end = Offset(600f, 800f)
    )

    val clickModifier = if (onClick != null) {
        Modifier
            .clip(shape)
            .clickable(onClick = onClick)
    } else {
        Modifier.clip(shape)
    }

    Box(
        modifier = modifier
            .then(clickModifier)
            .background(glassBrush, shape)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.28f),
                        borderColor,
                        glowColor.copy(alpha = 0.35f)
                    )
                ),
                shape = shape
            )
            .drawBehind {
                // Subtle top specular highlight
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            GlassHighlight,
                            Color.Transparent
                        )
                    ),
                    start = Offset(size.width * 0.12f, 2f),
                    end = Offset(size.width * 0.88f, 2f),
                    strokeWidth = 2f
                )
            }
            .padding(contentPadding),
        content = content
    )
}

/**
 * Interactive 3D tilt container responding to touch drag with spring return.
 */
@Composable
fun Tilt3DContainer(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxAngle: Float = 10f,
    content: @Composable () -> Unit
) {
    var rotX by remember { mutableFloatStateOf(0f) }
    var rotY by remember { mutableFloatStateOf(0f) }

    val animRotX by animateFloatAsState(
        targetValue = if (enabled) rotX else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 240f),
        label = "tiltX"
    )
    val animRotY by animateFloatAsState(
        targetValue = if (enabled) rotY else 0f,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = 240f),
        label = "tiltY"
    )

    Box(
        modifier = modifier
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectDragGestures(
                    onDragEnd = {
                        rotX = 0f
                        rotY = 0f
                    },
                    onDragCancel = {
                        rotX = 0f
                        rotY = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    rotY = (rotY + dragAmount.x * 0.14f).coerceIn(-maxAngle, maxAngle)
                    rotX = (rotX - dragAmount.y * 0.14f).coerceIn(-maxAngle, maxAngle)
                }
            }
            .graphicsLayer {
                rotationX = animRotX
                rotationY = animRotY
                cameraDistance = 14f * density
            }
    ) {
        content()
    }
}

@Composable
fun GlowingPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accentColor: Color = NebulaViolet,
    enabled: Boolean = true,
    testTag: String? = null
) {
    val baseMod = modifier.minimumInteractiveComponentSize()
    val taggedMod = if (testTag != null) baseMod.testTag(testTag) else baseMod

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) accentColor.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.06f),
        border = BorderStroke(
            1.dp,
            if (enabled) accentColor.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.16f)
        ),
        modifier = taggedMod
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) SilverMist else SilverMist.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) SilverMist else SilverMist.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
fun SubtleGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    testTag: String? = null
) {
    val baseMod = modifier.minimumInteractiveComponentSize()
    val taggedMod = if (testTag != null) baseMod.testTag(testTag) else baseMod
    OutlinedButton(
        onClick = onClick,
        shape = CircleShape,
        border = BorderStroke(1.dp, GlassBorderSilver),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White.copy(alpha = 0.05f),
            contentColor = SilverMist
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = taggedMod
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

private data class StarParticle(
    val xFrac: Float,
    val yFrac: Float,
    val radius: Float,
    val speed: Float,
    val phase: Float,
    val tint: Color
)

/**
 * Lightweight battery-friendly ambient cosmic background with optional floating particles.
 * Respects particlesEnabled & reduceAnimations toggles.
 */
@Composable
fun CosmicBackdrop(
    particlesEnabled: Boolean,
    reduceAnimations: Boolean,
    frostVariation: Boolean,
    modifier: Modifier = Modifier
) {
    val particles = remember {
        val rng = Random(42063)
        val palette = listOf(SilverMist, SoftLavender, NebulaViolet, CosmicIndigo)
        List(52) {
            StarParticle(
                xFrac = rng.nextFloat(),
                yFrac = rng.nextFloat(),
                radius = 1.2f + rng.nextFloat() * 2.6f,
                speed = 0.25f + rng.nextFloat() * 0.75f,
                phase = rng.nextFloat() * (2f * PI.toFloat()),
                tint = palette[it % palette.size]
            )
        }
    }

    val animateActive = particlesEnabled && !reduceAnimations
    val infiniteTransition = rememberInfiniteTransition(label = "cosmic_bg")
    val tick by if (animateActive) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2f * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 16000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "star_phase"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val bgTop = if (frostVariation) Color(0xFF141B36) else ObsidianBlack
    val bgBottom = if (frostVariation) Color(0xFF0A0F22) else MidnightBlue

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgTop, bgBottom, ObsidianBlack)))
    ) {
        // Soft ambient nebula orbs
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(NebulaViolet.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(size.width * 0.82f, size.height * 0.16f),
                radius = size.width * 0.65f
            ),
            center = Offset(size.width * 0.82f, size.height * 0.16f),
            radius = size.width * 0.65f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(CosmicIndigo.copy(alpha = 0.16f), Color.Transparent),
                center = Offset(size.width * 0.15f, size.height * 0.78f),
                radius = size.width * 0.72f
            ),
            center = Offset(size.width * 0.15f, size.height * 0.78f),
            radius = size.width * 0.72f
        )

        if (particlesEnabled) {
            particles.forEach { p ->
                val dx = if (animateActive) cos(tick * p.speed + p.phase) * 14f else 0f
                val dy = if (animateActive) sin(tick * p.speed + p.phase) * 18f else 0f
                val alpha = if (animateActive) {
                    (0.30f + 0.55f * ((sin(tick * 2f * p.speed + p.phase) + 1f) * 0.5f))
                } else {
                    0.45f
                }
                val cx = ((p.xFrac * size.width) + dx + size.width) % size.width
                val cy = ((p.yFrac * size.height) + dy + size.height) % size.height
                drawCircle(
                    color = p.tint.copy(alpha = alpha),
                    radius = p.radius * density * 0.6f,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}

/**
 * Small clickable glowing star badge that reveals secret messages anywhere in the app.
 */
@Composable
fun HiddenStarTrigger(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(18.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(SoftLavender.copy(alpha = 0.75f), Color.Transparent)
                ),
                radius = size.minDimension / 2f
            )
            drawCircle(
                color = SilverMist,
                radius = 2.5.dp.toPx()
            )
        }
    }
}
