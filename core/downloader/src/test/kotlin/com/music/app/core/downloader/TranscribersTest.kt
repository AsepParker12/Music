package com.music.app.core.downloader

import com.music.app.core.model.DownloaderPreferences
import com.music.app.core.model.TranscriptionService.GROQ
import com.music.app.core.model.TranscriptionService.SUPADATA
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TranscribersTest {
    @Test
    fun theChosenServiceGoesFirstAndOnlyThoseWithAKeyAreTried() {
        val both = DownloaderPreferences(supadataKey = "s", groqKey = "g")
        assertEquals(listOf(SUPADATA, GROQ), both.transcribers)
        assertEquals(listOf(GROQ, SUPADATA), both.copy(transcriptionService = GROQ).transcribers)
        assertEquals(listOf(SUPADATA), DownloaderPreferences(supadataKey = "s", transcriptionService = GROQ).transcribers)
        assertFalse(DownloaderPreferences(supadataKey = " ").transcribes)
    }
}
