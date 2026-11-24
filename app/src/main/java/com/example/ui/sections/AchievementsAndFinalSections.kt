package com.example.ui.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.VaultSnapshot
import com.example.data.WorldAchievements
import com.example.data.WorldSection
import com.example.ui.components.GlowingPillButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.Tilt3DContainer
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold

@Composable
fun AchievementsSection(
    vault: VaultSnapshot
) {
    val unlocked = vault.unlockedAchievements
    val total = WorldAchievements.ALL.size
    val progress = unlocked.size.toFloat() / total.coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("PROGRESS & MILESTONES", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
        Text("Achievements (${unlocked.size}/$total)", style = MaterialTheme.typography.headlineMedium, color = SilverMist)

        LiquidGlassCard(modifier = Modifier.fillMaxWidth(), glowColor = StarlightGold) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("World Exploration", style = MaterialTheme.typography.titleMedium, color = SilverMist)
                    Text("${vault.exploredSections.size}/${WorldSection.entries.size} sections", style = MaterialTheme.typography.labelSmall, color = StarlightGold)
                }
                LinearProgressIndicator(
                    progress = { progress },
                    color = StarlightGold,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
            }
        }

        WorldAchievements.ALL.forEach { ach ->
            val isUnlocked = ach.id in unlocked
            LiquidGlassCard(
                modifier = Modifier.fillMaxWidth(),
                glowColor = if (isUnlocked) AuroraMint else NebulaViolet
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isUnlocked) Icons.Rounded.CheckCircle else Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = if (isUnlocked) AuroraMint else SilverMist.copy(alpha = 0.3f),
                        modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(ach.title, style = MaterialTheme.typography.titleMedium, color = SilverMist)
                        Text(ach.description, style = MaterialTheme.typography.bodyMedium, color = SilverMist.copy(alpha = 0.72f))
                    }
                    Text(
                        text = if (isUnlocked) "UNLOCKED" else "LOCKED",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUnlocked) AuroraMint else SilverMist.copy(alpha = 0.4f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun FinalSecretSection(
    vault: VaultSnapshot,
    reduceAnimations: Boolean,
    onReplayExperience: () -> Unit,
    onRevealFinalGift: () -> Unit
) {
    val exploredCount = vault.exploredSections.size
    val requiredCount = 4
    val isUnlocked = exploredCount >= requiredCount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("FINAL CHAPTER", style = MaterialTheme.typography.labelSmall, color = StarlightGold)

        if (!isUnlocked) {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Lock, contentDescription = null, tint = SoftLavender, modifier = Modifier.size(40.dp))
                    Text("Almost There", style = MaterialTheme.typography.headlineMedium, color = SilverMist)
                    Text(
                        text = "Explore at least $requiredCount sections of this little world to unlock the closing chapter ($exploredCount/$requiredCount explored).",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SilverMist.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Tilt3DContainer(enabled = !reduceAnimations) {
                LiquidGlassCard(modifier = Modifier.fillMaxWidth(), glowColor = StarlightGold) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_cosmic_gift_art),
                            contentDescription = "Final crystal keepsake",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(190.dp)
                                .clip(RoundedCornerShape(24.dp))
                        )
                        Text(
                            text = "A Little World Made for You",
                            style = MaterialTheme.typography.headlineMedium,
                            color = StarlightGold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.final_secret_message),
                            style = MaterialTheme.typography.displayMedium,
                            color = SilverMist,
                            textAlign = TextAlign.Center
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            GlowingPillButton(
                                text = "Final Spark",
                                icon = Icons.Rounded.AutoAwesome,
                                accentColor = StarlightGold,
                                onClick = onRevealFinalGift
                            )
                            GlowingPillButton(
                                text = "Replay Intro",
                                icon = Icons.Rounded.Replay,
                                accentColor = NebulaViolet,
                                onClick = onReplayExperience
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
