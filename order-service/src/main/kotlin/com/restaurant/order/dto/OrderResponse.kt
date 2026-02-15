package com.restaurant.order.dto

import java.math.BigDecimal

data class OrderResponse(
    val id: Long,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val status: String,
    val totalAmount: BigDecimal,
    val items: List<OrderItemResponse>,
    val createdAt: String,
    val updatedAt: String?
)

data class OrderItemResponse(
    val id: Long,
    val menuItemId: Long,
    val menuItemName: String,
    val quantity: Int,
    val unitPrice: BigDecimal,
    val totalPrice: BigDecimal
)
