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
    // ------------------- formatToMonthYear: all 12 months -------------------

    @Test
    fun formatToMonthYearJanuary() {
        assertEquals("Jan 2024", utility.formatToMonthYear("2024-01-01T00:00:00.000Z"))
    }

    @Test
    fun formatToMonthYearFebruary() {
        assertEquals("Feb 2024", utility.formatToMonthYear("2024-02-15T00:00:00.000Z"))
    }

    @Test
    fun formatToMonthYearMarch() {
        assertEquals("Mar 2024", utility.formatToMonthYear("2024-03-01T00:00:00.000Z"))
    }

    @Test
    fun formatToMonthYearApril() {
        assertEquals("Apr 2024", utility.formatToMonthYear("2024-04-01T00:00:00.000Z"))
    }

    @Test
    fun formatToMonthYearMay() {
        assertEquals("May 2024", utility.formatToMonthYear("2024-05-01T00:00:00.000Z"))
    }

    @Test
    fun formatToMonthYearJune() {
        assertEquals("Jun 2024", utility.formatToMonthYear("2024-06-01T00:00:00.000Z"))
    }

    @Test
    fun formatToMonthYearJuly() {
        assertEquals("Jul 2024", utility.formatToMonthYear("2024-07-01T00:00:00.000Z"))
    }

    @Test
    fun formatToMonthYearAugust() {
        assertEquals("Aug 2024", utility.formatToMonthYear("2024-08-01T00:00:00.000Z"))
    }

    @Test
    fun formatToMonthYearSeptember() {
        assertEquals("Sep 2024", utility.formatToMonthYear("2024-09-01T00:00:00.000Z"))
    }

    @Test
    fun formatToMonthYearOctober() {
        assertEquals("Oct 2024", utility.formatToMonthYear("2024-10-01T00:00:00.000Z"))
    }

    @Test
    fun formatToMonthYearNovember() {
        assertEquals("Nov 2024", utility.formatToMonthYear("2024-11-01T00:00:00.000Z"))
    }

    @Test
    fun formatToMonthYearDecember() {
        assertEquals("Dec 2023", utility.formatToMonthYear("2023-12-01T00:00:00.000Z"))
    }

    // ------------------- formatToMonthYear: year correctness -------------------

    @Test
    fun formatToMonthYearYear2020IsCorrect() {
        assertEquals("Jan 2020", utility.formatToMonthYear("2020-01-01T00:00:00.000Z"))
    }

    @Test
    fun formatToMonthYearYear2099IsCorrect() {
        assertEquals("Dec 2099", utility.formatToMonthYear("2099-12-31T23:59:59.000Z"))
    }

    @Test
    fun formatToMonthYearYear2000IsCorrect() {
        assertEquals("Mar 2000", utility.formatToMonthYear("2000-03-15T00:00:00.000Z"))
    }

    // ------------------- formatToMonthYear: malformed input -------------------

    @Test
    fun formatToMonthYearWithDateOnlyStringHandledGracefully() {
        val result = utility.formatToMonthYear("2024-06-01")
        // implementation-defined; just must not throw
        assertTrue(result == null || result == "" || result.isNotEmpty())
    }

    // ------------------- timeAgo: past -------------------

    @Test
    fun timeAgoThreeDaysAgoPastIsCorrect() {
        assertEquals("3 days ago", utility.timeAgo(isoStringDaysFromNow(-3)))
    }

    @Test
    fun timeAgoTwentyDaysAgoIsCorrect() {
        val result = utility.timeAgo(isoStringDaysFromNow(-20))
        assertEquals("20 days ago", result)
    }

    @Test
    fun timeAgoFifteenDaysAgoIsCorrect() {
        val result = utility.timeAgo(isoStringDaysFromNow(-15))
        assertEquals("15 days ago", result)
    }
    // ------------------- timeAgo: future -------------------

    @Test
    fun timeAgoThreeDaysInFutureIsCorrect() {
        assertEquals("In 3 days", utility.timeAgo(isoStringDaysFromNow(3)))
    }

    @Test
    fun timeAgoFourteenDaysInFutureIsCorrect() {
        assertEquals("In 14 days", utility.timeAgo(isoStringDaysFromNow(14)))
    }
    // ------------------- timeAgo: boundary between labels -------------------

    @Test
    fun timeAgoTodayReturnsTodayNotDaysAgo() {
        val result = utility.timeAgo(isoStringDaysFromNow(0))
        assertEquals("Today", result)
    }

    @Test
    fun timeAgoYesterdayReturnsYesterdayNotDaysAgo() {
        val result = utility.timeAgo(isoStringDaysFromNow(-1))
        assertEquals("Yesterday", result)
    }

    @Test
    fun timeAgoTomorrowReturnsTomorrowNotInDays() {
        val result = utility.timeAgo(isoStringDaysFromNow(1))
        assertEquals("Tomorrow", result)
    }

    @Test
    fun timeAgoPastAndFutureResultsAreDifferent() {
        val past   = utility.timeAgo(isoStringDaysFromNow(-5))
        val future = utility.timeAgo(isoStringDaysFromNow(5))
        assertNotEquals(past, future)
    }
}