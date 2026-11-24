package com.example.ui.games

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlowingPillButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private data class MemoryCard(
    val id: Int,
    val symbol: String,
    val label: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

@Composable
fun MemoryFlipGame(
    bestSeconds: Int,
    onMatchSfx: () -> Unit,
    onComplete: (seconds: Int, starsEarned: Int) -> Unit
) {
    var pairCount by remember { mutableIntStateOf(6) }
    val cards = remember { mutableStateListOf<MemoryCard>() }
    var firstIdx by remember { mutableStateOf<Int?>(null) }
    var lockBoard by remember { mutableStateOf(false) }
    var moves by remember { mutableIntStateOf(0) }
    var elapsedSec by remember { mutableIntStateOf(0) }
    var completed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun setup(pairs: Int) {
        val symbols = listOf(
            "✦" to "Star",
            "☾" to "Moon",
            "◈" to "Prism",
            "✧" to "Spark",
            "✿" to "Bloom",
            "♪" to "Note",
            "☀" to "Glow",
            "❖" to "Gift"
        ).take(pairs)
        val deck = (symbols + symbols).shuffled().mapIndexed { idx, (sym, lbl) ->
            MemoryCard(id = idx, symbol = sym, label = lbl)
        }
        cards.clear()
        cards.addAll(deck)
        firstIdx = null
        lockBoard = false
        moves = 0
        elapsedSec = 0
        completed = false
    }

    LaunchedEffect(pairCount) {
        setup(pairCount)
    }

    LaunchedEffect(completed, cards.size) {
        while (!completed && cards.isNotEmpty()) {
            delay(1000)
            elapsedSec++
        }
    }

    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Moves: $moves | ${elapsedSec}s", style = MaterialTheme.typography.titleMedium, color = SilverMist)
            Text(
                text = if (bestSeconds > 0) "Best: ${bestSeconds}s" else "Best: --",
                style = MaterialTheme.typography.labelLarge,
                color = StarlightGold
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(4 to "Easy", 6 to "Medium", 8 to "Hard").forEach { (count, label) ->
                FilterChip(
                    selected = pairCount == count,
                    onClick = { pairCount = count },
                    label = { Text(label) }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            itemsIndexed(cards, key = { _, item -> item.id }) { index, card ->
                val rotation by animateFloatAsState(
                    targetValue = if (card.isFlipped || card.isMatched) 180f else 0f,
                    animationSpec = tween(320),
                    label = "card_flip"
                )
                LiquidGlassCard(
                    onClick = {
                        if (lockBoard || card.isFlipped || card.isMatched) return@LiquidGlassCard
                        cards[index] = card.copy(isFlipped = true)
                        val prev = firstIdx
                        if (prev == null) {
                            firstIdx = index
                        } else {
                            moves++
                            val prevCard = cards[prev]
                            if (prevCard.symbol == card.symbol) {
                                cards[prev] = prevCard.copy(isMatched = true)
                                cards[index] = cards[index].copy(isMatched = true)
                                firstIdx = null
                                onMatchSfx()
                                if (cards.all { it.isMatched }) {
                                    completed = true
                                    onComplete(elapsedSec.coerceAtLeast(1), pairCount / 2)
                                }
                            } else {
                                lockBoard = true
                                scope.launch {
                                    delay(650)
                                    cards[prev] = cards[prev].copy(isFlipped = false)
                                    cards[index] = cards[index].copy(isFlipped = false)
                                    firstIdx = null
                                    lockBoard = false
                                }
                            }
                        }
                    },
                    glowColor = if (card.isMatched) AuroraMint else NebulaViolet,
                    contentPadding = 6.dp,
                    modifier = Modifier
                        .aspectRatio(0.85f)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (rotation > 90f) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.graphicsLayer { rotationY = 180f }
                            ) {
                                Text(card.symbol, style = MaterialTheme.typography.headlineMedium, color = StarlightGold)
                                Text(card.label, style = MaterialTheme.typography.labelSmall, color = SilverMist)
                            }
                        } else {
                            Text("?", style = MaterialTheme.typography.headlineMedium, color = SoftLavender)
                        }
                    }
                }
            }
        }

        if (completed) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Matched all pairs in ${elapsedSec}s!", style = MaterialTheme.typography.titleMedium, color = AuroraMint)
                GlowingPillButton(
                    text = "New Round",
                    icon = Icons.Rounded.Refresh,
                    onClick = { setup(pairCount) }
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}
