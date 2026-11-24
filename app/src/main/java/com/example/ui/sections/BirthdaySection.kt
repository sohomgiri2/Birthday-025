package com.example.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.GlowingPillButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.SubtleGlassButton
import com.example.ui.components.Tilt3DContainer
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.CandleAmber
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.RoseQuartz
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun BirthdaySection(
    candlesAlreadyExtinguished: Boolean,
    savedWish: String,
    reduceAnimations: Boolean,
    onAllCandlesExtinguished: (String) -> Unit,
    onReplayBirthday: () -> Unit,
    onCandleBlowSfx: () -> Unit
) {
    val candleLitStates = remember(candlesAlreadyExtinguished) {
        mutableStateListOf(
            !candlesAlreadyExtinguished,
            !candlesAlreadyExtinguished,
            !candlesAlreadyExtinguished,
            !candlesAlreadyExtinguished,
            !candlesAlreadyExtinguished
        )
    }
    var wishInput by remember(savedWish) { mutableStateOf(savedWish) }
    var wishSavedBanner by remember { mutableStateOf(false) }

    val allOut = candleLitStates.none { it }
    LaunchedEffect(allOut) {
        if (allOut && !candlesAlreadyExtinguished) {
            onAllCandlesExtinguished(wishInput)
        }
    }

    val infinite = rememberInfiniteTransition(label = "bday_anim")
    val phase by if (!reduceAnimations) {
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = 6.283f,
            animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
            label = "bday_phase"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Confetti & 3D Floating Balloons Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val balloonColors = listOf(NebulaViolet, SkyCyan, RoseQuartz, SoftLavender)
            val xFracs = listOf(0.12f, 0.28f, 0.74f, 0.88f)
            val yFracs = listOf(0.16f, 0.24f, 0.14f, 0.26f)
            xFracs.forEachIndexed { idx, xf ->
                val bob = sin(phase + idx * 1.4f) * 14.dp.toPx()
                val bx = size.width * xf
                val by = size.height * yFracs[idx] + bob
                val c = balloonColors[idx % balloonColors.size]
                // String
                drawLine(
                    color = SilverMist.copy(alpha = 0.25f),
                    start = Offset(bx, by + 28.dp.toPx()),
                    end = Offset(bx + sin(phase + idx) * 6f, by + 76.dp.toPx()),
                    strokeWidth = 1.5.dp.toPx()
                )
                // 3D Balloon sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.85f), c, c.copy(alpha = 0.45f)),
                        center = Offset(bx - 7.dp.toPx(), by - 8.dp.toPx()),
                        radius = 32.dp.toPx()
                    ),
                    radius = 24.dp.toPx(),
                    center = Offset(bx, by)
                )
            }

            if (allOut) {
                val rng = Random(2026)
                val confettiColors = listOf(StarlightGold, NebulaViolet, AuroraMint, SkyCyan, RoseQuartz)
                for (i in 0 until 45) {
                    val cx = rng.nextFloat() * size.width
                    val cy = ((rng.nextFloat() + phase / 6.283f) % 1f) * size.height
                    drawCircle(
                        color = confettiColors[i % confettiColors.size].copy(alpha = 0.8f),
                        radius = (3 + rng.nextInt(4)).dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "CELEBRATION",
                style = MaterialTheme.typography.labelSmall,
                color = StarlightGold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Happy Birthday",
                style = MaterialTheme.typography.displayLarge,
                color = SilverMist,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (allOut) "Your wish is drifting up to the stars." else "Tap each candle flame to make a wish",
                style = MaterialTheme.typography.bodyMedium,
                color = SoftLavender,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Interactive 3D Birthday Cake Card
            Tilt3DContainer(enabled = !reduceAnimations) {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = if (allOut) StarlightGold else NebulaViolet
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 5 Interactive Candles Row
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            candleLitStates.forEachIndexed { idx, isLit ->
                                Box(
                                    modifier = Modifier
                                        .size(width = 34.dp, height = 72.dp)
                                        .testTag("birthday_candle_$idx")
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            if (isLit) {
                                                candleLitStates[idx] = false
                                                onCandleBlowSfx()
                                            }
                                        },
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val cx = size.width / 2f
                                        // Candle stick
                                        drawRoundRect(
                                            brush = Brush.verticalGradient(
                                                listOf(SoftLavender, NebulaViolet)
                                            ),
                                            topLeft = Offset(cx - 5.dp.toPx(), size.height * 0.38f),
                                            size = Size(10.dp.toPx(), size.height * 0.62f),
                                            cornerRadius = CornerRadius(4.dp.toPx())
                                        )
                                        // Wick
                                        drawLine(
                                            color = SilverMist,
                                            start = Offset(cx, size.height * 0.38f),
                                            end = Offset(cx, size.height * 0.28f),
                                            strokeWidth = 2.dp.toPx()
                                        )
                                        if (isLit) {
                                            val flicker = sin(phase * 3f + idx) * 2.dp.toPx()
                                            // Flame glow
                                            drawCircle(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        CandleAmber.copy(alpha = 0.65f),
                                                        Color.Transparent
                                                    )
                                                ),
                                                radius = 16.dp.toPx(),
                                                center = Offset(cx + flicker, size.height * 0.18f)
                                            )
                                            val flamePath = Path().apply {
                                                moveTo(cx, size.height * 0.29f)
                                                quadraticTo(
                                                    cx - 8.dp.toPx(),
                                                    size.height * 0.18f,
                                                    cx + flicker,
                                                    size.height * 0.04f
                                                )
                                                quadraticTo(
                                                    cx + 8.dp.toPx(),
                                                    size.height * 0.18f,
                                                    cx,
                                                    size.height * 0.29f
                                                )
                                            }
                                            drawPath(
                                                path = flamePath,
                                                brush = Brush.verticalGradient(
                                                    listOf(StarlightGold, CandleAmber)
                                                )
                                            )
                                        } else {
                                            // Subtle smoke wisp when blown out
                                            drawCircle(
                                                color = SilverMist.copy(alpha = 0.25f),
                                                radius = 4.dp.toPx(),
                                                center = Offset(cx + sin(phase + idx) * 4f, size.height * 0.18f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Multi-tier 3D Cake Body
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(132.dp)
                        ) {
                            val cx = size.width / 2f
                            // Top Tier
                            val topW = size.width * 0.56f
                            val topH = 48.dp.toPx()
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF4C3F91), Color(0xFF282459))
                                ),
                                topLeft = Offset(cx - topW / 2f, 4.dp.toPx()),
                                size = Size(topW, topH),
                                cornerRadius = CornerRadius(14.dp.toPx())
                            )
                            // Frosting drip
                            drawRoundRect(
                                color = SoftLavender.copy(alpha = 0.8f),
                                topLeft = Offset(cx - topW / 2f, 4.dp.toPx()),
                                size = Size(topW, 13.dp.toPx()),
                                cornerRadius = CornerRadius(10.dp.toPx())
                            )

                            // Bottom Tier
                            val botW = size.width * 0.78f
                            val botH = 58.dp.toPx()
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF2D275F), Color(0xFF141834))
                                ),
                                topLeft = Offset(cx - botW / 2f, topH),
                                size = Size(botW, botH),
                                cornerRadius = CornerRadius(18.dp.toPx())
                            )
                            drawRoundRect(
                                color = SilverMist.copy(alpha = 0.28f),
                                topLeft = Offset(cx - botW / 2f, topH),
                                size = Size(botW, botH),
                                cornerRadius = CornerRadius(18.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx())
                            )

                            // Glass Cake Plate
                            drawRoundRect(
                                color = SilverMist.copy(alpha = 0.25f),
                                topLeft = Offset(cx - botW * 0.56f, topH + botH + 2.dp.toPx()),
                                size = Size(botW * 1.12f, 10.dp.toPx()),
                                cornerRadius = CornerRadius(8.dp.toPx())
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!allOut) {
                                GlowingPillButton(
                                    text = "Blow All Candles",
                                    icon = Icons.Rounded.AutoAwesome,
                                    accentColor = CandleAmber,
                                    testTag = "blow_all_candles_button",
                                    onClick = {
                                        for (i in candleLitStates.indices) candleLitStates[i] = false
                                        onAllCandlesExtinguished(wishInput)
                                    }
                                )
                            } else {
                                SubtleGlassButton(
                                    text = "Replay Candles",
                                    icon = Icons.Rounded.Refresh,
                                    testTag = "replay_candles_button",
                                    onClick = {
                                        for (i in candleLitStates.indices) candleLitStates[i] = true
                                        onReplayBirthday()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Birthday Message Card
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = SoftLavender
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "A BIRTHDAY NOTE",
                        style = MaterialTheme.typography.labelSmall,
                        color = SoftLavender
                    )
                    Text(
                        text = stringResource(R.string.birthday_main_message),
                        style = MaterialTheme.typography.displayMedium,
                        color = SilverMist
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Make a Wish Interaction
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "SEAL A BIRTHDAY WISH",
                        style = MaterialTheme.typography.labelSmall,
                        color = StarlightGold
                    )
                    OutlinedTextField(
                        value = wishInput,
                        onValueChange = {
                            wishInput = it
                            wishSavedBanner = false
                        },
                        placeholder = { Text("Type a quiet wish for this year...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftLavender,
                            unfocusedBorderColor = SilverMist.copy(alpha = 0.28f),
                            focusedTextColor = SilverMist,
                            unfocusedTextColor = SilverMist
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("birthday_wish_input")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedVisibility(visible = wishSavedBanner, enter = fadeIn()) {
                            Text(
                                text = "Wish locked in SG DataVault",
                                style = MaterialTheme.typography.labelSmall,
                                color = AuroraMint
                            )
                        }
                        GlowingPillButton(
                            text = "Save Wish",
                            onClick = {
                                onAllCandlesExtinguished(wishInput)
                                wishSavedBanner = true
                            },
                            testTag = "save_wish_button"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
