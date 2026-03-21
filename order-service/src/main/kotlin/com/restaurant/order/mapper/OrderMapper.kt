package com.restaurant.order.mapper

import com.restaurant.order.dto.OrderResponse
import com.restaurant.order.dto.OrderItemResponse
import com.restaurant.order.entity.Order
import com.restaurant.order.entity.OrderItem

/**
 * Maps Order entities to DTOs
 */
object OrderMapper {

    fun toResponse(order: Order): OrderResponse {
        return OrderResponse(
            id = order.id!!,
            customerName = order.customerName,
            customerEmail = order.customerEmail,
            customerPhone = order.customerPhone,
            status = order.status.name,
            totalAmount = order.totalAmount,
            items = order.items.map { toItemResponse(it) },
            createdAt = order.createdAt,
            updatedAt = order.updatedAt
        )
    }

    private fun toItemResponse(item: OrderItem): OrderItemResponse {
        return OrderItemResponse(
            id = item.id!!,
            menuItemId = item.menuItemId,
            menuItemName = item.menuItemName,
            quantity = item.quantity,
            unitPrice = item.unitPrice,
            totalPrice = item.totalPrice
        )
    }
}
