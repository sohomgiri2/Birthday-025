package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.config.AppConfig
import com.example.data.WorldSection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Little World", appName)
    }

    @Test
    fun `verify scrapbook timeline and anushka letter removed`() {
        val names = WorldSection.entries.map { it.name.lowercase() }
        assertFalse(names.any { "scrapbook" in it || "timeline" in it || "anushka" in it })
        assertTrue(AppConfig.GAYATRI_LETTER_TEXT.contains("Eta baniyechi exam cholakalin"))
        assertTrue(AppConfig.GAYATRI_LETTER_TEXT.contains("Amar help please koris"))
    }
}
