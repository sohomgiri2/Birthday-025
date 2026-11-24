package com.example.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Cake
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MailOutline
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * All active sections in "A Little World Made for You".
 * Note: Scrapbook and Friendship/Memory Timeline have been removed completely per user request.
 * Progress & indexing derive directly from this ordered enum so slide numbers stay accurate.
 */
enum class WorldSection(
    val title: String,
    val subtitle: String,
    val icon: ImageVector
) {
    BIRTHDAY("Birthday", "Make a wish & blow the candles", Icons.Rounded.Cake),
    GALLERY("Memories", "Floating 3D gallery & photo wall", Icons.Rounded.PhotoLibrary),
    LETTERS("Letters", "Envelopes & Gayatri's favor", Icons.Rounded.MailOutline),
    GAMES("Mini-Games", "Six interactive cosmic games", Icons.Rounded.SportsEsports),
    UNIVERSE("3D Universe", "Interactive stars & planets", Icons.Rounded.Public),
    VIRTUAL_PET("Nova Pet", "Your little cosmic companion", Icons.Rounded.Pets),
    GIFT_BOXES("Mystery Gifts", "3D boxes filled with surprises", Icons.Rounded.CardGiftcard),
    SECRET_ROOM("Secret Room", "Hidden vault behind a code", Icons.Rounded.Lock),
    MUSIC_ROOM("Music Room", "Offline audio & visualizer", Icons.Rounded.MusicNote),
    ACHIEVEMENTS("Achievements", "Milestones & progress tracker", Icons.Rounded.EmojiEvents),
    FINAL_SECRET("Final World", "The closing chapter", Icons.Rounded.AutoAwesome),
    SETTINGS("Vault & Info", "Settings, SG DataVault & Credits", Icons.Rounded.Settings)
}

data class VaultSettings(
    val bgmEnabled: Boolean = false,
    val sfxEnabled: Boolean = true,
    val particlesEnabled: Boolean = true,
    val reduceAnimations: Boolean = false,
    val frostVariation: Boolean = false,
    val surprisePopupsEnabled: Boolean = true,
    val popupFrequencySeconds: Int = 45,
    val volume: Float = 0.75f,
    val immersiveFullscreen: Boolean = false
)

data class PetState(
    val name: String = "Nova",
    val happiness: Int = 78,
    val energy: Int = 82,
    val hunger: Int = 74,
    val interactions: Int = 0,
    val unlockedReactions: Set<String> = setOf("Purr")
)

data class GameStats(
    val sliceBest: Int = 0,
    val catchStarsBest: Int = 0,
    val memoryBestSeconds: Int = 0,
    val tapStarsBest: Int = 0,
    val wheelSpins: Int = 0,
    val scratchCardsCompleted: Int = 0,
    val totalStarsCollected: Int = 0
)

data class UserPhotoRecord(
    val id: String,
    val fileName: String,
    val caption: String,
    val secretNote: String,
    val isAsset: Boolean,
    val assetPath: String? = null,
    val localFilePath: String? = null
)

data class LetterItem(
    val id: String,
    val title: String,
    val recipient: String,
    val body: String,
    val attachedAssetPath: String? = null,
    val isGayatriFavor: Boolean = false
)

data class AchievementDef(
    val id: String,
    val title: String,
    val description: String
)

object WorldAchievements {
    val ALL = listOf(
        AchievementDef("first_explore", "First Step", "Explored your first section in this little world"),
        AchievementDef("candles_blown", "Wish Maker", "Extinguished all birthday candles"),
        AchievementDef("first_letter", "Sealed & Read", "Opened your first letter"),
        AchievementDef("gayatri_read", "A Small Favor", "Read Gayatri's special favor letter"),
        AchievementDef("first_game", "Player One", "Played a mini-game in the hub"),
        AchievementDef("stars_10", "Stargazer", "Collected 10+ stars across mini-games"),
        AchievementDef("all_gifts", "Collector", "Opened all 6 mystery gift boxes"),
        AchievementDef("pet_friend", "Cosmic Caretaker", "Interacted with Nova 5 times"),
        AchievementDef("secret_room", "Vault Breaker", "Unlocked the Secret Room")
    )
}

data class VaultSnapshot(
    val settings: VaultSettings = VaultSettings(),
    val pet: PetState = PetState(),
    val gameStats: GameStats = GameStats(),
    val exploredSections: Set<String> = setOf(WorldSection.BIRTHDAY.name),
    val openedLetters: Set<String> = emptySet(),
    val openedGifts: Set<String> = emptySet(),
    val unlockedAchievements: Set<String> = emptySet(),
    val candlesExtinguished: Boolean = false,
    val birthdayWishText: String = "",
    val gayatriResponded: Boolean = false,
    val secretRoomUnlocked: Boolean = false,
    val userPhotos: List<UserPhotoRecord> = emptyList(),
    val seenSecretMessages: Set<Int> = emptySet()
)
