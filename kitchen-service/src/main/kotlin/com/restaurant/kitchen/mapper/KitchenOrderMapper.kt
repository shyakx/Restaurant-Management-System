package com.restaurant.kitchen.mapper

import com.restaurant.kitchen.dto.KitchenOrderResponse
import com.restaurant.kitchen.dto.KitchenOrderItemResponse
import com.restaurant.kitchen.dto.KitchenOrderStatus as DtoKitchenOrderStatus
import com.restaurant.kitchen.entity.KitchenOrder
import com.restaurant.kitchen.entity.KitchenOrderItem

/**
 * Maps KitchenOrder entities to DTOs
 */
object KitchenOrderMapper {

    fun toResponse(order: KitchenOrder): KitchenOrderResponse {
        return KitchenOrderResponse(
            orderId = order.orderId,
            customerName = order.customerName,
            customerEmail = order.customerEmail,
            status = DtoKitchenOrderStatus.valueOf(order.status.name),
            totalAmount = order.totalAmount,
            items = order.items.map { toItemResponse(it) },
            receivedAt = order.receivedAt,
            startedPreparationAt = order.startedPreparationAt,
            completedAt = order.completedAt,
            estimatedCompletionTime = order.estimatedCompletionTime
        )
    }

    private fun toItemResponse(item: KitchenOrderItem): KitchenOrderItemResponse {
        return KitchenOrderItemResponse(
            id = item.id,
            menuItemId = item.menuItemId,
            menuItemName = item.menuItemName,
            quantity = item.quantity,
            unitPrice = item.unitPrice,
            totalPrice = item.totalPrice
        )
    }
}
