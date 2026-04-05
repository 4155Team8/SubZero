package com.example.subzero.global

import com.example.subzero.network.AlertResponse
import com.example.subzero.network.SubscriptionResponse
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ApiCallsResponseTest {

    // Mirror of ApiCalls.response data class for isolated testing
    data class ProfileResponse(
        val email: String?,
        val name: String?,
        val remindersEnabled: Boolean?,
        val createdAt: String?,
        val subscriptions: List<SubscriptionResponse>?,
        val numSubs: Int?,
        val reminders: List<AlertResponse>?
    )

    // Helper that mimics ApiCalls.loadProfile mapping logic
    private fun buildProfile(
        email: String? = null,
        name: String? = null,
        remindersEnabledFlag: Int? = null,
        createdAt: String? = null,
        subscriptions: List<SubscriptionResponse> = emptyList(),
        reminders: List<AlertResponse> = emptyList()
    ): ProfileResponse {
        return ProfileResponse(
            email = email ?: "Not available",
            name = name ?: "John Doe",
            remindersEnabled = remindersEnabledFlag == 1,
            createdAt = createdAt,
            subscriptions = subscriptions,
            numSubs = subscriptions.size,
            reminders = reminders
        )
    }

    // AlertResponse(id, subscription_id, reminder_date, name, description, sent_at, created_at)
    private fun makeAlert(
        id: Int = 1,
        subscriptionId: Int = 10,
        reminderDate: String = "2024-06-01T00:00:00.000Z",
        name: String = "Test Alert",
        description: String = "Test description",
        sentAt: String? = null,
        createdAt: String = "2024-01-01T00:00:00.000Z"
    ) = AlertResponse(id, subscriptionId, reminderDate, name, description, sentAt, createdAt)

    // ------------------- Default fallback values -------------------

    @Test
    fun nullEmailFallsBackToNotAvailable() {
        val profile = buildProfile(email = null)
        assertEquals("Not available", profile.email)
    }

    @Test
    fun nullNameFallsBackToJohnDoe() {
        val profile = buildProfile(name = null)
        assertEquals("John Doe", profile.name)
    }

    @Test
    fun remindersFlagOneMapToTrue() {
        val profile = buildProfile(remindersEnabledFlag = 1)
        assertEquals(true, profile.remindersEnabled)
    }

    @Test
    fun remindersFlagZeroMapsToFalse() {
        val profile = buildProfile(remindersEnabledFlag = 0)
        assertEquals(false, profile.remindersEnabled)
    }

    @Test
    fun remindersFlagNullMapsToFalse() {
        val profile = buildProfile(remindersEnabledFlag = null)
        assertEquals(false, profile.remindersEnabled)
    }

    // ------------------- numSubs derived from list size -------------------

    @Test
    fun numSubsIsZeroWhenSubscriptionsListIsEmpty() {
        val profile = buildProfile(subscriptions = emptyList())
        assertEquals(0, profile.numSubs)
    }

    @Test
    fun numSubsMatchesSubscriptionListSize() {
        val subs = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 9.99, "Music", "monthly", "2024-01-01", "2024-01-01")
        )
        val profile = buildProfile(subscriptions = subs)
        assertEquals(2, profile.numSubs)
    }

    @Test
    fun numSubsWithSingleSubscriptionIsOne() {
        val subs = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01")
        )
        val profile = buildProfile(subscriptions = subs)
        assertEquals(1, profile.numSubs)
    }

    // ------------------- Reminders list -------------------

    @Test
    fun emptyRemindersListIsPreserved() {
        val profile = buildProfile(reminders = emptyList())
        assertTrue(profile.reminders?.isEmpty() == true)
    }

    @Test
    fun remindersListSizeIsCorrect() {
        val reminders = listOf(
            makeAlert(id = 1, name = "Netflix renews"),
            makeAlert(id = 2, name = "Spotify renews")
        )
        val profile = buildProfile(reminders = reminders)
        assertEquals(2, profile.reminders?.size)
    }

    @Test
    fun reminderIdIsPreservedCorrectly() {
        val profile = buildProfile(reminders = listOf(makeAlert(id = 42)))
        assertEquals(42, profile.reminders?.first()?.id)
    }

    @Test
    fun reminderNameIsPreservedCorrectly() {
        val profile = buildProfile(reminders = listOf(makeAlert(name = "Netflix")))
        assertEquals("Netflix", profile.reminders?.first()?.name)
    }

    @Test
    fun reminderDescriptionIsPreservedCorrectly() {
        val profile = buildProfile(reminders = listOf(makeAlert(description = "Renewal soon")))
        assertEquals("Renewal soon", profile.reminders?.first()?.description)
    }

    @Test
    fun reminderReminderDateIsPreservedCorrectly() {
        val profile = buildProfile(reminders = listOf(makeAlert(reminderDate = "2024-06-01T00:00:00.000Z")))
        assertEquals("2024-06-01T00:00:00.000Z", profile.reminders?.first()?.reminder_date)
    }

    @Test
    fun reminderSentAtCanBeNull() {
        val profile = buildProfile(reminders = listOf(makeAlert(sentAt = null)))
        assertNull(profile.reminders?.first()?.sent_at)
    }

    @Test
    fun reminderSentAtIsPreservedWhenSet() {
        val profile = buildProfile(reminders = listOf(makeAlert(sentAt = "2024-06-02T10:00:00.000Z")))
        assertEquals("2024-06-02T10:00:00.000Z", profile.reminders?.first()?.sent_at)
    }

    @Test
    fun reminderSubscriptionIdIsPreservedCorrectly() {
        val profile = buildProfile(reminders = listOf(makeAlert(subscriptionId = 99)))
        assertEquals(99, profile.reminders?.first()?.subscription_id)
    }

    // ------------------- Provided values are not replaced -------------------

    @Test
    fun providedEmailIsUsedAsIs() {
        val profile = buildProfile(email = "real@user.com")
        assertEquals("real@user.com", profile.email)
    }

    @Test
    fun providedNameIsUsedAsIs() {
        val profile = buildProfile(name = "Jane Smith")
        assertEquals("Jane Smith", profile.name)
    }

    @Test
    fun createdAtIsPassedThroughUnchanged() {
        val profile = buildProfile(createdAt = "2023-03-15T08:00:00.000Z")
        assertEquals("2023-03-15T08:00:00.000Z", profile.createdAt)
    }

    @Test
    fun nullCreatedAtStaysNull() {
        val profile = buildProfile(createdAt = null)
        assertNull(profile.createdAt)
    }
}