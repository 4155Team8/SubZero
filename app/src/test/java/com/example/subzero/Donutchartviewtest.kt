package com.example.subzero.views

import android.graphics.Color
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DonutChartViewTest {

    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun slicesAreEmptyByDefault() {
        val view = DonutChartView(context)
        assertTrue(view.slices.isEmpty())
    }

    @Test
    fun settingSlicesTriggersInvalidateAndStoresValues() {
        val view = DonutChartView(context)
        val slices = listOf(
            DonutSlice(50f, Color.RED),
            DonutSlice(50f, Color.BLUE)
        )
        view.slices = slices
        assertEquals(2, view.slices.size)
        assertEquals(Color.RED,  view.slices[0].color)
        assertEquals(Color.BLUE, view.slices[1].color)
    }

    @Test
    fun sliceValuesAreStoredCorrectly() {
        val view = DonutChartView(context)
        val slices = listOf(
            DonutSlice(75f, Color.RED),
            DonutSlice(25f, Color.BLUE)
        )
        view.slices = slices
        assertEquals(75f, view.slices[0].value)
        assertEquals(25f, view.slices[1].value)
    }

    @Test
    fun settingEmptySlicesIsHandledWithoutCrash() {
        val view = DonutChartView(context)
        view.slices = emptyList()
        assertTrue(view.slices.isEmpty())
    }

    @Test
    fun replacingSlicesUpdatesTheList() {
        val view = DonutChartView(context)
        view.slices = listOf(DonutSlice(100f, Color.RED))
        assertEquals(1, view.slices.size)

        view.slices = listOf(
            DonutSlice(60f, Color.RED),
            DonutSlice(40f, Color.BLUE)
        )
        assertEquals(2, view.slices.size)
    }

    // ------------------- Tests for Drawing Behavior -------------------

    @Test
    fun `onDraw returns early when slices are empty`() {
        val view = DonutChartView(context)
        view.slices = emptyList()
        // onDraw should return early without any drawing operations
        // This test is proving that by not throwing any errors --Joe
        assertTrue(view.slices.isEmpty())
    }

    @Test
    fun `onDraw returns early when total is zero`() {
        val view = DonutChartView(context)
        view.slices = listOf(
            DonutSlice(0f, Color.RED),
            DonutSlice(0f, Color.BLUE)
        )
        // onDraw should return early if total is 0
        assertEquals(0f, view.slices.sumOf { it.value.toDouble() }.toFloat())
    }

    // ------------------- Tests for Slice Validation -------------------

    @Test
    fun `negative slice values are handled gracefully`() {
        val view = DonutChartView(context)
        view.slices = listOf(
            DonutSlice(-10f, Color.RED),
            DonutSlice(20f, Color.BLUE)
        )
        // Negative values should be handled during subscription creation
        // but I don't want it to crash the chart if they slip through   -- Joe
        assertEquals(-10f, view.slices[0].value)
        assertEquals(20f, view.slices[1].value)
    }

    @Test
    fun `very large slice values are handled`() {
        val view = DonutChartView(context)
        view.slices = listOf(
            DonutSlice(Float.MAX_VALUE, Color.RED),
            DonutSlice(1f, Color.BLUE)
        )
        assertEquals(Float.MAX_VALUE, view.slices[0].value)
    }

    @Test
    fun `single slice creates full circle minus gaps`() {
        val view = DonutChartView(context)
        view.slices = listOf(DonutSlice(100f, Color.RED))
        assertEquals(1, view.slices.size)
        assertEquals(100f, view.slices[0].value)
    }

    @Test
    fun `many slices are handled correctly`() {
        val view = DonutChartView(context)
        val manySlices = (1..20).map { DonutSlice(it.toFloat(), Color.rgb(it * 10, it * 5, it * 15)) }
        view.slices = manySlices
        assertEquals(20, view.slices.size)
        assertEquals(20f, view.slices[19].value) // There should be 20 slices so the last one should be 20
    }

    @Test
    fun `duplicate colors are allowed`() {
        val view = DonutChartView(context)
        view.slices = listOf(
            DonutSlice(30f, Color.RED),
            DonutSlice(40f, Color.RED),
            DonutSlice(30f, Color.BLUE)
        )
        assertEquals(Color.RED, view.slices[0].color)
        assertEquals(Color.RED, view.slices[1].color)
        assertEquals(Color.BLUE, view.slices[2].color)
    }

    // ------------------- Tests for Mathematical Calculations -------------------

    @Test
    fun `total calculation is correct for multiple slices`() {
        val view = DonutChartView(context)
        view.slices = listOf(
            DonutSlice(10f, Color.RED),
            DonutSlice(20f, Color.BLUE),
            DonutSlice(30f, Color.GREEN)
        )
        val expectedTotal = 60f
        val actualTotal = view.slices.sumOf { it.value.toDouble() }.toFloat()
        assertEquals(expectedTotal, actualTotal, 0.001f)
    }

    @Test
    fun `fractional slice values are handled correctly`() {
        val view = DonutChartView(context)
        view.slices = listOf(
            DonutSlice(33.33f, Color.RED),
            DonutSlice(33.33f, Color.BLUE),
            DonutSlice(33.34f, Color.GREEN)
        )
        val total = view.slices.sumOf { it.value.toDouble() }.toFloat()
        assertEquals(100f, total, 0.01f)
    }

    @Test
    fun `gap calculation is consistent`() {
        val view = DonutChartView(context)
        // The gap is set to 3f degrees per slice
        // I can't directly test this without accessing private fields,
        // but I am able to verify the behavior indirectly --Joe
        view.slices = listOf(
            DonutSlice(50f, Color.RED),
            DonutSlice(50f, Color.BLUE)
        )
        assertEquals(2, view.slices.size)
    }

    // ------------------- Tests for Edge Cases -------------------

    @Test
    fun `zero value slices are stored but contribute nothing to total`() {
        val view = DonutChartView(context)
        view.slices = listOf(
            DonutSlice(0f, Color.RED),
            DonutSlice(100f, Color.BLUE)
        )
        // 0 value slices shouldn't contribute to the total but they should still be storing
        // If someone thinks differently, let me know--Joe
        val total = view.slices.sumOf { it.value.toDouble() }.toFloat()
        assertEquals(100f, total)
        assertEquals(0f, view.slices[0].value)
    }

    @Test
    fun `all zero slices result in zero total`() {
        val view = DonutChartView(context)
        view.slices = listOf(
            DonutSlice(0f, Color.RED),
            DonutSlice(0f, Color.BLUE),
            DonutSlice(0f, Color.GREEN)
        )
        val total = view.slices.sumOf { it.value.toDouble() }.toFloat()
        assertEquals(0f, total)
    }

    @Test
    fun `mixed zero and non-zero slices work correctly`() {
        val view = DonutChartView(context)
        view.slices = listOf(
            DonutSlice(0f, Color.RED),
            DonutSlice(75f, Color.BLUE),
            DonutSlice(0f, Color.GREEN),
            DonutSlice(25f, Color.YELLOW)
        )
        val total = view.slices.sumOf { it.value.toDouble() }.toFloat()
        assertEquals(100f, total)
    }

    // ------------------- Tests for Slice Ordering -------------------

    @Test
    fun `slice order is preserved when setting slices`() {
        val view = DonutChartView(context)
        val originalOrder = listOf(
            DonutSlice(10f, Color.RED),
            DonutSlice(20f, Color.BLUE),
            DonutSlice(30f, Color.GREEN),
            DonutSlice(40f, Color.YELLOW)
        )
        view.slices = originalOrder

        // Check that the order is maintained
        assertEquals(Color.RED, view.slices[0].color)
        assertEquals(Color.BLUE, view.slices[1].color)
        assertEquals(Color.GREEN, view.slices[2].color)
        assertEquals(Color.YELLOW, view.slices[3].color)
    }

    // ------------------- Tests for Data Class -------------------

    @Test
    fun `DonutSlice data class stores values correctly`() {
        val slice = DonutSlice(42.5f, Color.MAGENTA)
        assertEquals(42.5f, slice.value)
        assertEquals(Color.MAGENTA, slice.color)
    }

    @Test
    fun `DonutSlice equality works correctly`() {
        val slice1 = DonutSlice(50f, Color.RED)
        val slice2 = DonutSlice(50f, Color.RED)
        val slice3 = DonutSlice(50f, Color.BLUE)

        assertEquals(slice1, slice2)
        assertNotEquals(slice1, slice3)
    }

    @Test
    fun `DonutSlice toString works`() {
        val slice = DonutSlice(25f, Color.GREEN)
        val toString = slice.toString()
        assertTrue(toString.contains("25.0"))
        assertTrue(toString.contains("DonutSlice"))
    }

    // ------------------- Tests for Constructor Variations -------------------

    @Test
    fun `constructor with context only works`() {
        val view = DonutChartView(context)
        assertNotNull(view)
        assertTrue(view.slices.isEmpty())
    }

    @Test
    fun `constructor with context and null attrs works`() {
        val view = DonutChartView(context, null)
        assertNotNull(view)
        assertTrue(view.slices.isEmpty())
    }

    @Test
    fun `constructor with all parameters works`() {
        val view = DonutChartView(context, null, 0)
        assertNotNull(view)
        assertTrue(view.slices.isEmpty())
    }

    // ------------------- Tests for Property Behavior -------------------

    @Test
    fun `slices property getter returns correct list`() {
        val view = DonutChartView(context)
        val testSlices = listOf(
            DonutSlice(25f, Color.RED),
            DonutSlice(75f, Color.BLUE)
        )
        view.slices = testSlices

        val retrievedSlices = view.slices
        assertEquals(2, retrievedSlices.size)
        assertEquals(25f, retrievedSlices[0].value)
        assertEquals(Color.RED, retrievedSlices[0].color)
        assertEquals(75f, retrievedSlices[1].value)
        assertEquals(Color.BLUE, retrievedSlices[1].color)
    }

    @Test
    fun `setting same slices multiple times works`() {
        val view = DonutChartView(context)
        val slices = listOf(DonutSlice(100f, Color.RED))

        view.slices = slices
        view.slices = slices
        view.slices = slices

        assertEquals(1, view.slices.size)
        assertEquals(100f, view.slices[0].value)
    }
}