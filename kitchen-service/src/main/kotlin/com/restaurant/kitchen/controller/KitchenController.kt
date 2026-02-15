package com.restaurant.kitchen.controller

import com.restaurant.kitchen.dto.KitchenOrderResponse
import com.restaurant.kitchen.dto.KitchenOrderItemResponse
import com.restaurant.kitchen.dto.KitchenOrderStatus as DtoKitchenOrderStatus
import com.restaurant.kitchen.entity.KitchenOrderStatus as EntityKitchenOrderStatus
import com.restaurant.kitchen.service.KitchenService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/kitchen")
class KitchenController(private val kitchenService: KitchenService) {

    @GetMapping("/orders")
    fun getAllKitchenOrders(): ResponseEntity<List<KitchenOrderResponse>> {
        val orders = kitchenService.getKitchenOrders()
        val orderResponses = orders.map { it.toResponse() }
        return ResponseEntity.ok(orderResponses)
    }

    @GetMapping("/orders/{orderId}")
    fun getKitchenOrderById(@PathVariable orderId: Long): ResponseEntity<KitchenOrderResponse> {
        val order = kitchenService.getKitchenOrderById(orderId)
        return ResponseEntity.ok(order.toResponse())
    }

    @GetMapping("/orders/status/{status}")
    fun getKitchenOrdersByStatus(@PathVariable status: DtoKitchenOrderStatus): ResponseEntity<List<KitchenOrderResponse>> {
        val entityStatus = EntityKitchenOrderStatus.valueOf(status.name)
        val orders = kitchenService.getKitchenOrdersByStatus(entityStatus)
        val orderResponses = orders.map { it.toResponse() }
        return ResponseEntity.ok(orderResponses)
    }

    @PutMapping("/orders/{orderId}/start-preparation")
    fun startPreparation(@PathVariable orderId: Long): ResponseEntity<KitchenOrderResponse> {
        val order = kitchenService.startPreparation(orderId)
        return ResponseEntity.ok(order.toResponse())
    }

    @PutMapping("/orders/{orderId}/ready")
    fun markAsReady(@PathVariable orderId: Long): ResponseEntity<KitchenOrderResponse> {
        val order = kitchenService.markAsReady(orderId)
        return ResponseEntity.ok(order.toResponse())
    }

    @PutMapping("/orders/{orderId}/complete")
    fun markAsCompleted(@PathVariable orderId: Long): ResponseEntity<KitchenOrderResponse> {
        val order = kitchenService.markAsCompleted(orderId)
        return ResponseEntity.ok(order.toResponse())
    }

    @GetMapping("/dashboard/stats")
    fun getKitchenDashboardStats(): ResponseEntity<Map<String, Any>> {
        val receivedOrders = kitchenService.getKitchenOrdersByStatus(EntityKitchenOrderStatus.RECEIVED)
        val inPreparationOrders = kitchenService.getKitchenOrdersByStatus(EntityKitchenOrderStatus.IN_PREPARATION)
        val readyOrders = kitchenService.getKitchenOrdersByStatus(EntityKitchenOrderStatus.READY)

        val stats = mapOf(
            "received" to receivedOrders.size,
            "inPreparation" to inPreparationOrders.size,
            "ready" to readyOrders.size,
            "totalActive" to (receivedOrders.size + inPreparationOrders.size + readyOrders.size)
        )

        return ResponseEntity.ok(stats)
    }
}

// Extension function to convert entity to response DTO
fun com.restaurant.kitchen.entity.KitchenOrder.toResponse(): KitchenOrderResponse {
    return KitchenOrderResponse(
        orderId = this.orderId,
        customerName = this.customerName,
        customerEmail = this.customerEmail,
        status = DtoKitchenOrderStatus.valueOf(this.status.name),
        totalAmount = this.totalAmount,
        items = this.items.map { item ->
            KitchenOrderItemResponse(
                id = item.id,
                menuItemId = item.menuItemId,
                menuItemName = item.menuItemName,
                quantity = item.quantity,
                unitPrice = item.unitPrice,
                totalPrice = item.totalPrice
            )
        },
        receivedAt = this.receivedAt,
        startedPreparationAt = this.startedPreparationAt,
        completedAt = this.completedAt,
        estimatedCompletionTime = this.estimatedCompletionTime
    )
}
