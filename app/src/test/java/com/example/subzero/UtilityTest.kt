package com.example.subzero.global

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.text.SimpleDateFormat
import java.util.*

@RunWith(JUnit4::class)
class UtilityTest {

    private lateinit var utility: Utility

    @Before
    fun setup() {
        utility = Utility()
    }

    // Builds an ISO string that timeAgo() will interpret as exactly offsetDays from today.
    // timeAgo() strips both dates to midnight LOCAL time before diffing, so we must do the same:
    // set to local midnight, then shift by offsetDays, then format in UTC for the ISO string.
    private fun isoStringDaysFromNow(offsetDays: Int): String {
        val cal = Calendar.getInstance() // local timezone
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, offsetDays)
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(cal.time)
    }

    // ------------------- formatToMonthYear tests -------------------

    @Test
    fun formatToMonthYearReturnsCorrectMonthAndYear() {
        val result = utility.formatToMonthYear("2024-01-15T10:30:00.000Z")
        assertEquals("Jan 2024", result)
    }

    @Test
    fun formatToMonthYearHandlesDecemberCorrectly() {
        val result = utility.formatToMonthYear("2023-12-25T00:00:00.000Z")
        assertEquals("Dec 2023", result)
    }

    @Test
    fun formatToMonthYearHandlesNullInputGracefully() {
        val result = utility.formatToMonthYear(null)
        assertTrue(result == null || result == "")
    }

    @Test
    fun formatToMonthYearHandlesEmptyStringGracefully() {
        val result = utility.formatToMonthYear("")
        assertTrue(result == null || result == "")
    }

    @Test
    fun formatToMonthYearHandlesMalformedStringGracefully() {
        val result = utility.formatToMonthYear("not-a-date")
        assertTrue(result == null || result == "")
    }

    @Test
    fun formatToMonthYearHandlesYearBoundaryCorrectly() {
        val result = utility.formatToMonthYear("2025-06-01T00:00:00.000Z")
        assertEquals("Jun 2025", result)
    }

    // ------------------- timeAgo tests -------------------

    @Test
    fun timeAgoReturnsTodayForCurrentDate() {
        val result = utility.timeAgo(isoStringDaysFromNow(0))
        assertEquals("Today", result)
    }

    @Test
    fun timeAgoReturnsYesterdayForOneDayAgo() {
        val result = utility.timeAgo(isoStringDaysFromNow(-1))
        assertEquals("Yesterday", result)
    }

    @Test
    fun timeAgoReturnsFiveDaysAgoForFiveDaysPast() {
        val result = utility.timeAgo(isoStringDaysFromNow(-5))
        assertEquals("5 days ago", result)
    }

    @Test
    fun timeAgoReturnsTenDaysAgoForTenDaysPast() {
        val result = utility.timeAgo(isoStringDaysFromNow(-10))
        assertEquals("10 days ago", result)
    }

    @Test
    fun timeAgoReturnsTomorrowForOneDayInFuture() {
        val result = utility.timeAgo(isoStringDaysFromNow(1))
        assertEquals("Tomorrow", result)
    }

    @Test
    fun timeAgoReturnsInSevenDaysForSevenDaysInFuture() {
        val result = utility.timeAgo(isoStringDaysFromNow(7))
        assertEquals("In 7 days", result)
    }

    @Test
    fun timeAgoReturnsInTwoDaysForTwoDaysInFuture() {
        val result = utility.timeAgo(isoStringDaysFromNow(2))
        assertEquals("In 2 days", result)
    }

    @Test
    fun timeAgoReturnsTwoDaysAgoForTwoDaysPast() {
        val result = utility.timeAgo(isoStringDaysFromNow(-2))
        assertEquals("2 days ago", result)
    }
}