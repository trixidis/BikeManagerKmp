package com.bikemanager.fake

import com.bikemanager.domain.common.Result
import com.bikemanager.domain.notification.NotificationScheduler

class FakeNotificationScheduler : NotificationScheduler {
    private val scheduledNotifications = mutableMapOf<String, Long>()

    override suspend fun scheduleReminder(
        notificationId: String,
        title: String,
        body: String,
        delayMillis: Long,
        deepLinkData: Map<String, String>
    ): Result<Unit> {
        scheduledNotifications[notificationId] = delayMillis
        return Result.Success(Unit)
    }

    override suspend fun cancelReminder(notificationId: String): Result<Unit> {
        scheduledNotifications.remove(notificationId)
        return Result.Success(Unit)
    }

    fun isScheduled(notificationId: String): Boolean {
        return scheduledNotifications.containsKey(notificationId)
    }

    fun getScheduledDelay(notificationId: String): Long? {
        return scheduledNotifications[notificationId]
    }
}
