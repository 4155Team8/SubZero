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
    fun `slices are empty by default`() {
        val view = DonutChartView(context)
        assertTrue(view.slices.isEmpty())
    }

    @Test
    fun `setting slices triggers invalidate and stores values`() {
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
    fun `slice values are stored correctly`() {
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
    fun `setting empty slices is handled without crash`() {
        val view = DonutChartView(context)
        view.slices = emptyList()
        assertTrue(view.slices.isEmpty())
    }

    @Test
    fun `replacing slices updates the list`() {
        val view = DonutChartView(context)
        view.slices = listOf(DonutSlice(100f, Color.RED))
        assertEquals(1, view.slices.size)

        view.slices = listOf(
            DonutSlice(60f, Color.RED),
            DonutSlice(40f, Color.BLUE)
        )
        assertEquals(2, view.slices.size)
    }
}