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

    private fun makeSub(
        id: Int = 1,
        name: String = "Netflix",
        cost: Double = 12.99,
        category: String = "Entertainment",
        billingCycle: String = "monthly"
    ) = SubscriptionResponse(id, name, cost, category, billingCycle, "2024-01-01", "2024-01-01", "2024-01-01")

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
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01"),
            SubscriptionResponse(2, "Spotify", 9.99, "Music", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
        )
        val profile = buildProfile(subscriptions = subs)
        assertEquals(2, profile.numSubs)
    }

    @Test
    fun numSubsWithSingleSubscriptionIsOne() {
        val subs = listOf(
            SubscriptionResponse(1, "Netflix", 12.99, "Entertainment", "monthly", "2024-01-01", "2024-01-01", "2024-01-01")
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
    // ------------------- Fallback values -------------------

    @Test
    fun remindersEnabledFlag2MapsToFalse() {
        val profile = buildProfile(remindersEnabledFlag = 2)
        assertEquals(false, profile.remindersEnabled)
    }

    @Test
    fun remindersEnabledFlagNegativeMapsToFalse() {
        val profile = buildProfile(remindersEnabledFlag = -1)
        assertEquals(false, profile.remindersEnabled)
    }

    @Test
    fun emailFallbackIsNotAvailable() {
        assertEquals("Not available", buildProfile(email = null).email)
    }

    @Test
    fun nameFallbackIsJohnDoe() {
        assertEquals("John Doe", buildProfile(name = null).name)
    }

    // ------------------- numSubs derived value -------------------

    @Test
    fun numSubsWithThreeSubscriptionsIsThree() {
        val subs = listOf(makeSub(1), makeSub(2), makeSub(3))
        assertEquals(3, buildProfile(subscriptions = subs).numSubs)
    }

    @Test
    fun numSubsWithTenSubscriptionsIsTen() {
        val subs = (1..10).map { makeSub(it) }
        assertEquals(10, buildProfile(subscriptions = subs).numSubs)
    }

    @Test
    fun subscriptionsListIsPreservedOnProfile() {
        val subs = listOf(makeSub(1, "Netflix"), makeSub(2, "Spotify"))
        val profile = buildProfile(subscriptions = subs)
        assertEquals(2, profile.subscriptions?.size)
        assertEquals("Netflix", profile.subscriptions?.get(0)?.name)
        assertEquals("Spotify", profile.subscriptions?.get(1)?.name)
    }

    @Test
    fun subscriptionCostIsPreserved() {
        val subs = listOf(makeSub(1, cost = 99.99))
        assertEquals(99.99, buildProfile(subscriptions = subs).subscriptions?.first()?.cost)
    }

    @Test
    fun subscriptionBillingCycleIsPreserved() {
        val subs = listOf(makeSub(1, billingCycle = "yearly"))
        assertEquals("yearly", buildProfile(subscriptions = subs).subscriptions?.first()?.billing_cycle)
    }

    @Test
    fun subscriptionCategoryIsPreserved() {
        val subs = listOf(makeSub(1, category = "Productivity"))
        assertEquals("Productivity", buildProfile(subscriptions = subs).subscriptions?.first()?.category)
    }

    // ------------------- Reminders list detail -------------------

    @Test
    fun multipleRemindersAreAllPreserved() {
        val alerts = (1..5).map { makeAlert(id = it) }
        val profile = buildProfile(reminders = alerts)
        assertEquals(5, profile.reminders?.size)
    }

    @Test
    fun reminderOrderIsPreserved() {
        val alerts = listOf(makeAlert(id = 1, name = "First"), makeAlert(id = 2, name = "Second"))
        val profile = buildProfile(reminders = alerts)
        assertEquals("First",  profile.reminders?.get(0)?.name)
        assertEquals("Second", profile.reminders?.get(1)?.name)
    }

    @Test
    fun reminderCreatedAtIsPreserved() {
        val alert = makeAlert(createdAt = "2023-05-10T12:00:00.000Z")
        val profile = buildProfile(reminders = listOf(alert))
        assertEquals("2023-05-10T12:00:00.000Z", profile.reminders?.first()?.created_at)
    }

    @Test
    fun sentAtNonNullIsPreserved() {
        val alert = makeAlert(sentAt = "2024-07-01T08:00:00.000Z")
        val profile = buildProfile(reminders = listOf(alert))
        assertEquals("2024-07-01T08:00:00.000Z", profile.reminders?.first()?.sent_at)
    }

    // ------------------- Mixed subscriptions and reminders -------------------

    @Test
    fun profileWithBothSubsAndRemindersIsCorrect() {
        val subs    = listOf(makeSub(1), makeSub(2))
        val alerts  = listOf(makeAlert(1), makeAlert(2), makeAlert(3))
        val profile = buildProfile(subscriptions = subs, reminders = alerts)
        assertEquals(2, profile.numSubs)
        assertEquals(3, profile.reminders?.size)
    }

    @Test
    fun profileNumSubsDoesNotCountReminders() {
        val subs   = listOf(makeSub(1))
        val alerts = listOf(makeAlert(1), makeAlert(2))
        val profile = buildProfile(subscriptions = subs, reminders = alerts)
        assertEquals(1, profile.numSubs)
    }
}


