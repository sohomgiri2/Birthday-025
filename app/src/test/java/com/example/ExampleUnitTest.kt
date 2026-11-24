package com.example

import com.example.config.AppConfig
import com.example.data.WorldSection
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun verifySectionsAndLetterConfig() {
        val names = WorldSection.entries.map { it.name.lowercase() }
        assertFalse(names.any { "scrapbook" in it || "timeline" in it || "anushka" in it })
        assertTrue(AppConfig.GAYATRI_LETTER_TEXT.contains("Eta baniyechi exam cholakalin"))
        assertTrue(AppConfig.GAYATRI_LETTER_TEXT.contains("Amar help please koris"))
    }
}
