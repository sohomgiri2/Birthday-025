package com.example.ui.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.config.AppConfig
import com.example.data.LetterItem
import com.example.ui.components.GlowingPillButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.SubtleGlassButton
import com.example.ui.components.Tilt3DContainer
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold
import kotlinx.coroutines.delay

@Composable
fun LettersSection(
    letters: List<LetterItem>,
    openedLetters: Set<String>,
    gayatriResponded: Boolean,
    reduceAnimations: Boolean,
    onOpenLetter: (LetterItem) -> Unit,
    onGayatriAction: () -> Unit
) {
    if (letters.isEmpty()) return
    var currentIndex by remember { mutableIntStateOf(0) }
    var isEnvelopeOpen by remember { mutableStateOf(false) }

    val letter = letters[currentIndex.coerceIn(0, letters.lastIndex)]
    var typedLength by remember(letter.id, isEnvelopeOpen) { mutableIntStateOf(0) }

    LaunchedEffect(letter.id, isEnvelopeOpen, reduceAnimations) {
        if (isEnvelopeOpen) {
            if (reduceAnimations) {
                typedLength = letter.body.length
            } else {
                typedLength = 0
                val total = letter.body.length
                val step = if (total > 180) 3 else 2
                while (typedLength < total) {
                    typedLength = (typedLength + step).coerceAtMost(total)
                    delay(18)
                }
            }
        }
    }

    val flapProgress by animateFloatAsState(
        targetValue = if (isEnvelopeOpen) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 200f),
        label = "envelope_flap"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("LETTER ROOM", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Letter ${currentIndex + 1} of ${letters.size}",
            style = MaterialTheme.typography.titleMedium,
            color = SilverMist.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Animated Envelope Card
        Tilt3DContainer(enabled = !reduceAnimations && !isEnvelopeOpen) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = if (letter.isGayatriFavor) StarlightGold else NebulaViolet
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Envelope Illustration
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(155.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width * 0.82f
                            val h = 112.dp.toPx()
                            val left = (size.width - w) / 2f
                            val top = 26.dp.toPx()

                            drawRoundRect(
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFF282B54), Color(0xFF141831))
                                ),
                                topLeft = Offset(left, top),
                                size = Size(w, h),
                                cornerRadius = CornerRadius(14.dp.toPx())
                            )
                            drawRoundRect(
                                color = SilverMist.copy(alpha = 0.3f),
                                topLeft = Offset(left, top),
                                size = Size(w, h),
                                cornerRadius = CornerRadius(14.dp.toPx()),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx())
                            )

                            // Animated Flap
                            val tipY = top + (h * 0.56f) * (1f - flapProgress) - (26.dp.toPx() * flapProgress)
                            val flapPath = Path().apply {
                                moveTo(left, top)
                                lineTo(left + w / 2f, tipY)
                                lineTo(left + w, top)
                                close()
                            }
                            drawPath(
                                path = flapPath,
                                brush = Brush.verticalGradient(
                                    listOf(Color(0xFF3E3B78), Color(0xFF23254A))
                                )
                            )
                            // Wax seal
                            if (flapProgress < 0.5f) {
                                drawCircle(
                                    color = if (letter.isGayatriFavor) StarlightGold else NebulaViolet,
                                    radius = 14.dp.toPx(),
                                    center = Offset(left + w / 2f, top + h * 0.50f)
                                )
                            }
                        }
                    }

                    Text(
                        text = letter.recipient.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (letter.isGayatriFavor) StarlightGold else SoftLavender
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = letter.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = SilverMist
                    )
                    if (letter.id in openedLetters) {
                        Text(
                            text = "Read",
                            style = MaterialTheme.typography.labelSmall,
                            color = AuroraMint
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isEnvelopeOpen) {
                        GlowingPillButton(
                            text = "Open Envelope",
                            icon = Icons.Rounded.MailOutline,
                            accentColor = if (letter.isGayatriFavor) StarlightGold else NebulaViolet,
                            testTag = "open_envelope_button",
                            onClick = {
                                isEnvelopeOpen = true
                                onOpenLetter(letter)
                            }
                        )
                    } else {
                        SubtleGlassButton(
                            text = "Close Envelope",
                            icon = Icons.Rounded.Close,
                            testTag = "close_envelope_button",
                            onClick = { isEnvelopeOpen = false }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Paper Unfolding & Typewriter Reveal
        AnimatedVisibility(
            visible = isEnvelopeOpen,
            enter = expandVertically(tween(420)) + fadeIn(tween(420)),
            exit = fadeOut(tween(220))
        ) {
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = if (letter.isGayatriFavor) StarlightGold else SoftLavender
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (letter.attachedAssetPath != null) {
                        val ctx = LocalContext.current
                        AsyncImage(
                            model = ImageRequest.Builder(ctx)
                                .data(letter.attachedAssetPath)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Attached photo for ${letter.title}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                    }

                    Text(
                        text = letter.body.take(typedLength),
                        style = MaterialTheme.typography.bodyLarge,
                        color = SilverMist,
                        modifier = Modifier.testTag("letter_body_text")
                    )

                    if (letter.isGayatriFavor && typedLength >= letter.body.length) {
                        Spacer(modifier = Modifier.height(4.dp))
                        GlowingPillButton(
                            text = if (gayatriResponded) "Noted — Thank You" else AppConfig.GAYATRI_LETTER_ACTION_TEXT,
                            icon = if (gayatriResponded) Icons.Rounded.CheckCircle else null,
                            accentColor = if (gayatriResponded) AuroraMint else StarlightGold,
                            testTag = "gayatri_response_button",
                            onClick = onGayatriAction
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Previous / Next Letter Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SubtleGlassButton(
                text = "Prev Letter",
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                testTag = "prev_letter_button",
                onClick = {
                    isEnvelopeOpen = false
                    currentIndex = (currentIndex - 1 + letters.size) % letters.size
                }
            )
            SubtleGlassButton(
                text = "Next Letter",
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                testTag = "next_letter_button",
                onClick = {
                    isEnvelopeOpen = false
                    currentIndex = (currentIndex + 1) % letters.size
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
