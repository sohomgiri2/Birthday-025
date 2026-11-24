package com.example.ui.sections

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.AssistChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.PetState
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VirtualPetSection(
    pet: PetState,
    reduceAnimations: Boolean,
    onAction: (String) -> Unit
) {
    var activeExpression by remember { mutableStateOf("Idle") }
    var bounceTrigger by remember { mutableStateOf(false) }

    val infinite = rememberInfiniteTransition(label = "pet_idle")
    val bob by if (!reduceAnimations) {
        infinite.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "pet_bob"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val tapScale by animateFloatAsState(
        targetValue = if (bounceTrigger) 1.12f else 1f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 360f),
        finishedListener = { bounceTrigger = false },
        label = "pet_bounce"
    )

    val moodText = when {
        activeExpression == "Sleepy" -> "Nova is drifting in stardust dreams..."
        pet.happiness >= 85 -> "Nova is glowing with pure joy!"
        pet.energy <= 30 -> "Nova looks sleepy — maybe nap time?"
        pet.hunger <= 35 -> "Nova wants a cosmic star-treat!"
        else -> "Tap Nova or choose an activity below."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("COSMIC COMPANION", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
            Text(pet.name, style = MaterialTheme.typography.headlineMedium, color = SilverMist)
            Text(moodText, style = MaterialTheme.typography.bodyMedium, color = SoftLavender)
        }

        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = if (activeExpression == "Excited") StarlightGold else NebulaViolet
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .graphicsLayer {
                            translationY = bob
                            scaleX = tapScale
                            scaleY = tapScale
                        }
                        .testTag("virtual_pet_avatar")
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            bounceTrigger = true
                            activeExpression = "Purr"
                            onAction("pet")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f + 10.dp.toPx()

                        // Ambient Halo
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(NebulaViolet.copy(alpha = 0.4f), Color.Transparent)
                            ),
                            radius = size.minDimension * 0.48f,
                            center = Offset(cx, cy)
                        )

                        // Cat-fox ears
                        val leftEar = Path().apply {
                            moveTo(cx - 42.dp.toPx(), cy - 28.dp.toPx())
                            lineTo(cx - 52.dp.toPx(), cy - 68.dp.toPx())
                            lineTo(cx - 16.dp.toPx(), cy - 46.dp.toPx())
                            close()
                        }
                        val rightEar = Path().apply {
                            moveTo(cx + 42.dp.toPx(), cy - 28.dp.toPx())
                            lineTo(cx + 52.dp.toPx(), cy - 68.dp.toPx())
                            lineTo(cx + 16.dp.toPx(), cy - 46.dp.toPx())
                            close()
                        }
                        drawPath(leftEar, color = Color(0xFF3E357C))
                        drawPath(rightEar, color = Color(0xFF3E357C))

                        // Glowing Head/Body sphere
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(SoftLavender, Color(0xFF2D2762), Color(0xFF171A38)),
                                center = Offset(cx - 14.dp.toPx(), cy - 18.dp.toPx()),
                                radius = 70.dp.toPx()
                            ),
                            radius = 58.dp.toPx(),
                            center = Offset(cx, cy)
                        )

                        // Eyes based on expression
                        if (activeExpression == "Sleepy") {
                            drawLine(
                                color = SilverMist,
                                start = Offset(cx - 26.dp.toPx(), cy - 4.dp.toPx()),
                                end = Offset(cx - 10.dp.toPx(), cy - 4.dp.toPx()),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = SilverMist,
                                start = Offset(cx + 10.dp.toPx(), cy - 4.dp.toPx()),
                                end = Offset(cx + 26.dp.toPx(), cy - 4.dp.toPx()),
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        } else {
                            drawCircle(color = StarlightGold, radius = 6.5.dp.toPx(), center = Offset(cx - 18.dp.toPx(), cy - 6.dp.toPx()))
                            drawCircle(color = StarlightGold, radius = 6.5.dp.toPx(), center = Offset(cx + 18.dp.toPx(), cy - 6.dp.toPx()))
                            drawCircle(color = Color.White, radius = 2.2.dp.toPx(), center = Offset(cx - 20.dp.toPx(), cy - 8.dp.toPx()))
                            drawCircle(color = Color.White, radius = 2.2.dp.toPx(), center = Offset(cx + 16.dp.toPx(), cy - 8.dp.toPx()))
                        }

                        // Cute smile
                        drawArc(
                            color = SilverMist,
                            startAngle = 20f,
                            sweepAngle = 140f,
                            useCenter = false,
                            topLeft = Offset(cx - 10.dp.toPx(), cy + 2.dp.toPx()),
                            size = Size(20.dp.toPx(), 12.dp.toPx()),
                            style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Forehead cosmic star crest
                        drawCircle(
                            color = if (activeExpression == "Excited") AuroraMint else StarlightGold,
                            radius = 3.5.dp.toPx(),
                            center = Offset(cx, cy - 26.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text("Interactions: ${pet.interactions}", style = MaterialTheme.typography.labelSmall, color = StarlightGold)
            }
        }

        // Meters
        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PetMeterRow("Happiness", pet.happiness, AuroraMint)
                PetMeterRow("Energy", pet.energy, SkyCyan)
                PetMeterRow("Fullness", pet.hunger, CandleAmber)
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlowingPillButton(
                text = "Feed",
                icon = Icons.Rounded.Fastfood,
                accentColor = CandleAmber,
                modifier = Modifier.weight(1f),
                onClick = {
                    bounceTrigger = true
                    activeExpression = "Happy"
                    onAction("feed")
                }
            )
            GlowingPillButton(
                text = "Play",
                icon = Icons.Rounded.SportsEsports,
                accentColor = RoseQuartz,
                modifier = Modifier.weight(1f),
                onClick = {
                    bounceTrigger = true
                    activeExpression = "Excited"
                    onAction("play")
                }
            )
            GlowingPillButton(
                text = "Sleep",
                icon = Icons.Rounded.Bedtime,
                accentColor = SkyCyan,
                modifier = Modifier.weight(1f),
                onClick = {
                    activeExpression = "Sleepy"
                    onAction("sleep")
                }
            )
        }

        // Unlockable Reactions
        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("UNLOCKED REACTIONS", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pet.unlockedReactions.forEach { reaction ->
                        AssistChip(
                            onClick = {
                                bounceTrigger = true
                                activeExpression = "Excited"
                                onAction("pet")
                            },
                            label = { Text(reaction) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun PetMeterRow(label: String, value: Int, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = SilverMist)
            Text("$value%", style = MaterialTheme.typography.labelSmall, color = color)
        }
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            color = color,
            trackColor = Color.White.copy(alpha = 0.1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(6.dp))
        )
    }
}
