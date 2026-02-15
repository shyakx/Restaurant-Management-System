package com.restaurant.kitchen.dto

import java.math.BigDecimal

data class KitchenOrderResponse(
    val orderId: Long,
    val customerName: String,
    val customerEmail: String,
    val status: KitchenOrderStatus,
    val totalAmount: BigDecimal,
    val items: List<KitchenOrderItemResponse>,
    val receivedAt: String,
    val startedPreparationAt: String?,
    val completedAt: String?,
    val estimatedCompletionTime: String?
)

data class KitchenOrderItemResponse(
    val id: Long?,
    val menuItemId: Long,
    val menuItemName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal
)

enum class KitchenOrderStatus {
    RECEIVED,
    IN_PREPARATION,
    READY,
    COMPLETED,
    CANCELLED
}
