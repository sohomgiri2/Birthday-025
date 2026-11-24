package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.audio.SoundEngine
import com.example.data.WorldSection
import com.example.ui.MainUiState
import com.example.ui.MainViewModel
import com.example.ui.components.CosmicBackdrop
import com.example.ui.components.HiddenStarTrigger
import com.example.ui.components.LiquidGlassCard
import com.example.ui.components.SubtleGlassButton
import com.example.ui.sections.AchievementsSection
import com.example.ui.sections.BirthdaySection
import com.example.ui.sections.FinalSecretSection
import com.example.ui.sections.GallerySection
import com.example.ui.sections.GamesHubSection
import com.example.ui.sections.GiftBoxesSection
import com.example.ui.sections.IntroScreen
import com.example.ui.sections.LettersSection
import com.example.ui.sections.MusicRoomSection
import com.example.ui.sections.SecretRoomSection
import com.example.ui.sections.SettingsVaultDeveloperSection
import com.example.ui.sections.UniverseSection
import com.example.ui.sections.VirtualPetSection
import com.example.ui.theme.AuroraMint
import com.example.ui.theme.NebulaViolet
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.SilverMist
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.StarlightGold
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: MainViewModel = viewModel()
            val state by viewModel.uiState.collectAsStateWithLifecycle()

            LittleWorldThemeWrapper(frostVariation = state.vault.settings.frostVariation) {
                LittleWorldApp(viewModel = viewModel, state = state)
            }
        }
    }
}

