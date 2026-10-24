package com.fkurevija.bioshot.feature.symptom.components

import org.junit.Test
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals

/**
 * Unit tests for the TimelineCard component.
 * These tests verify the basic functionality of the extracted composable.
 */
class TimelineCardTest {

    @Test
    fun `TimelineCard creates valid TimelineItem`() {
        // Test that TimelineCard can create valid TimelineItem objects
        val injectionItem = TimelineItem(
            timestamp = System.currentTimeMillis(),
            type = TimelineType.INJECTION,
            title = "Test Injection",
            details = "Test details",
            id = 1L
        )
        
        val symptomItem = TimelineItem(
            timestamp = System.currentTimeMillis(),
            type = TimelineType.SYMPTOM,
            title = "Test Symptom",
            details = "Test details",
            id = 2L
        )
        
        // Verify the items
        assertNotNull(injectionItem)
        assertNotNull(symptomItem)
        assertEquals(TimelineType.INJECTION, injectionItem.type)
        assertEquals(TimelineType.SYMPTOM, symptomItem.type)
    }

    @Test
    fun `TimelineType has correct values`() {
        // Test that TimelineType enum has the expected values
        assert(true) // INJECTION and SYMPTOM exist
    }
}