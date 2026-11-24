package com.example.config

/**
 * ============================================================================
 *  A LITTLE WORLD MADE FOR YOU — CENTRAL CONFIGURATION
 *  Edit passwords, special letters, secret messages, and media paths here.
 * ============================================================================
 */
object AppConfig {

    // ------------------------------------------------------------------------
    // 1. GAYATRI'S SPECIAL LETTER ("A Small Favor to Ask")
    // ------------------------------------------------------------------------
    const val GAYATRI_LETTER_TITLE = "A Small Favor to Ask"
    const val GAYATRI_LETTER_RECIPIENT = "For Gayatri"
    const val GAYATRI_LETTER_TEXT =
        "Eta baniyechi exam cholakalin so jodi valo lege thake ba nao lege thake idc \uD83D\uDE2D\n\n" +
            "Amar help please koris \uD83D\uDE4F please or sathe amar sob thik koriye deh"
    const val GAYATRI_LETTER_ACTION_TEXT = "Okay, I'll Think About It"

    // ------------------------------------------------------------------------
    // 3. SECRET VAULT ROOM PASSWORD
    //    Client-side fun privacy barrier — edit password here.
    // ------------------------------------------------------------------------
    const val SECRET_ROOM_PASSWORD = "2026"
    const val SECRET_ROOM_HINT = "The year this little world was built"

    // ------------------------------------------------------------------------
    // 4. MEDIA PATH CONFIGURATION (app/src/main/assets/...)
    // ------------------------------------------------------------------------
    const val PHOTOS_ASSET_DIR = "photos"
    const val PHOTO_PREFIX = "photo"
    const val MAX_NUMBERED_PHOTOS = 300
    val SUPPORTED_PHOTO_EXTENSIONS = listOf("png", "jpg", "jpeg")

    const val LETTERS_ASSET_DIR = "letters"
    const val MUSIC_ASSET_DIR = "music"
    val SUPPORTED_MUSIC_EXTENSIONS = listOf("mp3", "m4a", "wav", "ogg")

    // ------------------------------------------------------------------------
    // 5. DEVELOPER CREDITS
    // ------------------------------------------------------------------------
    const val DEVELOPER_NAME = "Sohom Giri"
    const val DEVELOPER_HANDLE = "@itz_sg_063"
    const val DEVELOPER_ROLE = "Independent Web Developer / Creative Developer"
    const val DEVELOPER_INSTAGRAM_URL = "https://www.instagram.com/itz_sg_063/"

    // ------------------------------------------------------------------------
    // 6. SECRET MESSAGES POOL (18 UNIQUE MESSAGES)
    // ------------------------------------------------------------------------
    val SECRET_MESSAGES = listOf(
        "You deserve more happy days.",
        "Never underestimate how special you are to the people around you.",
        "Here's a little reminder to smile today.",
        "Some friendships make life a little brighter.",
        "The world feels lighter when you laugh.",
        "Even quiet moments with good friends become core memories.",
        "Plot twist: you're actually awesome.",
        "May your playlist never skip a good song today.",
        "You just unlocked a tiny pocket of starlight.",
        "Little things done with care matter more than loud gestures.",
        "Keep being genuine — it's rarer than you think.",
        "Somewhere out there, a star is cheering for your next chapter.",
        "Take a deep breath. You're doing better than you give yourself credit for.",
        "Friendship is built on small favors, late talks, and inside jokes.",
        "Today is a good day to celebrate how far you've come.",
        "You bring calm chaos in the best possible way.",
        "Don't forget to wish for something big when the candles go out.",
        "Every corner of this little world hides a bit of gratitude."
    )

    // ------------------------------------------------------------------------
    // 7. RANDOM SURPRISE POPUP MESSAGES
    // ------------------------------------------------------------------------
    val SURPRISE_POPUPS = listOf(
        "Hey, don't forget to smile.",
        "You just unlocked a little moment of happiness.",
        "Plot twist: you're actually awesome.",
        "Friendly check-in: hydrate and smile!",
        "Tiny cosmic update: today is officially a good day.",
        "A hidden sparkle drifted past just to say happy birthday!"
    )
}
