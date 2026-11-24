package com.example.ui.sections

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.data.VaultSettings
import com.example.ui.MusicTrack
import com.example.ui.components.GlowingPillButton
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.SubtleGlassButton
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold
import kotlin.math.abs
import kotlin.math.sin

@Composable
fun MusicRoomSection(
    tracks: List<MusicTrack>,
    currentTrackIndex: Int,
    isPlaying: Boolean,
    progress: Float,
    settings: VaultSettings,
    onTogglePlay: (Int?) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onSeek: (Float) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleSfx: (Boolean) -> Unit,
    onToggleBgm: (Boolean) -> Unit,
    onImportAudio: (Uri, String) -> Unit
) {
    val activeTrack = tracks.getOrNull(currentTrackIndex)
    val hasLocalMp3 = tracks.any { !it.isBuiltInSynth }

    val audioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportAudio(uri, "track_${System.currentTimeMillis()}.mp3")
        }
    }

    val infinite = rememberInfiniteTransition(label = "visualizer")
    val animPhase by if (isPlaying && !settings.reduceAnimations) {
        infinite.animateFloat(
            initialValue = 0f,
            targetValue = 6.283f,
            animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Restart),
            label = "viz_phase"
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("MUSIC ROOM", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
                Text("Sound & Ambience", style = MaterialTheme.typography.headlineMedium, color = SilverMist)
            }
            SubtleGlassButton(
                text = "Add Audio",
                icon = Icons.Rounded.LibraryMusic,
                onClick = { audioPicker.launch(arrayOf("audio/*")) }
            )
        }

        // Now Playing Card + Visualizer
        LiquidGlassCard(
            modifier = Modifier.fillMaxWidth(),
            glowColor = if (isPlaying) NebulaViolet else SoftLavender
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Visualizer Bars
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                ) {
                    val bars = 22
                    val spacing = size.width / bars
                    for (i in 0 until bars) {
                        val amp = if (isPlaying) {
                            0.25f + 0.7f * abs(sin(animPhase + i * 0.45f))
                        } else {
                            0.18f
                        }
                        val barH = size.height * amp
                        drawRoundRect(
                            brush = Brush.verticalGradient(listOf(StarlightGold, NebulaViolet)),
                            topLeft = Offset(i * spacing + spacing * 0.2f, size.height - barH),
                            size = Size(spacing * 0.55f, barH),
                            cornerRadius = CornerRadius(6f)
                        )
                    }
                }

                Text(
                    text = activeTrack?.title ?: "No Track Selected",
                    style = MaterialTheme.typography.titleLarge,
                    color = SilverMist
                )
                Text(
                    text = activeTrack?.artist ?: "Add music/song1.mp3 in assets",
                    style = MaterialTheme.typography.labelSmall,
                    color = SoftLavender
                )

                Slider(
                    value = progress,
                    onValueChange = onSeek,
                    enabled = activeTrack != null && !activeTrack.isBuiltInSynth
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onPrev) {
                        Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous track", tint = SilverMist)
                    }
                    GlowingPillButton(
                        text = if (isPlaying) "Pause" else "Play",
                        icon = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        onClick = { onTogglePlay(null) }
                    )
                    IconButton(onClick = onNext) {
                        Icon(Icons.Rounded.SkipNext, contentDescription = "Next track", tint = SilverMist)
                    }
                }
            }
        }

        if (!hasLocalMp3) {
            LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tip: No local MP3 files were bundled in assets/music/ yet, so 3 built-in offline cosmic synth soundscapes are ready above. You can also tap Add Audio to play any track from your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SilverMist.copy(alpha = 0.8f)
                )
            }
        }

        // Volume & Toggles
        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = if (settings.volume <= 0.02f) Icons.Rounded.VolumeOff else Icons.Rounded.VolumeUp,
                        contentDescription = "Volume",
                        tint = SoftLavender
                    )
                    Slider(
                        value = settings.volume,
                        onValueChange = onVolumeChange,
                        modifier = Modifier.weight(1f)
                    )
                    Text("${(settings.volume * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = SilverMist)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sound Effects (UI & Games)", style = MaterialTheme.typography.bodyMedium, color = SilverMist)
                    Switch(checked = settings.sfxEnabled, onCheckedChange = onToggleSfx)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Allow Background Music", style = MaterialTheme.typography.bodyMedium, color = SilverMist)
                    Switch(checked = settings.bgmEnabled, onCheckedChange = onToggleBgm)
                }
            }
        }

        // Playlist
        LiquidGlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PLAYLIST", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
                tracks.forEachIndexed { idx, track ->
                    val selected = idx == currentTrackIndex
                    LiquidGlassCard(
                        onClick = { onTogglePlay(idx) },
                        glowColor = if (selected) AuroraMint else NebulaViolet,
                        contentPadding = 12.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.MusicNote,
                                    contentDescription = null,
                                    tint = if (selected) AuroraMint else SoftLavender
                                )
                                Column {
                                    Text(track.title, style = MaterialTheme.typography.titleMedium, color = SilverMist)
                                    Text(track.artist, style = MaterialTheme.typography.labelSmall, color = SilverMist.copy(alpha = 0.65f))
                                }
                            }
                            if (selected && isPlaying) {
                                Text("PLAYING", style = MaterialTheme.typography.labelSmall, color = AuroraMint)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