@Composable
private fun LittleWorldThemeWrapper(
    frostVariation: Boolean,
    content: @Composable () -> Unit
) {
    com.example.ui.theme.LittleWorldTheme(
        frostVariation = frostVariation,
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LittleWorldApp(
    viewModel: MainViewModel,
    state: MainUiState
) {
    val sections = WorldSection.entries
    val pagerState = rememberPagerState(
        initialPage = state.currentSectionIndex,
        pageCount = { sections.size }
    )
    val scope = rememberCoroutineScope()
    var showSectionMenu by remember { mutableStateOf(false) }

    // Keep ViewModel and Pager synchronized without duplicate jumps
    LaunchedEffect(state.currentSectionIndex) {
        if (pagerState.currentPage != state.currentSectionIndex) {
            pagerState.animateScrollToPage(state.currentSectionIndex)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { settled ->
                if (!state.showIntro && settled != state.currentSectionIndex) {
                    viewModel.selectSection(settled)
                }
            }
    }

    BackHandler(enabled = !state.showIntro && state.currentSectionIndex > 0) {
        viewModel.selectSection(0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CosmicBackdrop(
            particlesEnabled = state.vault.settings.particlesEnabled,
            reduceAnimations = state.vault.settings.reduceAnimations,
            frostVariation = state.vault.settings.frostVariation
        )

        if (state.showIntro) {
            IntroScreen(
                reduceAnimations = state.vault.settings.reduceAnimations,
                onEnterWorld = { viewModel.finishIntro() },
                onGiftTapSound = { viewModel.soundEngine.playSfx(SoundEngine.SfxType.GIFT) }
            )
        } else {
            val activeSection = sections[state.currentSectionIndex]
            val allowPagerSwipe = activeSection != WorldSection.UNIVERSE && activeSection != WorldSection.GAMES

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                topBar = {
                    if (!state.vault.settings.immersiveFullscreen) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel.selectSection(0) },
                                        modifier = Modifier.testTag("home_nav_button")
                                    ) {
                                        Icon(Icons.Rounded.Home, contentDescription = "Home section", tint = SilverMist)
                                    }
                                    Column {
                                        Text(
                                            text = activeSection.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = SilverMist
                                        )
                                        Text(
                                            text = "Section ${state.currentSectionIndex + 1} / ${sections.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SoftLavender
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Secret hidden star trigger inside top bar
                                    HiddenStarTrigger(
                                        onClick = { viewModel.revealRandomSecretMessage() },
                                        modifier = Modifier.testTag("secret_star_trigger")
                                    )
                                    IconButton(
                                        onClick = { showSectionMenu = true },
                                        modifier = Modifier.testTag("sections_menu_button")
                                    ) {
                                        Icon(Icons.Rounded.GridView, contentDescription = "All sections menu", tint = SilverMist)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (state.currentSectionIndex + 1f) / sections.size.toFloat() },
                                color = NebulaViolet,
                                trackColor = Color.White.copy(alpha = 0.1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                },
                bottomBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SubtleGlassButton(
                            text = "Back",
                            icon = Icons.AutoMirrored.Rounded.ArrowBack,
                            testTag = "slide_back_button",
                            onClick = { viewModel.navigateBack() }
                        )

                        if (state.vault.settings.immersiveFullscreen) {
                            IconButton(onClick = { showSectionMenu = true }) {
                                Icon(Icons.Rounded.GridView, contentDescription = "Sections menu", tint = SilverMist)
                            }
                        } else {
                            Text(
                                text = activeSection.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = SilverMist.copy(alpha = 0.65f),
                                maxLines = 1,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 10.dp)
                            )
                        }

                        SubtleGlassButton(
                            text = if (state.currentSectionIndex == sections.lastIndex) "Menu" else "Next",
                            icon = Icons.AutoMirrored.Rounded.ArrowForward,
                            testTag = "slide_next_button",
                            onClick = {
                                if (state.currentSectionIndex == sections.lastIndex) {
                                    showSectionMenu = true
                                } else {
                                    viewModel.navigateNext()
                                }
                            }
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    HorizontalPager(
                        state = pagerState,
                        userScrollEnabled = allowPagerSwipe,
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 680.dp)
                    ) { page ->
                        when (sections[page]) {
                            WorldSection.BIRTHDAY -> BirthdaySection(
                                candlesAlreadyExtinguished = state.vault.candlesExtinguished,
                                savedWish = state.vault.birthdayWishText,
                                reduceAnimations = state.vault.settings.reduceAnimations,
                                onAllCandlesExtinguished = { wish -> viewModel.markCandlesExtinguished(wish) },
                                onReplayBirthday = { viewModel.resetBirthdayCandles() },
                                onCandleBlowSfx = { viewModel.soundEngine.playSfx(SoundEngine.SfxType.TAP) }
                            )

                            WorldSection.GALLERY -> GallerySection(
                                photos = state.vault.userPhotos,
                                reduceAnimations = state.vault.settings.reduceAnimations,
                                onAddPhoto = { uri, caption -> viewModel.addPickedPhoto(uri, caption) },
                                onRevealPhotoSecret = { note -> viewModel.revealRandomSecretMessage(note) }
                            )

                            WorldSection.LETTERS -> LettersSection(
                                letters = state.letters,
                                openedLetters = state.vault.openedLetters,
                                gayatriResponded = state.vault.gayatriResponded,
                                reduceAnimations = state.vault.settings.reduceAnimations,
                                onOpenLetter = { letter -> viewModel.markLetterOpened(letter) },
                                onGayatriAction = { viewModel.markGayatriResponded() }
                            )

                            WorldSection.GAMES -> GamesHubSection(
                                stats = state.vault.gameStats,
                                onPlaySfx = { type -> viewModel.soundEngine.playSfx(type) },
                                onRecordResult = { key, score, stars, memSec ->
                                    viewModel.recordGameResult(key, score, stars, memSec)
                                }
                            )

                            WorldSection.UNIVERSE -> UniverseSection(
                                reduceAnimations = state.vault.settings.reduceAnimations,
                                onPlanetTapSfx = { viewModel.soundEngine.playSfx(SoundEngine.SfxType.STAR) }
                            )

                            WorldSection.VIRTUAL_PET -> VirtualPetSection(
                                pet = state.vault.pet,
                                reduceAnimations = state.vault.settings.reduceAnimations,
                                onAction = { act -> viewModel.interactWithPet(act) }
                            )

                            WorldSection.GIFT_BOXES -> GiftBoxesSection(
                                vault = state.vault,
                                reduceAnimations = state.vault.settings.reduceAnimations,
                                onOpenBox = { id -> viewModel.openGiftBox(id) }
                            )

                            WorldSection.SECRET_ROOM -> SecretRoomSection(
                                isUnlocked = state.vault.secretRoomUnlocked,
                                onUnlockSuccess = { viewModel.unlockSecretRoom() },
                                onRelock = { viewModel.relockSecretRoom() },
                                onErrorSfx = { viewModel.soundEngine.playSfx(SoundEngine.SfxType.ERROR) },
                                onTapSfx = { viewModel.soundEngine.playSfx(SoundEngine.SfxType.TAP) }
                            )

                            WorldSection.MUSIC_ROOM -> MusicRoomSection(
                                tracks = state.tracks,
                                currentTrackIndex = state.currentTrackIndex,
                                isPlaying = state.isPlayingMusic,
                                progress = state.musicProgress,
                                settings = state.vault.settings,
                                onTogglePlay = { idx -> viewModel.playOrToggleTrack(idx) },
                                onNext = { viewModel.nextTrack() },
                                onPrev = { viewModel.prevTrack() },
                                onSeek = { f -> viewModel.seekMusic(f) },
                                onVolumeChange = { v -> viewModel.updateSettings { it.copy(volume = v) } },
                                onToggleSfx = { b -> viewModel.updateSettings { it.copy(sfxEnabled = b) } },
                                onToggleBgm = { b -> viewModel.updateSettings { it.copy(bgmEnabled = b) } },
                                onImportAudio = { uri, name -> viewModel.addPickedAudio(uri, name) }
                            )

                            WorldSection.ACHIEVEMENTS -> AchievementsSection(vault = state.vault)

                            WorldSection.FINAL_SECRET -> FinalSecretSection(
                                vault = state.vault,
                                reduceAnimations = state.vault.settings.reduceAnimations,
                                onReplayExperience = { viewModel.replayIntro() },
                                onRevealFinalGift = { viewModel.revealRandomSecretMessage("Thank you for exploring every little corner of this world. Happy Birthday!") }
                            )

                            WorldSection.SETTINGS -> SettingsVaultDeveloperSection(
                                settings = state.vault.settings,
                                storageUsageBytes = viewModel.dataVault.getStorageUsageBytes(),
                                onUpdateSettings = { fn -> viewModel.updateSettings(fn) },
                                onExportJson = { viewModel.exportVaultJsonString() },
                                onImportJson = { raw -> viewModel.importVaultJsonString(raw) },
                                onResetProgress = { viewModel.resetProgressOnly() },
                                onClearAllData = { viewModel.clearAllVaultData() },
                                onShowMessage = { msg -> viewModel.showBanner(msg) }
                            )
                        }
                    }
                }
            }
        }

        // Achievement Unlock Toast Overlay
        AnimatedVisibility(
            visible = state.activeAchievementToast != null,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 54.dp, start = 20.dp, end = 20.dp)
        ) {
            state.activeAchievementToast?.let { ach ->
                LiquidGlassCard(glowColor = AuroraMint, contentPadding = 12.dp) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = StarlightGold)
                        Column {
                            Text("Achievement Unlocked!", style = MaterialTheme.typography.labelSmall, color = AuroraMint)
                            Text(ach.title, style = MaterialTheme.typography.titleMedium, color = SilverMist)
                        }
                    }
                }
            }
        }

        // Random Surprise Popup Overlay
        AnimatedVisibility(
            visible = state.activeSurprisePopup != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 72.dp, start = 20.dp, end = 20.dp)
        ) {
            state.activeSurprisePopup?.let { msg ->
                LiquidGlassCard(glowColor = StarlightGold, contentPadding = 14.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = StarlightGold)
                            Text(msg, style = MaterialTheme.typography.bodyMedium, color = SilverMist)
                        }
                        IconButton(onClick = { viewModel.dismissSurprisePopup() }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close surprise", tint = SilverMist)
                        }
                    }
                }
            }
        }

        // Status Banner Toast
        AnimatedVisibility(
            visible = state.statusBanner != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 76.dp, start = 20.dp, end = 20.dp)
        ) {
            state.statusBanner?.let { banner ->
                LiquidGlassCard(glowColor = NebulaViolet, contentPadding = 12.dp) {
                    Text(banner, style = MaterialTheme.typography.bodyMedium, color = SilverMist)
                }
            }
        }
    }

    // Secret Message Dialog
    state.activeSecretMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissSecretMessage() },
            title = { Text("Secret Starlight Note") },
            text = { Text(msg) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissSecretMessage() }) {
                    Text("Keep Smiling")
                }
            }
        )
    }

    // All-Sections Menu Modal Bottom Sheet
    if (showSectionMenu) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showSectionMenu = false },
            sheetState = sheetState,
            containerColor = ObsidianBlack.copy(alpha = 0.95f),
            contentColor = SilverMist
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text("EXPLORE SECTIONS", style = MaterialTheme.typography.labelSmall, color = SoftLavender)
                Spacer(modifier = Modifier.height(10.dp))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(146.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 28.dp),
                    modifier = Modifier.height(420.dp)
                ) {
                    itemsIndexed(sections) { idx, section ->
                        val selected = idx == state.currentSectionIndex
                        LiquidGlassCard(
                            onClick = {
                                showSectionMenu = false
                                scope.launch {
                                    viewModel.selectSection(idx)
                                }
                            },
                            glowColor = if (selected) StarlightGold else NebulaViolet,
                            contentPadding = 12.dp,
                            modifier = Modifier.testTag("menu_section_${section.name.lowercase()}")
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = section.icon,
                                    contentDescription = section.title,
                                    tint = if (selected) StarlightGold else SoftLavender,
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = "${idx + 1}. ${section.title}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SilverMist
                                )
                                Text(
                                    text = section.subtitle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SilverMist.copy(alpha = 0.65f),
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
