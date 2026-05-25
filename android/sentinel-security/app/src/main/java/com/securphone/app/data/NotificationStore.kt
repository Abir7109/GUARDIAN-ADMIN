package com.securphone.app.data

import com.securphone.app.data.models.BroadcastModel

object NotificationStore {
    private val broadcasts = mutableListOf<BroadcastModel>()

    fun add(broadcast: BroadcastModel) {
        broadcasts.add(0, broadcast)
        if (broadcasts.size > 50) broadcasts.removeAt(broadcasts.lastIndex)
    }

    fun getAll(): List<BroadcastModel> = broadcasts.toList()

    fun unreadCount(): Int = broadcasts.count { it.status == "pending" }

    fun markAllRead() {
        broadcasts.replaceAll { it.copy(status = "read") }
    }
}
