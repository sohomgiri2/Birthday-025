package com.example.ui.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.audio.SoundEngine
import com.example.data.GameStats
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.SubtleGlassButton
import com.example.ui.games.CatchStarsGame
import com.example.ui.games.MemoryFlipGame
import com.example.ui.games.ScratchRevealGame
import com.example.ui.games.SliceCutGame
import com.example.ui.games.SpinWheelGame
import com.example.ui.games.TapStarsGame
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.CandleAmber
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.RoseQuartz
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SkyCyan
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold

private enum class MiniGameId(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color
) {
    SLICE("Slice & Cut", "Swipe to slice glowing orbs", Icons.Rounded.ContentCut, RoseQuartz),
    CATCH("Catch Stars", "Glide basket to catch stars", Icons.Rounded.Star, StarlightGold),
    MEMORY("Memory Flip", "Match cosmic card pairs", Icons.Rounded.GridView, SkyCyan),
    SCRATCH("Scratch Card", "Reveal hidden foil surprises", Icons.Rounded.Brush, AuroraMint),
    WHEEL("Spin Wheel", "Spin for wishes & perks", Icons.Rounded.Casino, CandleAmber),
    TAP_STARS("Tap Stars", "20s reaction star blitz", Icons.Rounded.TouchApp, NebulaViolet)
}

@Composable
fun GamesHubSection(
    stats: GameStats,
    onPlaySfx: (SoundEngine.SfxType) -> Unit,
    onRecordResult: (gameKey: String, score: Int, stars: Int, memorySeconds: Int) -> Unit
) {
    var activeGame by remember { mutableStateOf<MiniGameId?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        if (activeGame == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ARCADE ROOM", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
                    Text("Mini-Games Hub", style = MaterialTheme.typography.headlineMedium, color = SilverMist)
                }
                LiquidGlassCard(contentPadding = 10.dp) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = StarlightGold)
                        Text("${stats.totalStarsCollected} ★", style = MaterialTheme.typography.labelLarge, color = StarlightGold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyVerticalGrid(
                columns = GridCells.Adaptive(156.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(MiniGameId.entries, key = { it.name }) { game ->
                    val bestLabel = when (game) {
                        MiniGameId.SLICE -> "Best: ${stats.sliceBest}"
                        MiniGameId.CATCH -> "Best: ${stats.catchStarsBest}"
                        MiniGameId.MEMORY -> if (stats.memoryBestSeconds > 0) "Best: ${stats.memoryBestSeconds}s" else "Best: --"
                        MiniGameId.SCRATCH -> "Done: ${stats.scratchCardsCompleted}"
                        MiniGameId.WHEEL -> "Spins: ${stats.wheelSpins}"
                        MiniGameId.TAP_STARS -> "Best: ${stats.tapStarsBest}"
                    }
                    LiquidGlassCard(
                        onClick = {
                            onPlaySfx(SoundEngine.SfxType.TAP)
                            activeGame = game
                        },
                        glowColor = game.accent,
                        modifier = Modifier.testTag("game_card_${game.name.lowercase()}")
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(game.icon, contentDescription = null, tint = game.accent)
                            Text(game.title, style = MaterialTheme.typography.titleMedium, color = SilverMist)
                            Text(game.subtitle, style = MaterialTheme.typography.bodyMedium, color = SilverMist.copy(alpha = 0.72f))
                            Text(bestLabel, style = MaterialTheme.typography.labelSmall, color = SoftLavender)
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubtleGlassButton(
                    text = "All Games",
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    testTag = "back_to_games_hub",
                    onClick = { activeGame = null }
                )
                Text(activeGame!!.title, style = MaterialTheme.typography.titleLarge, color = SilverMist)
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (activeGame!!) {
                MiniGameId.SLICE -> SliceCutGame(
                    bestScore = stats.sliceBest,
                    onSliceSfx = { onPlaySfx(SoundEngine.SfxType.SLICE) },
                    onGameOver = { s, stars -> onRecordResult("slice", s, stars, 0) }
                )
                MiniGameId.CATCH -> CatchStarsGame(
                    bestScore = stats.catchStarsBest,
                    onCatchSfx = { onPlaySfx(SoundEngine.SfxType.STAR) },
                    onGameOver = { s, stars -> onRecordResult("catch", s, stars, 0) }
                )
                MiniGameId.MEMORY -> MemoryFlipGame(
                    bestSeconds = stats.memoryBestSeconds,
                    onMatchSfx = { onPlaySfx(SoundEngine.SfxType.STAR) },
                    onComplete = { sec, stars -> onRecordResult("memory", 0, stars, sec) }
                )
                MiniGameId.SCRATCH -> ScratchRevealGame(
                    completedCount = stats.scratchCardsCompleted,
                    onRevealReward = {
                        onPlaySfx(SoundEngine.SfxType.GIFT)
                        onRecordResult("scratch", 0, 2, 0)
                    }
                )
                MiniGameId.WHEEL -> SpinWheelGame(
                    totalSpins = stats.wheelSpins,
                    onSpinStartSfx = { onPlaySfx(SoundEngine.SfxType.TAP) },
                    onSpinComplete = { _ ->
                        onPlaySfx(SoundEngine.SfxType.CELEBRATION)
                        onRecordResult("wheel", 0, 3, 0)
                    }
                )
                MiniGameId.TAP_STARS -> TapStarsGame(
                    bestScore = stats.tapStarsBest,
                    onStarTapSfx = { onPlaySfx(SoundEngine.SfxType.STAR) },
                    onFinish = { s, stars -> onRecordResult("tap", s, stars, 0) }
                )
            }
        }
    }
}
