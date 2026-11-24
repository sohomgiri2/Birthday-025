package com.example.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.components.SubtleGlassButton
import com.example.ui.components.Tilt3DContainer
import com.example.ui.theme.CandleAmber
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun IntroScreen(
    reduceAnimations: Boolean,
    onEnterWorld: () -> Unit,
    onGiftTapSound: () -> Unit
) {
    var showLine1 by remember { mutableStateOf(false) }
    var showLine2 by remember { mutableStateOf(false) }
    var showGift by remember { mutableStateOf(false) }
    var giftOpened by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(220)
        showLine1 = true
        delay(850)
        showLine2 = true
        delay(550)
        showGift = true
    }

    val infinite = rememberInfiniteTransition(label = "intro_float")
    val floatY by if (!reduceAnimations) {
        infinite.animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(2600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "gift_bob"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    val zoomScale by animateFloatAsState(
        targetValue = if (giftOpened) 1.55f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 180f),
        label = "intro_zoom"
    )
    val fadeOut by animateFloatAsState(
        targetValue = if (giftOpened) 0f else 1f,
        animationSpec = tween(550),
        label = "intro_fade"
    )

    val burstProgress by animateFloatAsState(
        targetValue = if (giftOpened) 1f else 0f,
        animationSpec = tween(650),
        label = "burst_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(24.dp)
            .alpha(fadeOut)
    ) {
        // Subtle portal backdrop image
        Image(
            painter = painterResource(id = R.drawable.img_world_portal),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(32.dp))
                .alpha(0.22f)
        )

        // Skip Intro button top-right
        SubtleGlassButton(
            text = stringResource(R.string.btn_skip_intro),
            icon = Icons.AutoMirrored.Rounded.ArrowForward,
            onClick = {
                if (!giftOpened) {
                    giftOpened = true
                    onEnterWorld()
                }
            },
            testTag = "skip_intro_button",
            modifier = Modifier.align(Alignment.TopEnd)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoomScale
                    scaleY = zoomScale
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "OPENING CHAPTER",
                style = MaterialTheme.typography.labelSmall,
                color = SoftLavender.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(
                visible = showLine1,
                enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { 24 }
            ) {
                Text(
                    text = stringResource(R.string.intro_line_1),
                    style = MaterialTheme.typography.displayMedium,
                    color = SilverMist,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(
                visible = showLine2,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { 24 }
            ) {
                Text(
                    text = stringResource(R.string.intro_line_2),
                    style = MaterialTheme.typography.headlineMedium,
                    color = SoftLavender,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(38.dp))

            AnimatedVisibility(
                visible = showGift,
                enter = fadeIn(tween(700)) + slideInVertically(tween(700)) { 40 }
            ) {
                Tilt3DContainer(enabled = !reduceAnimations) {
                    Box(
                        modifier = Modifier
                            .size(210.dp)
                            .graphicsLayer { translationY = floatY }
                            .testTag("intro_gift_box")
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (!giftOpened) {
                                    giftOpened = true
                                    onGiftTapSound()
                                    scope.launch {
                                        delay(560)
                                        onEnterWorld()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cx = size.width / 2f
                            val cy = size.height / 2f

                            // Ambient glow behind 3D gift box
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        NebulaViolet.copy(alpha = 0.45f),
                                        Color.Transparent
                                    ),
                                    center = Offset(cx, cy),
                                    radius = size.minDimension * 0.5f
                                ),
                                radius = size.minDimension * 0.5f,
                                center = Offset(cx, cy)
                            )

                            val boxW = size.width * 0.54f
                            val boxH = size.height * 0.44f
                            val lidLift = if (giftOpened) -28.dp.toPx() * burstProgress else 0f

                            // 3D Box Body
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF332968), Color(0xFF171C3B))
                                ),
                                topLeft = Offset(cx - boxW / 2f, cy - boxH * 0.25f),
                                size = Size(boxW, boxH),
                                cornerRadius = CornerRadius(18.dp.toPx())
                            )
                            // Glass border on box
                            drawRoundRect(
                                color = SilverMist.copy(alpha = 0.35f),
                                topLeft = Offset(cx - boxW / 2f, cy - boxH * 0.25f),
                                size = Size(boxW, boxH),
                                cornerRadius = CornerRadius(18.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                            )
                            // Vertical Ribbon
                            drawRect(
                                brush = Brush.verticalGradient(listOf(StarlightGold, CandleAmber)),
                                topLeft = Offset(cx - 10.dp.toPx(), cy - boxH * 0.25f),
                                size = Size(20.dp.toPx(), boxH)
                            )

                            // 3D Lid
                            val lidW = boxW * 1.1f
                            val lidH = 26.dp.toPx()
                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF4B3D8F), Color(0xFF232952))
                                ),
                                topLeft = Offset(cx - lidW / 2f, cy - boxH * 0.25f - lidH * 0.65f + lidLift),
                                size = Size(lidW, lidH),
                                cornerRadius = CornerRadius(12.dp.toPx())
                            )
                            // Ribbon Bow loops
                            drawCircle(
                                color = StarlightGold,
                                radius = 12.dp.toPx(),
                                center = Offset(cx - 14.dp.toPx(), cy - boxH * 0.25f - lidH * 0.8f + lidLift),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5.dp.toPx())
                            )
                            drawCircle(
                                color = StarlightGold,
                                radius = 12.dp.toPx(),
                                center = Offset(cx + 14.dp.toPx(), cy - boxH * 0.25f - lidH * 0.8f + lidLift),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.5.dp.toPx())
                            )

                            // Burst particles when tapped
                            if (burstProgress > 0.01f) {
                                val particleCount = 24
                                for (i in 0 until particleCount) {
                                    val angle = (i.toFloat() / particleCount) * (2f * PI.toFloat())
                                    val dist = size.minDimension * 0.48f * burstProgress
                                    val px = cx + cos(angle) * dist
                                    val py = cy + sin(angle) * dist
                                    drawCircle(
                                        color = if (i % 2 == 0) StarlightGold else SoftLavender,
                                        radius = (5.dp.toPx() * (1f - burstProgress * 0.5f)),
                                        center = Offset(px, py)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedVisibility(visible = showGift) {
                Text(
                    text = stringResource(R.string.btn_tap_gift),
                    style = MaterialTheme.typography.labelLarge,
                    color = StarlightGold
                )
            }
        }
    }
}
