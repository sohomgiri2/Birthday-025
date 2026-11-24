package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEngine
import com.example.config.AppConfig
import com.example.data.AchievementDef
import com.example.data.LetterItem
import com.example.data.SGDataVault
import com.example.data.UserPhotoRecord
import com.example.data.VaultSettings
import com.example.data.VaultSnapshot
import com.example.data.WorldAchievements
import com.example.data.WorldSection
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val sourceUri: String,
    val isBuiltInSynth: Boolean
)

data class MainUiState(
    val showIntro: Boolean = true,
    val currentSectionIndex: Int = 0,
    val vault: VaultSnapshot = VaultSnapshot(),
    val letters: List<LetterItem> = emptyList(),
    val activeSecretMessage: String? = null,
    val activeSurprisePopup: String? = null,
    val activeAchievementToast: AchievementDef? = null,
    val tracks: List<MusicTrack> = emptyList(),
    val currentTrackIndex: Int = 0,
    val isPlayingMusic: Boolean = false,
    val musicProgress: Float = 0f,
    val statusBanner: String? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val dataVault = SGDataVault(application.applicationContext)
    val soundEngine = SoundEngine(application.applicationContext)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var popupLoopJob: Job? = null
    private var musicProgressJob: Job? = null
    private val recentSecretIndices = ArrayDeque<Int>()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            val loaded = dataVault.loadSnapshot()
            soundEngine.sfxEnabled = loaded.settings.sfxEnabled
            soundEngine.bgmEnabled = loaded.settings.bgmEnabled
            soundEngine.updateVolume(loaded.settings.volume)

            val builtLetters = buildLetters()
            val discoveredTracks = buildTrackList()

            _uiState.update {
                it.copy(
                    vault = loaded,
                    letters = builtLetters,
                    tracks = discoveredTracks
                )
            }
            restartSurpriseLoop()
        }
    }

    private fun buildLetters(): List<LetterItem> {
        return listOf(
            LetterItem(
                id = "letter_welcome",
                title = "Welcome to Your Little World",
                recipient = "For You",
                body = "Today is about celebrating the warmth, laughter, and quiet kindness you bring into everyone's days.\n\nTake your time exploring every corner of this space — there are hidden stars, small games, and little surprises tucked away just for you.",
                attachedAssetPath = dataVault.discoverLetterAsset(1)
            ),
            LetterItem(
                id = "letter_friendship",
                title = "To Better Days Ahead",
                recipient = "A Note on Friendship",
                body = "Some friendships don't need loud announcements. They show up in random check-ins, shared jokes during stressful weeks, and knowing someone genuinely hopes things go well for you.\n\nHere's to a year full of peaceful mornings, wins you worked hard for, and reasons to grin.",
                attachedAssetPath = dataVault.discoverLetterAsset(2)
            ),
            LetterItem(
                id = "letter_gayatri",
                title = AppConfig.GAYATRI_LETTER_TITLE,
                recipient = AppConfig.GAYATRI_LETTER_RECIPIENT,
                body = AppConfig.GAYATRI_LETTER_TEXT,
                attachedAssetPath = dataVault.discoverLetterAsset(3),
                isGayatriFavor = true
            )
        )
    }

    private fun buildTrackList(): List<MusicTrack> {
        val localFiles = dataVault.listLocalMusicFiles().mapIndexed { idx, (name, uri) ->
            MusicTrack(
                id = "local_$idx",
                title = name,
                artist = "Local Audio",
                sourceUri = uri,
                isBuiltInSynth = false
            )
        }
        val synthFallback = listOf(
            MusicTrack("synth_0", "Midnight Glass (Ambient)", "Cosmic Synth", "synth:0", true),
            MusicTrack("synth_1", "Starlight Drift (Ambient)", "Cosmic Synth", "synth:1", true),
            MusicTrack("synth_2", "Obsidian Echo (Ambient)", "Cosmic Synth", "synth:2", true)
        )
        return localFiles + synthFallback
    }

    fun finishIntro() {
        soundEngine.playSfx(SoundEngine.SfxType.CELEBRATION)
        _uiState.update { it.copy(showIntro = false) }
        unlockAchievement("first_explore")
    }

    fun replayIntro() {
        soundEngine.playSfx(SoundEngine.SfxType.NAVIGATE)
        _uiState.update { it.copy(showIntro = true, currentSectionIndex = 0) }
    }

    fun selectSection(index: Int) {
        val sections = WorldSection.entries
        val safeIndex = index.coerceIn(0, sections.lastIndex)
        val section = sections[safeIndex]
        soundEngine.playSfx(SoundEngine.SfxType.NAVIGATE)

        updateVault { current ->
            val updatedExplored = current.exploredSections + section.name
            current.copy(exploredSections = updatedExplored)
        }
        unlockAchievement("first_explore")
        _uiState.update { it.copy(currentSectionIndex = safeIndex) }
    }

    fun navigateNext() {
        val next = (_uiState.value.currentSectionIndex + 1).coerceAtMost(WorldSection.entries.lastIndex)
        selectSection(next)
    }

    fun navigateBack() {
        val prev = (_uiState.value.currentSectionIndex - 1).coerceAtLeast(0)
        selectSection(prev)
    }

    fun markCandlesExtinguished(wishText: String) {
        soundEngine.playSfx(SoundEngine.SfxType.CELEBRATION)
        updateVault {
            it.copy(
                candlesExtinguished = true,
                birthdayWishText = wishText.ifBlank { it.birthdayWishText }
            )
        }
        unlockAchievement("candles_blown")
    }

    fun resetBirthdayCandles() {
        soundEngine.playSfx(SoundEngine.SfxType.TAP)
        updateVault { it.copy(candlesExtinguished = false) }
    }

    fun markLetterOpened(letter: LetterItem) {
        soundEngine.playSfx(SoundEngine.SfxType.ENVELOPE)
        updateVault { it.copy(openedLetters = it.openedLetters + letter.id) }
        unlockAchievement("first_letter")
        if (letter.isGayatriFavor) {
            unlockAchievement("gayatri_read")
        }
    }

    fun markGayatriResponded() {
        soundEngine.playSfx(SoundEngine.SfxType.STAR)
        updateVault { it.copy(gayatriResponded = true) }
        showBanner("Thank you for reading, Gayatri.")
    }

    fun unlockSecretRoom() {
        soundEngine.playSfx(SoundEngine.SfxType.UNLOCK)
        updateVault { it.copy(secretRoomUnlocked = true) }
        unlockAchievement("secret_room")
    }

    fun relockSecretRoom() {
        soundEngine.playSfx(SoundEngine.SfxType.TAP)
        updateVault { it.copy(secretRoomUnlocked = false) }
    }

    fun openGiftBox(giftId: String) {
        soundEngine.playSfx(SoundEngine.SfxType.GIFT)
        updateVault {
            val nextGifts = it.openedGifts + giftId
            it.copy(openedGifts = nextGifts)
        }
        if (_uiState.value.vault.openedGifts.size >= 6) {
            unlockAchievement("all_gifts")
        }
    }

    fun interactWithPet(action: String) {
        soundEngine.playSfx(SoundEngine.SfxType.STAR)
        updateVault { current ->
            val p = current.pet
            val newReactions = p.unlockedReactions.toMutableSet()
            val nextInteractions = p.interactions + 1
            if (nextInteractions >= 2) newReactions.add("Starry Blink")
            if (nextInteractions >= 4) newReactions.add("Cosmic Spin")
            if (nextInteractions >= 6) newReactions.add("Aurora Glow")

            val updatedPet = when (action) {
                "feed" -> p.copy(
                    hunger = (p.hunger + 18).coerceAtMost(100),
                    happiness = (p.happiness + 10).coerceAtMost(100),
                    interactions = nextInteractions,
                    unlockedReactions = newReactions
                )
                "play" -> p.copy(
                    happiness = (p.happiness + 16).coerceAtMost(100),
                    energy = (p.energy - 10).coerceAtLeast(10),
                    hunger = (p.hunger - 6).coerceAtLeast(10),
                    interactions = nextInteractions,
                    unlockedReactions = newReactions
                )
                "sleep" -> p.copy(
                    energy = (p.energy + 24).coerceAtMost(100),
                    happiness = (p.happiness + 6).coerceAtMost(100),
                    interactions = nextInteractions,
                    unlockedReactions = newReactions
                )
                else -> p.copy(
                    happiness = (p.happiness + 8).coerceAtMost(100),
                    interactions = nextInteractions,
                    unlockedReactions = newReactions
                )
            }
            current.copy(pet = updatedPet)
        }
        if (_uiState.value.vault.pet.interactions >= 5) {
            unlockAchievement("pet_friend")
        }
    }

    fun recordGameResult(
        gameKey: String,
        score: Int = 0,
        starsEarned: Int = 0,
        memorySeconds: Int = 0
    ) {
        unlockAchievement("first_game")
        updateVault { current ->
            val g = current.gameStats
            val newStars = g.totalStarsCollected + starsEarned
            val updated = when (gameKey) {
                "slice" -> g.copy(sliceBest = maxOf(g.sliceBest, score), totalStarsCollected = newStars)
                "catch" -> g.copy(catchStarsBest = maxOf(g.catchStarsBest, score), totalStarsCollected = newStars)
                "memory" -> g.copy(
                    memoryBestSeconds = if (g.memoryBestSeconds == 0) memorySeconds else minOf(g.memoryBestSeconds, memorySeconds),
                    totalStarsCollected = newStars
                )
                "tap" -> g.copy(tapStarsBest = maxOf(g.tapStarsBest, score), totalStarsCollected = newStars)
                "wheel" -> g.copy(wheelSpins = g.wheelSpins + 1, totalStarsCollected = newStars)
                "scratch" -> g.copy(scratchCardsCompleted = g.scratchCardsCompleted + 1, totalStarsCollected = newStars)
                else -> g.copy(totalStarsCollected = newStars)
            }
            current.copy(gameStats = updated)
        }
        if (_uiState.value.vault.gameStats.totalStarsCollected >= 10) {
            unlockAchievement("stars_10")
        }
    }

    fun addPickedPhoto(uri: Uri, caption: String) {
        viewModelScope.launch {
            val record = dataVault.importPhotoFromUri(uri, caption)
            if (record != null) {
                soundEngine.playSfx(SoundEngine.SfxType.STAR)
                updateVault { it.copy(userPhotos = it.userPhotos + record) }
                showBanner("Photo saved to SG DataVault")
            } else {
                showBanner("Could not load selected photo")
            }
        }
    }

    fun addPickedAudio(uri: Uri, name: String) {
        viewModelScope.launch {
            val file = dataVault.importAudioFromUri(uri, name)
            if (file != null) {
                val updated = buildTrackList()
                _uiState.update { it.copy(tracks = updated) }
                showBanner("Added track to Music Room")
            } else {
                showBanner("Could not import audio file")
            }
        }
    }

    fun playOrToggleTrack(index: Int? = null) {
        val state = _uiState.value
        val targetIndex = index ?: state.currentTrackIndex
        val track = state.tracks.getOrNull(targetIndex) ?: return

        if (index == null && state.isPlayingMusic) {
            soundEngine.pausePlayback()
            musicProgressJob?.cancel()
            _uiState.update { it.copy(isPlayingMusic = false) }
            return
        }

        _uiState.update {
            it.copy(
                currentTrackIndex = targetIndex,
                isPlayingMusic = true,
                musicProgress = 0f
            )
        }
        soundEngine.playLocalTrack(track.sourceUri) {
            nextTrack()
        }
        startMusicProgressTracker()
    }

    fun nextTrack() {
        val tracks = _uiState.value.tracks
        if (tracks.isEmpty()) return
        val next = (_uiState.value.currentTrackIndex + 1) % tracks.size
        playOrToggleTrack(next)
    }

    fun prevTrack() {
        val tracks = _uiState.value.tracks
        if (tracks.isEmpty()) return
        val prev = (_uiState.value.currentTrackIndex - 1 + tracks.size) % tracks.size
        playOrToggleTrack(prev)
    }

    fun seekMusic(fraction: Float) {
        soundEngine.seekToFraction(fraction)
        _uiState.update { it.copy(musicProgress = fraction) }
    }

    private fun startMusicProgressTracker() {
        musicProgressJob?.cancel()
        musicProgressJob = viewModelScope.launch {
            while (isActive && _uiState.value.isPlayingMusic) {
                val (pos, dur) = soundEngine.getPlaybackProgress()
                if (dur > 0 && pos >= 0) {
                    _uiState.update { it.copy(musicProgress = (pos.toFloat() / dur).coerceIn(0f, 1f)) }
                }
                delay(400)
            }
        }
    }

    fun revealRandomSecretMessage(customMessage: String? = null) {
        soundEngine.playSfx(SoundEngine.SfxType.STAR)
        if (customMessage != null) {
            _uiState.update { it.copy(activeSecretMessage = customMessage) }
            return
        }
        val pool = AppConfig.SECRET_MESSAGES
        val availableIndices = pool.indices.filter { it !in recentSecretIndices }
        val chosenIdx = if (availableIndices.isNotEmpty()) availableIndices.random() else pool.indices.random()
        recentSecretIndices.addLast(chosenIdx)
        if (recentSecretIndices.size > 10) recentSecretIndices.removeFirst()

        updateVault { it.copy(seenSecretMessages = it.seenSecretMessages + chosenIdx) }
        _uiState.update { it.copy(activeSecretMessage = pool[chosenIdx]) }
    }

    fun dismissSecretMessage() {
        _uiState.update { it.copy(activeSecretMessage = null) }
    }

    fun dismissSurprisePopup() {
        _uiState.update { it.copy(activeSurprisePopup = null) }
    }

    fun dismissBanner() {
        _uiState.update { it.copy(statusBanner = null) }
    }

    fun showBanner(message: String) {
        _uiState.update { it.copy(statusBanner = message) }
        viewModelScope.launch {
            delay(3200)
            if (_uiState.value.statusBanner == message) {
                _uiState.update { it.copy(statusBanner = null) }
            }
        }
    }

    fun updateSettings(transform: (VaultSettings) -> VaultSettings) {
        updateVault { current ->
            val nextSettings = transform(current.settings)
            soundEngine.sfxEnabled = nextSettings.sfxEnabled
            soundEngine.bgmEnabled = nextSettings.bgmEnabled
            soundEngine.updateVolume(nextSettings.volume)
            if (!nextSettings.bgmEnabled && _uiState.value.isPlayingMusic) {
                soundEngine.pausePlayback()
                _uiState.update { it.copy(isPlayingMusic = false) }
            }
            current.copy(settings = nextSettings)
        }
        restartSurpriseLoop()
    }

    private fun restartSurpriseLoop() {
        popupLoopJob?.cancel()
        val settings = _uiState.value.vault.settings
        if (!settings.surprisePopupsEnabled) return
        popupLoopJob = viewModelScope.launch {
            while (isActive) {
                val freqMs = settings.popupFrequencySeconds.coerceIn(20, 180) * 1000L
                delay(freqMs)
                val current = _uiState.value
                if (!current.showIntro && current.vault.settings.surprisePopupsEnabled && current.activeSurprisePopup == null) {
                    val msg = AppConfig.SURPRISE_POPUPS.random()
                    _uiState.update { it.copy(activeSurprisePopup = msg) }
                }
            }
        }
    }

    fun unlockAchievement(id: String) {
        val current = _uiState.value.vault.unlockedAchievements
        if (id in current) return
        val def = WorldAchievements.ALL.find { it.id == id } ?: return
        soundEngine.playSfx(SoundEngine.SfxType.ACHIEVEMENT)
        updateVault { it.copy(unlockedAchievements = it.unlockedAchievements + id) }
        _uiState.update { it.copy(activeAchievementToast = def) }
        viewModelScope.launch {
            delay(3400)
            if (_uiState.value.activeAchievementToast?.id == id) {
                _uiState.update { it.copy(activeAchievementToast = null) }
            }
        }
    }

    fun resetProgressOnly() {
        soundEngine.playSfx(SoundEngine.SfxType.TAP)
        val preservedSettings = _uiState.value.vault.settings
        val preservedPhotos = _uiState.value.vault.userPhotos
        val fresh = VaultSnapshot(settings = preservedSettings, userPhotos = preservedPhotos)
        viewModelScope.launch {
            dataVault.saveSnapshot(fresh)
            _uiState.update { it.copy(vault = fresh, currentSectionIndex = 0) }
            showBanner("Progress reset while keeping settings & photos")
        }
    }

    fun clearAllVaultData() {
        soundEngine.playSfx(SoundEngine.SfxType.TAP)
        soundEngine.stopPlayback()
        viewModelScope.launch {
            dataVault.clearAllData()
            val fresh = dataVault.loadSnapshot()
            _uiState.update {
                it.copy(
                    vault = fresh,
                    tracks = buildTrackList(),
                    isPlayingMusic = false,
                    currentSectionIndex = 0
                )
            }
            showBanner("SG DataVault cleared")
        }
    }

    suspend fun exportVaultJsonString(): String {
        return dataVault.exportVaultJson(_uiState.value.vault)
    }

    fun importVaultJsonString(json: String) {
        viewModelScope.launch {
            val imported = dataVault.importVaultJson(json)
            if (imported != null) {
                soundEngine.sfxEnabled = imported.settings.sfxEnabled
                soundEngine.bgmEnabled = imported.settings.bgmEnabled
                soundEngine.updateVolume(imported.settings.volume)
                _uiState.update { it.copy(vault = imported) }
                showBanner("SG DataVault backup restored")
            } else {
                soundEngine.playSfx(SoundEngine.SfxType.ERROR)
                showBanner("Invalid backup JSON file")
            }
        }
    }

    private fun updateVault(transform: (VaultSnapshot) -> VaultSnapshot) {
        val next = transform(_uiState.value.vault)
        _uiState.update { it.copy(vault = next) }
        viewModelScope.launch {
            dataVault.saveSnapshot(next)
        }
    }

    override fun onCleared() {
        soundEngine.stopPlayback()
        super.onCleared()
    }
}
