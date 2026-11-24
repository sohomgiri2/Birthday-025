package com.example.data

import android.content.Context
import android.net.Uri
import com.example.config.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * ============================================================================
 *  SG DataVault — Developer: Sohom Giri (@itz_sg_063)
 *  Offline-first atomic JSON persistence + local photo/media management.
 * ============================================================================
 */
class SGDataVault(private val context: Context) {

    private val vaultFile: File
        get() = File(context.filesDir, "sg_datavault_v1.json")

    private val photosDir: File
        get() = File(context.filesDir, "vault_photos").apply { if (!exists()) mkdirs() }

    private val musicDir: File
        get() = File(context.filesDir, "vault_music").apply { if (!exists()) mkdirs() }

    suspend fun loadSnapshot(): VaultSnapshot = withContext(Dispatchers.IO) {
        val base = try {
            if (!vaultFile.exists()) {
                VaultSnapshot()
            } else {
                parseSnapshot(vaultFile.readText())
            }
        } catch (_: Exception) {
            VaultSnapshot()
        }
        val discoveredAssetPhotos = discoverAssetPhotos()
        val persistedLocalPhotos = base.userPhotos.filter { !it.isAsset && it.localFilePath != null && File(it.localFilePath).exists() }
        base.copy(userPhotos = discoveredAssetPhotos + persistedLocalPhotos)
    }

    suspend fun saveSnapshot(snapshot: VaultSnapshot) = withContext(Dispatchers.IO) {
        try {
            val json = serializeSnapshot(snapshot)
            val tempFile = File(context.filesDir, "sg_datavault_v1.tmp")
            tempFile.writeText(json)
            if (vaultFile.exists()) vaultFile.delete()
            tempFile.renameTo(vaultFile)
        } catch (_: Exception) {
            // Handle storage errors gracefully without crashing
        }
    }

    suspend fun importPhotoFromUri(uri: Uri, caption: String): UserPhotoRecord? = withContext(Dispatchers.IO) {
        try {
            val id = UUID.randomUUID().toString().take(8)
            val dest = File(photosDir, "user_photo_$id.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null

            val note = AppConfig.SECRET_MESSAGES.random()
            UserPhotoRecord(
                id = "user_$id",
                fileName = dest.name,
                caption = caption.ifBlank { "Captured Memory" },
                secretNote = note,
                isAsset = false,
                localFilePath = dest.absolutePath
            )
        } catch (_: Exception) {
            null
        }
    }

    suspend fun importAudioFromUri(uri: Uri, displayName: String): File? = withContext(Dispatchers.IO) {
        try {
            val sanitized = displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_").ifBlank { "track_${System.currentTimeMillis()}.mp3" }
            val dest = File(musicDir, sanitized)
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null
            dest
        } catch (_: Exception) {
            null
        }
    }

    fun listLocalMusicFiles(): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        // 1. Check assets/music/song1..song50
        try {
            val assetList = context.assets.list(AppConfig.MUSIC_ASSET_DIR)?.toSet() ?: emptySet()
            assetList.sorted().forEach { name ->
                val ext = name.substringAfterLast('.', "").lowercase()
                if (ext in AppConfig.SUPPORTED_MUSIC_EXTENSIONS) {
                    list.add(name.substringBeforeLast('.') to "asset:${AppConfig.MUSIC_ASSET_DIR}/$name")
                }
            }
        } catch (_: Exception) { }

        // 2. Check user-imported local music
        musicDir.listFiles()?.sortedBy { it.name }?.forEach { file ->
            list.add(file.nameWithoutExtension to "file:${file.absolutePath}")
        }
        return list
    }

    /**
     * Automatically checks sequential filenames photo1..photo300 (.png, .jpg, .jpeg)
     * in app/src/main/assets/photos/ without needing manual code edits, skipping missing files.
     */
    fun discoverAssetPhotos(): List<UserPhotoRecord> {
        val found = mutableListOf<UserPhotoRecord>()
        try {
            val existingAssets = context.assets.list(AppConfig.PHOTOS_ASSET_DIR)?.toSet() ?: emptySet()
            if (existingAssets.isEmpty()) return emptyList()

            for (index in 1..AppConfig.MAX_NUMBERED_PHOTOS) {
                var matchedFile: String? = null
                for (ext in AppConfig.SUPPORTED_PHOTO_EXTENSIONS) {
                    val candidate = "${AppConfig.PHOTO_PREFIX}$index.$ext"
                    if (existingAssets.contains(candidate)) {
                        matchedFile = candidate
                        break
                    }
                }
                if (matchedFile != null) {
                    found.add(
                        UserPhotoRecord(
                            id = "asset_photo_$index",
                            fileName = matchedFile,
                            caption = "Memory #$index",
                            secretNote = AppConfig.SECRET_MESSAGES[(index - 1) % AppConfig.SECRET_MESSAGES.size],
                            isAsset = true,
                            assetPath = "file:///android_asset/${AppConfig.PHOTOS_ASSET_DIR}/$matchedFile"
                        )
                    )
                }
            }
        } catch (_: Exception) {
            // Gracefully return empty if no assets/photos folder exists
        }
        return found
    }

