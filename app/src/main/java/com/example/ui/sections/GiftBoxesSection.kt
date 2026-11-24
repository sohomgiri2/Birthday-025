package com.example.ui.sections

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.data.VaultSnapshot
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.Tilt3DContainer
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.CandleAmber
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.RoseQuartz
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold

private data class MysteryBoxDef(
    val id: String,
    val title: String,
    val rewardType: String,
    val rewardMessage: String,
    val color: Color,
    val unlockHint: String? = null,
    val isUnlocked: (VaultSnapshot) -> Boolean = { true }
)

@Composable
fun GiftBoxesSection(
    vault: VaultSnapshot,
    reduceAnimations: Boolean,
    onOpenBox: (String) -> Unit
) {
    val boxes = remember {
        listOf(
            MysteryBoxDef(
                id = "gift_1",
                title = "Silver Box",
                rewardType = "Secret Message",
                rewardMessage = "You deserve peaceful days, genuine laughs, and people who show up for you.",
                color = SoftLavender
            ),
            MysteryBoxDef(
                id = "gift_2",
                title = "Violet Box",
                rewardType = "Compliment",
                rewardMessage = "You handle tough days with way more grace than you think.",
                color = NebulaViolet
            ),
            MysteryBoxDef(
                id = "gift_3",
                title = "Amber Box",
                rewardType = "Birthday Surprise",
                rewardMessage = "Free Virtual Cake Refill! You can replay the birthday candles anytime in Section 1.",
                color = CandleAmber
            ),
            MysteryBoxDef(
                id = "gift_4",
                title = "Cyan Box",
                rewardType = "Wish Unlocked",
                rewardMessage = "Unlocked after making a birthday wish! May this year treat you kindly.",
                color = SkyCyan,
                unlockHint = "Blow out the birthday candles first",
                isUnlocked = { it.candlesExtinguished }
            ),
            MysteryBoxDef(
                id = "gift_5",
                title = "Emerald Box",
                rewardType = "Gamer Reward",
                rewardMessage = "Unlocked for playing a mini-game! Bonus cosmic luck activated for your next spin.",
                color = AuroraMint,
                unlockHint = "Play any mini-game to unlock",
                isUnlocked = { "first_game" in it.unlockedAchievements }
            ),
            MysteryBoxDef(
                id = "gift_6",
                title = "Rose Box",
                rewardType = "Keepsake Note",
                rewardMessage = "No matter how busy life gets, good friendships stay anchored in the little moments.",
                color = RoseQuartz,
                unlockHint = "Collect 5+ stars in Mini-Games",
                isUnlocked = { it.gameStats.totalStarsCollected >= 5 }
            )
        )
    }

    var inspectedBox by remember { mutableStateOf<MysteryBoxDef?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("MYSTERY GIFT BOXES", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
                Text("Opened ${vault.openedGifts.size} of ${boxes.size}", style = MaterialTheme.typography.titleLarge, color = SilverMist)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Adaptive(154.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(boxes, key = { it.id }) { box ->
                val isAvailable = box.isUnlocked(vault)
                val isOpened = box.id in vault.openedGifts
                val popScale by animateFloatAsState(
                    targetValue = if (isOpened) 1.02f else 1f,
                    animationSpec = spring(),
                    label = "gift_scale"
                )
                Tilt3DContainer(enabled = !reduceAnimations) {
                    LiquidGlassCard(
                        onClick = {
                            if (isAvailable) {
                                onOpenBox(box.id)
                            }
                            inspectedBox = box
                        },
                        glowColor = if (isAvailable) box.color else Color.Gray,
                        modifier = Modifier.graphicsLayer {
                            scaleX = popScale
                            scaleY = popScale
                        }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = when {
                                    !isAvailable -> Icons.Rounded.Lock
                                    isOpened -> Icons.Rounded.AutoAwesome
                                    else -> Icons.Rounded.CardGiftcard
                                },
                                contentDescription = box.title,
                                tint = if (isAvailable) box.color else SilverMist.copy(alpha = 0.4f),
                                modifier = Modifier.size(34.dp)
                            )
                            Text(box.title, style = MaterialTheme.typography.titleMedium, color = SilverMist)
                            Text(
                                text = when {
                                    !isAvailable -> "Locked"
                                    isOpened -> box.rewardType
                                    else -> "Tap to Open"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAvailable) StarlightGold else SilverMist.copy(alpha = 0.55f)
                            )
                        }
                    }
                }
            }
        }
    }

    inspectedBox?.let { box ->
        val unlocked = box.isUnlocked(vault)
        AlertDialog(
            onDismissRequest = { inspectedBox = null },
            title = { Text(if (unlocked) "${box.title} — ${box.rewardType}" else "${box.title} (Locked)") },
            text = {
                Text(if (unlocked) box.rewardMessage else (box.unlockHint ?: "Keep exploring to unlock this box."))
            },
            confirmButton = {
                TextButton(onClick = { inspectedBox = null }) {
                    Text("Close")
                }
            }
        )
    }
}
