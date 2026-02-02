package com.restaurant.menu.event

import java.time.LocalDateTime

/**
 * Menu event for Kafka messaging.
 */
data class MenuEvent(
    val eventType: String,
    val menuItemId: Long?,
    val menuItemName: String,
    val category: String?,
    val price: Double,
    val available: Boolean,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val service: String = "menu-service"
)