    fun discoverLetterAsset(letterIndex: Int): String? {
        return try {
            val files = context.assets.list(AppConfig.LETTERS_ASSET_DIR)?.toSet() ?: return null
            for (ext in AppConfig.SUPPORTED_PHOTO_EXTENSIONS) {
                val candidate = "letter$letterIndex.$ext"
                if (files.contains(candidate)) {
                    return "file:///android_asset/${AppConfig.LETTERS_ASSET_DIR}/$candidate"
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun exportVaultJson(snapshot: VaultSnapshot): String = withContext(Dispatchers.Default) {
        serializeSnapshot(snapshot, pretty = true)
    }

    suspend fun importVaultJson(rawJson: String): VaultSnapshot? = withContext(Dispatchers.Default) {
        try {
            val parsed = parseSnapshot(rawJson)
            saveSnapshot(parsed)
            loadSnapshot()
        } catch (_: Exception) {
            null
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        try {
            if (vaultFile.exists()) vaultFile.delete()
            photosDir.listFiles()?.forEach { it.delete() }
            musicDir.listFiles()?.forEach { it.delete() }
        } catch (_: Exception) { }
    }

    fun getStorageUsageBytes(): Long {
        var total = if (vaultFile.exists()) vaultFile.length() else 0L
        photosDir.listFiles()?.forEach { total += it.length() }
        musicDir.listFiles()?.forEach { total += it.length() }
        return total
    }

    private fun serializeSnapshot(s: VaultSnapshot, pretty: Boolean = false): String {
        val root = JSONObject()
        root.put("vault", "SG DataVault")
        root.put("developer", AppConfig.DEVELOPER_NAME)
        root.put("version", 1)

        val settingsObj = JSONObject().apply {
            put("bgmEnabled", s.settings.bgmEnabled)
            put("sfxEnabled", s.settings.sfxEnabled)
            put("particlesEnabled", s.settings.particlesEnabled)
            put("reduceAnimations", s.settings.reduceAnimations)
            put("frostVariation", s.settings.frostVariation)
            put("surprisePopupsEnabled", s.settings.surprisePopupsEnabled)
            put("popupFrequencySeconds", s.settings.popupFrequencySeconds)
            put("volume", s.settings.volume.toDouble())
            put("immersiveFullscreen", s.settings.immersiveFullscreen)
        }
        root.put("settings", settingsObj)

        val petObj = JSONObject().apply {
            put("name", s.pet.name)
            put("happiness", s.pet.happiness)
            put("energy", s.pet.energy)
            put("hunger", s.pet.hunger)
            put("interactions", s.pet.interactions)
            put("unlockedReactions", JSONArray(s.pet.unlockedReactions.toList()))
        }
        root.put("pet", petObj)

        val statsObj = JSONObject().apply {
            put("sliceBest", s.gameStats.sliceBest)
            put("catchStarsBest", s.gameStats.catchStarsBest)
            put("memoryBestSeconds", s.gameStats.memoryBestSeconds)
            put("tapStarsBest", s.gameStats.tapStarsBest)
            put("wheelSpins", s.gameStats.wheelSpins)
            put("scratchCardsCompleted", s.gameStats.scratchCardsCompleted)
            put("totalStarsCollected", s.gameStats.totalStarsCollected)
        }
        root.put("gameStats", statsObj)

        root.put("exploredSections", JSONArray(s.exploredSections.toList()))
        root.put("openedLetters", JSONArray(s.openedLetters.toList()))
        root.put("openedGifts", JSONArray(s.openedGifts.toList()))
        root.put("unlockedAchievements", JSONArray(s.unlockedAchievements.toList()))
        root.put("candlesExtinguished", s.candlesExtinguished)
        root.put("birthdayWishText", s.birthdayWishText)
        root.put("gayatriResponded", s.gayatriResponded)
        root.put("secretRoomUnlocked", s.secretRoomUnlocked)
        root.put("seenSecretMessages", JSONArray(s.seenSecretMessages.toList()))

        val photosArr = JSONArray()
        s.userPhotos.filter { !it.isAsset }.forEach { p ->
            photosArr.put(JSONObject().apply {
                put("id", p.id)
                put("fileName", p.fileName)
                put("caption", p.caption)
                put("secretNote", p.secretNote)
                put("localFilePath", p.localFilePath ?: "")
            })
        }
        root.put("localPhotos", photosArr)

        return if (pretty) root.toString(2) else root.toString()
    }

    private fun parseSnapshot(raw: String): VaultSnapshot {
        val root = JSONObject(raw)
        val settingsJson = root.optJSONObject("settings")
        val settings = if (settingsJson != null) {
            VaultSettings(
                bgmEnabled = settingsJson.optBoolean("bgmEnabled", false),
                sfxEnabled = settingsJson.optBoolean("sfxEnabled", true),
                particlesEnabled = settingsJson.optBoolean("particlesEnabled", true),
                reduceAnimations = settingsJson.optBoolean("reduceAnimations", false),
                frostVariation = settingsJson.optBoolean("frostVariation", false),
                surprisePopupsEnabled = settingsJson.optBoolean("surprisePopupsEnabled", true),
                popupFrequencySeconds = settingsJson.optInt("popupFrequencySeconds", 45),
                volume = settingsJson.optDouble("volume", 0.75).toFloat().coerceIn(0f, 1f),
                immersiveFullscreen = settingsJson.optBoolean("immersiveFullscreen", false)
            )
        } else VaultSettings()

        val petJson = root.optJSONObject("pet")
        val pet = if (petJson != null) {
            PetState(
                name = petJson.optString("name", "Nova"),
                happiness = petJson.optInt("happiness", 78).coerceIn(0, 100),
                energy = petJson.optInt("energy", 82).coerceIn(0, 100),
                hunger = petJson.optInt("hunger", 74).coerceIn(0, 100),
                interactions = petJson.optInt("interactions", 0),
                unlockedReactions = jsonArrayToStringSet(petJson.optJSONArray("unlockedReactions")).ifEmpty { setOf("Purr") }
            )
        } else PetState()

        val statsJson = root.optJSONObject("gameStats")
        val gameStats = if (statsJson != null) {
            GameStats(
                sliceBest = statsJson.optInt("sliceBest", 0),
                catchStarsBest = statsJson.optInt("catchStarsBest", 0),
                memoryBestSeconds = statsJson.optInt("memoryBestSeconds", 0),
                tapStarsBest = statsJson.optInt("tapStarsBest", 0),
                wheelSpins = statsJson.optInt("wheelSpins", 0),
                scratchCardsCompleted = statsJson.optInt("scratchCardsCompleted", 0),
                totalStarsCollected = statsJson.optInt("totalStarsCollected", 0)
            )
        } else GameStats()

        val localPhotos = mutableListOf<UserPhotoRecord>()
        val photosArr = root.optJSONArray("localPhotos")
        if (photosArr != null) {
            for (i in 0 until photosArr.length()) {
                val obj = photosArr.optJSONObject(i) ?: continue
                val path = obj.optString("localFilePath", "")
                if (path.isNotBlank()) {
                    localPhotos.add(
                        UserPhotoRecord(
                            id = obj.optString("id", "user_$i"),
                            fileName = obj.optString("fileName", "photo.jpg"),
                            caption = obj.optString("caption", "Memory"),
                            secretNote = obj.optString("secretNote", AppConfig.SECRET_MESSAGES.first()),
                            isAsset = false,
                            localFilePath = path
                        )
                    )
                }
            }
        }

        return VaultSnapshot(
            settings = settings,
            pet = pet,
            gameStats = gameStats,
            exploredSections = jsonArrayToStringSet(root.optJSONArray("exploredSections")).ifEmpty { setOf(WorldSection.BIRTHDAY.name) },
            openedLetters = jsonArrayToStringSet(root.optJSONArray("openedLetters")),
            openedGifts = jsonArrayToStringSet(root.optJSONArray("openedGifts")),
            unlockedAchievements = jsonArrayToStringSet(root.optJSONArray("unlockedAchievements")),
            candlesExtinguished = root.optBoolean("candlesExtinguished", false),
            birthdayWishText = root.optString("birthdayWishText", ""),
            gayatriResponded = root.optBoolean("gayatriResponded", false),
            secretRoomUnlocked = root.optBoolean("secretRoomUnlocked", false),
            userPhotos = localPhotos,
            seenSecretMessages = jsonArrayToIntSet(root.optJSONArray("seenSecretMessages"))
        )
    }

    private fun jsonArrayToStringSet(arr: JSONArray?): Set<String> {
        if (arr == null) return emptySet()
        val set = mutableSetOf<String>()
        for (i in 0 until arr.length()) {
            arr.optString(i)?.takeIf { it.isNotBlank() }?.let { set.add(it) }
        }
        return set
    }

    private fun jsonArrayToIntSet(arr: JSONArray?): Set<Int> {
        if (arr == null) return emptySet()
        val set = mutableSetOf<Int>()
        for (i in 0 until arr.length()) {
            set.add(arr.optInt(i))
        }
        return set
    }
}
