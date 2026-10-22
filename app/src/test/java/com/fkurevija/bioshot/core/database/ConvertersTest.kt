package com.fkurevija.bioshot.core.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun sideEffectsRoundTrip() {
        val original = listOf("nausea", "headache", "fatigue")
        val serialized = converters.fromSideEffects(original)
        val deserialized = converters.toSideEffects(serialized)
        assertEquals(original, deserialized)
    }

    @Test
    fun emptySideEffectsRoundTrip() {
        val original: List<String> = emptyList<String>()
        val serialized = converters.fromSideEffects(original)
        val deserialized = converters.toSideEffects(serialized)
        assertTrue(deserialized.isEmpty())
    }
}

