package com.restaurant.kitchen.controller

import com.restaurant.kitchen.dto.KitchenOrderResponse
import com.restaurant.kitchen.dto.KitchenOrderItemResponse
import com.restaurant.kitchen.dto.KitchenOrderStatus as DtoKitchenOrderStatus
import com.restaurant.kitchen.entity.KitchenOrderStatus as EntityKitchenOrderStatus
import com.restaurant.kitchen.service.KitchenService
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Kitchen Controller - REST API for kitchen order management.
 */
@RestController
@RequestMapping("/api/kitchen")
class KitchenController(
    private val kitchenService: KitchenService
) {

    companion object {
        private val requestCount = java.util.concurrent.atomic.AtomicInteger(0)
    }

    // Get all kitchen orders
    @GetMapping("/orders")
    fun getAllKitchenOrders(): List<KitchenOrderResponse> {
        val orders = kitchenService.getKitchenOrders()
        return orders.map { it.toResponse() }
    }

    // Get kitchen order by ID
    @GetMapping("/orders/{orderId}")
    fun getKitchenOrderById(@PathVariable orderId: Long): ResponseEntity<KitchenOrderResponse> {
        val order = kitchenService.getKitchenOrderById(orderId)
        return ResponseEntity.ok(order.toResponse())
    }

    // Get kitchen orders by status
    @GetMapping("/orders/status/{status}")
    fun getKitchenOrdersByStatus(@PathVariable status: DtoKitchenOrderStatus): ResponseEntity<List<KitchenOrderResponse>> {
        // Convert DTO status to entity status
        val entityStatus = EntityKitchenOrderStatus.valueOf(status.name)
        val orders = kitchenService.getKitchenOrdersByStatus(entityStatus)
        val orderResponses = orders.map { it.toResponse() }
        return ResponseEntity.ok(orderResponses)
    }

    // Start order preparation
    @PutMapping("/orders/{orderId}/start-preparation")
    @CacheEvict(value = ["active-orders", "kitchen-stats"], allEntries = true)
    fun startPreparation(@PathVariable orderId: Long): ResponseEntity<KitchenOrderResponse> {
        val order = kitchenService.startPreparation(orderId)
        return ResponseEntity.ok(order.toResponse())
    }

    // Mark order as ready for pickup
    @PutMapping("/orders/{orderId}/ready")
    @CacheEvict(value = ["active-orders", "kitchen-stats"], allEntries = true)
    fun markAsReady(@PathVariable orderId: Long): ResponseEntity<KitchenOrderResponse> {
        val order = kitchenService.markAsReady(orderId)
        return ResponseEntity.ok(order.toResponse())
    }

    // Cancel order
    @PutMapping("/orders/{orderId}/cancel")
    @CacheEvict(value = ["active-orders", "kitchen-stats"], allEntries = true)
    fun cancelOrder(@PathVariable orderId: Long): ResponseEntity<KitchenOrderResponse> {
        val order = kitchenService.cancelOrder(orderId)
        return ResponseEntity.ok(order.toResponse())
    }

    // Mark order as completed after pickup
    @PutMapping("/orders/{orderId}/complete")
    @CacheEvict(value = ["active-orders", "kitchen-stats"], allEntries = true)
    fun markAsCompleted(@PathVariable orderId: Long): ResponseEntity<KitchenOrderResponse> {
        val order = kitchenService.markAsCompleted(orderId)
        return ResponseEntity.ok(order.toResponse())
    }

    // Get kitchen dashboard statistics
    @GetMapping("/dashboard/stats")
    @Cacheable(value = ["kitchen-stats"], key = "'dashboard'")
    fun getKitchenDashboardStats(): Map<String, Any> {
        // Retrieve orders by status for dashboard
        val receivedOrders = kitchenService.getKitchenOrdersByStatus(EntityKitchenOrderStatus.RECEIVED)
        val inPreparationOrders = kitchenService.getKitchenOrdersByStatus(EntityKitchenOrderStatus.IN_PREPARATION)
        val readyOrders = kitchenService.getKitchenOrdersByStatus(EntityKitchenOrderStatus.READY)

        // Calculate dashboard statistics
        return mapOf(
            "received" to receivedOrders.size,
            "inPreparation" to inPreparationOrders.size,
            "ready" to readyOrders.size,
            "totalActive" to (receivedOrders.size + inPreparationOrders.size + readyOrders.size)
        )
    }
}

// Extension functions for entity-to-DTO mapping

// Convert KitchenOrder entity to KitchenOrderResponse DTO
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
