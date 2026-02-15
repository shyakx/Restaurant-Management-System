package com.restaurant.kitchen.dto

import java.math.BigDecimal

data class OrderEvent(
    val orderId: Long,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val status: OrderStatus,
    val totalAmount: BigDecimal,
    val items: List<OrderItemEvent>,
    val timestamp: String = java.time.LocalDateTime.now().toString(),
    val eventType: EventType
)

data class OrderItemEvent(
    val menuItemId: Long,
    val menuItemName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal
)

enum class EventType {
    ORDER_PLACED,
    ORDER_CONFIRMED,
    ORDER_PREPARING,
    ORDER_READY,
    ORDER_COMPLETED,
    ORDER_CANCELLED
}

enum class OrderStatus {
    PENDING,
    CONFIRMED,
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED
}
