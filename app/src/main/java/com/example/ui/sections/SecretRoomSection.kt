package com.example.ui.sections

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.config.AppConfig
import com.example.ui.components.GlowingPillButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.SubtleGlassButton
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.RoseQuartz
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold
import kotlinx.coroutines.launch

@Composable
fun SecretRoomSection(
    isUnlocked: Boolean,
    onUnlockSuccess: () -> Unit,
    onRelock: () -> Unit,
    onErrorSfx: () -> Unit,
    onTapSfx: () -> Unit
) {
    var enteredCode by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val shake = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun checkCode() {
        if (enteredCode == AppConfig.SECRET_ROOM_PASSWORD) {
            errorMsg = null
            enteredCode = ""
            onUnlockSuccess()
        } else {
            errorMsg = "Incorrect vault code"
            onErrorSfx()
            scope.launch {
                listOf(-16f, 16f, -10f, 10f, 0f).forEach {
                    shake.animateTo(it, tween(45))
                }
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
        Text("SECRET VAULT ROOM", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(targetState = isUnlocked, label = "secret_room_anim") { unlocked ->
            if (!unlocked) {
                LiquidGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { translationX = shake.value }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Rounded.Lock, contentDescription = null, tint = SoftLavender, modifier = Modifier.size(34.dp))
                        Text("Enter Vault Passcode", style = MaterialTheme.typography.titleLarge, color = SilverMist)
                        Text("Hint: ${AppConfig.SECRET_ROOM_HINT}", style = MaterialTheme.typography.labelSmall, color = StarlightGold)

                        LiquidGlassCard(contentPadding = 12.dp, modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (enteredCode.isEmpty()) "• • • •" else "•".repeat(enteredCode.length),
                                style = MaterialTheme.typography.headlineMedium,
                                color = SilverMist,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        if (errorMsg != null) {
                            Text(errorMsg!!, style = MaterialTheme.typography.bodyMedium, color = RoseQuartz)
                        }

                        // Nothing-OS Glass Keypad
                        val rows = listOf(
                            listOf("1", "2", "3"),
                            listOf("4", "5", "6"),
                            listOf("7", "8", "9"),
                            listOf("C", "0", "⌫")
                        )
                        rows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                row.forEach { key ->
                                    LiquidGlassCard(
                                        onClick = {
                                            onTapSfx()
                                            errorMsg = null
                                            when (key) {
                                                "C" -> enteredCode = ""
                                                "⌫" -> if (enteredCode.isNotEmpty()) enteredCode = enteredCode.dropLast(1)
                                                else -> if (enteredCode.length < 10) enteredCode += key
                                            }
                                        },
                                        contentPadding = 14.dp,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                            if (key == "⌫") {
                                                Icon(Icons.AutoMirrored.Rounded.Backspace, contentDescription = "Backspace", tint = SilverMist)
                                            } else {
                                                Text(key, style = MaterialTheme.typography.titleLarge, color = SilverMist)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        GlowingPillButton(
                            text = "Unlock Secret Room",
                            icon = Icons.Rounded.LockOpen,
                            accentColor = NebulaViolet,
                            onClick = { checkCode() }
                        )
                    }
                }
            } else {
                LiquidGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    glowColor = AuroraMint
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("VAULT UNLOCKED", style = MaterialTheme.typography.labelSmall, color = AuroraMint)
                            SubtleGlassButton(
                                text = "Relock",
                                icon = Icons.Rounded.Lock,
                                onClick = onRelock
                            )
                        }

                        Image(
                            painter = painterResource(id = R.drawable.img_secret_letter_art),
                            contentDescription = "Secret vault artwork",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(18.dp))
                        )

                        Text(
                            text = "A Quiet Secret Note",
                            style = MaterialTheme.typography.headlineMedium,
                            color = SilverMist
                        )
                        Text(
                            text = "Some things don't belong on noisy social feeds — they belong in a quiet corner made with effort. Thank you for taking the time to unlock this room. Keep shining at your own pace.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = SilverMist.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}
