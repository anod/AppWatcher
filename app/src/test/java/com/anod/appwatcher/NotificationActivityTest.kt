package com.anod.appwatcher

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationActivityTest {

    @Test
    fun dismissActionsMarkUpdatesViewed() {
        assertTrue(shouldMarkUpdatesViewed(NOTIFICATION_ACTION_DISMISS))
        assertTrue(shouldMarkUpdatesViewed(NOTIFICATION_ACTION_MARK_VIEWED))
    }

    @Test
    fun navigationActionsDoNotMarkUpdatesViewed() {
        assertFalse(shouldMarkUpdatesViewed(NOTIFICATION_ACTION_PLAY_STORE))
        assertFalse(shouldMarkUpdatesViewed(NOTIFICATION_ACTION_MY_APPS))
    }
}